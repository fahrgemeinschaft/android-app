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

import de.fahrgemeinschaft.ContactProvider.CONTACT;
import de.fahrgemeinschaft.util.EditTextImageButton.TextListener;
import de.fahrgemeinschaft.util.PrivacyImageButton;
import de.fahrgemeinschaft.util.PrivacyImageButton.PrivacyListener;

public class EditRideFragment3 extends SherlockFragment
                implements TextListener, PrivacyListener {

    private static final String NAME = "Name";
    private static final String EMPTY = "";
    private PrivacyImageButton mobile;
    private PrivacyImageButton plate;
    private PrivacyImageButton email;
    private PrivacyImageButton land;
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
        email.setTextListener(CONTACT.EMAIL, this);
        String uri = "content://de.fahrgemeinschaft.private/users/"
                        + prefs.getString("user", EMPTY);
        email.text.setAutocompleteUri(Uri.parse(uri + "/mails"));
        mobile.text.setAutocompleteUri(Uri.parse(uri + "/mobiles"));
        land.text.setAutocompleteUri(Uri.parse(uri + "/landlines"));
        plate.text.setAutocompleteUri(Uri.parse(uri + "/plates"));
        mobile.setTextListener(CONTACT.MOBILE, this);
        land.setTextListener(CONTACT.LANDLINE, this);
        plate.setTextListener(CONTACT.PLATE, this);
        email.setPrivacyListener(CONTACT.EMAIL, this);
        mobile.setPrivacyListener(CONTACT.MOBILE, this);
        land.setPrivacyListener(CONTACT.LANDLINE, this);
        plate.setPrivacyListener(CONTACT.PLATE, this);
        name.setPrivacyListener(NAME, this);
        name.text.setKeyListener(null);
    }

    public void setRide(Ride ride) {
        try {
            JSONObject d = ride.getDetails();
            if (!d.isNull(CONTACT.EMAIL))
                email.text.setText(d.getString(CONTACT.EMAIL));
            else {
                email.text.setText(
                    prefs.getString(CONTACT.EMAIL,
                    prefs.getString(ProfileFragment.LOGIN, EMPTY)));
            }
            if (!d.isNull(CONTACT.LANDLINE))
                land.text.setText(d.getString(CONTACT.LANDLINE));
            else {
                land.text.setText(prefs.getString(CONTACT.LANDLINE, EMPTY));
            }
            if (!d.isNull(CONTACT.MOBILE))
                mobile.text.setText(d.getString(CONTACT.MOBILE));
            else {
                mobile.text.setText(prefs.getString(CONTACT.MOBILE, EMPTY));
            }
            if (ride.getMode().equals(Ride.Mode.TRAIN)) {
                plate.setVisibility(View.GONE);
            } else {
                plate.setVisibility(View.VISIBLE);
                if (!d.isNull(CONTACT.PLATE)) {
                    plate.text.setText(d.getString(CONTACT.PLATE));
                } else {
                    plate.text.setText(prefs.getString(CONTACT.PLATE, EMPTY));
                }
            }
            name.text.setText(prefs.getString(ProfileFragment.LASTNAME, EMPTY));
            if (d.isNull(FahrgemeinschaftConnector.PRIVACY))
                d.put(FahrgemeinschaftConnector.PRIVACY, new JSONObject());
            JSONObject p = d.getJSONObject(FahrgemeinschaftConnector.PRIVACY);
            if (!p.isNull(CONTACT.EMAIL))
                email.setPrivacy(p.getInt(CONTACT.EMAIL)); // 'm'
            else setPublic(email, p, CONTACT.EMAIL);
            if (!p.isNull(CONTACT.LANDLINE))
                land.setPrivacy(p.getInt(CONTACT.LANDLINE));
            else setPublic(land, p, CONTACT.LANDLINE);
            if (!p.isNull(CONTACT.MOBILE))
                mobile.setPrivacy(p.getInt(CONTACT.MOBILE));
            else setPublic(mobile, p, CONTACT.MOBILE);
            if (!p.isNull(CONTACT.PLATE))
                plate.setPrivacy(p.getInt(CONTACT.PLATE));
            else setPublic(plate, p, CONTACT.PLATE);
            if (!p.isNull(NAME)) name.setPrivacy(p.getInt(NAME));
            else setPublic(name, p, NAME);
        } catch (JSONException e) {
            e.printStackTrace();
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
                .getJSONObject(FahrgemeinschaftConnector.PRIVACY)
                        .put(key, privacy);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
