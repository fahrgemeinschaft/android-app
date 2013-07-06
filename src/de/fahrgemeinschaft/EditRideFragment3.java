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

public class EditRideFragment3 extends SherlockFragment {


    private EditContactButton email;
    private EditContactButton land;
    private EditContactButton mobile;
    private EditContactButton plate;
    private EditContactButton name;



    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit3, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        email = (EditContactButton) v.findViewById(R.id.email);
        land = (EditContactButton) v.findViewById(R.id.landline);
        mobile = (EditContactButton) v.findViewById(R.id.mobile);
        plate = (EditContactButton) v.findViewById(R.id.plate);
        name = (EditContactButton) v.findViewById(R.id.name);
    }

}
