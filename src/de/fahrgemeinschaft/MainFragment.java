/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.util.Calendar;
import java.util.Date;

import org.teleportr.Ride;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.util.DateImageButton;
import de.fahrgemeinschaft.util.PlaceImageButton;

public class MainFragment extends SherlockFragment
        implements OnClickListener, OnDateSetListener {

    public static final Uri PLACES_URI
            = Uri.parse("content://de.fahrgemeinschaft/places");
    protected static final String TAG = "fahrgemeinschaft";
    private static final int FROM = 42;
    private static final int TO = 55;
    private DateImageButton when;
    private PlaceImageButton from;
    private PlaceImageButton to;
    public Ride ride;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_main, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        from = (PlaceImageButton) v.findViewById(R.id.btn_autocomplete_from);
        from.name.setOnClickListener(this);
        from.icon.setOnClickListener(this);
        to = (PlaceImageButton) v.findViewById(R.id.btn_autocomplete_to);
        to.name.setOnClickListener(this);
        to.icon.setOnClickListener(this);
        when = (DateImageButton) v.findViewById(R.id.btn_pick_date);
        when.btn.setOnClickListener(this);
        when.icon.setOnClickListener(this);
        v.findViewById(R.id.btn_mitfahren)
            .setOnClickListener((OnClickListener) getActivity());
        v.findViewById(R.id.btn_selberfahren)
            .setOnClickListener((OnClickListener) getActivity());

        if (savedInstanceState != null) {
            ride = savedInstanceState.getParcelable("ride");
            ride.setContext(getActivity());
            from.setPlace(ride.getFrom());
            to.setPlace(ride.getTo());
        } else {
            ride = new Ride(getActivity());
            ride.dep(getMorning(System.currentTimeMillis()));
        }
        when.setDate(ride.getDep());
    }

    @Override
    public void onClick(View btn) {
        if (btn == from.name) {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    PLACES_URI).putExtra("show_textfield", true), FROM);
            getActivity().overridePendingTransition(
                    R.anim.slide_in_left, R.anim.do_nix);
        } else if (btn == from.icon) {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    PLACES_URI), FROM);
            getActivity().overridePendingTransition(
                    R.anim.slide_in_left, R.anim.do_nix);
        } else if (btn == to.name) {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    PLACES_URI.buildUpon().appendQueryParameter("from_id",
                            String.valueOf(ride.getFromId())).build())
            .putExtra("show_textfield", true), TO);
            getActivity().overridePendingTransition(
                    R.anim.slide_in_right, R.anim.do_nix);
        } else if (btn == to.icon) {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    PLACES_URI.buildUpon().appendQueryParameter("from_id",
                            String.valueOf(ride.getFromId())).build()), TO);
            getActivity().overridePendingTransition(
                    R.anim.slide_in_right, R.anim.do_nix);
        } else if (btn == when.btn || btn == when.icon) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(ride.getDep()));
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(getActivity(), this, year, month, day).show();
        }
    }

    @Override
    public void onActivityResult(int req, int res, final Intent intent) {
        if (res == Activity.RESULT_OK) {
            Log.d(TAG, "selected " + intent.getData());
            switch (req) {
            case FROM:
                animatePulse(from.name);
                ride.from(intent.getData());
                from.setPlace(ride.getFrom());
                break;
            case TO:
                animatePulse(to.name);
                ride.to(intent.getData());
                to.setPlace(ride.getTo());
                break;
            }
        }
    }

    @Override
    public void onDateSet(DatePicker picker, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        ride.dep(cal.getTime());
        when.setDate(ride.getDep());
    }

    private void animatePulse(final View view) {
        Animation fade_in = new AlphaAnimation(0.3f, 1f);
        fade_in.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setBackgroundResource(android.R.color.white);
                Animation fade_out = new AlphaAnimation(1f, 0.7f);
                fade_out.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setBackgroundResource(R.drawable.btn_white);
                    }
                });
                fade_out.setDuration(1400);
                view.startAnimation(fade_out);
            }
        });
        fade_in.setDuration(190);
        view.startAnimation(fade_in);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("ride", ride);
        super.onSaveInstanceState(outState);
    }

    public static long getMorning(long dep) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dep);
        c.set(Calendar.HOUR_OF_DAY, 0); // reset
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

}