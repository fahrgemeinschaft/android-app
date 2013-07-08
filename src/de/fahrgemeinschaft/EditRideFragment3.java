/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.util.EditTextVisibilityButton;

public class EditRideFragment3 extends SherlockFragment {


    private EditTextVisibilityButton email;
    private EditTextVisibilityButton land;
    private EditTextVisibilityButton mobile;
    private EditTextVisibilityButton plate;
    private EditTextVisibilityButton name;



    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit3, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        email = (EditTextVisibilityButton) v.findViewById(R.id.email);
        land = (EditTextVisibilityButton) v.findViewById(R.id.landline);
        mobile = (EditTextVisibilityButton) v.findViewById(R.id.mobile);
        plate = (EditTextVisibilityButton) v.findViewById(R.id.plate);
        name = (EditTextVisibilityButton) v.findViewById(R.id.name);
    }

}
