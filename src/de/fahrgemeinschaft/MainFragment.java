package de.fahrgemeinschaft;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
import android.widget.Button;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragment;

public class MainFragment extends SherlockFragment
        implements OnClickListener, OnDateSetListener {

    public static final Uri PLACES_URI
            = Uri.parse("content://de.fahrgemeinschaft/places");
    protected static final String TAG = "fahrgemeinschaft";
    private static final int FROM = 42;
    private static final int TO = 55;
    private Button when;
    private Button from;
    private Button to;
    public Ride ride;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_main, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        from = (Button) v.findViewById(R.id.btn_autocomplete_from);
        to = (Button) v.findViewById(R.id.btn_autocomplete_to);
        when = (Button) v.findViewById(R.id.btn_pick_date);
        to.setOnClickListener(this);
        from.setOnClickListener(this);
        when.setOnClickListener(this);
        v.findViewById(R.id.btn_mitfahren)
            .setOnClickListener((OnClickListener) getActivity());
        v.findViewById(R.id.btn_selberfahren)
            .setOnClickListener((OnClickListener) getActivity());
        v.findViewById(R.id.btn_pick_to).setOnClickListener(this);
        v.findViewById(R.id.btn_pick_from).setOnClickListener(this);
        v.findViewById(R.id.icn_pick_date).setOnClickListener(this);

        if (savedInstanceState != null) {
            ride = savedInstanceState.getParcelable("ride");
            from.setText(ride.getFrom().getName());
            to.setText(ride.getTo().getName());
            setDateButtonText(ride.getDep());
        } else {
            ride = new Ride(getActivity());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_pick_from:
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    PLACES_URI), FROM);
            getActivity().overridePendingTransition(
                    R.anim.slide_in_left, R.anim.do_nix);
            break;

        case R.id.btn_pick_to:
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    PLACES_URI.buildUpon().appendQueryParameter("from_id",
                            String.valueOf(ride.getFromId())).build()), TO);
            getActivity().overridePendingTransition(
                    R.anim.slide_in_right, R.anim.do_nix);
            break;

        case R.id.btn_autocomplete_from:
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    PLACES_URI).putExtra("show_textfield", true), FROM);
            getActivity().overridePendingTransition(
                    R.anim.slide_in_left, R.anim.do_nix);
            break;

        case R.id.btn_autocomplete_to:
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    PLACES_URI.buildUpon().appendQueryParameter("from_id",
                            String.valueOf(ride.getFromId())).build())
                    .putExtra("show_textfield", true), TO);
            getActivity().overridePendingTransition(
                    R.anim.slide_in_right, R.anim.do_nix);
            break;
        case R.id.btn_pick_date:
        case R.id.icn_pick_date:
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(ride.getDep()));
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(getActivity(), this, year, month, day).show();
            break;
        }
    }

    @Override
    public void onActivityResult(int req, int res, final Intent intent) {
        if (res == Activity.RESULT_OK) {
            Log.d(TAG, "selected " + intent.getData());
            switch (req) {
            case FROM:
                animatePulse(from);
                ride.from(intent.getData());
                from.setText(ride.getFrom().getName());
                break;
            case TO:
                animatePulse(to);
                ride.to(intent.getData());
                to.setText(ride.getTo().getName());
                break;
            }
        }
    }

    public void setDateButtonText(long date) {
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTimeInMillis(date);
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        if (dayOfYear == -1 || dayOfYear == today)
            when.setText(getString(R.string.now));
        else if (dayOfYear == today + 1)
            when.setText(getString(R.string.tomorrow));
        else if (dayOfYear == today + 2)
            when.setText(getString(R.string.after_tomorrow));
        else
            when.setText(new SimpleDateFormat("dd. MMM yyyy",
                    Locale.GERMANY).format(ride.getDep()));
    }

    @Override
    public void onDateSet(DatePicker picker, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 00);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        ride.dep(cal.getTime());
        setDateButtonText(ride.getDep());
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

}