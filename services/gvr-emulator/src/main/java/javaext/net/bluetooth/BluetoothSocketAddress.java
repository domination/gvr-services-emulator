package javaext.net.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.net.SocketAddress;
import java.util.UUID;

public class BluetoothSocketAddress extends SocketAddress {

    private String address;
    private UUID uuid;
    private BluetoothDevice bluetoothDevice = null;

    public String getAddress() {
        return address;
    }

    public UUID getUUID() {
        return uuid;
    }

    public BluetoothSocketAddress(String address, UUID uuid) {
        this.address = address;
        this.uuid = uuid;
    }

    public BluetoothDevice getRemoteDevice() {
        if (bluetoothDevice == null) {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.address);
        }
        return bluetoothDevice;
    }

    //BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addressEx.getHostName());
    //this.bluetoothSocket = device.createRfcommSocketToServiceRecord(addressEx.getUUID());


}
