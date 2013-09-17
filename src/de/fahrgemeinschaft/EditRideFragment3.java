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
import de.fahrgemeinschaft.util.PrivacyImageButton;
import de.fahrgemeinschaft.util.PrivacyImageButton.PrivacyListener;

public class EditRideFragment3 extends SherlockFragment
                implements TextListener, PrivacyListener {

    private static final String EMAIL = "Email";
    private static final String PLATE = "NumberPlate";
    private static final String MOBILE = "Mobile";
    private static final String LANDLINE = "Landline";
    private static final String NAME = "Name";
    private PrivacyImageButton email;
    private PrivacyImageButton land;
    private PrivacyImageButton mobile;
    private PrivacyImageButton plate;
    private PrivacyImageButton name;
    private SharedPreferences prefs;



    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return lI.inflate(R.layout.fragment_ride_edit3, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        email = (PrivacyImageButton) v.findViewById(R.id.email);
        land = (PrivacyImageButton) v.findViewById(R.id.landline);
        mobile = (PrivacyImageButton) v.findViewById(R.id.mobile);
        plate = (PrivacyImageButton) v.findViewById(R.id.plate);
        name = (PrivacyImageButton) v.findViewById(R.id.name);
        email.setTextListener(EMAIL, this);
        mobile.setTextListener(MOBILE, this);
        land.setTextListener(LANDLINE, this);
        plate.setTextListener(PLATE, this);
        email.setPrivacyListener(EMAIL, this);
        mobile.setPrivacyListener(MOBILE, this);
        land.setPrivacyListener(LANDLINE, this);
        plate.setPrivacyListener(PLATE, this);
        name.setPrivacyListener(NAME, this);
        name.text.setKeyListener(null);
    }

    public void setRide(Ride ride) {
        try {
            JSONObject d = ride.getDetails();
            if (!d.isNull(EMAIL)) email.text.setText(d.getString(EMAIL));
            else email.text.setText(
                    prefs.getString(EMAIL,
                    prefs.getString("login", "")));
            if (!d.isNull(LANDLINE)) land.text.setText(d.getString(LANDLINE));
            else land.text.setText(prefs.getString(LANDLINE, ""));
            if (!d.isNull(MOBILE)) mobile.text.setText(d.getString(MOBILE));
            else mobile.text.setText(prefs.getString(MOBILE, ""));
            if (ride.getMode().equals(Ride.Mode.TRAIN)) {
                plate.setVisibility(View.GONE);
            } else {
                plate.setVisibility(View.VISIBLE);
                if (!d.isNull(PLATE)) {
                    plate.text.setText(d.getString(PLATE));
                } else plate.text.setText(prefs.getString(PLATE, ""));
            }
            name.text.setText(prefs.getString("lastname", "n/a"));
            if (d.isNull("Privacy")) d.put("Privacy", new JSONObject());
            JSONObject p = d.getJSONObject("Privacy");
            if (!p.isNull("Email")) email.setPrivacy(p.getInt("Email")); // 'm'
            else setPublic(email, p, "Email");
            if (!p.isNull(LANDLINE)) land.setPrivacy(p.getInt(LANDLINE));
            else setPublic(land, p, LANDLINE);
            if (!p.isNull(MOBILE)) mobile.setPrivacy(p.getInt(MOBILE));
            else setPublic(mobile, p, MOBILE);
            if (!p.isNull(PLATE)) plate.setPrivacy(p.getInt(PLATE));
            else setPublic(plate, p, PLATE);
            if (!p.isNull(NAME)) name.setPrivacy(p.getInt(NAME));
            else setPublic(name, p, NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            //TODO what?
        }
    }

    private void setPublic(PrivacyImageButton btn, JSONObject p, String key)
            throws JSONException {
        btn.setPrivacy(1);
        p.put(key, 1);
    }

    @Override
    public void onTextChange(String key, String text) {
        Ride ride = ((EditRideActivity)getActivity()).ride;
        try {
            ride.getDetails().put(key, text);
            prefs.edit().putString(key, text).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrivacyChange(String key, int privacy) {
        try {
            ((EditRideActivity)getActivity()).ride.getDetails()
                .getJSONObject("Privacy").put(key, privacy);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
