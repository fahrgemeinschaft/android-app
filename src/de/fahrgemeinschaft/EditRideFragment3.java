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
import android.net.Uri;
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
        email.setTextListener(EditRideActivity.EMAIL, this);
        String uri = "content://de.fahrgemeinschaft.private/users/"
                        + prefs.getString("user", "");
        email.setAutocompleteUri(Uri.parse(uri + "/mails"));
        mobile.setTextListener(EditRideActivity.MOBILE, this);
        land.setTextListener(EditRideActivity.LANDLINE, this);
        plate.setTextListener(EditRideActivity.PLATE, this);
        email.setPrivacyListener(EditRideActivity.EMAIL, this);
        mobile.setPrivacyListener(EditRideActivity.MOBILE, this);
        land.setPrivacyListener(EditRideActivity.LANDLINE, this);
        plate.setPrivacyListener(EditRideActivity.PLATE, this);
        name.setPrivacyListener(NAME, this);
        name.text.setKeyListener(null);
    }

    public void setRide(Ride ride) {
        try {
            JSONObject d = ride.getDetails();
            if (!d.isNull(EditRideActivity.EMAIL))
                email.text.setText(d.getString(EditRideActivity.EMAIL));
            else email.text.setText(
                    prefs.getString(EditRideActivity.EMAIL,
                    prefs.getString("login", "")));
            if (!d.isNull(EditRideActivity.LANDLINE))
                land.text.setText(d.getString(EditRideActivity.LANDLINE));
            else land.text.setText(
                    prefs.getString(EditRideActivity.LANDLINE, ""));
            if (!d.isNull(EditRideActivity.MOBILE))
                mobile.text.setText(d.getString(EditRideActivity.MOBILE));
            else mobile.text.setText(
                    prefs.getString(EditRideActivity.MOBILE, ""));
            if (ride.getMode().equals(Ride.Mode.TRAIN)) {
                plate.setVisibility(View.GONE);
            } else {
                plate.setVisibility(View.VISIBLE);
                if (!d.isNull(EditRideActivity.PLATE)) {
                    plate.text.setText(d.getString(EditRideActivity.PLATE));
                } else plate.text.setText(
                        prefs.getString(EditRideActivity.PLATE, ""));
            }
            name.text.setText(prefs.getString("lastname", "n/a"));
            if (d.isNull("Privacy")) d.put("Privacy", new JSONObject());
            JSONObject p = d.getJSONObject("Privacy");
            if (!p.isNull("Email")) email.setPrivacy(p.getInt("Email")); // 'm'
            else setPublic(email, p, "Email");
            if (!p.isNull(EditRideActivity.LANDLINE))
                land.setPrivacy(p.getInt(EditRideActivity.LANDLINE));
            else setPublic(land, p, EditRideActivity.LANDLINE);
            if (!p.isNull(EditRideActivity.MOBILE))
                mobile.setPrivacy(p.getInt(EditRideActivity.MOBILE));
            else setPublic(mobile, p, EditRideActivity.MOBILE);
            if (!p.isNull(EditRideActivity.PLATE))
                plate.setPrivacy(p.getInt(EditRideActivity.PLATE));
            else setPublic(plate, p, EditRideActivity.PLATE);
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
