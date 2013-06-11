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
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;

public class EditRideFragment1 extends SherlockFragment {

    private static final String TAG = "Fahrgemeinschaft";

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit1, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        Button from = (Button) v.findViewById(R.id.from).findViewById(R.id.text);
        from.setText(R.string.from);
        Button to = (Button) v.findViewById(R.id.to).findViewById(R.id.text);
        to.setText(R.string.to);
    }

}
