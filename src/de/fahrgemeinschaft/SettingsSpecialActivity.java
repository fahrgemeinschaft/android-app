/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import de.fahrgemeinschaft.util.Util;

public class SettingsSpecialActivity extends SherlockPreferenceActivity {

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        getSupportActionBar().setHomeButtonEnabled(true);
        addPreferencesFromResource(R.xml.settings_special);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findPreference("about").setIntent(Util.aboutIntent(this));
    }

}
