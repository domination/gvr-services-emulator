package com.google.vr.vrcore.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class VrCoreListenerService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("VrCoreListenerService", "onBind " + intent.getAction());
        return null;
    }
}
