package com.google.vr.vrcore;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppCompatPreferenceFragment extends PreferenceFragment {

    private int resId;
    private Preference.OnPreferenceChangeListener onPreferenceChangeListener;
    private Context context;
    @Override
    public Context getContext() {
        return context;
    }

    public void Init(Context context, Preference.OnPreferenceChangeListener onPreferenceChangeListener) {
        this.context = context;
        this.onPreferenceChangeListener = onPreferenceChangeListener;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        // Trigger the listener immediately with the preference's
        // current value.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        if (preference instanceof SwitchPreference) {
            onPreferenceChangeListener.onPreferenceChange(preference, sp.getBoolean(preference.getKey(), false));
        } else {
            onPreferenceChangeListener.onPreferenceChange(preference, sp.getString(preference.getKey(), ""));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getFragmentManager

        String settings = getArguments().getString("settings");
        resId = getResources().getIdentifier(settings, "xml", this.getContext().getPackageName());

        if (resId == 0) {
            Toast.makeText(this.getContext(), settings + " " + resId, Toast.LENGTH_SHORT).show();
        }
        addPreferencesFromResource(resId);
        setHasOptionsMenu(true);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        //bindPreferenceSummaryToValue(findPreference("example_list"));
        Set<String> keys = this.getPreferenceManager().getSharedPreferences().getAll().keySet();
        for (String prefName : keys) {
            Preference pref = findPreference(prefName);
            if (pref != null) {
                bindPreferenceSummaryToValue(pref);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
