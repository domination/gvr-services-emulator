package javaext.net.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class BluetoothSocket extends Socket {

    private volatile boolean isCreated = false;
    private boolean isClosed = false;
    private boolean isBound = false;
    private final Object connectLock = new Object();

    private android.bluetooth.BluetoothSocket bluetoothSocket = null;

    @Override
    public void connect(SocketAddress remoteAddress, int timeout) throws IOException {
        //super.connect(remoteAddr, timeout);
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        }
        if (isConnected()) {
            throw new SocketException("Already connected");
        }
        if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress == null");
        }

        if (!(remoteAddress instanceof BluetoothSocketAddress)) {
            throw new IllegalArgumentException("Remote address not an BluetoothSocketAddress: " +
                    remoteAddress.getClass());
        }
        BluetoothSocketAddress bluetoothSocketAddress = (BluetoothSocketAddress) remoteAddress;

        checkOpenAndCreate(true, bluetoothSocketAddress);

        if (bluetoothSocketAddress.getRemoteDevice() == null) {
            throw new UnknownHostException("Device not found: " + bluetoothSocketAddress.getAddress());
        }

        synchronized (connectLock) {
            try {
                if (!isBound()) {
//                    // socket already created at this point by earlier call or
//                    // checkOpenAndCreate this caused us to lose socket
//                    // options on create
//                    // impl.create(true);
//                    if (!usingSocks()) {
//                        impl.bind(iNet6Address.ANY, 0);
//                    }
                    isBound = true;
                }
                //impl.connect(remoteAddress, timeout);
                if (this.bluetoothSocket != null) {
                    this.bluetoothSocket.connect();
                }
                //isConnected = true;
                //cacheLocalAddress();
            } catch (IOException e) {
                this.close();
                //Log.d("BluetoothSocket", "connect");
                //e.printStackTrace();
                throw new ConnectException();
            }
        }
    }

    @Override
    public boolean isBound() {
        return this.isBound;
    }

    @Override
    public boolean isConnected() {
        return this.bluetoothSocket != null && this.bluetoothSocket.isConnected();
    }

    @Override
    public synchronized void close() throws IOException {
        isClosed = true;
        if (this.bluetoothSocket != null)
            this.bluetoothSocket.close();
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        checkOpenAndCreate(false, null);
        return this.bluetoothSocket.getInputStream();
    }

    private void checkOpenAndCreate(boolean create, BluetoothSocketAddress bluetoothSocketAddress) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        if (!create) {
            if (!isConnected()) {
                throw new SocketException("Socket is not connected");
                // a connected socket must be created
            }

            /*
             * return directly to fix a possible bug, if !create, should return
             * here
             */
            return;
        }
        if (isCreated) {
            return;
        }
        synchronized (this) {
            if (isCreated) {
                return;
            }
            try {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                BluetoothDevice device = bluetoothSocketAddress.getRemoteDevice();
                if (device != null) {
                    this.bluetoothSocket = device.createRfcommSocketToServiceRecord(bluetoothSocketAddress.getUUID());
                } else {
                    throw new ConnectException();
                }
            } catch (SocketException e) {
                throw e;
            } catch (IOException e) {
                throw new SocketException(e.toString());
            }
            isCreated = true;
        }
    }
}
