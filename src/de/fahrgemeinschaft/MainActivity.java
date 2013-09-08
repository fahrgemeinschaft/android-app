/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.util.Calendar;

import org.teleportr.ConnectorService;
import org.teleportr.ConnectorService.ServiceCallback;
import org.teleportr.Ride;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.util.SpinningZebraListFragment.ListFragmentCallback;
import de.fahrgemeinschaft.util.Util;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends SherlockFragmentActivity
       implements OnClickListener, LoaderCallbacks<Cursor>,
       OnBackStackChangedListener, ListFragmentCallback, 
       OnPageChangeListener, ServiceCallback<String>,
       ServiceConnection {

    public MainFragment main;
    private MenuItem ic_profile;
    public RideListFragment results;
    private RideListFragment myrides;
    public RideDetailsFragment details;
    private RideDetailsFragment mydetails;
    public ConnectorService service;
    private MenuItem ic_myrides;
    public static final String TAG = "Fahrgemeinschaft";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentManager fm = getSupportFragmentManager();
        main = (MainFragment) fm.findFragmentByTag("main");
        results = (RideListFragment)
                fm.findFragmentByTag(getString(R.string.results));
        if (results == null) results = new RideListFragment();
        results.setSpinningEnabled(true);
        myrides = (RideListFragment)
                fm.findFragmentByTag(getString(R.string.myrides));
        if (myrides == null) myrides = new RideListFragment();
        myrides.setSpinningEnabled(false);
        details = (RideDetailsFragment)
                fm.findFragmentByTag(getString(R.string.details));
        if (details == null) details = new RideDetailsFragment();
        mydetails = (RideDetailsFragment)
                fm.findFragmentByTag(getString(R.string.mydetails));
        if (mydetails == null) mydetails = new RideDetailsFragment();
        if (savedInstanceState != null)
            handleIntent(getIntent().getData());
        else
            onNewIntent(getIntent());
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        onBackStackChanged();
    }

    @Override
    protected void onStart() {
        bindService(new Intent(this, ConnectorService.class), this, 0);
        super.onStart();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder bg) {
        service = ((ConnectorService.Bind) bg).getService();
        service.myrides.register(this);
        service.authCallback = this;
        service.publish.register(this);
        service.search.register(results);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getData() != null) {
            handleIntent(intent.getData());
            switch (uriMatcher.match(intent.getData())) {
            case SEARCH:
                showFragment(results, getString(R.string.results),
                        R.anim.slide_in_right,R.anim.slide_out_right);
                break;
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

    private void handleIntent(Uri uri) {
        if (uri != null) {
            setIntent(getIntent().setData(uri));
            switch (uriMatcher.match(uri)) {
            case SEARCH:
                getSupportLoaderManager().restartLoader(SEARCH, null, this);
                break;
            case MYRIDES:
                getSupportLoaderManager().restartLoader(MYRIDES, null, this);
                break;
            }
        }
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
            this.overridePendingTransition(
                    R.anim.slide_in_bottom, R.anim.slide_out_top);
            break;
        case R.id.btn_mitfahren:
            if (main.ride.getFrom() == null || main.ride.getTo() == null) {
                Crouton.makeText(this, getString(R.string.incomplete),
                        Style.INFO).show();
                return;
            }
            r.type(Ride.SEARCH).arr(r.getDep() +2*24*3600*1000)
                .store(this);
            startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
            handleIntent(r.toUri());
            showFragment(results, getString(R.string.results),
                    R.anim.slide_in_right,R.anim.slide_out_right);
            break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle b) {
        return new CursorLoader(this, getIntent().getData(),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor rides) {
        switch (loader.getId()) {
        case MYRIDES:
            myrides.swapCursor(rides);
            mydetails.swapCursor(rides);
            break;
        case SEARCH:
            results.swapCursor(rides);
            details.swapCursor(rides);
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
        case SEARCH:
            results.swapCursor(null);
            details.swapCursor(null);
            break;
        case MYRIDES:
            myrides.swapCursor(null);
            mydetails.swapCursor(null);
            break;
        }
    }

    @Override
    public void onSpinningWheelClick() {
        main.ride.type(Ride.SEARCH).arr(getNextDayMorning(
                main.ride.getArr() + 2 * 24 * 3600 * 1000)).store(this);
        startService(new Intent(this, ConnectorService.class)
            .setAction(ConnectorService.SEARCH));
    }

    @Override
    public void onListItemClick(int position) {
        if (getIntent().getData().equals(MY_RIDES_URI)) {
            mydetails.setSelection(position);
            showFragment(mydetails, getString(R.string.mydetails),
                    R.anim.slide_in_right, R.anim.slide_out_right);
        } else {
            details.setSelection(position);
            showFragment(details, getString(R.string.details),
                    R.anim.slide_in_right, R.anim.slide_out_right);
        }
    }

    @Override
    public void onPageSelected(final int position) {
        System.out.println("selected " + position);
//        results.getListView().setSelection(position);
    }

    public void contact(View v) {
        Cursor cursor;
        if (getIntent().getData().equals(MY_RIDES_URI)) {
            cursor = ((CursorAdapter) myrides.getListAdapter()).getCursor();
        } else {
            cursor = ((CursorAdapter) results.getListAdapter()).getCursor();
        }
        cursor.moveToPosition(details.getSelection());
        Util.openContactOptionsChooserDialog(this, cursor);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.action_bar, menu);
        ic_profile = menu.findItem(R.id.profile);
        ic_myrides = menu.findItem(R.id.myrides);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.myrides:
            showMyRides();
            return true;
        case R.id.profile:
            showProfile();
            return true;
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            this.overridePendingTransition(
                    R.anim.slide_in_top, R.anim.do_nix);
            return true;
        case android.R.id.home: // up
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else showAbout();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void showAbout() {
        showFragment(new AboutFragment(), getString(R.string.about),
            R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void showProfile() {
        if (isTopFragment(R.string.details))
            getSupportFragmentManager().popBackStackImmediate();
        showFragment(new ProfileFragment(), getString(R.string.profile),
                R.anim.slide_in_top, R.anim.slide_out_top);
    }

    public void showMyRides() {
        handleIntent(MY_RIDES_URI);
        showFragment(myrides, getString(R.string.myrides),
                R.anim.slide_in_top, R.anim.slide_out_top);
        startService(new Intent(this, ConnectorService.class)
            .setAction(ConnectorService.PUBLISH));
    }

    private void showFragment(Fragment fragment, String name, int in, int out) {
        setTitle(name);
        FragmentManager fm = getSupportFragmentManager();
        for (int i = fm.getBackStackEntryCount() - 1; i >= 0; i--) {
            if (fm.getBackStackEntryAt(i).getName().equals(name)) {
                for (int j = fm.getBackStackEntryCount() - 1; j > i; j--) {
                    fm.popBackStackImmediate();
                }
                return;
            }
        }
        fm.beginTransaction()
            .setCustomAnimations(
                in, R.anim.do_nix, R.anim.do_nix, out)
            .add(R.id.container, fragment, name)
            .addToBackStack(name)
        .commit();
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fm = getSupportFragmentManager();
        int backstack = fm.getBackStackEntryCount();
        if (backstack == 0)
            setTitle("");
        else {
            String name = fm.getBackStackEntryAt(backstack - 1).getName();
            if (name.equals(getString(R.string.results)) ||
                    name.equals(getString(R.string.details))) {
                handleIntent(main.ride.toUri());
            } else if (name.equals(getString(R.string.myrides)) ||
                    name.equals(getString(R.string.mydetails))) {
                handleIntent(MY_RIDES_URI);
            }
            setTitle(name);
        }
    }

    public boolean isTopFragment(int id) {
        FragmentManager fm = getSupportFragmentManager();
        int backstack = fm.getBackStackEntryCount();
        return backstack > 0 && fm.getBackStackEntryAt(backstack - 1)
                .getName().equals(getString(id));
    }


    
    @Override
    public void onProgress(String what, int how) {
        if (what.equals(ConnectorService.MYRIDES) && ic_myrides != null) {
            ic_myrides.setActionView(R.layout.view_progress);
        } else if (what.equals(ConnectorService.AUTH) && ic_profile != null) {
            Crouton.makeText(this, what, Style.INFO).show();
            ic_profile.setActionView(R.layout.view_progress);
        }
    }

    @Override
    public void onFail(String what, String reason) {
        Crouton.makeText(this, what + " fail: " + reason, Style.ALERT).show();
        if (what.equals(ConnectorService.MYRIDES) && ic_myrides != null) {
            ic_myrides.setActionView(null);
        } else if (what.equals(ConnectorService.AUTH) && ic_profile != null) {
            ic_profile.setActionView(null);
            showFragment(new ProfileFragment(), getString(R.string.profile),
                    R.anim.slide_in_top, R.anim.slide_out_top);
        }
    }

    @Override
    public void onSuccess(String what, int number) {
        if (what == null) return;
        Crouton.makeText(this, what + " success.", Style.CONFIRM).show();
        if (what.equals(ConnectorService.MYRIDES)) {
            ic_myrides.setActionView(null);
        } else if (what.equals(ConnectorService.AUTH)) {
            ic_profile.setActionView(null);
            startService(new Intent(this, ConnectorService.class)
                    .setAction(ConnectorService.SEARCH));
            ((NotificationManager) getSystemService(Context
                    .NOTIFICATION_SERVICE)).cancel(42);
        }
    }

    @Override
    protected void onStop() {
        unbindService(this);
        super.onStop();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    public static long getNextDayMorning(long dep) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dep + 24 * 3600000); // plus one day
        c.set(Calendar.HOUR_OF_DAY, 0); // reset
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}

    private static final String AUTHORITY = "de.fahrgemeinschaft";
    public static final Uri MY_RIDES_URI =
            Uri.parse("content://de.fahrgemeinschaft/myrides");
    private static final int DETAILS = 4;
    private static final int MYRIDES = 5;
    private static final int SEARCH = 42;
    private static final int PROFILE = 3;
    private static final int ABOUT = 112;

    static final UriMatcher uriMatcher = new UriMatcher(0);
    static {
        uriMatcher.addURI(AUTHORITY, "rides", SEARCH);
        uriMatcher.addURI(AUTHORITY, "myrides", MYRIDES);
//        uriMatcher.addURI(AUTHORITY, "rides/#", DETAILS);
        uriMatcher.addURI(AUTHORITY, "profile", PROFILE);
        uriMatcher.addURI(AUTHORITY, "about", ABOUT);
    }
}
