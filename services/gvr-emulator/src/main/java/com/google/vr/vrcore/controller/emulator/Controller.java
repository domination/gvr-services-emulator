package com.google.vr.vrcore.controller.emulator;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.vr.gvr.io.proto.nano.Protos.PhoneEvent;
import com.google.vr.vrcore.controller.BaseController;
import com.google.vr.vrcore.controller.api.ControllerStates;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import javaext.net.bluetooth.BluetoothSocketAddress;
import javaext.nio.channels.bluetooth.BluetoothSelector;
import javaext.nio.channels.bluetooth.BluetoothSocketChannel;

public class Controller extends BaseController {

    private Context context;

    public Controller(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    private SocketChannel channel;
    private Selector selector;

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        handleClient(channel);
        if (!channel.isOpen()) {
            key.cancel();
        }
    }

    private static int blockingRead(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        int totalRead = 0;
        int wantToRead = buffer.remaining(); //buffer.limit() - buffer.position();
        while (socketChannel.isConnected() && totalRead < wantToRead && totalRead >= 0) {
            totalRead += socketChannel.read(buffer);
        }
        return totalRead;
    }

    private ByteBuffer lengthBytes = ByteBuffer.allocate(4); // 4 bytes for integer - length of msg
    private ByteBuffer protoBytes = ByteBuffer.allocate(100); // it should be enough
    private CodedInputByteBufferNano e = null;

    private PhoneEvent readFromChannel(SocketChannel channel) throws IOException {
        lengthBytes.rewind(); //ByteBuffer lengthBytes = ByteBuffer.allocate(4);
        if (blockingRead(channel, lengthBytes) < 4) {
            return null;
        }
        lengthBytes.rewind();
        int length = lengthBytes.getInt();
        protoBytes.rewind().limit(length); //ByteBuffer protoBytes = ByteBuffer.allocate(length);
        if (blockingRead(channel, protoBytes) >= length) {
            //return PhoneEvent.mergeFrom(phoneEvent.clear(), protoBytes.array(), protoBytes.arrayOffset(), length);
            protoBytes.rewind();
            try {
                int newBufferSize = protoBytes.arrayOffset() + length;
                if (e == null) {
                    e = CodedInputByteBufferNano.newInstance(protoBytes.array(), protoBytes.arrayOffset(), 100);//length);
                }// else {
                e.rewindToPosition(0);

                e.popLimit(100);
                e.pushLimit(newBufferSize);
                //}

                phoneEvent.clear().mergeFrom(e);
                e.checkLastTagWas(0);
                return phoneEvent;
            } catch (InvalidProtocolBufferNanoException var5) {
                var5.printStackTrace();
                throw var5;
            } catch (IOException var6) {
                var6.printStackTrace();
                throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).");
            }
        }
        return null;
    }

    protected PhoneEvent phoneEvent;

    private void handleClient(SocketChannel channel) throws IOException {
        if (!channel.isConnected()) {
            return;
        }

        if (phoneEvent == null) {
            phoneEvent = new PhoneEvent();
        }

        try {
            phoneEvent = readFromChannel(channel);
        } catch (ClosedByInterruptException ce) {
            ce.printStackTrace();
            return;

        } catch (Exception ce) {
            phoneEvent = null;
            ce.printStackTrace();
        }

        try {
            OnPhoneEvent(phoneEvent);
        } catch (DeadObjectException ce) {
            ce.printStackTrace();
            channel.close();
            return;
        } catch (RemoteException ce) {
            ce.printStackTrace();
        }

        if (!channel.isConnected() || phoneEvent == null) {
            setState(ControllerStates.DISCONNECTED);
            isEnabled = false;
            channel.close();
            Log.d("Controller", "handleClient :: !channel.isConnected");
        }
    }

    @Override
    public boolean isAvailable() {
        return isEnabled;
    }

    @Override
    public boolean isConnected() {
        return registeredControllerListener.currentState == ControllerStates.CONNECTED;
    }

    public boolean setBluetooth(boolean enable) {
        this.tryBluetooth = enable;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return false;
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        return true;
    }

    @Override
    public boolean tryConnect() {
        try {
            selector = this.tryBluetooth ? BluetoothSelector.open() : Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setState(ControllerStates.CONNECTING);

        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            channel = null;

            SocketAddress address;
            if (tryBluetooth) {
                setBluetooth(true);

                //Set<BluetoothDevice> bluetoothDeviceIterator = this.bluetoothAdapter.getBondedDevices();
                //for (BluetoothDevice device : bluetoothDeviceIterator) {
                //    Log.d("bluetooth", device.getAddress() + " " + device.getName());
                //    address = new BluetoothSocketAddress(device.getAddress(), UUID.fromString("ab001ac1-d740-4abb-a8e6-1cb5a49628fa"));
                //    break;
                //}
                String bt_address = PreferenceManager.getDefaultSharedPreferences(this.context).getString("emulator_bt_device", "");
                address = new BluetoothSocketAddress(bt_address, UUID.fromString("ab001ac1-d740-4abb-a8e6-1cb5a49628fa"));
                channel = BluetoothSocketChannel.open();
                Log.d("tryConnect", "Device name: " + ((BluetoothSocketAddress) address).getRemoteDevice().getName());
            } else {
                String ip = PreferenceManager.getDefaultSharedPreferences(this.context).getString("emulator_ip_address", "192.168.1.101" /* default value pref_default_ip_address */);
                String port = PreferenceManager.getDefaultSharedPreferences(this.context).getString("emulator_port_number", "7003" /* default value pref_default_port_number */);
                address = new InetSocketAddress(ip, Integer.parseInt(port));
                channel = SocketChannel.open();
            }

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);

            channel.connect(address);

            if (channel.isConnected()) {
                setState(ControllerStates.CONNECTED);
            }
        } catch (ConnectException e) {
            isEnabled = false;
            //e.printStackTrace();
            return false;
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean tryBluetooth = false;

    @Override
    public boolean handle() {
        try {
            selector.select(5000);
        } catch (IOException e) {
            e.printStackTrace();
            setState(ControllerStates.DISCONNECTED);
            return false;
        }

        for (SelectionKey key : selector.selectedKeys()) {

            if (!key.isValid()) continue;

            try {
                //Log.d("handle", key.isConnectable() + " " + channel.isConnectionPending() + " " + channel.finishConnect());
                if (key.isConnectable() && channel.isConnectionPending() && channel.finishConnect()) {
                    connect(key);
                    setState(ControllerStates.CONNECTED);
                }
            } catch (ConnectException e) {
                isEnabled = false;
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            if (key.isValid() && key.isReadable()) {
                try {
                    read(key);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                if (!key.isValid()) {
                    setState(ControllerStates.DISCONNECTED);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void disconnect() {
        setState(ControllerStates.DISCONNECTED);
        try {
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}