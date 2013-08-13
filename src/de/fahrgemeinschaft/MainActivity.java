/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.teleportr.ConnectorService;
import org.teleportr.Ride;
import org.teleportr.Ride.COLUMNS;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.util.SpinningZebraListFragment.ListFragmentCallback;
import de.fahrgemeinschaft.util.Util;

public class MainActivity extends SherlockFragmentActivity
       implements OnClickListener, ListFragmentCallback,
       LoaderCallbacks<Cursor>, OnPageChangeListener {

    public static final Uri MY_RIDES_URI =
            Uri.parse("content://de.fahrgemeinschaft/myrides");
    public static final Uri BG_JOBS_URI =
            Uri.parse("content://de.fahrgemeinschaft/jobs/search");
    public static final String TAG = "Fahrgemeinschaft";
    private static final String DETAILS = "details";
    private static final String RESULTS = "results";
    public RideDetailsFragment details;
    public RideListFragment results;
    public MainFragment main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        FragmentManager fm = getSupportFragmentManager();
        main = (MainFragment) fm.findFragmentById(R.id.main);
        results = (RideListFragment) fm.findFragmentByTag(RESULTS);
        if (results == null) results = new RideListFragment();
        details = (RideDetailsFragment) fm.findFragmentByTag(DETAILS);
        if (details == null) details = new RideDetailsFragment();
        if (getIntent().getData() != null) {
            handleIntent(getIntent().getData());
        }
        if (fm.getBackStackEntryCount() == 0) setTitle("");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getData() != null) {
            handleIntent(intent.getData());
            showFragment(results, RESULTS,
                    R.anim.slide_in_right,R.anim.slide_out_right);
        }
    }

    private void handleIntent(Uri uri) {
        if (getIntent().getData() != uri) {
            setIntent(getIntent().setData(uri));
            getSupportLoaderManager().destroyLoader(0);
        }
        if (uri.getLastPathSegment().equals("rides")) {
            setTitle(R.string.results);
            results.setSpinningEnabled(true);
            getSupportLoaderManager().initLoader(0, null, this);
        } else if (uri.equals(MY_RIDES_URI)) {
            setTitle(R.string.my_rides);
            results.setSpinningEnabled(false);
            getSupportLoaderManager().initLoader(0, null, this);
        } else if (uri.getLastPathSegment().equals("profile")) {
            showFragment(new ProfileFragment(), "profile",
                    R.anim.slide_in_top,R.anim.slide_out_top);
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
                Toast.makeText(this, getString(R.string.uncomplete),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            r.type(Ride.SEARCH).arr(r.getDep() +2*24*3600*1000)
                .store(this);
            startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
            handleIntent(Uri.parse("content://de.fahrgemeinschaft/rides"
                    + "?from_id=" + r.getFromId()
                    + "&to_id=" + r.getToId()
                    + "&dep=" + r.getDep()));
            showFragment(results, RESULTS,
                    R.anim.slide_in_right,R.anim.slide_out_right);
            break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle b) {
        Uri uri = getIntent().getData();
        System.out.println("create loader " + id + " : " + uri);
        if (uri != null)
            return new CursorLoader(this, uri, null, null, null, null);
        else return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor rides) {
        results.swapCursor(rides);
        details.swapCursor(rides);
        if (rides.getCount() > 0) {
            rides.moveToLast();
            long latest_dep = rides.getLong(COLUMNS.DEPARTURE);
            System.out.println("ALREADY in CACHE until "
                    + new SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY)
                            .format(new Date(latest_dep)));
            if (latest_dep - main.ride.getArr() > 24*3600000) {// inc window
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(latest_dep);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                main.ride.arr(cal.getTimeInMillis());
            }
        }
    }

    @Override
    public void onSpinningWheelClick() {
        System.out.println("arr: " + new SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY)
            .format(new Date(main.ride.getArr())));
        main.ride.arr(main.ride.getArr() + 2 * 24 * 3600 * 1000).store(this);
        System.out.println("arr after: " + new SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY)
            .format(new Date(main.ride.getArr())));
        startService(new Intent(this, ConnectorService.class)
            .setAction(ConnectorService.SEARCH));
    }

    @Override
    public void onListItemClick(int position) {
        showFragment(details, DETAILS,
                R.anim.slide_in_right, R.anim.slide_out_right);
        details.setSelection(position);
    }

    @Override
    public void onPageSelected(final int position) {
        System.out.println("selected " + position);
//        results.getListView().setSelection(position);
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
            handleIntent(MY_RIDES_URI);
            showFragment(results, RESULTS,
                    R.anim.slide_in_top, R.anim.slide_out_top);
            startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.PUBLISH));
            return true;
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            this.overridePendingTransition(
                    R.anim.slide_in_top, R.anim.do_nix);
            return true;
        case R.id.profile:
            showFragment(new ProfileFragment(), "profile",
                        R.anim.slide_in_top, R.anim.slide_out_top);
            return true;
        case android.R.id.home:
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else {
                showFragment(new AboutFragment(), "about",
                        R.anim.slide_in_left, R.anim.slide_out_left);
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showFragment(Fragment fragment, String name,
            int anim_in, int anim_out) {
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
                anim_in, R.anim.do_nix,
                R.anim.do_nix, anim_out)
            .replace(R.id.container, fragment, name)
            .addToBackStack(name)
        .commitAllowingStateLoss();
    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("loader reset " + loader.getId());
        results.swapCursor(null);
        details.swapCursor(null);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}
}
