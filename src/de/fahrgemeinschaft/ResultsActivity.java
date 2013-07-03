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

import de.fahrgemeinschaft.EndlessSpinningZebraListFragment.ListFragmentCallback;

public class ResultsActivity extends SherlockFragmentActivity implements
        LoaderCallbacks<Cursor>, ListFragmentCallback, OnPageChangeListener {

    public RideDetailsFragment details;
    public RideListFragment myrides;
    public RideListFragment results;
    private Ride query;
    private Uri jobs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_results);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        results = (RideListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.rides);
        details = new RideDetailsFragment();
        myrides = new RideListFragment();
        query = new Ride(getIntent().getData());
        
        results.load(getIntent().getData());
        
        jobs = Uri.parse("content://" + getPackageName() + "/jobs/search");
        getContentResolver().registerContentObserver(jobs, false,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        System.out.println("CHANGE " + selfChange);
                        getSupportLoaderManager()
                            .restartLoader(0, null, ResultsActivity.this);
                    }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        return new CursorLoader(this, jobs, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor jobs) {
        if (jobs.getCount() != 0) {
            System.out.println("START   SPINNING");
            results.startSpinning();
            //TODO visualize ...
        } else {
            System.out.println("STOP   SPINNING");
            results.stopSpinning();
        }
    }

    @Override
    public void onLoadFinished(Fragment fragment, Cursor cursor) {
        switch (fragment.getId()) {
        case R.id.rides:
            setTitle("Results");
            details.swapCursor(cursor);
            break;
        }
    }

    @Override
    public void onListItemClick(int position) {
        onPageSelected(position);
        showFragment(details);
    }

    @Override
    public void onPageSelected(final int position) {
        results.getListView().setSelection(position);
        details.setSelection(position);
    }

    @Override
    public void onSpinningWheelClick() {
        query.arr(query.getArr() + 2 * 24 * 3600 * 1000).store(this);
        startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
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
