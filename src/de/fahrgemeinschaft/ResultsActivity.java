/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;
import org.teleportr.Ride;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.CursorAdapter;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.EndlessSpinningZebraListFragment.ListItemClicker;

public class ResultsActivity extends SherlockFragmentActivity implements
        LoaderCallbacks<Cursor>, ListItemClicker, OnPageChangeListener {

    private static final int RESULTS = 1;
    private static final int MYRIDES = 2;
    private static final int SERVICE = 3;
    public RideDetailsFragment details;
    public RideListFragment myrides;
    public RideListFragment list;
    private Ride query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_results);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        list = (RideListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.rides);
        details = new RideDetailsFragment();
        myrides = new RideListFragment();
        query = new Ride(getIntent().getData());
        getSupportLoaderManager().initLoader(RESULTS, null, this);
        getContentResolver().registerContentObserver(Uri.parse(
                    "content://" + getPackageName() + "/jobs/search"), false,
                    new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    System.out.println("CHANGE " + selfChange);
                    getSupportLoaderManager()
                        .restartLoader(SERVICE, null, ResultsActivity.this);
                }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
        case RESULTS:
            Uri uri = getIntent().getData();
            System.out.println("query rides:   " + uri);
            return new CursorLoader(this, uri, null, null, null, null);
        case MYRIDES:
            System.out.println("query my rides");
            return new CursorLoader(this, Uri.parse(
                    "content://" + getPackageName() + "/myrides"),
                    null, null, null, null);
        case SERVICE:
            return new CursorLoader(this, Uri.parse(
                    "content://" + getPackageName() + "/jobs/search"),
                    null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
        case MYRIDES:
            setTitle("My Rides");
            myrides.swapCursor(cursor);
            break;
        case RESULTS:
            list.swapCursor(cursor);
            details.swapCursor(cursor);
            break;
        case SERVICE: // background search jobs
            if (cursor.getCount() != 0) {
                System.out.println("START   SPINNING");
                list.startSpinning();
                //TODO visualize ...
            } else {
                System.out.println("STOP   SPINNING");
                list.stopSpinning();
            }
        }
    }

    @Override
    public void onListItemClick(int position) {
        onPageSelected(position);
        showFragment(details);
    }

    @Override
    public void onPageSelected(final int position) {
        list.getListView().setSelection(position);
        details.setSelection(position);
    }

    @Override
    public void onSpinningWheelClick() {
        query.arr(query.getArr() + 2 * 24 * 3600 * 1000).store(this);
        startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
    }


    public void contact(View v) {
        Cursor cursor = ((CursorAdapter) list.getListAdapter()).getCursor();
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
            getSupportLoaderManager().initLoader(MYRIDES, null, this);
            return true;
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        case R.id.profile:
            startActivity(new Intent(this, SettingsActivity.class)
                    .putExtra("profile", true));
            return true;
        case android.R.id.home:
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else finish();
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
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
