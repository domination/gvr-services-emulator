package com.google.vr.vrcore.settings.helper;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.google.vr.vrcore.R;

public class IPAddressPreference extends EditTextPreference implements Preference.OnPreferenceChangeListener {
    public IPAddressPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public IPAddressPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public IPAddressPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IPAddressPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        getEditText().setFilters(
                new InputFilter[]{
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int destStart, int destEnd) {
                                Log.d("filter", "'" + source + "' start: " + start + ", end: " + end + ", dest: " + dest.toString() + ", dstart:" + destStart + ", dend: " + destEnd);
                                if (end > start) {
                                    String destTxt = dest.toString();
                                    String resultingTxt = destTxt.substring(0, destStart) + source.subSequence(start, end) + destTxt.substring(destEnd);
                                    if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                                        Log.d("filter", "!match");
                                        resultingTxt = destTxt.substring(0, destStart) + "." + destTxt.substring(destEnd);
                                        if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                                            if (destTxt.equals("")) {
                                                return null;
                                            }
                                            return "";
                                        } else {
                                            return "." + source.subSequence(start, end);
                                        }
                                    } else {
                                        String[] splits = resultingTxt.split("\\.");
                                        for (String split : splits) {
                                            if (Integer.valueOf(split) > 255) {
                                                Log.d("filter", "bigger > 255");
                                                if (resultingTxt.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                                                    return "";
                                                }
                                                return ".";
                                            }
                                        }
                                    }
                                }
                                return null;
                            }
                        }
                }
        );

        super.setOnPreferenceChangeListener(this);
    }

//    @Override
//    public void onClick(DialogInterface dialog, int which) {
//        if (which == DialogInterface.BUTTON_POSITIVE) {
//            if (!getEditText().getText().toString().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
//                this.showDialog(this.getExtras());
//                return;
//            }
//        }
//        super.onClick(dialog, which);
//    }

    private OnPreferenceChangeListener onPreferenceChangeListener;

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.onPreferenceChangeListener = onPreferenceChangeListener;
        super.setOnPreferenceChangeListener(this);
    }

    private String getIpAddress(String newValue) {
        if (newValue != null && newValue.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            String[] splits = newValue.split("\\.");
            String resultingTxt = "";
            for (String split : splits) {
                resultingTxt += Integer.valueOf(split) + ".";
            }
            newValue = resultingTxt.substring(0, resultingTxt.length() - 1);
        }
        return newValue;
    }

    @Override
    protected boolean persistString(String newValue) {
        return super.persistString(getIpAddress(newValue));
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return getIpAddress(super.getPersistedString(defaultReturnValue));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue.toString().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            String newValueStr = getIpAddress(newValue.toString());
            return this.onPreferenceChangeListener.onPreferenceChange(preference, newValueStr);
        }
        Toast.makeText(this.getContext(), getContext().getResources().getString(R.string.wrong_ip_address), Toast.LENGTH_SHORT).show();
        return false;
    }
}
