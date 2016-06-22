package com.google.vr.vrcore.settings;

import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;

import com.google.vr.vrcore.R;

import java.util.List;

public class VrSettingsActivity extends PreferenceActivity {
//    final BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d("onActivityResult", "requestCode: " + requestCode + ", resultCode: " + resultCode);
//        if (data != null) {
//            Log.d("onActivityResult", data.toString());
//        }
//    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.pref_headers, target);
//
//        if (!bluetooth.isEnabled()) {
//            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
//        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        boolean result = true;
        String appCompatClassName = VrSettingsFragment.class.getName();
        if (!appCompatClassName.equals(fragmentName)) {
            try {
                String superClassName = Class.forName(fragmentName).getSuperclass().getName();
                result = superClassName.equals(appCompatClassName);
            } catch (ClassNotFoundException e) {
                Log.d("isValidFragment", "ClassNotFoundException " + fragmentName);
            }
        }
        return result || PreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
