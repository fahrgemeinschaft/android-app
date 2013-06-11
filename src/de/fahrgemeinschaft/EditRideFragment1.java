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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class EditRideFragment1 extends SherlockFragment implements OnClickListener {

    private static final String TAG = "Fahrgemeinschaft";
    private LinearLayout seats;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit1, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        
        seats = (LinearLayout) v.findViewById(R.id.seats);
        v.findViewById(R.id.seats_one).setOnClickListener(this);
        v.findViewById(R.id.seats_two).setOnClickListener(this);
        v.findViewById(R.id.seats_three).setOnClickListener(this);
        v.findViewById(R.id.seats_four).setOnClickListener(this);
        v.findViewById(R.id.seats_five).setOnClickListener(this);
        
        Button from = (Button) v.findViewById(R.id.from).findViewById(R.id.text);
        from.setText(R.string.from);
        Button to = (Button) v.findViewById(R.id.to).findViewById(R.id.text);
        to.setText(R.string.to);
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < seats.getChildCount(); i++) {
            seats.getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
    }

}
