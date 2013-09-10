/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.util.Calendar;

import org.teleportr.ConnectorService;
import org.teleportr.Ride;

import android.content.ComponentName;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import de.fahrgemeinschaft.util.SpinningZebraListFragment.ListFragmentCallback;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends BaseActivity
       implements OnClickListener, ListFragmentCallback {

    public MainFragment main;
    public RideListFragment results;
    public RideDetailsFragment details;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        FragmentManager fm = getSupportFragmentManager();
        main = (MainFragment) fm.findFragmentByTag("main");
        results = (RideListFragment)
                fm.findFragmentByTag(getString(R.string.results));
        if (results == null) results = new RideListFragment();
        results.setSpinningEnabled(true);
        details = (RideDetailsFragment)
                fm.findFragmentByTag(getString(R.string.details));
        if (details == null) details = new RideDetailsFragment();
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    protected void handleIntent(Intent intent) {
        if (intent.getData() != null) {
            switch (uriMatcher.match(intent.getData())) {
            case MYRIDES:
                showMyRides();
                break;
            case PROFILE:
                showProfile();
                break;
            case ABOUT:
                showAbout();
                break;
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder bg) {
        super.onServiceConnected(name, bg);
        service.search.register(results);
    }

    @Override
    public void onClick(View v) {
        Ride r = main.ride;
        switch (v.getId()) {
        case R.id.btn_selberfahren:
            long now = System.currentTimeMillis();
            Uri uri = r.type(Ride.OFFER)
                    .dep(r.getDep() < now? now + 3600000 : r.getDep())
                    .mode(Ride.Mode.CAR).seats(3).store(this);
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
            overridePendingTransition(
                    R.anim.slide_in_bottom, R.anim.slide_out_top);
            break;
        case R.id.btn_mitfahren:
            if (main.ride.getFrom() == null || main.ride.getTo() == null) {
                Crouton.makeText(this, getString(R.string.incomplete),
                        Style.INFO).show();
                return;
            }
            r.type(Ride.SEARCH).arr(r.getDep() + 48 * 3600000).store(this);
            startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
            results.load(r.toUri(), SEARCH);
            showFragment(results, getString(R.string.results),
                    R.anim.slide_in_right,R.anim.slide_out_right);
            break;
        }
    }

    @Override
    public void onListItemClick(int position, int fragment) {
        if (fragment == SEARCH) {
            showFragment(details, getString(R.string.details),
                    R.anim.slide_in_right, R.anim.slide_out_right);
            details.setTargetFragment(results, SEARCH);
            details.setSelection(position);
        } else super.onListItemClick(position, fragment);
    }

    @Override
    public void onSpinningWheelClick() {
        main.ride.type(Ride.SEARCH).arr(getNextDayMorning(
                main.ride.getArr() + 2 * 24 * 3600 * 1000)).store(this);
        startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
    }

    public static long getNextDayMorning(long dep) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dep + 24 * 3600000); // plus one day
        c.set(Calendar.HOUR_OF_DAY, 0); // reset
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis();
    }



    static final UriMatcher uriMatcher = new UriMatcher(0);
    static {
        uriMatcher.addURI(AUTHORITY, "rides", SEARCH);
        uriMatcher.addURI(AUTHORITY, "myrides", MYRIDES);
//        uriMatcher.addURI(AUTHORITY, "rides/#", DETAILS);
        uriMatcher.addURI(AUTHORITY, "profile", PROFILE);
        uriMatcher.addURI(AUTHORITY, "about", ABOUT);
    }
}
