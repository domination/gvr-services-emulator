package com.google.vr.vrcore.controller.emulator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.vr.gvr.io.proto.nano.Protos;
import com.google.vr.vrcore.controller.BaseController;
import com.google.vr.vrcore.controller.api.ControllerStates;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

class InetSocketAddressEx extends InetSocketAddress {

    private UUID uuid;

    public InetSocketAddressEx(String device, UUID uuid) {
        super(device, 0);
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public InetSocketAddressEx(int port) {
        super(port);
    }

    public InetSocketAddressEx(InetAddress address, int port) {
        super(address, port);
    }

    public InetSocketAddressEx(String host, int port) {
        super(host, port);
    }
}

class SocketEx extends Socket {
    BluetoothSocket bluetoothSocket = null;

    @Override
    public void connect(SocketAddress remoteAddr) throws IOException {
        InetSocketAddressEx addressEx = (InetSocketAddressEx) remoteAddr;
        if (addressEx == null) return;

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addressEx.getHostName());
        this.bluetoothSocket = device.createRfcommSocketToServiceRecord(addressEx.getUUID());

        try {
            this.bluetoothSocket.connect();
        } catch (IOException e) {
            try {
                this.bluetoothSocket.close();
            } catch (IOException close) {
                Log.d("SocketEx::connect", "Could not close connection:" + e.toString());
            }
            throw new ConnectException();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (this.bluetoothSocket != null)
            this.bluetoothSocket.close();
    }

    @Override
    public boolean isConnected() {
        return this.bluetoothSocket != null && this.bluetoothSocket.isConnected();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.bluetoothSocket.getInputStream();
    }
}

class SelectionKeyEx extends SelectionKey {

    private SocketChannel socketChannel;

    public SelectionKeyEx(SelectableChannel socketChannel) {
        this.socketChannel = (SocketChannel) socketChannel;
    }

    @Override
    public void cancel() {
        Log.w("SelectionKeyEx", "cancel");
        try {
            this.socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SelectableChannel channel() {
        return this.socketChannel;
    }

    @Override
    public int interestOps() {
        Log.w("SelectionKeyEx", "interestOps");
        return 0;
    }

    @Override
    public SelectionKey interestOps(int operations) {
        Log.w("SelectionKeyEx", "interestOps");
        return null;
    }

    @Override
    public boolean isValid() {
        //Log.w("SelectionKeyEx", "isValid");
        return this.socketChannel.isConnected();
    }

    @Override
    public int readyOps() {
        //Log.w("SelectionKeyEx", "readyOps");
        if (socketChannel.isConnected()) {
            return OP_READ;
        }
        return 0;
    }

    @Override
    public Selector selector() {
        Log.w("SelectionKeyEx", "selector");
        return null;
    }
}

class SelectorEx extends AbstractSelector {

    public static Selector open() throws IOException {
        //return SelectorProvider.provider().openSelector();
        return SelectorProviderImplEx.provider().openSelector();
    }

    protected SelectorEx(SelectorProvider selectorProvider) {
        super(selectorProvider);
        keys = new HashSet<>();
    }

    @Override
    protected void implCloseSelector() throws IOException {
        Log.w("SelectorEx", "implCloseSelector");
    }

    @Override
    protected SelectionKey register(AbstractSelectableChannel channel, int operations, Object attachment) {
        //Log.w("SelectorEx", "register");
        SelectionKey key = new SelectionKeyEx(channel);
        keys.add(key);
        return key;
    }

    @Override
    public Set<SelectionKey> keys() {
        Log.w("SelectorEx", "keys");
        return null;
    }

    @Override
    public int select() throws IOException {
        Log.w("SelectorEx", "select 1");
        return 0;
    }

    @Override
    public int select(long timeout) throws IOException {
        //Log.w("SelectorEx", "select 2");
        return 0;
    }

    private HashSet<SelectionKey> keys;

    @Override
    public Set<SelectionKey> selectedKeys() {
        //Log.w("SelectorEx", "selectedKeys");
        return new HashSet<>(keys);
    }

    @Override
    public int selectNow() throws IOException {
        Log.w("SelectorEx", "selectNow");
        return 0;
    }

    @Override
    public Selector wakeup() {
        Log.w("SelectorEx", "wakeup");
        return null;
    }
}

class SelectorProviderImplEx extends SelectorProvider {

    @Override
    public DatagramChannel openDatagramChannel() throws IOException {
        Log.w("SelectorProviderImplEx", "openDatagramChannel");
        return null;
    }

    @Override
    public Pipe openPipe() throws IOException {
        Log.w("SelectorProviderImplEx", "openPipe");
        return null;
    }

    @Override
    public AbstractSelector openSelector() throws IOException {
        return new SelectorEx(this);
    }

    @Override
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        Log.w("SelectorProviderImplEx", "openServerSocketChannel");
        return null;
    }

    @Override
    public SocketChannel openSocketChannel() throws IOException {
        Log.w("SelectorProviderImplEx", "openSocketChannel");
        return null;
    }

    private static SelectorProvider provider;

    synchronized public static SelectorProvider provider() {
        provider = new SelectorProviderImplEx();
        return provider;
    }
}

final class SocketChannelEx extends SocketChannel {

    private SocketEx socketEx = null;

    protected SocketChannelEx(SelectorProvider selectorProvider) {
        super(selectorProvider);
        this.fd = new FileDescriptor();
    }

    public static SocketChannelEx open() {
        return new SocketChannelEx(SelectorProvider.provider());
    }

    @Override
    public Socket socket() {
        return socketEx;
    }

    @Override
    public boolean isConnected() {
        return socketEx.isConnected();
    }

    @Override
    public boolean isConnectionPending() {
        Log.w("SocketChannelEx", "isConnectionPending");
        return false;
    }

    @Override
    public boolean connect(SocketAddress address) throws IOException {
        if (this.socketEx != null) {
            this.socketEx.close();
        }
        if (readChannel != null) {
            this.readChannel = null;
        }
        socketEx = new SocketEx();
        socketEx.connect(address);
        return socketEx.isConnected();
    }

    @Override
    public boolean finishConnect() throws IOException {
        Log.w("SocketChannelEx", "finishConnect");
        return true;
    }

    private ReadableByteChannel readChannel = null;

    @Override
    public int read(ByteBuffer target) throws IOException {
        if (readChannel == null) {
            readChannel = Channels.newChannel(this.socketEx.getInputStream());
        }
        return readChannel.read(target);
    }

    @Override
    public long read(ByteBuffer[] targets, int offset, int length) throws IOException {
        Log.w("SocketChannelEx", "read 2");
        return 0;
    }

    @Override
    public int write(ByteBuffer source) throws IOException {
        Log.w("SocketChannelEx", "write 1");
        return 0;
    }

    @Override
    public long write(ByteBuffer[] sources, int offset, int length) throws IOException {
        Log.w("SocketChannelEx", "write 2");
        return 0;
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        Log.w("SocketChannelEx", "implCloseSelectableChannel");
        this.socketEx.close();
    }

    @Override
    protected void implConfigureBlocking(boolean blocking) throws IOException {
        Log.w("SocketChannelEx", "implConfigureBlocking");
    }

    private FileDescriptor fd;

    public FileDescriptor getFD() {
        return this.fd;
    } //interface java.nio.FileDescriptorChannel
}

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
            isEnabled = false;
            Log.d("EmulatorClientSocket", "!channel.isConnected");
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

    private BluetoothAdapter bluetoothAdapter;

    public boolean setBluetooth(boolean enable) {
        this.tryBluetooth = enable;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
            selector = this.tryBluetooth ? SelectorEx.open() : Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setState(ControllerStates.CONNECTING);

        try {
            InetSocketAddress address = null;
            if (tryBluetooth) {
                setBluetooth(true);

                Set<BluetoothDevice> bluetoothDeviceIterator = this.bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : bluetoothDeviceIterator) {
                    Log.w("bluetooth", device.getAddress() + " " + device.getName());
                    address = new InetSocketAddressEx(device.getAddress(), UUID.fromString("ab001ac1-d740-4abb-a8e6-1cb5a49628fa"));
                    break;
                }
                channel = SocketChannelEx.open();
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