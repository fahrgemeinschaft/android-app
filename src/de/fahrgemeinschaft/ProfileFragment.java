/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileFragment extends SherlockFragment implements OnClickListener {

    private EditContactButton username;
    private EditContactButton password;
    private SharedPreferences prefs;


    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_profile, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        username = (EditContactButton) v.findViewById(R.id.username);
        password = (EditContactButton) v.findViewById(R.id.password);
        username.text.setText(prefs.getString("username", ""));
        password.text.setText(prefs.getString("password", ""));
        v.findViewById(R.id.login).setOnClickListener(this);
        super.onViewCreated(v, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        prefs.edit()
            .putString("username", username.text.getText().toString())
            .putString("password", password.text.getText().toString())
        .commit();
        Crouton.makeText(getActivity(), "stored", Style.INFO).show();
    }

}
