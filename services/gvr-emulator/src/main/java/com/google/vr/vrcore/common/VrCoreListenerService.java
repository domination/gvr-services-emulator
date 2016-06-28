package com.google.vr.vrcore.common;

import android.util.Log;

import com.google.vr.vrcore.common.helper.VrListenerService;

public class VrCoreListenerService extends /*android.service.vr.*/VrListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("VrCoreListenerService", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("VrCoreListenerService", "onDestroy");
    }
}
