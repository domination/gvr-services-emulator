package com.google.vr.vrcore;

import android.content.Context;
import android.os.DeadObjectException;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.vr.gvr.io.proto.nano.Protos.PhoneEvent;
import com.google.vr.vrcore.controller.api.ControllerAccelEvent;
import com.google.vr.vrcore.controller.api.ControllerButtonEvent;
import com.google.vr.vrcore.controller.api.ControllerGyroEvent;
import com.google.vr.vrcore.controller.api.ControllerOrientationEvent;
import com.google.vr.vrcore.controller.api.ControllerStates;
import com.google.vr.vrcore.controller.api.ControllerTouchEvent;
import com.google.vr.vrcore.controller.api.IControllerListener;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

public class EmulatorClientSocket implements Runnable {

    private boolean shouldStop;

    private SocketChannel channel;

    private int controllerId;
    private Context context;
    private IControllerListener controllerListener;

    public EmulatorClientSocket(Context context, IControllerListener controllerListener, int controllerId) {
        this.context = context;
        this.controllerListener = controllerListener;
        this.controllerId = controllerId;
        shouldStop = false;
    }

    private static int blockingRead(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        int totalRead = 0;
        int wantToRead = buffer.limit() - buffer.position();
        while (socketChannel.isConnected() && totalRead < wantToRead) {
            totalRead += socketChannel.read(buffer);
        }
        return totalRead;
    }

    @Nullable
    public static PhoneEvent readFromChannel(SocketChannel channel) throws IOException {
        channel.configureBlocking(false);
        ByteBuffer header = ByteBuffer.allocate(4);
        if (blockingRead(channel, header) < 4) {
            channel.configureBlocking(true);
            return null;
        }
        header.rewind();
        int length = header.getInt();
        ByteBuffer protoBytes = ByteBuffer.allocate(length);
        if (blockingRead(channel, protoBytes) >= length) {
            channel.configureBlocking(true);
            return PhoneEvent.mergeFrom(new PhoneEvent(), protoBytes.array(), protoBytes.arrayOffset(), length);
        }
        return null;
    }

    private void setState(int state) {
        try {
            controllerListener.onControllerStateChanged(controllerId, state);
        } catch (DeadObjectException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!this.shouldStop) {

            setState(ControllerStates.CONNECTING);

            try {
                String ip = PreferenceManager.getDefaultSharedPreferences(this.context).getString("emulator_ip_address", "192.168.16.101");
                String port = PreferenceManager.getDefaultSharedPreferences(this.context).getString("emulator_port_number", "7003");
                InetSocketAddress address = new InetSocketAddress(ip, Integer.parseInt(port));
                channel = SocketChannel.open();
                channel.connect(address);

                if (channel.isConnected()) {
                    setState(ControllerStates.CONNECTED);
                }

                handleClient(channel);

                Log.d("EmulatorClientSocket", "handleClient");
            } catch (ConnectException e) {
                //e.printStackTrace();
            } catch (ClosedByInterruptException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            setState(ControllerStates.DISCONNECTED);
            try {
                if(channel.isOpen()) {
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Log.d("EmulatorClientSocket", "Sleep");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d("EmulatorClientSocket", "InterruptedException");
                this.shouldStop = true;
            }
        }
    }

    private void handleClient(SocketChannel channel) {
        PhoneEvent event;
        while (!this.shouldStop) {

            if(!channel.isConnected()) {
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
                return;
            } catch (RemoteException ce) {
                ce.printStackTrace();
            }

            if (!channel.isConnected()) {
                Log.d("EmulatorClientSocket", "!channel.isConnected");
                return;
            }
        }
    }

    public class ButtonCode {
        public static final int kApp = 0x52;
        public static final int kClick = 0x42;
        public static final int kHome = 3;
        public static final int kNone = 0;
        public static final int kVolumeUp = 0x18;
        public static final int kVolumeDown = 0x19;
    }

    private long lastDownTimeMS;

    private void OnPhoneEvent(PhoneEvent event) throws RemoteException {
        if (event != null) {
            int type = event.getType();
            switch (type) {
                case PhoneEvent.Type.MOTION:
                    ControllerTouchEvent eventM = new ControllerTouchEvent();
                    int action = ControllerTouchEvent.ACTION_NONE;
                    switch (event.motionEvent.getAction()) {
                        case 0:
                            action = ControllerTouchEvent.ACTION_DOWN;
                            break;
                        case 2:
                            action = ControllerTouchEvent.ACTION_MOVE;
                            break;
                        case 1:
                            action = ControllerTouchEvent.ACTION_UP;
                            break;
                        case 3:
                            action = ControllerTouchEvent.ACTION_CANCEL;
                            break;

                        case 6:
                        case 5:
                        case 7:
                        case 9:
                        case 10:
                            Log.w("ControllerTouchEvent", "action = " + event.motionEvent.getAction());
                            break;
                    }
                    eventM.action = action;
                    eventM.fingerId = event.motionEvent.pointers[0].getId();
                    eventM.x = event.motionEvent.pointers[0].getNormalizedX();
                    eventM.y = event.motionEvent.pointers[0].getNormalizedY();

                    //eventM.timestampNanos = event.motionEvent.getTimestamp();
                    eventM.timestampNanos = ((event.motionEvent.getAction() & 0xff/*ACTION_MASK*/) == 0) ? ((int) (event.motionEvent.getTimestamp() - lastDownTimeMS)) : 0;
                    controllerListener.onControllerTouchEvent(eventM);
                    if ((event.motionEvent.getAction() & 0xff/*ACTION_MASK*/) != 0) {
                        lastDownTimeMS = event.motionEvent.getTimestamp();
                    }
                    break;
                case PhoneEvent.Type.GYROSCOPE:
                    ControllerGyroEvent eventG = new ControllerGyroEvent();
                    eventG.x = event.gyroscopeEvent.getX();
                    eventG.y = event.gyroscopeEvent.getY();
                    eventG.z = event.gyroscopeEvent.getZ();

                    eventG.timestampNanos = event.gyroscopeEvent.getTimestamp();
                    controllerListener.onControllerGyroEvent(eventG); //probably not used
                    //Log.d("GYRO", "(" + eventG.x + ", " + eventG.y + ", " + eventG.z + ")");
                    break;
                case PhoneEvent.Type.ACCELEROMETER:
                    ControllerAccelEvent eventA = new ControllerAccelEvent();
                    eventA.x = event.accelerometerEvent.getX();
                    eventA.y = event.accelerometerEvent.getY();
                    eventA.z = -event.accelerometerEvent.getZ();

                    eventA.timestampNanos = event.accelerometerEvent.getTimestamp();
                    controllerListener.onControllerAccelEvent(eventA);  //probably not used
                    //Log.d("ACCEL", "(" + eventA.x + ", " + eventA.y + ", " + eventA.z + ")");
                    break;
                case PhoneEvent.Type.ORIENTATION:
                    ControllerOrientationEvent eventO = new ControllerOrientationEvent();
                    eventO.qx = -event.orientationEvent.getX();
                    eventO.qy = -event.orientationEvent.getZ();
                    eventO.qz = event.orientationEvent.getY();
                    eventO.qw = event.orientationEvent.getW();

                    eventO.timestampNanos = event.orientationEvent.getTimestamp();
                    controllerListener.onControllerOrientationEvent(eventO); //must be send
                    //Log.d("Orient", "(" + eventO.qx + ", " + eventO.qy + ", " + eventO.qz + ", " + eventO.qw + ")");
                    break;
                case PhoneEvent.Type.KEY:
                    ControllerButtonEvent eventB = new ControllerButtonEvent();
                    int button = 0;
                    switch (event.keyEvent.getCode()) {
                        case ButtonCode.kNone:
                            button = ControllerButtonEvent.BUTTON_NONE;
                            break;
                        case ButtonCode.kClick:
                            button = ControllerButtonEvent.BUTTON_CLICK;
                            break;
                        case ButtonCode.kHome:
                            button = ControllerButtonEvent.BUTTON_HOME;
                            break;
                        case ButtonCode.kApp:
                            button = ControllerButtonEvent.BUTTON_APP;
                            break;
                        case ButtonCode.kVolumeUp:
                            button = ControllerButtonEvent.BUTTON_VOLUME_UP;
                            break;
                        case ButtonCode.kVolumeDown:
                            button = ControllerButtonEvent.BUTTON_VOLUME_DOWN;
                            break;
                    }
                    eventB.button = button;
                    eventB.down = event.keyEvent.getAction() == 0;

                    controllerListener.onControllerButtonEvent(eventB);
                    break;
                case PhoneEvent.Type.DEPTH_MAP:
                    break;
            }
        }
    }
}
