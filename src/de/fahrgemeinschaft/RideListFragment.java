/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class RideListFragment extends SherlockListFragment {

    private static final String TAG = "Fahrgemeinschaft";
    private static final SimpleDateFormat day = new SimpleDateFormat("EE");
    private static final SimpleDateFormat date = new SimpleDateFormat("dd.MM");
    private static SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    private boolean spin = true;
    private View wheel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "on create list " + savedInstanceState);
        super.onCreate(savedInstanceState);
    }

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

                v.from_place.setText(ride.getString(1));
                split = ride.getString(2).split(",");
                if (split.length > 1)
                    v.from_city.setText(split[1]);
                else
                    v.from_city.setText(ride.getString(2));

                v.to_place.setText(ride.getString(3));
                split = ride.getString(4).split(",");
                if (split.length > 1)
                    v.to_city.setText(split[1]);
                else
                    v.to_city.setText(ride.getString(4));

                Date timestamp = new Date(ride.getLong(5));
                v.day.setText(day.format(timestamp).substring(0, 2));
                v.date.setText(date.format(timestamp));
                v.time.setText(time.format(timestamp));

                v.price.setText(ride.getLong(9) + "€");
                v.seats.setText(ride.getLong(10) + "");
                
                if (ride.getPosition() % 2 == 0) {
                    v.setBackgroundColor(getResources().getColor(
                            R.color.medium_green));
                } else {
                    v.setBackgroundColor(getResources().getColor(
                            R.color.light_green));
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
                        wheel = v.findViewById(R.id.progress);
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
        if (wheel != null) wheel.setVisibility(View.VISIBLE);
        spin = false;
    }

    public void stopSpinningWheel() {
        if (wheel != null) wheel.setVisibility(View.INVISIBLE);
        spin = true;
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
