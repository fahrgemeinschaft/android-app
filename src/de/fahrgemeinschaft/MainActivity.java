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

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity
        implements OnClickListener, OnDateSetListener {

    protected static final String TAG = "fahrgemeinschaft";
    private static final int FROM = 42;
    private static final int TO = 55;
    private Button from_btn;
    private Button to_btn;
    private int from_id;
    private int to_id;
    private long dep;
    private View selberfahren_btn;
    private View mitfahren_btn;
    private Button when_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        getSupportActionBar().setHomeButtonEnabled(true);

        from_btn = (Button) findViewById(R.id.btn_autocomplete_from);
        to_btn = (Button) findViewById(R.id.btn_autocomplete_to);
        when_btn = (Button) findViewById(R.id.btn_datepicker);
        selberfahren_btn = findViewById(R.id.btn_selberfahren);
        mitfahren_btn = findViewById(R.id.btn_mitfahren);

        to_btn.setOnClickListener(this);
        from_btn.setOnClickListener(this);
        mitfahren_btn.setOnClickListener(this);
        selberfahren_btn.setOnClickListener(this);
        findViewById(R.id.btn_pick_to).setOnClickListener(this);
        findViewById(R.id.btn_pick_from).setOnClickListener(this);

        dep = System.currentTimeMillis();
        if (savedInstanceState != null) {
            setFromButtonText(Uri.parse("content://de.fahrgemeinschaft/places/"
                    + savedInstanceState.getInt("from_id")));
            setToButtonText(Uri.parse("content://de.fahrgemeinschaft/places/"
                    + savedInstanceState.getInt("to_id")));
            setDateButtonText(savedInstanceState.getLong("dep"), -1);
        }
        setTitle("");
//        startActivity(new Intent(Intent.ACTION_VIEW,
//                Uri.parse("content://" + getPackageName() + "/rides" +
//                        "?from_id=1&to_id=2")));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_pick_from:
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    Uri.parse("content://de.fahrgemeinschaft/places")), FROM);
            overridePendingTransition(R.anim.slide_in_left, R.anim.do_nix);
            break;

        case R.id.btn_pick_to:
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    Uri.parse("content://de.fahrgemeinschaft/places"
                            + "?from_id=" + from_id)), TO);
            overridePendingTransition(R.anim.slide_in_right, R.anim.do_nix);
            break;

        case R.id.btn_autocomplete_from:
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    Uri.parse("content://de.fahrgemeinschaft/places"))
                .putExtra("show_textfield", true), FROM);
            overridePendingTransition(R.anim.slide_in_left, R.anim.do_nix);
            break;

        case R.id.btn_autocomplete_to:
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    Uri.parse("content://de.fahrgemeinschaft/places"
                            + "?from_id=" + from_id))
                .putExtra("show_textfield", true), TO);
            overridePendingTransition(R.anim.slide_in_right, R.anim.do_nix);
            break;

        case R.id.btn_mitfahren:
            if (from_id != 0 && to_id != 0) {
                new Ride()
                    .type(Ride.SEARCH)
                    .from(from_id)
                    .to(to_id)
                    .dep(dep)
                    .arr(new Date(System.currentTimeMillis() + 2*24*3600*1000))
                .store(this);
                // Toast.makeText(this, "yay", 200).show();
                startService(new Intent(this, ConnectorService.class)
                        .setAction(ConnectorService.SEARCH));
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("content://" + getPackageName() + "/rides"
                                + "?from_id=" + from_id
                                + "&to_id=" + to_id
                                + "&dep=" + dep)));
            }
            break;
        case R.id.btn_selberfahren:
            startActivity(new Intent(this, EditRideActivity.class));
        }
    }

    public void showTimePickerDialog(View v) {
        final Calendar c = Calendar.getInstance();
        c.setTime(new Date(dep));
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog d = new DatePickerDialog(this, this, year, month, day);
        d.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ready),
                (android.content.DialogInterface.OnClickListener) null);
        d.show();
    }

    @Override
    public void onDateSet(DatePicker picker, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        
        // If day is today ignore the following
           cal.set(Calendar.HOUR_OF_DAY, 00);
           cal.set(Calendar.MINUTE, 00);
           cal.set(Calendar.SECOND, 00);
        // end
        
        dep = cal.getTime().getTime();
        setDateButtonText(dep, cal.get(Calendar.DAY_OF_YEAR));
    }

    @Override
    protected void onActivityResult(int req, int res, final Intent intent) {
        if (res == RESULT_OK) {
            Log.d(TAG, "selected " + intent.getData());
            switch (req) {
            case FROM:
                setFromButtonText(intent.getData());
                break;
            case TO:
                setToButtonText(intent.getData());
                break;
            }
        }
    }

    public void setDateButtonText(long date, int dayOfYear) {
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_YEAR);
        if (dayOfYear == -1 || dayOfYear == today)
            when_btn.setText(getString(R.string.now));
        else if (dayOfYear == today + 1)
            when_btn.setText(getString(R.string.tomorrow));
        else if (dayOfYear == today + 2)
            when_btn.setText(getString(R.string.after_tomorrow));
        else
            when_btn.setText(new SimpleDateFormat("dd. MMM yyyy",
                    Locale.GERMANY).format(date));
    }

    private void setFromButtonText(Uri uri) {
        Cursor place = getContentResolver().query(uri, null, null, null, null);
        if (place.getCount() > 0) {
            place.moveToFirst();
            animatePulse(from_btn);
            from_id = place.getInt(0);
            from_btn.setText(place.getString(2));
            from_btn.setTextAppearance(this, R.style.white_button_text);
        }
        place.close();
    }

    private void setToButtonText(Uri uri) {
        Cursor place = getContentResolver().query(uri, null, null, null, null);
        if (place.getCount() > 0) {
            place.moveToFirst();
            animatePulse(to_btn);
            to_id = place.getInt(0);
            to_btn.setText(place.getString(2));
            to_btn.setTextAppearance(this, R.style.white_button_text);
        }
        place.close();
    }

    private void animatePulse(final View view) {
        Animation fade_in = new AlphaAnimation(0.3f, 1f);
        fade_in.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setBackgroundResource(android.R.color.white);
                Animation fade_out = new AlphaAnimation(1f, 0.7f);
                fade_out.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setBackgroundResource(R.drawable.btn_white);
                    }
                });
                fade_out.setDuration(1400);
                view.startAnimation(fade_out);
            }
        });
        fade_in.setDuration(190);
        view.startAnimation(fade_in);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        case android.R.id.home:
            getSupportFragmentManager().beginTransaction()
            .replace(R.id.layout, new AboutFragment())
            .addToBackStack("")
            .commit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("from_id", from_id);
        outState.putInt("to_id", to_id);
        outState.putLong("dep", dep);
    }

}
