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

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileActivity extends SherlockPreferenceActivity {

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile);

        final EditTextPreference user = (EditTextPreference)
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

        final EditTextPreference pass = (EditTextPreference)
                findPreference("password");
        pass.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    
                    @Override
                    public boolean onPreferenceChange(Preference p, Object o) {
                        String word = "";
                        Crouton.makeText(ProfileActivity.this, "logging in...",
                                Style.INFO).show();
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
}
