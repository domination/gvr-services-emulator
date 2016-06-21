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
    private Handler handler;

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

    public ControllerService service;

    @Override
    public void run() {
        if (isEnabled) {
            if (!this.isConnected()) {
                this.tryConnect();
            }
            if (!this.handle()) {
                if (isEnabled) {
                    this.handler.postDelayed(this, 2000);
                    return;
                } else {
                    this.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            service.refreshMapListeners();
                        }
                    });
                    return;
                }
            }
            this.handler.post(this);
        } else {
            if (this.isConnected()) {
                this.disconnect();
            }

            this.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    service.refreshMapListeners();
                }
            }, 2000);
        }
    }

    public void stop() {
        this.isEnabled = false;
        this.handler.removeCallbacks(this);
    }

    public void setControllerListener(RegisteredControllerListener controllerListener) {
        this.registeredControllerListener = controllerListener;
        this.controllerListener = this.registeredControllerListener.listener;
    }

    public boolean setState(int state) {
        return this.registeredControllerListener.setState(state);
    }

    public void OnPhoneEvent(PhoneEvent event) throws RemoteException {
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
                            Log.d("ControllerTouchEvent", "action = " + event.motionEvent.getAction());
                            break;
                    }
                    eventM.action = action;
                    eventM.fingerId = event.motionEvent.pointers[0].getId();
                    eventM.x = event.motionEvent.pointers[0].getNormalizedX();
                    eventM.y = event.motionEvent.pointers[0].getNormalizedY();

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
                    break;
                case PhoneEvent.Type.ACCELEROMETER:
                    ControllerAccelEvent eventA = new ControllerAccelEvent();
                    eventA.x = event.accelerometerEvent.getX();
                    eventA.y = event.accelerometerEvent.getY();
                    eventA.z = -event.accelerometerEvent.getZ();

                    eventA.timestampNanos = event.accelerometerEvent.getTimestamp();
                    controllerListener.onControllerAccelEvent(eventA);  //probably not used
                    break;
                case PhoneEvent.Type.ORIENTATION:
                    ControllerOrientationEvent eventO = new ControllerOrientationEvent();
                    eventO.qx = -event.orientationEvent.getX();
                    eventO.qy = -event.orientationEvent.getZ();
                    eventO.qz = event.orientationEvent.getY();
                    eventO.qw = event.orientationEvent.getW();

                    eventO.timestampNanos = event.orientationEvent.getTimestamp();
                    controllerListener.onControllerOrientationEvent(eventO); //must be send
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