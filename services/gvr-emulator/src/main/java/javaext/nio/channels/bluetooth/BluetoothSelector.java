package javaext.nio.channels.bluetooth;

import android.util.Log;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Set;

public class BluetoothSelector extends AbstractSelector {

    private HashSet<SelectionKey> keys;

    protected BluetoothSelector(SelectorProvider selectorProvider) {
        super(selectorProvider);
        keys = new HashSet<>();
    }

    public static Selector open() throws IOException {
        //return SelectorProvider.provider().openSelector();
        return BluetoothSelectorProvider.provider().openSelector();
    }

    @Override
    protected void implCloseSelector() throws IOException {
        Log.w("BluetoothSelector", "implCloseSelector");
    }

    @Override
    protected SelectionKey register(AbstractSelectableChannel channel, int operations, Object attachment) {
        Log.w("BluetoothSelector", "register");
        SelectionKey key = new BluetoothSelectionKey(channel);
        keys.add(key);
        return key;
    }

    @Override
    public Set<SelectionKey> keys() {
        Log.w("BluetoothSelector", "keys");
        return null;
    }

    @Override
    public int select() throws IOException {
        Log.w("BluetoothSelector", "select");
        return 0;
    }

    @Override
    public int select(long timeout) throws IOException {
        //Log.w("BluetoothSelector", "select");
        return 0;
    }

    @Override
    public Set<SelectionKey> selectedKeys() {
        //Log.w("BluetoothSelector", "selectedKeys");
        return new HashSet<>(this.keys);
    }

    @Override
    public int selectNow() throws IOException {
        Log.w("BluetoothSelector", "selectNow");
        return 0;
    }

    @Override
    public Selector wakeup() {
        Log.w("BluetoothSelector", "wakeup");
        return null;
    }
}
