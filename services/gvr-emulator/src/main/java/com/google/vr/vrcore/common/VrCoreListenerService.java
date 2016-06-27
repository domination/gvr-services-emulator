package com.google.vr.vrcore.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class VrCoreListenerService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("VrCoreListenerService", "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("VrCoreListenerService", "onBind " + intent.getAction());
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("VrCoreListenerService", "onDestroy");
    }
}
