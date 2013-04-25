/**
 * Fahrgemeinschaft Ridesharing App
 *
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class RideDetailsFragment extends SherlockFragment
        implements LoaderCallbacks<Cursor> {

    private static final String TAG = "Fahrgemeinschaft";
    private ViewPager pager;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        Log.d(TAG, "on create view");
        return lI.inflate(R.layout.fragment_ride_details, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle savedInstanceState) {
        super.onViewCreated(layout, savedInstanceState);

        pager = (ViewPager) layout.findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter() {

            @Override
            public void destroyItem(View view, int arg1, Object object) {
                Log.d(TAG, "destroyItem_ViewPager");
                ((ViewPager) view).removeView((View) object);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                Log.d(TAG, "is view from obj");
                return view == object;
            }

            @Override
            public Object instantiateItem(View view, int position) {
                // View viewRideDetails = getActivity().getLayoutInflater()
                // .inflate(R.layout.view_ride_details, null, false);
                // ((ViewPager) view).addView(viewRideDetails);
                // Log.d(TAG, "instantiateItem");
                // return viewRideDetails;
                return null;
            }

            @Override
            public int getCount() {
                Log.d(TAG, "getcount_viewPager");
                return 41;
            }
        });

        Log.d(TAG, "view created");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        Uri uri = Uri.parse("content://...");
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor msges) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        Log.d(TAG, "onLoaderReset");
    }
}
