package com.google.vr.vrcore;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.vr.vrcore.controller.api.ControllerServiceConsts;

public class VrCoreListenerService extends Service {

    private HandlerThread thread;

    private final ControllerService controllerService = new ControllerService();

    @Override
    public void onCreate() {
        Log.d("com.google.vr.vrcore", "onCreate");
        thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_LESS_FAVORABLE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("com.google.vr.vrcore", "onBind " + intent.getAction());
        if (ControllerServiceConsts.BIND_INTENT_ACTION.equals(intent.getAction())) {

            if (!thread.isAlive()) {
                thread.start();
            }

            controllerService.setContext(getApplicationContext());
            controllerService.setLooper(thread.getLooper());

            return controllerService;
        }
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("com.google.vr.vrcore", "onUnbind " + intent.getAction());
        if (ControllerServiceConsts.BIND_INTENT_ACTION.equals(intent.getAction())) {
            thread.getLooper().quit();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        if (thread.isAlive()) {
            thread.interrupt();
        }
        Log.d("com.google.vr.vrcore", "onDestroy");
        super.onDestroy();
    }
}
