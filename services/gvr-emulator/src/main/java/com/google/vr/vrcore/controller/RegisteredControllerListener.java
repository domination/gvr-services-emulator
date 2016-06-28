package com.google.vr.vrcore.controller;

import android.os.RemoteException;

import com.google.vr.vrcore.controller.api.ControllerListenerOptions;
import com.google.vr.vrcore.controller.api.IControllerListener;

public class RegisteredControllerListener {
    public final IControllerListener listener;
    public ControllerListenerOptions options;
    public int apiVersion;
    public final int controllerId;
    public int currentState;

    public RegisteredControllerListener(int controllerId, IControllerListener listener) throws RemoteException {
        this.currentState = 0;
        this.listener = listener;
        this.controllerId = controllerId;
        RemoteException re = null;
        try {
            this.apiVersion = listener.getApiVersion();
        } catch (RemoteException e) {
            e.printStackTrace();
            re = e;
        }
        try {
            if (this.apiVersion >= 2) {
                this.options = listener.getOptions();
            }
            if (this.options == null) {
                this.options = new ControllerListenerOptions();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            re = e;
        }
        if (re != null) {
            throw re;
        }
    }

    public boolean setState(int state) {
        try {
            this.currentState = state;
            listener.onControllerStateChanged(this.controllerId, state);
            return true;
        } catch (RemoteException e) {
            this.currentState = 0;
            e.printStackTrace();
            return false;
        }
    }
}
