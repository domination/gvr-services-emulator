package com.google.vr.sdk.samples.controllerclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.Controller.ConnectionStates;
import com.google.vr.sdk.controller.ControllerManager;
import com.google.vr.sdk.controller.ControllerManager.ApiStatus;

import java.util.List;
import java.util.Locale;

/**
 * Minimal example demonstrating how to receive and process Daydream controller input. It connects
 * to a Daydream Controller and displays a simple graphical and textual representation of the
 * controller's sensors. This example only works with Android N and Daydream-ready phones.
 */
public class ControllerClientActivity extends Activity {

    private static final String TAG = "ControllerClientActivit";

    // These two objects are the primary APIs for interacting with the Daydream controller.
    private ControllerManager controllerManager;
    private Controller controller;

    // These TextViews display controller events.
    private TextView apiStatusView;
    private TextView controllerStateView;
    private TextView controllerOrientationText;
    private TextView controllerTouchpadView;
    private TextView controllerButtonView;

    // This is a 3D representation of the controller's pose. See its comments for more information.
    private OrientationView controllerOrientationView;

    // The various events we need to handle happen on arbitrary threads. They need to be reposted to
    // the UI thread in order to manipulate the TextViews. This is only required if your app needs to
    // perform actions on the UI thread in response to controller events.
    private Handler uiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // General view initialization.
        setContentView(R.layout.main_layout);
        apiStatusView = (TextView) findViewById(R.id.api_status_view);
        controllerStateView = (TextView) findViewById(R.id.controller_state_view);
        controllerTouchpadView = (TextView) findViewById(R.id.controller_touchpad_view);
        controllerButtonView = (TextView) findViewById(R.id.controller_button_view);
        controllerOrientationText = (TextView) findViewById(R.id.controller_orientation_text);
        controllerTouchpadView = (TextView) findViewById(R.id.controller_touchpad_view);
        controllerButtonView = (TextView) findViewById(R.id.controller_button_view);

        // Start the ControllerManager and acquire a Controller object which represents a single
        // physical controller. Bind our listener to the ControllerManager and Controller.
        EventListener listener = new EventListener();
        controllerManager = new ControllerManager(this, listener);
        apiStatusView.setText("Binding to VR Service");
        controller = controllerManager.getController();
        controller.setEventListener(listener);

        // Bind the OrientationView to our acquired controller.
        controllerOrientationView = (OrientationView) findViewById(R.id.controller_orientation_view);
        controllerOrientationView.setController(controller);

        // This configuration won't be required for normal GVR apps. However, since this sample doesn't
        // use GvrView, it needs pretend to be a VR app in order to receive controller events. The
        // Activity.setVrModeEnabled is only enabled on in N, so this is an GVR-internal utility method
        // to configure the app via reflection.
        //
        // If this sample is compiled with the N SDK, Activity.setVrModeEnabled can be called directly.
        //AndroidNCompat.setIsAtLeastNForTesting(true);
        //AndroidCompat.setVrModeEnabled(this, true);
        //
        String servicePackage = "com.google.vr.vrcore";
        String serviceClass = "com.google.vr.vrcore.common.VrCoreListenerService";
        ComponentName serviceComponent = new ComponentName(servicePackage, serviceClass);
        try {
            setVrModeEnabled(true, serviceComponent);
        } catch (PackageManager.NameNotFoundException e) {
            List<ApplicationInfo> installed = getPackageManager().getInstalledApplications(0);
            boolean isInstalled = false;
            for (ApplicationInfo app : installed) {
                if (app.packageName.equals(servicePackage)) {
                    isInstalled = true;
                    break;
                }
            }
            if (isInstalled) {
                // Package is installed, but not enabled in Settings.  Let user enable it.
                startActivity(new Intent("android.settings.VR_LISTENER_SETTINGS"));//Settings.ACTION_VR_LISTENER_SETTINGS));
            } else {
                // Package is not installed.  Send an intent to download this.
                //sentIntentToLaunchAppStore(servicePackage);
            }
        }
    }


    public void setVrModeEnabled(boolean paramBoolean, ComponentName paramComponentName) throws PackageManager.NameNotFoundException {
        Log.w(TAG, "setVrModeEnabled");
        ComponentName componentName = new ComponentName("com.google.vr.vrcore", "com.google.vr.vrcore.common.VrCoreListenerService");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        controllerManager.start();
        controllerOrientationView.startTrackingOrientation();
    }

    @Override
    protected void onStop() {
        controllerManager.stop();
        controllerOrientationView.stopTrackingOrientation();
        super.onStop();
    }

    // We receive all events from the Controller through this listener. In this example, our
    // listener handles both ControllerManager.EventListener and Controller.EventListener events.
    // This class is also a Runnable since the events will be reposted to the UI thread.
    private class EventListener extends Controller.EventListener
            implements ControllerManager.EventListener, Runnable {

        // The status of the overall controller API. This is primarily used for error handling since
        // it rarely changes.
        private String apiStatus;

        // The state of a specific Controller connection.
        private int controllerState = ConnectionStates.DISCONNECTED;

        @Override
        public void onApiStatusChanged(int state) {
            apiStatus = ApiStatus.toString(state);
            uiHandler.post(this);
        }

        @Override
        public void onConnectionStateChanged(int state) {
            controllerState = state;
            uiHandler.post(this);
        }

        @Override
        public void onRecentered() {
            // If this was a real GVR application, this would call
            // {@link com.google.vr.sdk.base.GvrView#resetHeadTracker} instead of this method.
            controllerOrientationView.resetYaw();
        }

        @Override
        public void onUpdate() {
            uiHandler.post(this);
        }

        // Update the various TextViews in the UI thread.
        @Override
        public void run() {
            apiStatusView.setText(apiStatus);
            controllerStateView.setText(ConnectionStates.toString(controllerState));
            controller.update();
            controllerOrientationText.setText(
                    " " + controller.orientation + "\n" + controller.orientation.toAxisAngleString());
            if (controller.isTouching) {
                controllerTouchpadView.setText(
                        String.format(Locale.US, "[%4.2f, %4.2f]", controller.touch.x, controller.touch.y));
            } else {
                controllerTouchpadView.setText("[ NO TOUCH ]");
            }
            controllerButtonView.setText(String.format("[%s][%s][%s][%s][%s]",
                    controller.appButtonState ? "A" : " ",
                    controller.homeButtonState ? "H" : " ",
                    controller.clickButtonState ? "T" : " ",
                    controller.volumeUpButtonState ? "+" : " ",
                    controller.volumeDownButtonState ? "-" : " "));
        }
    }
}