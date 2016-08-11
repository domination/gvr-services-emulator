package com.google.vr.vrcore.common.helper;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import javaext.util.Log;

public abstract class VrListenerService extends Service {

    public static final String SERVICE_INTERFACE = "android.service.vr.VrListenerService";

    //https://developer.android.com/reference/android/provider/Settings.html#ACTION_VR_LISTENER_SETTINGS
    public static final String ACTION_VR_LISTENER_SETTINGS = "android.settings.VR_LISTENER_SETTINGS";

    public VrListenerService() {
        Log.d("VrListenerService", "VrListenerService");
    }

    public static final boolean isVrModePackageEnabled(Context context, ComponentName requestedComponent) {
        Log.call("VrListenerService", "isVrModePackageEnabled", context.toString(), requestedComponent.toString());
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.call("VrListenerService", "onBind", intent.getAction());
        return null;
    }

    public void onCurrentVrActivityChanged(ComponentName component) {
        Log.call("VrListenerService", "onCurrentVrActivityChanged", component.toString());
    }
}