package com.example.android.xmppclienttest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(getString(R.string.shared_preference_file));
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
        String remoteHostKey = getString(R.string.remote_host_address_key);
        String defaultAddress = getString(R.string.settings_server_ip_addr_default);

        SharedPreferences savedPrefs = getPreferenceManager().getSharedPreferences();
        EditTextPreference server_address_pref =
                (EditTextPreference) findPreference(remoteHostKey);
        server_address_pref.setText(savedPrefs.getString(remoteHostKey, defaultAddress));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
    }
}
