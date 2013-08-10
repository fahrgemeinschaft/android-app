/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.teleportr.ConnectorService;
import org.teleportr.ConnectorService.BackgroundListener;
import org.teleportr.Ride;
import org.teleportr.Ride.COLUMNS;
import org.teleportr.Ride.Mode;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.fahrgemeinschaft.util.RideRowView;
import de.fahrgemeinschaft.util.SpinningZebraListFragment;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RideListFragment extends SpinningZebraListFragment
        implements ServiceConnection, BackgroundListener {

    private static final SimpleDateFormat day =
            new SimpleDateFormat("EEE", Locale.GERMANY);
    private static final SimpleDateFormat date =
            new SimpleDateFormat("dd.MM.", Locale.GERMANY);
    private String[] split;
    private long currently_searching_date;

    @Override
    public void bindListItemView(View view, Cursor ride) {
        RideView v = (RideView) view;
        v.id = ride.getInt(0);
        v.from_place.setText(ride.getString(COLUMNS.FROM_NAME));
        split = ride.getString(COLUMNS.FROM_ADDRESS).split(",");
        if (split.length > 1)
            v.from_city.setText(split[1]);
        else
            v.from_city.setText(ride.getString(COLUMNS.FROM_ADDRESS));

        v.to_place.setText(ride.getString(COLUMNS.TO_NAME));
        split = ride.getString(COLUMNS.TO_ADDRESS).split(",");
        if (split.length > 1)
            v.to_city.setText(split[1]);
        else
            v.to_city.setText(ride.getString(COLUMNS.TO_ADDRESS));

        v.row.bind(ride, getActivity());
        long dif = ride.getLong(COLUMNS.DEPARTURE) - currently_searching_date;
        if (ride.getShort(COLUMNS.DIRTY) == 1 || dif > 0 && dif < 24*3600000) {
            v.loading.setVisibility(View.VISIBLE);
        } else {
            v.loading.setVisibility(View.GONE);
        }

        if (ride.getString(COLUMNS.MODE).equals(Mode.CAR.name())) {
            v.mode.setImageResource(R.drawable.icn_mode_car);
        } else {
            v.mode.setImageResource(R.drawable.icn_mode_train);
        }

        if (ride.getInt(COLUMNS.ACTIVE) == 0) {
            v.active.setVisibility(View.VISIBLE);
        } else {
            v.active.setVisibility(View.GONE);
        }

        if (isActiveMyRide(ride)) {
            v.showButtons();
        } else {
            view.findViewById(R.id.stub).setVisibility(View.GONE);
        }
    }

    private boolean isActiveMyRide(Cursor ride) {
        return (ride.getString(COLUMNS.WHO).equals("") ||
                ride.getString(COLUMNS.WHO).equals(PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("user", "")))
                && ride.getInt(COLUMNS.ACTIVE) == 1;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(
                new Intent(activity, ConnectorService.class), this, 0);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ((ConnectorService.Bind) service).getService().register(this);
    }

    @Override
    public void onBackgroundSearch(Ride query) {
        if (onScreen) {
            currently_searching_date = getNextDayMorning(query.getDep());
            startSpinning(getString(R.string.searching),
                    day.format(currently_searching_date) + " "
                            + date.format(currently_searching_date));
        }
    }

    @Override
    public void onBackgroundSuccess(Ride query, int numberOfRidesFound) {
        if (onScreen) {
            if (numberOfRidesFound == 0) {
                Toast.makeText(getActivity(), 
                        getString(R.string.nothing) + " "
                                + day.format(currently_searching_date) + " "
                                + date.format(currently_searching_date),
                                Toast.LENGTH_SHORT).show();
            }
            stopSpinning("weiteren Tag suchen");
        }
        currently_searching_date = 0;
    }

    @Override
    public void onBackgroundFail(Ride query, String reason) {
        if (onScreen) {
            Crouton.makeText(getActivity(), 
                    reason + " while "
                    + day.format(query.getDep()) + " "
                    + date.format(query.getDep()), Style.ALERT).show();
            stopSpinning(reason);
        }
        currently_searching_date = 0;
    }

    @Override
    public void onDetach() {
        getActivity().setTitle("");
//        Crouton.cancelAllCroutons();
        getActivity().unbindService(this);
        super.onDetach();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {}



    static class RideView extends RelativeLayout implements OnClickListener {

        TextView from_place;
        TextView from_city;
        TextView to_place;
        TextView to_city;
        ProgressBar loading;
        RideRowView row;
        ImageView mode;
        View active;
        int id;

        public RideView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            from_place = (TextView) findViewById(R.id.from_place);
            from_city = (TextView) findViewById(R.id.from_city);
            to_place = (TextView) findViewById(R.id.to_place);
            to_city = (TextView) findViewById(R.id.to_city);
            loading = (ProgressBar) findViewById(R.id.loading);
            row = (RideRowView) findViewById(R.id.row);
            mode = (ImageView) findViewById(R.id.mode);
            active = findViewById(R.id.active);
        }

        public void showButtons() {
            findViewById(R.id.stub).setVisibility(View.VISIBLE);
            findViewById(R.id.edit).setOnClickListener(this);
            findViewById(R.id.increase_seats).setOnClickListener(this);
            findViewById(R.id.decrease_seats).setOnClickListener(this);
            findViewById(R.id.edit).setFocusable(false);
            findViewById(R.id.increase_seats).setFocusable(false);
            findViewById(R.id.decrease_seats).setFocusable(false);
        }

        @Override
        public void onClick(View v) {
            Ride ride = new Ride(id, getContext());
            switch (v.getId()) {
            case R.id.edit:
                getContext().startActivity(new Intent(getContext(),
                        EditRideActivity.class).setData(Uri.parse(
                                "content://de.fahrgemeinschaft/rides/" + id)));
                break;
            case R.id.increase_seats:
                ride.seats(ride.getSeats() + 1).dirty().store(getContext());
                getContext().startService(
                        new Intent(getContext(), ConnectorService.class)
                                .setAction(ConnectorService.PUBLISH));
                break;
            case R.id.decrease_seats:
                ride.seats(ride.getSeats() - 1).dirty().store(getContext());
                getContext().startService(
                        new Intent(getContext(), ConnectorService.class)
                        .setAction(ConnectorService.PUBLISH));
            }
        }
    }

    private long getNextDayMorning(long dep) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dep + 24 * 3600000); // plus one day
        c.set(Calendar.HOUR_OF_DAY, 0); // reset
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis();
    }

}