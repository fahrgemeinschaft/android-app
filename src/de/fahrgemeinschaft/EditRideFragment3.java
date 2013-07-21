/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import org.json.JSONException;
import org.teleportr.Ride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.util.EditTextVisibilityButton;

public class EditRideFragment3 extends SherlockFragment
        implements OnFocusChangeListener {


    private static final String MAIL = "mail";
    private static final String PLATE = "plate";
    private static final String MOBILE = "mobile";
    private static final String LANDLINE = "landline";
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

        email.text.setOnFocusChangeListener(this);
        land.text.setOnFocusChangeListener(this);
        mobile.text.setOnFocusChangeListener(this);
        plate.text.setOnFocusChangeListener(this);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        try {
            Ride ride = ((EditRideActivity)getActivity()).ride;
            email.text.setText(ride.getDetails().getString(MAIL));
            email.text.setText(ride.getDetails().getString(MAIL));
            land.text.setText(ride.getDetails().getString(LANDLINE));
            mobile.text.setText(ride.getDetails().getString(MOBILE));
            plate.text.setText(ride.getDetails().getString(PLATE));
        } catch (JSONException e) {
            //TODO what?
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            try {
                Ride ride = ((EditRideActivity)getActivity()).ride;
                if (v.getParent().equals(email)) {
                    ride.getDetails().put(MAIL,
                            ((EditText) v).getText().toString());
                } else if (v.getParent().equals(land)) {
                    ride.getDetails().put(LANDLINE,
                            ((EditText) v).getText().toString());
                } else if (v.getParent().equals(mobile)) {
                    ride.getDetails().put(MOBILE,
                            ((EditText) v).getText().toString());
                } else if (v.getParent().equals(plate)) {
                    ride.getDetails().put(PLATE,
                            ((EditText) v).getText().toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
