/**
 * Fahrgemeinschaft Ridesharing App
 *
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.actionbarsherlock.app.SherlockListFragment;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RideListFragment extends SherlockListFragment implements
        LoaderCallbacks<Cursor> {

    private static final String TAG = "Fahrgemeinschaft";
    private static final SimpleDateFormat day = new SimpleDateFormat("EE");
    private static final SimpleDateFormat date = new SimpleDateFormat("dd.MM");
    private static SimpleDateFormat time = new SimpleDateFormat("HH:mm");

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_list, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle savedInstanceState) {
        super.onViewCreated(layout, savedInstanceState);

        setListAdapter(new CursorAdapter(getActivity(), null, 0) {

            @Override
            public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
                return getLayoutInflater(null).inflate(
                        R.layout.view_ride_list_entry, parent, false);
            }

            @Override
            public void bindView(View view, Context ctx, Cursor ride) {
                RideView v = (RideView) view;
                v.from_city.setText(ride.getString(2));
                v.from_place.setText(ride.getString(1));
                v.to_city.setText(ride.getString(4));
                v.to_place.setText(ride.getString(3));

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
        });

        getActivity().getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        Uri uri = getActivity().getIntent().getData();
        getActivity().getContentResolver().registerContentObserver(
                Uri.parse("content://"+getActivity().getPackageName()+"/rides"),
                false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        if (getActivity() != null) {
                            getActivity().getSupportLoaderManager()
                                .restartLoader(0, null, RideListFragment.this);
                        }
                    }
                });
        Log.d(TAG, "query " + uri);
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor rides) {
        ((CursorAdapter) getListAdapter()).swapCursor(rides);
        Log.d(TAG, "got results: " + rides.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        Log.d(TAG, "onLoaderReset");
    }

    public interface ListItemClicker {
        public void onListItemClick(String id);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ((ListItemClicker) getActivity()).onListItemClick("foo");
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
