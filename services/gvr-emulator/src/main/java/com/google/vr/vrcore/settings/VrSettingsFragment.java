package com.google.vr.vrcore.settings;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import java.util.Set;

public class VrSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Bundle args = getArguments();
        if (args != null) {
            String settings = args.getString("settings");

            int resId = getResources().getIdentifier(settings, "xml", this.getActivity().getApplicationContext().getPackageName());

            if (resId == 0) {
                Toast.makeText(this.getContext(), settings + " " + resId, Toast.LENGTH_SHORT).show();
            }
            addPreferencesFromResource(resId);

            Set<String> keys = this.getPreferenceManager().getSharedPreferences().getAll().keySet();
            for (String prefName : keys) {
                Preference pref = findPreference(prefName);
                if (pref != null) {
                    bindPreferenceSummaryToValue(pref);
                }
            }
        }
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's current value.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        if (preference instanceof SwitchPreference) {
            this.onPreferenceChange(preference, sp.getBoolean(preference.getKey(), false));
        } else {
            this.onPreferenceChange(preference, sp.getString(preference.getKey(), ""));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            // For all other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }
}
