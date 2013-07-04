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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class EditRideFragment1 extends SherlockFragment implements OnClickListener {

    private static final String TAG = "Fahrgemeinschaft";
    private LinearLayout seats;
    private LinearLayout route;
    private Button from;
    private Button to;

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
        
        from = (Button) v.findViewById(R.id.from).findViewById(R.id.text);
        from.setOnClickListener(autocompletePlace);
        v.findViewById(R.id.from).findViewById(R.id.icon)
            .setOnClickListener(pickPlace);
        
        to = (Button) v.findViewById(R.id.to).findViewById(R.id.text);
        to.setOnClickListener(autocompletePlace);
        v.findViewById(R.id.to).findViewById(R.id.icon)
            .setOnClickListener(pickPlace);
    }

    public void setRide(Ride ride) {
        this.ride = ride;
        Log.d(TAG, ride.getFromId()+ "");
        from.setText(ride.getFrom().getName());
        to.setText(ride.getTo().getName());
        setVias(ride.getVias());
        setMode(ride.getMode());
        if (getActivity().getIntent().hasExtra("count_down_seats")) {
            Toast.makeText(getActivity(), "Sitzplätze runter gezählt auf "
                    + (ride.getSeats() - 1) , Toast.LENGTH_SHORT).show();
            setSeats(ride.getSeats() - 1);
            ride.store(getActivity());
        } else {
            setSeats(ride.getSeats());
        }
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
        }
    };

    private void setVias(List<Place> vias) {
        route.removeViews(1, route.getChildCount() - 2);
        ride.removeVias();
        for (int i = 0; i < vias.size(); i++) {
            ride.via(vias.get(i).id);
            addVia(vias.get(i));
            ImageButton icon = (ImageButton)
                    route.getChildAt(i + 1)
                    .findViewById(R.id.icon);
            icon.setImageResource(R.drawable.icn_close);
            icon.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    List<Place> old_vias = ride.getVias();
                    old_vias.remove(route.indexOfChild((View) v.getParent()) - 1);
                    setVias(old_vias);
                }
            });
        }
        addVia(null);
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
	private Ride ride;

    @Override
    public void onActivityResult(final int i, int res, final Intent intent) {
        if (res == Activity.RESULT_OK) {
            Log.d(TAG, i + "selected " + intent.getData());
            Cursor place = getActivity().getContentResolver()
                    .query(intent.getData(), null, null, null, null);
            place.moveToFirst();
            
            if (i == 0) {
                Log.d(TAG, "from " + intent.getDataString());
                from.setText(place.getString(2));
                ride.from(place.getInt(0));
            } else if (i == route.getChildCount() - 1 ) {
                Log.d(TAG, "to " + intent.getDataString());
                to.setText(place.getString(2));
                ride.to(place.getInt(0));
            } else {
                Log.d(TAG, "via " + intent.getDataString());
                List<Place> vias = ride.getVias();
                
                if (i == route.getChildCount() - 2) { // new via?
                    vias.add(new Place(place.getInt(0)).name(place.getString(2)));
                } else {
                    vias.get(i + 1).name(place.getString(2)).id = place.getInt(0);
                }
                setVias(vias);
            }
        }
    }

    private void addVia(Place place) {
        View btn = getLayoutInflater(null)
                .inflate(R.layout.place_pick_button, null, false);
        ((TextView) btn.findViewById(R.id.text))
                .setText(getString(R.string.via));
        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.topMargin = getResources().getDimensionPixelSize(R.dimen.small);
        lp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.small);
        lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.xlarge);
        btn.setLayoutParams(lp);
        btn.findViewById(R.id.text).setOnClickListener(autocompletePlace);
        btn.findViewById(R.id.icon).setOnClickListener(pickPlace);
        route.addView(btn, route.getChildCount() - 1);
        if (place != null) {
            ((Button) btn.findViewById(R.id.text)).setText(place.getName());
        }
    }


    private void setSeats(int s) {
        for (int i = 0; i < seats.getChildCount(); i++) {
            seats.getChildAt(i).setSelected(false);
        }
        seats.getChildAt(s).setSelected(true);
        ride.seats(s);
    }

    @Override
    public void onClick(View v) {
        setSeats(seats.indexOfChild(v));
    }

}
