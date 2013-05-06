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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.calciumion.widget.BasePagerAdapter;

public class RideDetailsFragment extends SherlockFragment {

    private static final String TAG = "Fahrgemeinschaft";
    private static final SimpleDateFormat day = new SimpleDateFormat("EE");
    private static final SimpleDateFormat date = new SimpleDateFormat("dd.MM");
    private static SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    ViewPager pager;
    private Cursor cursor;
    private int current;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "on create details " + savedInstanceState);
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_details, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle savedInstanceState) {
        super.onViewCreated(layout, savedInstanceState);
        Log.d(TAG, "on create detail view " + savedInstanceState);
        pager = (ViewPager) layout.findViewById(R.id.pager);
        
        pager.setAdapter(new BasePagerAdapter() {
            
            @Override
            public int getCount() {
                if (cursor == null)
                    return 0;
                else
                    return cursor.getCount();
            }
            
            @Override
            protected View getView(Object position, View v, ViewGroup parent) {
                if (v == null) {
                    v = getActivity().getLayoutInflater()
                            .inflate(R.layout.view_ride_details, null, false);
                }
                RideView view = (RideView) v;
                if (view.content.getChildCount() > 3)
                    view.content.removeViews(1, view.content.getChildCount() - 3);

                cursor.moveToPosition((Integer) position);

                view.from_place.setText(cursor.getString(2));
                view.to_place.setText(cursor.getString(4));

                Date timestamp = new Date(cursor.getLong(5));
                view.day.setText(day.format(timestamp).substring(0, 2));
                view.date.setText(date.format(timestamp));
                view.time.setText(time.format(timestamp));

                view.price.setText(cursor.getLong(9) + "€");
                view.seats.setText(cursor.getLong(10) + "");
                view.details.setText(cursor.getString(8));

                getActivity().getSupportLoaderManager()
                    .initLoader((int) cursor.getLong(0), null, view);
                return view;
            }
            
            @Override
            protected Object getItem(int position) {
                return position;
            }
        });
        pager.setOnPageChangeListener((OnPageChangeListener) getActivity());
    }

    public void setCursor(Cursor cursor) {
        Log.d(TAG, "change cursor");
        this.cursor = cursor;
        if (pager != null)
            pager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        pager.setCurrentItem(((ResultsActivity) getActivity()).selected);
    }

    static class RideView extends RelativeLayout implements LoaderCallbacks<Cursor>{

        TextView from_place;
        TextView to_place;
        TextView seats;
        TextView price;
        TextView day;
        TextView date;
        TextView time;
        TextView details;
        LinearLayout content;

        public RideView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            from_place = (TextView) findViewById(R.id.from_place);
            to_place = (TextView) findViewById(R.id.to_place);
            seats = (TextView) findViewById(R.id.seats);
            price = (TextView) findViewById(R.id.price);
            day = (TextView) findViewById(R.id.day);
            date = (TextView) findViewById(R.id.date);
            time = (TextView) findViewById(R.id.time);
            details = (TextView) findViewById(R.id.details);
            content = (LinearLayout) findViewById(R.id.content);
        }
        @Override
        public Loader<Cursor> onCreateLoader(int ride_id, Bundle b) {
            Log.d(TAG, "loading subrides for ride " + ride_id);
            return new CursorLoader(getContext(), Uri.parse(
                    "content://de.fahrgemeinschaft/rides/" + ride_id + "/rides")
                    ,null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> l, Cursor c) {
            Log.d(TAG, "finished loading subrides " + l.getId());
            for (int i = 2; i < c.getCount(); i++) {
                c.moveToPosition(i);
                TextView tv = new TextView(getContext());
                tv.setText(c.getString(2));
                tv.setBackgroundResource(R.color.medium_green);
                tv.setTextAppearance(getContext(), R.style.dark);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                lp.leftMargin = 34;
                tv.setLayoutParams(lp);
                content.addView(tv, i - 1);
                Log.d(TAG, c.getString(1) + " --> " + c.getString(3));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> l) {
            Log.d(TAG, "loader " + l.getId() + "reset.");
        }
    }


}
