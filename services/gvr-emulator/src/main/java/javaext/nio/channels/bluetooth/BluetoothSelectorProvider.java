package javaext.nio.channels.bluetooth;

import android.util.Log;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class BluetoothSelectorProvider extends SelectorProvider {

    private static SelectorProvider provider;

    synchronized public static SelectorProvider provider() {
        provider = new BluetoothSelectorProvider();
        return provider;
    }

    @Override
    public DatagramChannel openDatagramChannel() throws IOException {
        Log.w("BluetoothSelectorProvider", "openDatagramChannel");
        return null;
    }

    @Override
    public Pipe openPipe() throws IOException {
        Log.w("BluetoothSelectorProvider", "openPipe");
        return null;
    }

    @Override
    public AbstractSelector openSelector() throws IOException {
        Log.w("BluetoothSelectorProvider", "openSelector");
        return new BluetoothSelector(this);
    }

    @Override
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        Log.w("BluetoothSelectorProvider", "openServerSocketChannel");
        return null;
    }

    @Override
    public SocketChannel openSocketChannel() throws IOException {
        Log.w("BluetoothSelectorProvider", "openSocketChannel");
        return null;
    }
}
