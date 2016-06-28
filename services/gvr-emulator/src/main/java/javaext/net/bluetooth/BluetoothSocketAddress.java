package javaext.net.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.net.SocketAddress;
import java.util.UUID;

public class BluetoothSocketAddress extends SocketAddress {

    private final String address;
    private final UUID uuid;
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
            try {
                bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.address);
            } catch (IllegalArgumentException e) {
                //e.printStackTrace(); // is not a valid Bluetooth address
            }
        }
        return bluetoothDevice;
    }

    public String getDeviceName() {
        this.getRemoteDevice();
        return bluetoothDevice != null ? bluetoothDevice.getName() : "";
    }

    public String getDeviceAddress() {
        this.getRemoteDevice();
        return bluetoothDevice != null ? bluetoothDevice.getAddress() : "";
    }

    //BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addressEx.getHostName());
    //this.bluetoothSocket = device.createRfcommSocketToServiceRecord(addressEx.getUUID());


}
