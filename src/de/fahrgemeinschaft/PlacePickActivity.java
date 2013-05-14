/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import de.fahrgemeinschaft.PlaceListFragment.PlacePickListener;

public class PlacePickActivity extends FragmentActivity implements
        PlacePickListener {

    private PlaceListFragment place_list;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.dimAmount = 0.7f;
//        getWindow().setAttributes(params);

        setContentView(R.layout.activity_place_pick);

        place_list = (PlaceListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_list);

        if (getIntent().getBooleanExtra("show_textfield", false)) {
            place_list.toggleSearchField();
        }
    }

    @Override
    public void onPlacePicked(Uri uri) {

        setResult(RESULT_OK, new Intent("", uri));
        finish();
        if (getIntent().getData().getQueryParameter("from_id") == null)
            overridePendingTransition(R.anim.do_nix, R.anim.slide_out_left);
        else
            overridePendingTransition(R.anim.do_nix, R.anim.slide_out_right);

        // .parse("https://maps.googleapis.com/maps/api/place"
        // + "/details/json?sensor=true&reference="
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        if (getIntent().getData().getQueryParameter("from_id") == null)
            overridePendingTransition(R.anim.do_nix, R.anim.slide_out_left);
        else
            overridePendingTransition(R.anim.do_nix, R.anim.slide_out_right);
        super.onBackPressed();
    }
}
