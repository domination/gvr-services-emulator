package com.google.vr.vrcore.controller;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.google.vr.gvr.io.proto.nano.Protos.PhoneEvent;
import com.google.vr.vrcore.controller.api.ControllerAccelEvent;
import com.google.vr.vrcore.controller.api.ControllerButtonEvent;
import com.google.vr.vrcore.controller.api.ControllerGyroEvent;
import com.google.vr.vrcore.controller.api.ControllerOrientationEvent;
import com.google.vr.vrcore.controller.api.ControllerTouchEvent;
import com.google.vr.vrcore.controller.api.IControllerListener;

public abstract class BaseController implements Runnable {

    public class ButtonCode {
        public static final int kApp = 0x52;
        public static final int kClick = 0x42;
        public static final int kHome = 3;
        public static final int kNone = 0;
        public static final int kVolumeUp = 0x18;
        public static final int kVolumeDown = 0x19;
    }

    protected RegisteredControllerListener registeredControllerListener;
    private IControllerListener controllerListener;

    private long lastDownTimeMS;
    private final Handler handler;

    public BaseController(Handler handler) {
        this.handler = handler;
        this.lastDownTimeMS = 0;
    }

    public abstract boolean isAvailable();

    public abstract boolean isConnected();

    public abstract boolean tryConnect();

    public abstract boolean handle();

    public abstract void disconnect();

    public boolean isEnabled = true;

    public Runnable afterDisconnect;

    @Override
    public void run() {
        boolean refresh = false;
        if (isEnabled) {
            boolean handled = false;

            if (!this.isConnected()) {
                if (this.tryConnect()) {
                    handled = this.handle();
                }
            } else {
                handled = this.handle();
            }

            if (!handled) {
                refresh = true;
            }

        } else {
            refresh = true;
        }

        if (refresh) {
            this.handler.removeCallbacks(this);
            disconnect();
            this.handler.postDelayed(afterDisconnect, 2000);
        } else {
            this.handler.post(this);
        }
    }

    public void stop() {
        this.isEnabled = false;
    }

    public void setControllerListener(RegisteredControllerListener controllerListener) {
        this.registeredControllerListener = controllerListener;
        this.controllerListener = this.registeredControllerListener.listener;
    }

    public boolean setState(int state) {
        return this.registeredControllerListener.setState(state);
    }

    private final ControllerTouchEvent controllerTouchEvent = new ControllerTouchEvent();
    private final ControllerGyroEvent controllerGyroEvent = new ControllerGyroEvent();
    private final ControllerAccelEvent controllerAccelEvent = new ControllerAccelEvent();
    private final ControllerOrientationEvent controllerOrientationEvent = new ControllerOrientationEvent();
    private final ControllerButtonEvent controllerButtonEvent = new ControllerButtonEvent();

    public void OnPhoneEvent(PhoneEvent event) throws RemoteException {
        if (event != null) {
            switch (event.getType()) {
                case PhoneEvent.Type.MOTION:
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
                        default:
                            Log.d("ControllerTouchEvent", "action = " + event.motionEvent.getAction());
                            break;
                    }
                    controllerTouchEvent.action = action;
                    controllerTouchEvent.fingerId = event.motionEvent.pointers[0].getId();
                    controllerTouchEvent.x = event.motionEvent.pointers[0].getNormalizedX();
                    controllerTouchEvent.y = event.motionEvent.pointers[0].getNormalizedY();

                    controllerTouchEvent.timestampNanos = ((event.motionEvent.getAction() & 0xff/*ACTION_MASK*/) == 0) ? ((int) (event.motionEvent.getTimestamp() - lastDownTimeMS)) : 0;
                    controllerListener.onControllerTouchEvent(controllerTouchEvent);
                    if ((event.motionEvent.getAction() & 0xff/*ACTION_MASK*/) != 0) {
                        lastDownTimeMS = event.motionEvent.getTimestamp();
                    }
                    break;
                case PhoneEvent.Type.GYROSCOPE:
                    controllerGyroEvent.x = event.gyroscopeEvent.getX();
                    controllerGyroEvent.y = event.gyroscopeEvent.getY();
                    controllerGyroEvent.z = event.gyroscopeEvent.getZ();

                    controllerGyroEvent.timestampNanos = event.gyroscopeEvent.getTimestamp();
                    controllerListener.onControllerGyroEvent(controllerGyroEvent); //probably not used
                    break;
                case PhoneEvent.Type.ACCELEROMETER:
                    controllerAccelEvent.x = event.accelerometerEvent.getX();
                    controllerAccelEvent.y = event.accelerometerEvent.getY();
                    controllerAccelEvent.z = -event.accelerometerEvent.getZ();

                    controllerAccelEvent.timestampNanos = event.accelerometerEvent.getTimestamp();
                    controllerListener.onControllerAccelEvent(controllerAccelEvent); //probably not used

                    //controllerOrientationEvent.timestampNanos = event.accelerometerEvent.getTimestamp(); //this is just test for phone without gyroscope
                    //controllerListener.onControllerOrientationEvent(controllerOrientationEvent); //faking it when no orientation event occurs
                    break;
                case PhoneEvent.Type.ORIENTATION:
                    controllerOrientationEvent.qx = -event.orientationEvent.getX();
                    controllerOrientationEvent.qy = -event.orientationEvent.getZ();
                    controllerOrientationEvent.qz = event.orientationEvent.getY();
                    controllerOrientationEvent.qw = event.orientationEvent.getW();

                    controllerOrientationEvent.timestampNanos = event.orientationEvent.getTimestamp();
                    controllerListener.onControllerOrientationEvent(controllerOrientationEvent); //must be send
                    break;
                case PhoneEvent.Type.KEY:
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
                    controllerButtonEvent.button = button;
                    controllerButtonEvent.down = event.keyEvent.getAction() == 0;

                    controllerListener.onControllerButtonEvent(controllerButtonEvent);
                    break;
                case PhoneEvent.Type.DEPTH_MAP: //not used right now
                    Log.d("DEPTH_MAP", event.depthMapEvent.toString());
                    break;
            }
        }
    }
}