/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;
import org.teleportr.ConnectorService.BackgroundListener;
import org.teleportr.Ride;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.CursorAdapter;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.fahrgemeinschaft.EndlessSpinningZebraListFragment.ListFragmentCallback;

public class ResultsActivity extends SherlockFragmentActivity
       implements ServiceConnection, BackgroundListener,
           ListFragmentCallback, OnPageChangeListener {

    public static final Uri MY_RIDES_URI =
            Uri.parse("content://de.fahrgemeinschaft/myrides");
    public static final Uri BG_JOBS_URI =
            Uri.parse("content://de.fahrgemeinschaft/jobs/search");
    public RideDetailsFragment details;
    public RideListFragment results;
    public RideListFragment myrides;
    private Ride query;

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
    }

    @Override
    protected void onStart() {
        bindService(new Intent(this, ConnectorService.class), this, 0);
        super.onStart();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ((ConnectorService.Bind) service).getService().register(this);
    }

    @Override
    public void on(String what, final short how) {
        switch (how) {
        case ConnectorService.BackgroundListener.START:
            results.startSpinning();
            break;
        case ConnectorService.BackgroundListener.PAUSE:
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
    protected void onPause() {
        unbindService(this);
        super.onPause();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {}

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}
}
