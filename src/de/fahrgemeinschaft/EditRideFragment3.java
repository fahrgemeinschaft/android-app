/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Ride;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.util.EditTextImageButton.TextListener;
import de.fahrgemeinschaft.util.EditTextPrivacyButton;
import de.fahrgemeinschaft.util.EditTextPrivacyButton.PrivacyListener;

public class EditRideFragment3 extends SherlockFragment
                implements TextListener, PrivacyListener {


    private static final String EMAIL = "EMail";
    private static final String PLATE = "NumberPlate";
    private static final String MOBILE = "Mobile";
    private static final String LANDLINE = "Landline";
    private EditTextPrivacyButton email;
    private EditTextPrivacyButton land;
    private EditTextPrivacyButton mobile;
    private EditTextPrivacyButton plate;
    private EditTextPrivacyButton name;
    private SharedPreferences prefs;



    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return lI.inflate(R.layout.fragment_ride_edit3, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        email = (EditTextPrivacyButton) v.findViewById(R.id.email);
        land = (EditTextPrivacyButton) v.findViewById(R.id.landline);
        mobile = (EditTextPrivacyButton) v.findViewById(R.id.mobile);
        plate = (EditTextPrivacyButton) v.findViewById(R.id.plate);
        name = (EditTextPrivacyButton) v.findViewById(R.id.name);
        email.setTextListener(EMAIL, this);
        mobile.setTextListener(MOBILE, this);
        land.setTextListener(LANDLINE, this);
        plate.setTextListener(PLATE, this);
        email.setPrivacyListener(EMAIL, this);
        mobile.setPrivacyListener(MOBILE, this);
        land.setPrivacyListener(LANDLINE, this);
        plate.setPrivacyListener(PLATE, this);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        setRide(((EditRideActivity)getActivity()).ride);
        super.onViewStateRestored(savedInstanceState);
    }

    public void setRide(Ride ride) {
        try {
            JSONObject d = ride.getDetails();
            if (!d.isNull(EMAIL)) email.text.setText(d.getString(EMAIL));
            else email.text.setText(prefs.getString(EMAIL, ""));
            if (!d.isNull(LANDLINE)) land.text.setText(d.getString(LANDLINE));
            else land.text.setText(prefs.getString(LANDLINE, ""));
            if (!d.isNull(MOBILE)) mobile.text.setText(d.getString(MOBILE));
            else mobile.text.setText(prefs.getString(MOBILE, ""));
            if (!d.isNull(PLATE)) plate.text.setText(d.getString(PLATE));
            else plate.text.setText(prefs.getString(PLATE, ""));

            if (d.isNull("Privacy")) d.put("Privacy", new JSONObject());
            JSONObject p = d.getJSONObject("Privacy");
            if (!p.isNull(EMAIL)) email.setPrivacy(p.getInt(EMAIL));
            else email.setPrivacy(EditTextPrivacyButton.ANYONE);
            if (!p.isNull(LANDLINE)) land.setPrivacy(p.getInt(LANDLINE));
            else land.setPrivacy(EditTextPrivacyButton.ANYONE);
            if (!p.isNull(MOBILE)) mobile.setPrivacy(p.getInt(MOBILE));
            else mobile.setPrivacy(EditTextPrivacyButton.ANYONE);
            if (!p.isNull(PLATE)) plate.setPrivacy(p.getInt(PLATE));
            else plate.setPrivacy(EditTextPrivacyButton.ANYONE);
        } catch (JSONException e) {
            e.printStackTrace();
            //TODO what?
        }
    }

    @Override
    public void onTextChange(String key, String text) {
        Ride ride = ((EditRideActivity)getActivity()).ride;
        try {
            ride.getDetails().put(key, text);
            if (prefs.getString(key, null) == null)
                prefs.edit().putString(key, text).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrivacyChange(String key, int visibility) {
        try {
            ((EditRideActivity)getActivity()).ride.getDetails()
                .getJSONObject("Privacy").put(key, visibility);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
