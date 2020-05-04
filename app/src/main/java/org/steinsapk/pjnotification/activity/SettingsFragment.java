package org.steinsapk.pjnotification.activity;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import org.steinsapk.pjnotification.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }
}
