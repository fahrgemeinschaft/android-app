/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra("profile")) {
            profile();
        } else {
            settings();
        }
    }
    
    private void profile() {
        addPreferencesFromResource(R.xml.profile);
        
        EditTextPreference user = (EditTextPreference)
                findPreference("username");
        user.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    
                    @Override
                    public boolean onPreferenceChange(Preference p, Object o) {
                        p.setSummary(o.toString());
                        return true;
                    }
                });
        user.setSummary(user.getText());
        EditTextPreference pass = (EditTextPreference)
                findPreference("password");
        pass.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    
                    @Override
                    public boolean onPreferenceChange(Preference p, Object o) {
                        String word = "";
                        for (int i = 0; i < o.toString().length(); i++) {
                            word += "*";
                        }
                        p.setSummary(word);
                        return true;
                    }
                });
        pass.getOnPreferenceChangeListener()
                .onPreferenceChange(pass,
                        (pass.getText() != null)? pass.getText() : "***");
    }

    private void settings() {
        addPreferencesFromResource(R.xml.settings);
        findPreference("background_interval").setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    
                    @Override
                    public boolean onPreferenceChange(Preference p, Object o) {
                        p.setSummary(getResources().getString(
                                R.string.background_interval_description, o));
                        return true;
                    }
                });
    }

}
