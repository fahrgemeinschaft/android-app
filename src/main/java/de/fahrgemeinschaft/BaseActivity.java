/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.ConnectorService;
import org.teleportr.ConnectorService.ServiceCallback;
import org.teleportr.Ride.COLUMNS;
import org.teleportr.RidesProvider;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.ContactProvider.CONTACT;
import de.fahrgemeinschaft.util.SpinningZebraListFragment.ListFragmentCallback;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class BaseActivity extends SherlockFragmentActivity
       implements ListFragmentCallback,
       OnPageChangeListener, ServiceCallback<String>,
       ServiceConnection, OnBackStackChangedListener {

    public static final String TAG = "Fahrgemeinschaft";
    public static final int MYRIDES = 1;
    public static final int DETAILS = 2;
    public static final int PROFILE = 3;
    public static final int ABOUT = 112;
    public static final int SEARCH = 0;
    private RideDetailsFragment mydetails;
    private RideListFragment myrides;
    public ConnectorService service;
    private MenuItem ic_myrides;
    public MenuItem ic_profile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        onBackStackChanged();
        myrides = (RideListFragment) getSupportFragmentManager()
                    .findFragmentByTag(getString(R.string.myrides));
        mydetails = (RideDetailsFragment) getSupportFragmentManager()
                    .findFragmentByTag(getString(R.string.mydetails));
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
    }


    @Override
    public void onListItemClick(int position, int fragment) {
        if (fragment == MYRIDES) {
            if (mydetails == null) mydetails = new RideDetailsFragment();
            showFragment(mydetails, getString(R.string.mydetails),
                    R.anim.slide_in_right, R.anim.slide_out_right);
            mydetails.setTargetFragment(myrides, MYRIDES);
            mydetails.setSelection(position);
        }
    }

    @Override
    public void onSpinningWheelClick() {}

    @Override
    public void onPageSelected(final int position) {
        System.out.println("selected " + position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.action_bar, menu);
        ic_profile = menu.findItem(R.id.profile);
        setProfileIcon();
        ic_myrides = menu.findItem(R.id.myrides);
        return super.onCreateOptionsMenu(menu);
    }

    public void setProfileIcon() {
        if (ic_profile == null) return;
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getString("auth", null) != null) {
            ic_profile.setIcon(R.drawable.ic_topmenu_user_ok);
        } else {
            ic_profile.setIcon(R.drawable.ic_topmenu_user);
        }
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
            this.overridePendingTransition(R.anim.slide_in_top, R.anim.do_nix);
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
        showFragment(new ProfileFragment(), getString(R.string.profile),
                R.anim.slide_in_top, R.anim.slide_out_top);
    }

    public void showMyRides() {
        if (myrides == null) {
            myrides = new RideListFragment();
            myrides.setSpinningEnabled(false);
        }
        myrides.load(RidesProvider.getMyRidesUri(this), MYRIDES);
        showFragment(myrides, getString(R.string.myrides),
                R.anim.slide_in_top, R.anim.slide_out_top);
        startService(new Intent(this, ConnectorService.class)
            .setAction(ConnectorService.PUBLISH));
    }

    protected void showFragment(Fragment f, String name, int in, int out) {
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
            .add(R.id.container, f, name)
            .addToBackStack(name)
        .commit();
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fm = getSupportFragmentManager();
        int backstack = fm.getBackStackEntryCount();
        if (fm.getBackStackEntryCount() == 0) {
            setTitle("");
        } else {
            setTitle(fm.getBackStackEntryAt(backstack - 1).getName());
        }
    }

    @Override
    public void onProgress(String what, int how) {
        if (what.equals(ConnectorService.MYRIDES) && ic_myrides != null) {
            ic_myrides.setActionView(R.layout.view_progress);
        } else if (what.equals(ConnectorService.AUTH) && ic_profile != null) {
            ic_profile.setActionView(R.layout.view_progress);
        }
    }

    @Override
    public void onFail(String what, String reason) {
        if (reason.equals("wrong login or password")) {
            Crouton.makeText(this, getString(R.string.auth_error), Style.ALERT).show();
        } else {
            Crouton.makeText(this, getString(R.string.auth_no_network), Style.ALERT).show();
        }
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
        if (what.equals(ConnectorService.MYRIDES) && ic_myrides != null) {
            ic_myrides.setActionView(null);
            if (PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(ProfileFragment.INIT_CONTACTS, false)) {
                new GetContactsFromMyridesTask().execute(new String[]{});
            }
        } else if (what.equals(ConnectorService.AUTH) & ic_profile != null) {
            Crouton.makeText(this, getString(R.string.auth_success), Style.CONFIRM)
                    .show();
            ic_profile.setActionView(null);
            startService(new Intent(this, ConnectorService.class)
                    .setAction(ConnectorService.SEARCH));
            ((NotificationManager) getSystemService(Context
                    .NOTIFICATION_SERVICE)).cancel(42);
        }
        setProfileIcon();
    }

    class GetContactsFromMyridesTask extends AsyncTask<String, String, String> {

        private static final String EMPTY = "";

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "getting contacts from myrides");
            Cursor mr =  getContentResolver().query(RidesProvider
                            .getMyRidesUri(BaseActivity.this),
                                null, null, null, null);
            System.out.println("myrides: " + mr.getCount());
                for (int i = 0; i < mr.getCount(); i++) {
                    mr.moveToPosition(i);
                    try {
                        storeContacts(new JSONObject(
                            mr.getString(COLUMNS.DETAILS)));
                    } catch (JSONException e) {
                        Log.e(TAG, "error getting myride details");
                        e.printStackTrace();
                    }
                }
                PreferenceManager.getDefaultSharedPreferences(BaseActivity.this)
                        .edit().remove(ProfileFragment.INIT_CONTACTS).commit();
                Log.d(TAG, "got contacts from myrides");
            return null;
        }

        public void storeContacts(JSONObject details) throws JSONException {
            ContentValues cv = new ContentValues();
            if (details.has(CONTACT.EMAIL))
                cv.put(CONTACT.EMAIL, details.getString(CONTACT.EMAIL));
            if (details.has(CONTACT.MOBILE))
                cv.put(CONTACT.MOBILE, details.getString(CONTACT.MOBILE));
            if (details.has(CONTACT.LANDLINE))
                cv.put(CONTACT.LANDLINE, details.getString(CONTACT.LANDLINE));
            if (details.has(CONTACT.PLATE))
                cv.put(CONTACT.PLATE, details.getString(CONTACT.PLATE));
            cv.put(CONTACT.USER, PreferenceManager
                    .getDefaultSharedPreferences(BaseActivity.this)
                    .getString(CONTACT.USER, EMPTY));
            getContentResolver().insert(Uri.parse(ContactProvider.URI), cv);
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

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}
}
