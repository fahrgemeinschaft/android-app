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
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity
        implements OnSharedPreferenceChangeListener {

    private SharedPreferences prefs;
    private boolean radius_changed;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        getSupportActionBar().setHomeButtonEnabled(true);
        addPreferencesFromResource(R.xml.settings);
        prefs.registerOnSharedPreferenceChangeListener(this);
        setTitle(R.string.settings);
    }

    @SuppressWarnings("deprecation")
    void setSummaries() {
        ListPreference list = (ListPreference) findPreference("refresh");
        list.setSummary(getResources().getString(
                R.string.refresh_description, list.getEntry()));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("radius_from") || key.equals("radius_to")) {
            radius_changed = true;
        }
    }

    @Override
    protected void onPause() {
        if (radius_changed) {
            Log.d(MainActivity.TAG, "clear cache");
            getContentResolver().delete(Uri.parse(
                    "content://de.fahrgemeinschaft/rides"), null, null);
            startService(new Intent(this, ConnectorService.class)
                    .setAction(ConnectorService.SEARCH));
            prefs.edit().putLong("cleanup",
                    System.currentTimeMillis()).commit();
        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
