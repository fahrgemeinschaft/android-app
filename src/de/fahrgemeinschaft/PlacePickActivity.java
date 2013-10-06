/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.Ride;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import de.fahrgemeinschaft.PlaceListFragment.PlacePickListener;

public class PlacePickActivity extends FragmentActivity implements
        PlacePickListener {

    public static final String SHOW_TEXTFIELD = "show_textfield";
    private PlaceListFragment place_list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_place_pick);

        place_list = (PlaceListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_list);

        if (getIntent().getBooleanExtra(SHOW_TEXTFIELD, false)) {
            place_list.toggleSearchField();
        }
    }

    @Override
    public void onPlacePicked(Uri uri) {
        setResult(RESULT_OK, new Intent("", uri));
        if (getIntent().getData().getQueryParameter(Ride.FROM_ID) == null)
            overridePendingTransition(R.anim.do_nix, R.anim.slide_out_left);
        else
            overridePendingTransition(R.anim.do_nix, R.anim.slide_out_right);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        if (getIntent().getData().getQueryParameter(Ride.FROM_ID) == null)
            overridePendingTransition(R.anim.do_nix, R.anim.slide_out_left);
        else
            overridePendingTransition(R.anim.do_nix, R.anim.slide_out_right);
        finish();
    }
}