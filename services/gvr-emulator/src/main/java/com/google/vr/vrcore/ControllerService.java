package com.google.vr.vrcore;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.google.vr.vrcore.controller.api.ControllerStates;
import com.google.vr.vrcore.controller.api.IControllerListener;
import com.google.vr.vrcore.controller.api.IControllerService;

public class ControllerService extends IControllerService.Stub {

    private Looper mLooper;
    void setLooper(Looper looper) {
        mLooper = looper;
    }
    private Context mContext;
    void setContext(Context context) {
        mContext = context;
    }

    private IControllerListener controllerListener;

    @Override
    public int initialize(int targetApiVersion) throws RemoteException {
        Log.d("ControllerService", "initialize");
        return ControllerStates.DISCONNECTED;
    }

    @Override
    public int getApiVersion() throws RemoteException {
        Log.d("ControllerService", "getApiVersion");
        return 2;
    }

    @Override
    public void recenter(int controllerId) throws RemoteException {
        Log.d("ControllerService", "recenter");
    }

    @Override
    public boolean registerListener(int controllerId, String key, IControllerListener listener) throws RemoteException {
        Log.d("ControllerService", "registerListener(" + controllerId + ", " + key + ", " + listener.getClass().getName() + ")");
        this.controllerListener = listener;

        EmulatorClientSocket ecs = new EmulatorClientSocket(this.mContext, this.controllerListener, controllerId);

        Handler mServiceHandler = new Handler(mLooper);
        mServiceHandler.post(ecs);
        return true;
    }

    @Override
    public boolean unregisterListener(String key) throws RemoteException {
        Log.d("ControllerService", "unregisterListener(" + key + ")");
        return false;
    }
}
