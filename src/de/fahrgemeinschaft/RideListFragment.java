/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.Ride.COLUMNS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class RideListFragment extends SherlockListFragment
        implements LoaderCallbacks<Cursor> {

    private static final String TAG = "Fahrgemeinschaft";
    private static final SimpleDateFormat day = new SimpleDateFormat("EEE", Locale.GERMANY);
    private static final SimpleDateFormat date = new SimpleDateFormat("dd.MM.", Locale.GERMANY);
    private static SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.GERMANY);
    private boolean spin;
    private View wheel;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_list, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle savedInstanceState) {
        super.onViewCreated(layout, savedInstanceState);

        setListAdapter(new CursorAdapter(getActivity(), null, 0) {

            private String[] split;

            @Override
            public View newView(Context ctx, Cursor rides, ViewGroup parent) {
                    return getLayoutInflater(null).inflate(
                            R.layout.view_ride_list_entry, parent, false);
            }

            @Override
            public void bindView(View view, Context ctx, Cursor ride) {
                if (ride.getPosition() == ride.getCount()) return;

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

                v.price.setText(ride.getInt(COLUMNS.PRICE) / 100 + "€");
                switch(ride.getInt(COLUMNS.SEATS)){
                case 0:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_full);
                    break;
                case 1:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_1);
                    break;
                case 2:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_2);
                    break;
                case 3:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_3);
                    break;
                default:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_many);
                    break;
                }
                
                if (ride.getPosition() % 2 == 0) {
                    v.setBackgroundColor(getResources().getColor(
                            R.color.light_green));
                } else {
                    v.setBackgroundColor(getResources().getColor(
                            R.color.almost_medium_green));
                }
            }

            @Override
            public int getCount() {
                return super.getCount() + 1;
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position) {
                if (position < getCount() - 1)
                    return 0;
                else
                    return 1;
            }

            @Override
            public View getView(int pos, View v, ViewGroup parent) {
                if (pos < getCount() - 1)
                    return super.getView(pos, v, parent);
                else {
                    if (v == null) {
                        v = getLayoutInflater(null).inflate(
                                R.layout.loading, parent, false);
                        if (spin) startSpinningWheel();
                        else stopSpinningWheel();
                    }
                    if (pos % 2 == 0) {
                        v.setBackgroundColor(getResources().getColor(
                                R.color.medium_green));
                    } else {
                        v.setBackgroundColor(getResources().getColor(
                                R.color.light_green));
                    }
                    return v;
                }
            }
        });
        getListView().setFocusableInTouchMode(true);
    }

    public void startSpinningWheel() {
        wheel = getListView().findViewById(R.id.progress);
        if (wheel != null) {
            final RotateAnimation rotateAnimation = new RotateAnimation(
                    0f, 360f, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(600);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            wheel.startAnimation(rotateAnimation);
        }
        spin = true;
    }

    public void stopSpinningWheel() {
        if (wheel != null) wheel.clearAnimation();
        spin = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle b) {
        System.out.println("query jobs");
        return new CursorLoader(getActivity(), Uri.parse(
                "content://" + getActivity().getPackageName() + "/jobs/search"),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor jobs) {
        System.out.println(" jobs " + jobs.getCount());
        if (jobs.getCount() == 0) {
            stopSpinningWheel();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        Log.d(TAG, "onLoaderReset in RideListFragment");
    }


    public interface ListItemClicker {
        public void onListItemClick(int position);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ((ListItemClicker) getActivity()).onListItemClick(position);
    }

    static class RideView extends RelativeLayout {

        TextView from_city;
        TextView from_place;
        TextView to_city;
        TextView to_place;
        TextView seats;
        TextView price;
        TextView day;
        TextView date;
        TextView time;

        public RideView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            from_city = (TextView) findViewById(R.id.from_city);
            from_place = (TextView) findViewById(R.id.from_place);
            to_city = (TextView) findViewById(R.id.to_city);
            to_place = (TextView) findViewById(R.id.to_place);
            seats = (TextView) findViewById(R.id.seats);
            price = (TextView) findViewById(R.id.price);
            day = (TextView) findViewById(R.id.day);
            date = (TextView) findViewById(R.id.date);
            time = (TextView) findViewById(R.id.time);
        }
    }

}
