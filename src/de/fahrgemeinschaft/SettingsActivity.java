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
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity
        implements OnSharedPreferenceChangeListener {

    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        addPreferencesFromResource(R.xml.settings);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    void setSummaries() {
        ListPreference list = (ListPreference) findPreference("refresh");
        list.setSummary(getResources().getString(
                R.string.refresh_description, list.getEntry()));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("radius_from") || key.equals("radius_to")) {
            getContentResolver().delete(Uri.parse(
                    "content://de.fahrgemeinschaft/rides"), null, null);
            startService(new Intent(this, ConnectorService.class)
                    .setAction(ConnectorService.SEARCH));
            prefs.edit()
                    .putLong("cleanup", System.currentTimeMillis()).commit();
        }
    }
}
