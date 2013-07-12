/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;
import org.teleportr.Ride;
import org.teleportr.Ride.COLUMNS;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.util.SpinningZebraListFragment.ListFragmentCallback;
import de.fahrgemeinschaft.util.Util;

public class MainActivity extends SherlockFragmentActivity
       implements OnClickListener, ListFragmentCallback, OnPageChangeListener {

    public static final Uri MY_RIDES_URI =
            Uri.parse("content://de.fahrgemeinschaft/myrides");
    public static final Uri BG_JOBS_URI =
            Uri.parse("content://de.fahrgemeinschaft/jobs/search");
    public RideDetailsFragment details;
    public RideListFragment results;
    public RideListFragment myrides;
    public MainFragment main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        main = (MainFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main);
        results = new RideListFragment();
        myrides = new RideListFragment();
        myrides.setSpinningEnabled(false);
        details = new RideDetailsFragment();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_selberfahren:
            Uri uri = main.ride.type(Ride.OFFER).mode(Ride.Mode.CAR).seats(3)
                .store(this);
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
            break;
        case R.id.btn_mitfahren:
            main.ride.type(Ride.SEARCH).arr(main.ride.getDep() +2*24*3600*1000)
                .store(this);
            startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
            results.load(Uri.parse("content://de.fahrgemeinschaft/rides"
                    + "?from_id=" + main.ride.getFromId()
                    + "&to_id=" + main.ride.getToId()
                    + "&dep=" + main.ride.getDep()));
            showFragment(results);
            break;
        }
    }

    @Override
    public void onLoadFinished(Fragment fragment, Cursor cursor) {
        if (fragment.equals(results)) {
            setTitle("Results");
            details.swapCursor(cursor);
            if (cursor.getCount() > 0) {
                cursor.moveToLast();
                long latest_dep = cursor.getLong(COLUMNS.DEPARTURE);
                if (latest_dep > main.ride.getArr()) // inc time window
                    main.ride.arr(cursor.getLong(COLUMNS.DEPARTURE));
            }
        } else {
            setTitle("MyRides");
            details.swapCursor(cursor);
        }
    }

    @Override
    public void onSpinningWheelClick() {
        main.ride.arr(main.ride.getArr() + 2 * 24 * 3600 * 1000).store(this);
        startService(new Intent(this, ConnectorService.class)
        .setAction(ConnectorService.SEARCH));
    }

    @Override
    public void onListItemClick(int position) {
        details.setSelection(position);
        showFragment(details);
    }

    @Override
    public void onPageSelected(final int position) {
        results.getListView().setSelection(position);
        details.setSelection(position);
    }


    public void contact(View v) {
        Cursor cursor = ((CursorAdapter) results.getListAdapter()).getCursor();
        cursor.moveToPosition(details.getSelection());
        Util.openContactOptionsChooserDialog(this, cursor);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.my_rides:
            showFragment(myrides);
            myrides.load(MY_RIDES_URI);
            return true;
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        case R.id.profile:
            showFragment(new ProfileFragment());
            return true;
        case android.R.id.home:
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else finish();
            return true;
        }
        return false;
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.do_nix,
                R.anim.do_nix, R.anim.slide_out_right)
            .add(R.id.container, fragment, null)
            .addToBackStack(null)
        .commit();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}
}
