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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    protected static final String TAG = "fahrgemeinschaft";
    private static final int FROM = 42;
    private static final int TO = 55;
    private Button from_btn;
    private Button to_btn;
    private int from_id;
    private int to_id;
    private View selberfahren_btn;
    private View mitfahren_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        from_btn = (Button) findViewById(R.id.btn_autocomplete_from);
        to_btn = (Button) findViewById(R.id.btn_autocomplete_to);
        selberfahren_btn = findViewById(R.id.btn_selberfahren);
        mitfahren_btn = findViewById(R.id.btn_mitfahren);

        to_btn.setOnClickListener(this);
        from_btn.setOnClickListener(this);
        mitfahren_btn.setOnClickListener(this);
        selberfahren_btn.setOnClickListener(this);
        findViewById(R.id.btn_pick_to).setOnClickListener(this);
        findViewById(R.id.btn_pick_from).setOnClickListener(this);

        if (savedInstanceState != null) {
            setFromButtonText(Uri.parse("content://de.fahrgemeinschaft/places/"
                    + savedInstanceState.getInt("from_id")));
            setToButtonText(Uri.parse("content://de.fahrgemeinschaft/places/"
                    + savedInstanceState.getInt("to_id")));
        }
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
                    .dep(new Date())
                    .store(this);
                // Toast.makeText(this, "yay", 200).show();
                startService(new Intent(this, ConnectorService.class)
                .setAction(ConnectorService.SEARCH));
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("content://" + getPackageName() + "/rides" +
                                "?from_id=" + from_id + "&to_id=" + to_id)));
            }
            break;
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("from_id", from_id);
        outState.putInt("to_id", to_id);
    }

}
