/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.util.List;

import org.teleportr.Place;
import org.teleportr.Ride;
import org.teleportr.Ride.Mode;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.util.PlaceImageButton;

public class EditRideFragment1 extends SherlockFragment implements OnClickListener {

    private static final String TAG = "Fahrgemeinschaft";
    private Ride ride;
    private LinearLayout seats;
    private LinearLayout route;
    private PlaceImageButton from;
    private PlaceImageButton to;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit1, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        
        v.findViewById(R.id.mode_car).setOnClickListener(selectMode);
        v.findViewById(R.id.mode_rail).setOnClickListener(selectMode);

        seats = (LinearLayout) v.findViewById(R.id.seats);
        v.findViewById(R.id.seats_zero).setOnClickListener(this);
        v.findViewById(R.id.seats_one).setOnClickListener(this);
        v.findViewById(R.id.seats_two).setOnClickListener(this);
        v.findViewById(R.id.seats_three).setOnClickListener(this);
        v.findViewById(R.id.seats_many).setOnClickListener(this);

        route = (LinearLayout) v.findViewById(R.id.route);
        from = (PlaceImageButton) v.findViewById(R.id.from);
        from.name.setOnClickListener(autocompletePlace);
        from.icon.setOnClickListener(pickPlace);
        to = (PlaceImageButton) v.findViewById(R.id.to);
        to.name.setOnClickListener(autocompletePlace);
        to.icon.setOnClickListener(pickPlace);
    }

    public void setRide(Ride ride) {
        this.ride = ride;
        if (ride.getFrom() != null)
            from.setPlace(ride.getFrom());
        if (ride.getTo() != null)
            to.setPlace(ride.getTo());
        setVias(ride.getVias());
        setMode(ride.getMode());
        setSeats(ride.getSeats());
    }

    private void setMode(Mode mode) {
        ride.mode(mode);
        switch(mode) {
        case CAR:
            getActivity().findViewById(R.id.mode_car).setSelected(true);
            getActivity().findViewById(R.id.mode_rail).setSelected(false);
            ((TextView) getActivity().findViewById(R.id.mode_car_text))
                .setTextColor(getResources().getColor(R.color.dark_green));
            ((TextView) getActivity().findViewById(R.id.mode_rail_text))
                .setTextColor(getResources().getColor(R.color.white));
            break;
        case TRAIN:
            getActivity().findViewById(R.id.mode_car).setSelected(false);
            getActivity().findViewById(R.id.mode_rail).setSelected(true);
            ((TextView) getActivity().findViewById(R.id.mode_car_text))
                .setTextColor(getResources().getColor(R.color.white));
            ((TextView) getActivity().findViewById(R.id.mode_rail_text))
                .setTextColor(getResources().getColor(R.color.dark_green));
            break;
        }
    }

    OnClickListener selectMode = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.mode_car:
                setMode(Mode.CAR);
                break;
            case R.id.mode_rail:
                setMode(Mode.TRAIN);
                break;
            }
            ((EditRideActivity)getActivity()).f3.setRide(ride);
        }
    };

    private void setVias(List<Place> vias) {
        route.removeViews(1, route.getChildCount() - 2);
        ride.removeVias();
        for (int i = 0; i < vias.size(); i++) {
            ride.via(vias.get(i).id);
            ImageButton icn = addViaBtn(vias.get(i)).icon;
            icn.setImageResource(R.drawable.icn_close);
            icn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    List<Place> old_vias = ride.getVias();
                    old_vias.remove(route.indexOfChild((View) v.getParent())-1);
                    setVias(old_vias);
                }
            });
        }
        addViaBtn(null);
    }

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
            Place place = new Place(intent.getData(), getActivity());
            if (i == 0) {
                Log.d(TAG, "from " + intent.getDataString());
                from.setPlace(place);
                ride.from(place);
            } else if (i == route.getChildCount() - 1 ) {
                Log.d(TAG, "to " + intent.getDataString());
                to.setPlace(place);
                ride.to(place);
            } else {
                Log.d(TAG, "via " + intent.getDataString());
                List<Place> vias = ride.getVias();
                
                if (i == route.getChildCount() - 2) { // new via?
                    vias.add(place);
                } else {
                    vias.set(i + 1, place);
                }
                setVias(vias);
            }
            route.requestFocus();
        }
    }

    private PlaceImageButton addViaBtn(Place place) {
        PlaceImageButton b = new PlaceImageButton(getActivity());
        b.name.setText(getString(R.string.via));
        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.medium);
        lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.xlarge);
        b.setLayoutParams(lp);
        b.icon.setImageResource(R.drawable.icn_dropdown);
        b.name.setOnClickListener(autocompletePlace);
        b.icon.setOnClickListener(pickPlace);
        route.addView(b, route.getChildCount() - 1);
        if (place != null) {
            b.setPlace(place);
        }
        return b;
    }


    private void setSeats(int s) {
        for (int i = 0; i < seats.getChildCount(); i++) {
            seats.getChildAt(i).setSelected(false);
        }
        if (s >= 0 && s <= 4 )
            seats.getChildAt(s).setSelected(true);
        ride.seats(s);
    }

    @Override
    public void onClick(View v) {
        setSeats(seats.indexOfChild(v));
    }

}
