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
        main = (MainFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main);
        if (results == null)
            results = new RideListFragment();
        if (details == null)
            details = new RideDetailsFragment();
        handleIntent(getIntent().getData());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent.getData());
    }

    private void handleIntent(Uri uri) {
        System.out.println("intent " + uri);
        if (uri != null) {
            setIntent(getIntent().setData(uri));
            getSupportLoaderManager().restartLoader(0, null, this);
            if (uri.equals(MY_RIDES_URI)) {
                setTitle(R.string.my_rides);
                startService(new Intent(this, ConnectorService.class)
                        .setAction(ConnectorService.PUBLISH));
                results.setSpinningEnabled(false);
                showFragment(results);
            } else if (uri.getLastPathSegment().equals("rides")) {
                results.setSpinningEnabled(true);
                setTitle(R.string.results);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (main.ride.getFrom() == null || main.ride.getTo() == null) {
            Toast.makeText(this, getString(R.string.uncomplete),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Ride r = main.ride;
        switch (v.getId()) {
        case R.id.btn_selberfahren:
            long now = System.currentTimeMillis();
            Uri uri = r.type(Ride.OFFER)
                    .dep(r.getDep() < now? now + 3600000 : r.getDep())
                    .mode(Ride.Mode.CAR).seats(3).store(this);
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
            break;
        case R.id.btn_mitfahren:
            r.type(Ride.SEARCH).arr(r.getDep() +2*24*3600*1000)
                .store(this);
            startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
            setIntent(getIntent().setData(Uri.parse("content://de.fahrgemeinschaft/rides"
                    + "?from_id=" + r.getFromId()
                    + "&to_id=" + r.getToId()
                    + "&dep=" + r.getDep())));
            handleIntent(getIntent().getData());
            showFragment(results);
            break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle b) {
        return new CursorLoader(this,
                getIntent().getData(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor rides) {
        System.out.println("swap cursors");
        results.swapCursor(rides);
        details.swapCursor(rides);
        if (rides.getCount() > 0) {
            rides.moveToLast();
            long latest_dep = rides.getLong(COLUMNS.DEPARTURE);
            System.out.println("ALREADY in CACHE until "
                    + new SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY)
                    .format(new Date(latest_dep)));
            if (latest_dep > main.ride.getArr()) {// inc time window
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(latest_dep);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                main.ride.arr(cal.getTimeInMillis()).store(this);
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
        details.setSelection(position);
        showFragment(details);
    }

    @Override
    public void onPageSelected(final int position) {
//        results.getListView().setSelection(position);
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
            handleIntent(MY_RIDES_URI);
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
            else {
                showFragment(new AboutFragment());
            }
            return true;
        }
        return false;
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        if (!fragment.equals(results) && !fragment.equals(details))
            fm.popBackStackImmediate();
        fm.beginTransaction()
            .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.do_nix,
                    R.anim.do_nix, R.anim.slide_out_right)
                .replace(R.id.container, fragment, null)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {}

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}
}
