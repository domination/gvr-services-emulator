package com.google.vr.vrcore.settings;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.vr.cardboard.VrParamsProvider;
import com.google.vr.cardboard.VrSettingsProviderContract;
import com.google.vrtoolkit.cardboard.proto.nano.CardboardDevice;

import java.util.Arrays;

import javaext.util.Log;

public class VrSettingsProvider extends ContentProvider {

    private VrParamsProvider vrParamsProvider;
    private UriMatcher uriMatcher;

    public VrSettingsProvider() {
        super();
    }

    VrSettingsProvider(VrParamsProvider paramsProvider) {
        super();
        this.vrParamsProvider = paramsProvider;
    }

    @Override
    public boolean onCreate() {
        Log.d("VrSettingsProvider", "onCreate");
        if (this.vrParamsProvider == null) {
            this.vrParamsProvider = new VrCoreParamsProvider(getContext());
        }
        this.uriMatcher = new UriMatcher(-1);
        return true;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        Log.call("VrSettingsProvider", "attachInfo", context, info);

        if (info == null) {
            Log.e("VrSettingsProvider", "Invalid VrSettingsProvider attachment info");
            return;
        }

        this.uriMatcher.addURI(info.authority, VrSettingsProviderContract.DEVICE_PARAMS_SETTING, 100);
        this.uriMatcher.addURI(info.authority, VrSettingsProviderContract.PHONE_PARAMS_SETTING, 101);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.call("VrSettingsProvider", "query", uri, Arrays.toString(projection), selection, Arrays.toString(selectionArgs), sortOrder);
        switch (this.uriMatcher.match(uri)) {
            case 100:
                return cursorFromMessage(this.vrParamsProvider.readDeviceParams());
            case 101:
                return cursorFromMessage(this.vrParamsProvider.readPhoneParams());
        }
        return null;
    }

    private static Cursor cursorFromMessage(MessageNano messageNano) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{VrSettingsProviderContract.SETTING_VALUE_KEY});
        if (messageNano != null) {
            matrixCursor.addRow(new Object[]{MessageNano.toByteArray(messageNano)});
            return matrixCursor;
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.call("VrSettingsProvider", "update", uri, values.toString(), selection, selectionArgs);
        switch (this.uriMatcher.match(uri)) {
            case 100:
                if (!values.containsKey(VrSettingsProviderContract.SETTING_VALUE_KEY)) {
                    Log.e("VrSettingsProvider", "Viewer params must use key value");
                    return 0;
                }
                byte[] value = values.getAsByteArray(VrSettingsProviderContract.SETTING_VALUE_KEY);
                if (value == null) {
                    Log.e("VrSettingsProvider", "Invalid byte array value for viewer params update");
                    return 0;
                }
                try {
                    CardboardDevice.DeviceParams deviceParams = CardboardDevice.DeviceParams.parseFrom(value);
                    if (this.vrParamsProvider.writeDeviceParams(deviceParams)) {
                        getContext().getContentResolver().notifyChange(uri, null, false);
                        return 1;
                    }
                } catch (InvalidProtocolBufferNanoException e) {
                    e.printStackTrace();
                }
                break;
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        Log.call("VrSettingsProvider", "TODO getType", uri);
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.call("VrSettingsProvider", "TODO insert", uri, values);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.call("VrSettingsProvider", "TODO delete", uri, selection, Arrays.toString(selectionArgs));
        return 0;
    }
}
