/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class EditRideActivity extends SherlockFragmentActivity
        implements LoaderCallbacks<Cursor> {

    private static final String TAG = "Results";
    protected static final int RIDE = -1;
    private Uri uri;
    int selected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create activity");
        setContentView(R.layout.activity_ride_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        list = (RideListFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.rides);

        uri = getIntent().getData();
//        getSupportLoaderManager().initLoader(RIDE, null, this);
        if (savedInstanceState != null) {
            selected = savedInstanceState.getInt("selected");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor rides) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        Log.d(TAG, "onLoaderReset");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("selected", selected);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            return true;
        
        default:
            return super.onOptionsItemSelected(item);
        }
    }

}
