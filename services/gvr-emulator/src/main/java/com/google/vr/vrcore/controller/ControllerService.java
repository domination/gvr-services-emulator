package com.google.vr.vrcore.controller;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.vr.vrcore.R;
import com.google.vr.vrcore.controller.api.ControllerServiceConsts;
import com.google.vr.vrcore.controller.api.ControllerStates;
import com.google.vr.vrcore.controller.api.IControllerListener;
import com.google.vr.vrcore.controller.api.IControllerService;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javaext.net.bluetooth.BluetoothSocketAddress;

public class ControllerService extends Service {

    private HandlerThread handlerThread;
    private Handler handler;
    private Runnable stopAction;

    private IControllerService.Stub controllerService;
    private final Map<String, RegisteredControllerListener> mapListeners;
    private final Map<String, BaseController> mapControllerProviders;
    private int registerCount;

    public ControllerService() {
        this.mapListeners = Collections.synchronizedMap(new HashMap<String, RegisteredControllerListener>());
        this.registerCount = 0;
        this.mapControllerProviders = Collections.synchronizedMap(new HashMap<String, BaseController>());
        this.stopAction = new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        };
        this.controllerService = new IControllerService.Stub() {

            @Override
            public int initialize(int targetApiVersion) throws RemoteException {
                Log.d("ControllerService", "initialize(" + targetApiVersion + ")");
                return 0; //com.google.vr.sdk.controller.ControllerManager.ApiStatus.OK = 0
            }

            @Override
            public void recenter(int controllerId) throws RemoteException {
                Log.d("ControllerService", "recenter(" + controllerId + ")");
            }

            @Override
            public boolean registerListener(final int controllerId, final String key, final IControllerListener listener) throws RemoteException {
                Log.d("ControllerService", "registerListener(" + controllerId + ", " + key + ", " + listener.getClass().getName() + ")");
                if (controllerId != 0) {
                    return false;
                }
                final String keyMasked = getKeyUid(key);
                handler.post(new Runnable() {
                    @Override
                    public final void run() {
                        ControllerService.this.registerListener(controllerId, keyMasked, listener);
                    }
                });
                return true;
            }

            @Override
            public boolean unregisterListener(String key) throws RemoteException {
                Log.d("ControllerService", "unregisterListener(" + key + ")");
                String keyMasked = getKeyUid(key);
                return ControllerService.this.unregisterListener(keyMasked);
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("com.google.vr.vrcore", "onCreate");

        if (this.handler == null) {
            this.handlerThread = new HandlerThread(String.valueOf(ControllerService.class.getSimpleName()).concat(":Thread"));
            this.handlerThread.start();
            this.handler = new Handler(this.handlerThread.getLooper());
        }

        boolean isWiFi = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getBoolean("emulator_wifi", true);
        boolean isBluetooth = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getBoolean("emulator_bluetooth", true);

        if (isWiFi) {
            String ip = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("emulator_ip_address", getString(R.string.pref_default_ip_address));
            String port = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("emulator_port_number", getString(R.string.pref_default_port_number));
            this.mapControllerProviders.put("EmulatorWiFi", new com.google.vr.vrcore.controller.emulator.Controller(this.handler, new InetSocketAddress(ip, Integer.parseInt(port))));
        }
        if (isBluetooth) {
            String bt_address = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("emulator_bt_device", getString(R.string.pref_default_bt_device));
            com.google.vr.vrcore.controller.emulator.Controller emulator = new com.google.vr.vrcore.controller.emulator.Controller(this.handler, new BluetoothSocketAddress(bt_address, UUID.fromString(getString(R.string.pref_default_bt_uuid))));
            if (emulator.setBluetooth(true)) {
                this.mapControllerProviders.put("EmulatorBluetooth", emulator);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("com.google.vr.vrcore", "onBind " + intent.getAction());
        restart();
        if (ControllerServiceConsts.BIND_INTENT_ACTION.equals(intent.getAction())) {
            return controllerService.asBinder();
        }
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("com.google.vr.vrcore", "onRebind " + intent.getAction());
        restart();
    }

    private BaseController controller = null;

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("com.google.vr.vrcore", "onUnbind " + intent.getAction());
        if (this.controller != null) {
            this.controller.stop();
            this.controller = null;
        }
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                ensureLooper();
                if (registerCount > 0) {
                    registerCount--;
                }
                if (registerCount <= 0) {
                    handler.postDelayed(stopAction, 5000);
                }
            }
        });
        super.onUnbind(intent);
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d("com.google.vr.vrcore", "onDestroy");
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        this.handler.post(new Runnable() {
            @Override
            public void run() {
                ensureLooper();
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (this.handlerThread != null) {
            this.handler.removeCallbacksAndMessages(null);
            this.handlerThread.quitSafely();
            this.handlerThread.getLooper().quit();
        }
        this.handler = null;
        Log.d("com.google.vr.vrcore", "onDestroy - complete");
        super.onDestroy();
    }

    private String getKeyUid(String key) {
        return String.format(Locale.US, "%d:%d:%s", Binder.getCallingUid(), Binder.getCallingPid(), key);
    }

    private void ensureLooper() {
        if (Looper.myLooper() != this.handler.getLooper()) {
            throw new IllegalStateException("This must run on the ControllerService's thread.");
        }
    }

    private void restart() {
        this.handler.removeCallbacks(this.stopAction);

        this.startService(new Intent(this, ControllerService.class));
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                ensureLooper();
                registerCount++;
            }
        });
    }

    public final void registerListener(int controllerId, String key, IControllerListener listener) {
        ensureLooper();
        Log.d("ControllerService", "registerListener(" + controllerId + ", " + key + ", " + listener.getClass().toString() + ")");
        try {
            this.mapListeners.put(key, new RegisteredControllerListener(controllerId, listener));

            for (Object o : this.mapControllerProviders.entrySet()) {
                ((BaseController) ((Map.Entry) o).getValue()).isEnabled = true;
            }

            refreshMapListeners();
        } catch (RemoteException e) {
            Log.d(ControllerService.class.getSimpleName(), "Attempted to register a dead listener, ID " + (key.length() != 0 ? key : ""));
        }
    }

    public final boolean unregisterListener(String key) {
        if ((mapListeners.get(key)) == null) {
            Log.d(ControllerService.class.getSimpleName(), "Listener not registered. Unregister failed.");
            return false;
        }
        mapListeners.remove(key);
        return true;
    }

    public final void refreshMapListeners() {
        synchronized (this.mapListeners) {

            if (this.registerCount <= 0) return;

            Iterator it = this.mapListeners.entrySet().iterator();
            while (it.hasNext()) {
                RegisteredControllerListener registeredControllerListener = (RegisteredControllerListener) ((Map.Entry) it.next()).getValue();
                if (!registeredControllerListener.setState(ControllerStates.SCANNING)) {
                    it.remove();
                    continue;
                }

                controller = null;

                synchronized (this.mapControllerProviders) {
                    for (Object o : this.mapControllerProviders.entrySet()) {
                        BaseController _controller = (BaseController) ((Map.Entry) o).getValue();
                        if (_controller.isAvailable()) {
                            _controller.isEnabled = true;
                            _controller.setControllerListener(registeredControllerListener);
                            _controller.service = this;
                            controller = _controller;
                            break;
                        }
                    }
                }
            }
        }

        if (controller != null) {
            this.handler.post(controller);
        } else {
            synchronized (this.mapControllerProviders) {

                if (this.mapControllerProviders.size() == 0) return;

                Iterator itControllerProviders = this.mapControllerProviders.entrySet().iterator();
                while (itControllerProviders.hasNext()) {
                    ((BaseController) ((Map.Entry) itControllerProviders.next()).getValue()).isEnabled = true;
                }
            }
            this.handler.postDelayed(new Runnable() {
                @Override
                public final void run() {
                    refreshMapListeners();
                }
            }, 2000);
        }
    }
}
