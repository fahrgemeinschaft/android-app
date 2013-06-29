/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity {

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        final ListPreference list = (ListPreference) findPreference("refresh");
        list.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    
                    @Override
                    public boolean onPreferenceChange(Preference p, Object o) {
                        p.setSummary(getResources().getString(
                                R.string.refresh_description, list.getEntries()
                                        [list.findIndexOfValue((String) o)]));
                        return true;
                    }
                });
        list.getOnPreferenceChangeListener()
        .onPreferenceChange(list, list.getValue());
    }
}
