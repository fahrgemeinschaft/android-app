/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.util.Date;

import org.teleportr.ConnectorService;
import org.teleportr.Ride;
import org.teleportr.RidesProvider;

import android.content.ContentValues;
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

import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.ContactProvider.CONTACT;
import de.fahrgemeinschaft.util.PrivacyImageButton;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class EditRideActivity extends BaseActivity
        implements LoaderCallbacks<Cursor>, OnClickListener {

    private static final String RIDE = "ride";
    public Ride ride;
    public EditRideFragment3 f3;
    public EditRideFragment2 f2;
    public EditRideFragment1 f1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_edit);
        setTitle(R.string.offer);
        f1 = (EditRideFragment1) getSupportFragmentManager()
                .findFragmentById(R.id.fragment1);
        f2 = (EditRideFragment2) getSupportFragmentManager()
                .findFragmentById(R.id.fragment2);
        f3 = (EditRideFragment3) getSupportFragmentManager()
                .findFragmentById(R.id.fragment3);
        findViewById(R.id.publish).setOnClickListener(this);
        if (savedInstanceState != null) {
            ride = savedInstanceState.getParcelable(RIDE);
            ride.setContext(this);
            setRide();
        } else {
            if (getIntent().getData() != null) {
                getSupportLoaderManager().initLoader(0, null, this);
            } else {
                ride = new Ride(this).type(Ride.OFFER).dep(new Date());
                setRide();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        Uri uri = getIntent().getData();
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        System.out.println("got " + cursor.getCount());
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            ride = new Ride(cursor, this);
            setRide();
        }
        cursor.close();
    }

    private void setRide() {
        if (ride != null) {
            if (ride.getRef() != null) {
                setTitle(R.string.edit);
            }
            f1.setRide(ride);
            f2.setRide(ride);
            f3.setRide(ride);
            ((EditText)findViewById(R.id.comment)).setText(ride.get("Comment"));
            long delta = ride.getDep() - System.currentTimeMillis();
            if (delta < 0) {
                delta = delta % 86400000;
                ride.dep(System.currentTimeMillis() + 86400000 + delta);
                f2.openDatePicker();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            overridePendingTransition(
                    R.anim.slide_in_top, R.anim.slide_out_bottom);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        if (ride.getFrom() == null || ride.getTo() == null) {
            Crouton.makeText(this, getString(R.string.incomplete), Style.INFO)
                .show();
        } else {
            ride.set("Comment", ((EditText) findViewById(
                    R.id.comment)).getText().toString());
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            if (!prefs.contains("Email")) {
                prefs.edit().putString("Email", ((PrivacyImageButton)
                        findViewById(R.id.email)).text.getText().toString())
                        .commit();
            }
            ride.marked().dirty().store(this);
            startService(new Intent(this, ConnectorService.class)
                    .setAction(ConnectorService.PUBLISH));
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .setData(RidesProvider.getMyRidesUri(this)));
            ContentValues cv = new ContentValues();
            cv.put(CONTACT.USER, prefs.getString(CONTACT.USER, ""));
            cv.put(CONTACT.EMAIL, ride.get(CONTACT.EMAIL));
            cv.put(CONTACT.MOBILE, ride.get(CONTACT.MOBILE));
            cv.put(CONTACT.LANDLINE, ride.get(CONTACT.LANDLINE));
            cv.put(CONTACT.PLATE, ride.get(CONTACT.PLATE));
            getContentResolver().insert(Uri.parse(
                    "content://de.fahrgemeinschaft.private/contacts"), cv);
            Toast.makeText(this, getString(R.string.stored), Toast.LENGTH_SHORT)
                    .show();
            overridePendingTransition(
                    R.anim.slide_in_top, R.anim.slide_out_bottom);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ride.set("Comment", ((EditText) findViewById(
                R.id.comment)).getText().toString());
        outState.putParcelable(RIDE, ride);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
