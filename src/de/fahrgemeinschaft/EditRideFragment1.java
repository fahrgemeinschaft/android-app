/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.actionbarsherlock.app.SherlockFragment;

public class EditRideFragment1 extends SherlockFragment implements OnClickListener {

    private static final String TAG = "Fahrgemeinschaft";
    private LinearLayout seats;
    private LinearLayout route;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit1, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        
        v.findViewById(R.id.mode_car).setOnClickListener(selectMode);
        v.findViewById(R.id.mode_rail).setOnClickListener(selectMode);
        route = (LinearLayout) v.findViewById(R.id.route);
        seats = (LinearLayout) v.findViewById(R.id.seats);
        v.findViewById(R.id.seats_zero).setOnClickListener(this);
        v.findViewById(R.id.seats_one).setOnClickListener(this);
        v.findViewById(R.id.seats_two).setOnClickListener(this);
        v.findViewById(R.id.seats_three).setOnClickListener(this);
        v.findViewById(R.id.seats_many).setOnClickListener(this);
        
        Button from = (Button) v.findViewById(R.id.from).findViewById(R.id.text);
        from.setText(R.string.from);
        from.setOnClickListener(autocompletePlace);
        v.findViewById(R.id.from).findViewById(R.id.icon)
            .setOnClickListener(pickPlace);
        
        Button to = (Button) v.findViewById(R.id.to).findViewById(R.id.text);
        to.setText(R.string.to);
        to.setOnClickListener(autocompletePlace);
        v.findViewById(R.id.to).findViewById(R.id.icon)
            .setOnClickListener(pickPlace);
        addVia();
    }

    OnClickListener selectMode = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.mode_car) {
                getActivity().findViewById(R.id.mode_car).setSelected(true);
                getActivity().findViewById(R.id.mode_rail).setSelected(false);
                ((TextView) getActivity().findViewById(R.id.mode_car_text))
                    .setTextColor(getResources().getColor(R.color.dark_green));
                ((TextView) getActivity().findViewById(R.id.mode_rail_text))
                    .setTextColor(getResources().getColor(R.color.white));
            } else {
                getActivity().findViewById(R.id.mode_car).setSelected(false);
                getActivity().findViewById(R.id.mode_rail).setSelected(true);
                ((TextView) getActivity().findViewById(R.id.mode_car_text))
                    .setTextColor(getResources().getColor(R.color.white));
                ((TextView) getActivity().findViewById(R.id.mode_rail_text))
                    .setTextColor(getResources().getColor(R.color.dark_green));
            }
        }
    };

    OnClickListener pickPlace = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    Uri.parse("content://de.fahrgemeinschaft/places")),
                    route.indexOfChild((View) v.getParent()));
        }
    };

    OnClickListener autocompletePlace = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    Uri.parse("content://de.fahrgemeinschaft/places"))
                            .putExtra("show_textfield", true),
                    route.indexOfChild((View) v.getParent()));
        }
    };

    @Override
    public void onActivityResult(final int i, int res, final Intent intent) {
        if (res == Activity.RESULT_OK) {
            Log.d(TAG, i + "selected " + intent.getData());
            Cursor place = getActivity().getContentResolver()
                    .query(intent.getData(), null, null, null, null);
            if (place.getCount() > 0) {
                place.moveToFirst();
                Button b = (Button) route.getChildAt(i).findViewById(R.id.text);
                if (i == route.getChildCount() - 2) {
                    addVia();
                }
                b.setText(place.getString(2));
            }
            if (i == 0) {
                Log.d(TAG, "from " + intent.getDataString());
            } else if (i == route.getChildCount() - 1 ) {
                Log.d(TAG, "to " + intent.getDataString());
            } else {
                Log.d(TAG, "via " + intent.getDataString());
                ImageButton icon = (ImageButton) route
                        .getChildAt(i).findViewById(R.id.icon);
                icon.setImageResource(R.drawable.icn_close);
                icon.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        route.removeViewAt(
                                route.indexOfChild(
                                        (View) v.getParent()));
                    }
                });
            }
        }
    }

    private void addVia() {
        View btn = getLayoutInflater(null)
                .inflate(R.layout.place_pick_button, null, false);
        ((TextView) btn.findViewById(R.id.text))
        .setText(getString(R.string.via));
        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.topMargin = getResources().getDimensionPixelSize(R.dimen.small);
        lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.small);
        lp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.small);
        lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.xlarge);
        btn.setLayoutParams(lp);
        btn.findViewById(R.id.text).setOnClickListener(autocompletePlace);
        btn.findViewById(R.id.icon).setOnClickListener(pickPlace);
        route.addView(btn, route.getChildCount() - 1);
    }


    @Override
    public void onClick(View v) {
        for (int i = 0; i < seats.getChildCount(); i++) {
            seats.getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
    }
}
