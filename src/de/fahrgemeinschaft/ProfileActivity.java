/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileActivity extends SherlockPreferenceActivity
        implements OnSharedPreferenceChangeListener {

    private SharedPreferences prefs;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // in case the background service isn't running anyway
        startService(new Intent(this, ConnectorService.class));

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.profile);
        setSummaries();
    }

    private void setSummaries() {
        ((EditTextPreference) findPreference("username"))
            .setSummary(prefs.getString("username", ""));

        String word = "";
        String p = prefs.getString("password", "");
        for (int i = 0; i < p.toString().length(); i++) word += "*";
        ((EditTextPreference) findPreference("password")).setSummary(word);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Crouton.makeText(ProfileActivity.this, "saved "+key, Style.INFO).show();
        setSummaries();
    }
}
