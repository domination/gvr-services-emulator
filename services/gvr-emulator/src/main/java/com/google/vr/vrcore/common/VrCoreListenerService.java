package com.google.vr.vrcore.common;

import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import javaext.util.Log;

import com.google.vr.vrcore.common.helper.VrListenerService;

public class VrCoreListenerService extends /*android.service.vr.*/VrListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("VrCoreListenerService", "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("VrCoreListenerService", "onBind");
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.call("VrCoreListenerService", "onUnbind", intent.getAction());
        //return super.onUnbind(intent);

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("VrCoreListenerService", "onDestroy");
    }

    @Override
    public void onCurrentVrActivityChanged(ComponentName component) {
        Log.d("VrCoreListenerService", "onCurrentVrActivityChanged");
        super.onCurrentVrActivityChanged(component);
    }
}
