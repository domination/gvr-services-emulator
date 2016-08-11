package com.google.vr.vrcore.settings.helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.google.vr.vrcore.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BluetoothDevicePreference extends ListPreference implements DialogInterface.OnClickListener, Preference.OnPreferenceChangeListener {

    public BluetoothDevicePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public BluetoothDevicePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BluetoothDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BluetoothDevicePreference(Context context) {
        super(context);
        init();
    }

    private List<Map<String, String>> entryList = null;

    private void readBluetoothDevices() {
        entryList.clear();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            for (BluetoothDevice bt : pairedDevices) {
                Map<String, String> m = new HashMap<>();
                m.put("name", bt.getName());
                m.put("address", bt.getAddress());
                entryList.add(m);
            }
        }// else {
//            Map<String, String> m = new HashMap<>();
//            m.put("name", "None");
//            m.put("address", getContext().getString(R.string.pref_default_bt_device));
//            entryList.add(m);
//        }
    }

    private void init() {
        entryList = new ArrayList<>();

        readBluetoothDevices();

        String[] keys = {"name", "address"};
        int[] widgetIds = {android.R.id.text1, android.R.id.text2};

        listAdapter = new SimpleAdapter(this.getContext(), entryList, android.R.layout.simple_list_item_2, keys, widgetIds);
    }

    private OnPreferenceChangeListener onPreferenceChangeListener;

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.onPreferenceChangeListener = onPreferenceChangeListener;
        super.setOnPreferenceChangeListener(this);
    }

    private ListAdapter listAdapter;

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which >= 0) {
            getDialog().dismiss();
            Map<String, String> m = entryList.get(which);
            persistString(m.get("address"));
            getOnPreferenceChangeListener().onPreferenceChange(this, m.get("address"));
        }
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        String p = super.getPersistedString(defaultReturnValue);
        Log.d("getPersistedString", "defaultReturnValue - " + p);
        return p;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        //super.onPrepareDialogBuilder(builder);
        builder.setTitle("Bluetooth");
        //builder.setAdapter(listAdapter, this);
        builder.setSingleChoiceItems(listAdapter, -1, this);
    }

    private Dialog mDialog;

    @Override
    public Dialog getDialog() {
        //return super.getDialog();
        return mDialog;
    }

    @Override
    public int findIndexOfValue(String value) {
        int c = entryList.size();
        for (int i = 0; i < c; i++) {
            if (entryList.get(i).get("address").equals(value)) {
                return i;
            }
        }
        return -1;//super.findIndexOfValue(value);
    }

    @Override
    public CharSequence[] getEntries() {
        //return super.getEntries();
        CharSequence[] output = new CharSequence[entryList.size()];
        int c = entryList.size();
        for (int i = 0; i < c; i++) {
            output[i] = entryList.get(i).get("address");
        }
        return output;
    }

    private boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return false;
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        return true;
    }

    @Override
    protected void showDialog(Bundle state) {
//        super.showDialog(state);
//        AlertDialog dialog = (AlertDialog) super.getDialog();
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
//        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);

        if(!setBluetooth(true)) {
            return;
        }

        readBluetoothDevices();

        Context context = getContext();

        //mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context)
                .setTitle(getTitle())
                .setIcon(getIcon());
        //.setPositiveButton(mPositiveButtonText, this)
        //.setNegativeButton(mNegativeButtonText, this);
        onCreateDialogView();
        View contentView = onCreateDialogView();
        //LayoutInflater inflater = LayoutInflater.from(mBuilder.getContext());
        //View contentView = inflater.inflate(getDialogLayoutResource(), null);
        if (contentView != null) {
            onBindDialogView(contentView);
            mBuilder.setView(contentView);
        } else {
            mBuilder.setMessage(getDialogMessage());
        }

        onPrepareDialogBuilder(mBuilder);

        //getPreferenceManager().registerOnActivityDestroyListener(this);

        // Create the dialog
        final Dialog dialog = mDialog = mBuilder.create();
        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        //if (needInputMethod()) {
        //    requestInputMethod(dialog);
        //}
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //persistString(newValue.toString());
        return this.onPreferenceChangeListener.onPreferenceChange(preference, newValue);
    }
}
