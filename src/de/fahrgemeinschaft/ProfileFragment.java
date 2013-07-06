/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class ProfileFragment extends SherlockFragment implements OnClickListener {

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_profile, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
//        v.findViewById(R.id.blablabla).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//        case R.id.blablabla:
//            break;
        }
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
        .putString("username", "Moritz").commit();
    }

}
