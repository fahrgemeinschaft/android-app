/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;
import org.teleportr.ConnectorService.ServiceCallback;
import org.teleportr.RidesProvider;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.util.SpinningZebraListFragment.ListFragmentCallback;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class BaseActivity extends SherlockFragmentActivity
       implements ListFragmentCallback,
       OnPageChangeListener, ServiceCallback<String>,
       ServiceConnection, OnBackStackChangedListener {

    private static final String SUCCESS = " success.";
    private static final String FAIL = " fail: ";
    private MenuItem ic_profile;
    private MenuItem ic_myrides;
    public ConnectorService service;
    private RideListFragment myrides;
    private RideDetailsFragment mydetails;
    public static final String TAG = "Fahrgemeinschaft";
    public static final String AUTHORITY = "de.fahrgemeinschaft";
    public static final int SEARCH = 0;
    public static final int MYRIDES = 1;
    public static final int DETAILS = 2;
    public static final int PROFILE = 3;
    public static final int ABOUT = 112;

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
            Crouton.makeText(this, what, Style.INFO).show();
            ic_profile.setActionView(R.layout.view_progress);
        }
    }

    @Override
    public void onFail(String what, String reason) {
        Crouton.makeText(this, what + FAIL + reason, Style.ALERT).show();
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
        Crouton.makeText(this, what + SUCCESS, Style.CONFIRM).show();
        if (what.equals(ConnectorService.MYRIDES) && ic_myrides != null) {
            ic_myrides.setActionView(null);
        } else if (what.equals(ConnectorService.AUTH) & ic_profile != null) {
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

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}
}
