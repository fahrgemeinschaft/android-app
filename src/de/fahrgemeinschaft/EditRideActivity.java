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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.util.EditTextPrivacyButton;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class EditRideActivity extends SherlockFragmentActivity
        implements LoaderCallbacks<Cursor>, OnClickListener {

    public Ride ride;
    public EditRideFragment3 f3;
    public EditRideFragment2 f2;
    public EditRideFragment1 f1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            ride = savedInstanceState.getParcelable("ride");
            ride.setContext(this);
        } else {
            ride = new Ride(this).type(Ride.OFFER);
            if (getIntent().getData() != null) {
                getSupportLoaderManager().initLoader(0, null, this);
            }
        }
        initFragments();
        findViewById(R.id.publish).setOnClickListener(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        Uri uri = getIntent().getData();
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            ride = new Ride(cursor, this);
        }
        initFragments();
    }

    private void initFragments() {
        f1 = (EditRideFragment1) getSupportFragmentManager()
                .findFragmentById(R.id.fragment1);
        f2 = (EditRideFragment2) getSupportFragmentManager()
                .findFragmentById(R.id.fragment2);
        f3 = (EditRideFragment3) getSupportFragmentManager()
                .findFragmentById(R.id.fragment3);
        if (ride != null) {
            f1.setRide(ride);
            f2.setRide(ride);
            f3.setRide(ride);
            ((EditText)findViewById(R.id.comment)).setText(ride.get("Comment"));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
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
            startActivity(new Intent(this, MainActivity.class)
                .setData(MainActivity.MY_RIDES_URI));
            break;
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            break;
        case R.id.profile:
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ProfileFragment())
                .addToBackStack("").commit();
            break;
        case android.R.id.home:
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else startActivity(new Intent(this, MainActivity.class));
            break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (ride.getFrom() == null || ride.getTo() == null) {
            Crouton.makeText(this, getString(R.string.uncomplete), Style.INFO)
                .show();
        } else {
            ride.set("Comment", ((EditText) findViewById(
                    R.id.comment)).getText().toString());
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            if (!prefs.contains("EMail")) {
                prefs.edit().putString("EMail", ((EditTextPrivacyButton)
                        findViewById(R.id.email)).text.getText().toString())
                        .commit();
            }
            ride.marked().dirty().store(this);
            startService(new Intent(this, ConnectorService.class)
                    .setAction(ConnectorService.PUBLISH));
            startActivity(new Intent(this, MainActivity.class)
                    .setData(MainActivity.MY_RIDES_URI));
            Toast.makeText(this, getString(R.string.stored), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ride.set("Comment", ((EditText) findViewById(
                R.id.comment)).getText().toString());
        outState.putParcelable("ride", ride);
        super.onSaveInstanceState(outState);
    }
}
