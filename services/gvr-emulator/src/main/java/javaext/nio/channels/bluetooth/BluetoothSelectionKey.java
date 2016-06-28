package javaext.nio.channels.bluetooth;

import android.util.Log;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class BluetoothSelectionKey extends SelectionKey {

    private final SocketChannel channel;
    private final Selector selector;
    private int operations;

    public BluetoothSelectionKey(SelectableChannel channel, Selector selector) {
        this.channel = (SocketChannel) channel;
        this.operations = 0;
        this.selector = selector;
    }

    @Override
    public void cancel() {
        Log.d("BluetoothSelectionKey", "cancel");
        try {
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SelectableChannel channel() {
        //Log.d("BluetoothSelectionKey", "channel");
        return this.channel;
    }

    @Override
    public int interestOps() {
        Log.d("BluetoothSelectionKey", "interestOps");
        return this.operations;
    }

    @Override
    public SelectionKey interestOps(int operations) {
        Log.d("BluetoothSelectionKey", "interestOps");
        this.operations = operations;
        return this;
    }

    @Override
    public boolean isValid() {
        //Log.d("BluetoothSelectionKey", "isValid");
        boolean isValid = this.channel.isConnected();
        if (!isValid && this.isConnectable()) {
            isValid = true;
        }
        return isValid;
    }

    @Override
    public int readyOps() {
        //Log.d("BluetoothSelectionKey", "readyOps");
        if (channel.isConnected()) {
            return OP_READ;
        }
        return OP_CONNECT;
    }

    @Override
    public Selector selector() {
        Log.d("BluetoothSelectionKey", "selector");
        return this.selector;
    }
}
