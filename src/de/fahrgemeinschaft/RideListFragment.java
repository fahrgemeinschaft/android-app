/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.teleportr.ConnectorService;
import org.teleportr.ConnectorService.BackgroundListener;
import org.teleportr.Ride;
import org.teleportr.Ride.COLUMNS;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.fahrgemeinschaft.util.SpinningZebraListFragment;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RideListFragment extends SpinningZebraListFragment
        implements ServiceConnection, BackgroundListener {

    private static final SimpleDateFormat day =
            new SimpleDateFormat("EEE", Locale.GERMANY);
    private static final SimpleDateFormat date =
            new SimpleDateFormat("dd.MM.", Locale.GERMANY);
    private static SimpleDateFormat time =
            new SimpleDateFormat("HH:mm", Locale.GERMANY);
    private String[] split;
    private long currently_searching_date;

    @Override
    public void bindListItemView(View view, Cursor ride) {
        RideView v = (RideView) view;
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

        Date timestamp = new Date(ride.getLong(COLUMNS.DEPARTURE));
        v.day.setText(day.format(timestamp));
        v.date.setText(date.format(timestamp));
        v.time.setText(time.format(timestamp));

        v.price.setText(ride.getInt(COLUMNS.PRICE) / 100 + "");
        switch(ride.getInt(COLUMNS.SEATS)){
        case 0:
            v.seats.setImageResource(R.drawable.icn_seats_white_full); break;
        case 1:
            v.seats.setImageResource(R.drawable.icn_seats_white_1); break;
        case 2:
            v.seats.setImageResource(R.drawable.icn_seats_white_2); break;
        case 3:
            v.seats.setImageResource(R.drawable.icn_seats_white_3); break;
        default:
            v.seats.setImageResource(R.drawable.icn_seats_white_many); break;
        }
        long dif = ride.getLong(COLUMNS.DEPARTURE) - currently_searching_date;
//        System.out.println("DELTA " + delta / 3600000);
        if (ride.getShort(COLUMNS.DIRTY) == 1 || dif > 0 && dif < 24*3600000) {
            v.loading.setVisibility(View.VISIBLE);
        } else {
            v.loading.setVisibility(View.GONE);
        }
        if (ride.getString(COLUMNS.WHO).equals("")) {
            view.findViewById(R.id.stub).setVisibility(View.VISIBLE);
            final Uri edit_uri = Uri.parse(
                    "content://de.fahrgemeinschaft/rides/" + ride.getLong(0));
            view.findViewById(R.id.edit).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(),
                            EditRideActivity.class).setData(edit_uri));
                }
            });
        } else {
            view.findViewById(R.id.stub).setVisibility(View.GONE);
        }
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
            startSpinning(getString(R.string.searching),
                    day.format(query.getDep()) + " "
                            + date.format(query.getDep()));
            currently_searching_date = query.getDep();
//            ((CursorAdapter)getListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onBackgroundSuccess(Ride query, int numberOfRidesFound) {
        if (onScreen) {
            if (numberOfRidesFound == 0) {
//                Crouton.makeText(getActivity(), 
//                        getString(R.string.nothing) + " "
//                        + day.format(query.getDep()) + " "
//                        + date.format(query.getDep()), Style.INFO).show();
                Toast.makeText(getActivity(), 
                        getString(R.string.nothing) + " "
                                + day.format(query.getDep()) + " "
                                + date.format(query.getDep()),
                                Toast.LENGTH_SHORT).show();
            }
            stopSpinning("click for weida..");
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



    static class RideView extends RelativeLayout {

        TextView from_place;
        TextView from_city;
        TextView to_place;
        TextView to_city;
        ImageView seats;
        TextView price;
        TextView date;
        TextView time;
        TextView day;
        ProgressBar loading;

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
            seats = (ImageView) findViewById(R.id.seats);
            price = (TextView) findViewById(R.id.price);
            date = (TextView) findViewById(R.id.date);
            time = (TextView) findViewById(R.id.time);
            day = (TextView) findViewById(R.id.day);
            loading = (ProgressBar) findViewById(R.id.loading);
        }
    }
}