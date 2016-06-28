package javaext.nio.channels.bluetooth;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import javaext.net.bluetooth.BluetoothSocket;

public class BluetoothSocketChannel extends SocketChannel /* implements java.nio.FileDescriptorChannel */ {

    private BluetoothSocket socket = null;
    private ReadableByteChannel readChannel = null;

    protected BluetoothSocketChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
        this.socket = new BluetoothSocket();
    }

    public static BluetoothSocketChannel open() {
        return new BluetoothSocketChannel(SelectorProvider.provider());
    }

    @Override
    public Socket socket() {
        return this.socket;
    }

    @Override
    public boolean isConnected() {
        return this.socket.isConnected();
    }

    @Override
    public boolean isConnectionPending() {
        return false;
    }

    @Override
    public boolean connect(SocketAddress address) throws IOException {
        this.socket.connect(address);
        return this.socket.isConnected();
    }

    @Override
    public boolean finishConnect() throws IOException {
        return false;
    }

    @Override
    public int read(ByteBuffer target) throws IOException {
        if (this.readChannel == null) {
            this.readChannel = Channels.newChannel(this.socket.getInputStream());
        }
        int readed;
        try {
            readed = this.readChannel.read(target);
        } catch (ClosedByInterruptException e) {
            throw e;
        } catch (ClosedChannelException e) {
            throw e;
        } catch (IOException e) {
            // do nothing
            readed = -1;
        }
        return readed;
    }

    @Override
    public long read(ByteBuffer[] targets, int offset, int length) throws IOException {
        Log.d("BluetoothSocketChannel", "read 2");
        return 0;
    }

    @Override
    public int write(ByteBuffer source) throws IOException {
        Log.d("BluetoothSocketChannel", "write 1");
        return 0;
    }

    @Override
    public long write(ByteBuffer[] sources, int offset, int length) throws IOException {
        Log.d("BluetoothSocketChannel", "write 2");
        return 0;
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        this.socket.close();
        this.readChannel = null;
    }

    @Override
    protected void implConfigureBlocking(boolean blocking) throws IOException {
        //Log.d("BluetoothSocketChannel", "implConfigureBlocking");
    }
}
