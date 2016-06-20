package com.google.vr.vrcore.controller.emulator;

import android.content.Context;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.vr.gvr.io.proto.nano.Protos;
import com.google.vr.vrcore.controller.BaseController;
import com.google.vr.vrcore.controller.api.ControllerStates;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

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
        int wantToRead = buffer.limit() - buffer.position();
        while (socketChannel.isConnected() && totalRead < wantToRead && totalRead >= 0) {
            totalRead += socketChannel.read(buffer);
        }
        return totalRead;
    }

    private static Protos.PhoneEvent readFromChannel(SocketChannel channel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(4);
        if (blockingRead(channel, header) < 4) {
            return null;
        }
        header.rewind();
        int length = header.getInt();
        ByteBuffer protoBytes = ByteBuffer.allocate(length);
        if (blockingRead(channel, protoBytes) >= length) {
            return Protos.PhoneEvent.mergeFrom(new Protos.PhoneEvent(), protoBytes.array(), protoBytes.arrayOffset(), length);
        }
        return null;
    }

    private void handleClient(SocketChannel channel) throws IOException {
        Protos.PhoneEvent event;

        if (!channel.isConnected()) {
            return;
        }

        try {
            event = readFromChannel(channel);
        } catch (ClosedByInterruptException ce) {
            ce.printStackTrace();
            return;

        } catch (Exception ce) {
            event = null;
            ce.printStackTrace();
        }

        try {
            OnPhoneEvent(event);
        } catch (DeadObjectException ce) {
            ce.printStackTrace();
            channel.close();
            return;
        } catch (RemoteException ce) {
            ce.printStackTrace();
        }

        if (!channel.isConnected() || event == null) {
            channel.close();
            setState(ControllerStates.DISCONNECTED);
            Log.d("EmulatorClientSocket", "!channel.isConnected");
            return;
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return registeredControllerListener.currentState == ControllerStates.CONNECTED;
    }

    @Override
    public boolean tryConnect() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setState(ControllerStates.CONNECTING);

        try {
            String ip = PreferenceManager.getDefaultSharedPreferences(this.context).getString("emulator_ip_address", "192.168.1.101" /* default value pref_default_ip_address */);
            String port = PreferenceManager.getDefaultSharedPreferences(this.context).getString("emulator_port_number", "7003" /* default value pref_default_port_number */);
            InetSocketAddress address = new InetSocketAddress(ip, Integer.parseInt(port));
            channel = SocketChannel.open();

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);

            channel.connect(address);

            if (channel.isConnected()) {
                setState(ControllerStates.CONNECTED);
            }
        } catch (ConnectException e) {
            e.printStackTrace();
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

    @Override
    public boolean handle() {
        try {
            selector.select(5000);
        } catch (IOException e) {
            e.printStackTrace();
            setState(ControllerStates.DISCONNECTED);
            return false;
        }

        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (!key.isValid()) continue;

            try {
                if (key.isConnectable() && channel.isConnectionPending() && channel.finishConnect()) {
                    connect(key);
                    setState(ControllerStates.CONNECTED);
                }
            } catch (ConnectException e) {
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