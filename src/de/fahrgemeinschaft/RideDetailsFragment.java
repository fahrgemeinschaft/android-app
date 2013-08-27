/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Ride;
import org.teleportr.Ride.COLUMNS;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.calciumion.widget.BasePagerAdapter;

import de.fahrgemeinschaft.util.ProfileRequest;
import de.fahrgemeinschaft.util.ReoccuringWeekDaysView;
import de.fahrgemeinschaft.util.RideRowView;
import de.fahrgemeinschaft.util.Util;

public class RideDetailsFragment extends SherlockFragment
        implements Response.ErrorListener, OnPageChangeListener {

    private static final String TAG = "Details";
    private static final SimpleDateFormat lrdate =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
    private static final SimpleDateFormat lwdate =
            new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    private static final SimpleDateFormat lwhdate =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
    private ViewPager pager;
    private RequestQueue queue;
    public Cursor cursor;
    private int selected;
    private MenuItem edit;
    private MenuItem delete;
    private MenuItem duplicate;
    private MenuItem duplicate_retour;
    private MenuItem toggle_active;
    private static ImageLoader imageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        queue = Volley.newRequestQueue(getActivity());
        imageLoader = new ImageLoader(queue, ProfileRequest.imageCache);
        queue.start();
        if (savedInstanceState != null) {
            selected = savedInstanceState.getInt("selected");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_details, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle savedInstanceState) {
        super.onViewCreated(layout, savedInstanceState);
        pager = (ViewPager) layout.findViewById(R.id.pager);
        pager.setAdapter(new BasePagerAdapter() {

            @Override
            public int getCount() {
                if (cursor == null)
                    return 0;
                else
                    return cursor.getCount();
            }

            @Override
            protected View getView(Object position, View v, ViewGroup parent) {
                if (v == null) {
                    v = getActivity().getLayoutInflater()
                            .inflate(R.layout.view_ride_details, null, false);
                }
                if (cursor.isClosed()) return v;
                RideView view = (RideView) v;
                
                if (view.content.getChildCount() > 5)
                    view.content.removeViews(1, view.content.getChildCount()-5);
                cursor.moveToPosition((Integer) position);

                view.url = null;

                view.from_place.setText(cursor.getString(COLUMNS.FROM_ADDRESS));
                view.to_place.setText(cursor.getString(COLUMNS.TO_ADDRESS));

                view.row.bind(cursor, getActivity());
                view.reoccur.setDays(Ride.getDetails(cursor));
                if (view.reoccur.isReoccuring())
                    view.reoccur.setVisibility(View.VISIBLE);
                else view.reoccur.setVisibility(View.GONE);

                try {
                    view.details.setText(
                            Ride.getDetails(cursor).getString("Comment"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getActivity().getSupportLoaderManager()
                    .initLoader((int) cursor.getLong(0), null, view);

                view.avatar.setImageResource(R.drawable.icn_view_user);
                view.name.setText("");
                view.last_login.setText("");
                view.reg_date.setText("");
                view.name.setVisibility(View.GONE);
                view.last_login.setVisibility(View.GONE);
                view.reg_date.setVisibility(View.GONE);

                if (isMyRide(cursor)) {
                    if (cursor.getLong(COLUMNS.DEPARTURE)
                            - System.currentTimeMillis() > 0  // future ride
                            && (cursor.getInt(COLUMNS.ACTIVE) == 1)) {
                        view.streifenhoernchen.setVisibility(View.GONE);
                    } else {
                        view.streifenhoernchen.setVisibility(View.VISIBLE);
                    }
                } else {
                    view.streifenhoernchen.setVisibility(View.GONE);
                }

                if (cursor.getString(COLUMNS.WHO).equals("")) {
                    String user = PreferenceManager.getDefaultSharedPreferences(
                            getActivity()).getString("user", "");
                    queue.add(new ProfileRequest(user,
                            view, RideDetailsFragment.this));
                    view.userId = user;
                } else {
                    queue.add(new ProfileRequest(cursor.getString(COLUMNS.WHO),
                            view, RideDetailsFragment.this));
                    view.userId = cursor.getString(COLUMNS.WHO);
                }
                view.visible = Util.isVisible("Name", Ride.getDetails(cursor));
                return view;
            }

            @Override
            protected Object getItem(int position) {
                return position;
            }
        });
    }

    public void setSelection(int position) {
        selected = position;
        updateOptionsMenu();
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        updateOptionsMenu();
        if (pager != null) {
            pager.setCurrentItem(selected);
            pager.getAdapter().notifyDataSetChanged();
        }
    }

    public int getSelection() {
        return selected;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("selected", selected);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        pager.setCurrentItem(selected);
        pager.setOnPageChangeListener(this);
        updateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getSherlockActivity().getSupportMenuInflater()
                .inflate(R.menu.ride_actions, menu);
        edit = menu.findItem(R.id.edit);
        delete = menu.findItem(R.id.delete);
        duplicate = menu.findItem(R.id.duplicate);
        duplicate_retour = menu.findItem(R.id.duplicate_retour);
        toggle_active = menu.findItem(R.id.toggle_active);
        updateOptionsMenu();
    }

    @Override
    public void onPageSelected(int position) {
        selected = position;
        if (getActivity() != null)
            ((OnPageChangeListener) getActivity()).onPageSelected(position);
        updateOptionsMenu();
    }

    private void updateOptionsMenu() {
        if (cursor != null && cursor.getCount() >= selected
                && edit != null && getActivity() != null) {
            cursor.moveToPosition(selected);
            if (isMyRide()) {
                edit.setVisible(true);
                delete.setVisible(true);
                duplicate.setVisible(true);
                duplicate_retour.setVisible(true);
                if (cursor.getLong(COLUMNS.DEPARTURE)
                        - System.currentTimeMillis() > 0) { // future ride
                    toggle_active.setVisible(true);
                    if (cursor.getInt(COLUMNS.ACTIVE) == 1) {
                        toggle_active.setTitle(R.string.deactivate);
                    } else {
                        toggle_active.setTitle(R.string.activate);
                    }
                } else { // past ride
                    toggle_active.setVisible(false);
                }
            } else {
                edit.setVisible(false);
                delete.setVisible(false);
                duplicate.setVisible(false);
                duplicate_retour.setVisible(false);
                toggle_active.setVisible(false);
            }
        }
    }

    private boolean isMyRide() {
        return cursor.getString(COLUMNS.WHO).equals("") ||
                cursor.getString(COLUMNS.WHO).equals(PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("user", ""));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        cursor.moveToPosition(selected);
        Ride ride = new Ride(cursor, getActivity());
        if (item.getItemId() == R.id.delete) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        return Util.handleRideAction(item.getItemId(), ride, getActivity());
    }



    static class RideView extends RelativeLayout
        implements LoaderCallbacks<Cursor>, Response.Listener<JSONObject> {

        LinearLayout driver_info;
        String url;
        RideRowView row;
        String userId;
        TextView name;
        boolean visible;
        ImageView avatar;
        TextView reg_date;
        TextView last_login;
        LinearLayout content;
        TextView from_place;
        TextView to_place;
        TextView details;
        View streifenhoernchen;
        ReoccuringWeekDaysView reoccur;
        ProgressBar name_loading;

        public RideView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            from_place = (TextView)
                    ((FrameLayout) findViewById(R.id.from_place)).getChildAt(1);
            FrameLayout to = (FrameLayout) findViewById(R.id.to_place);
            to_place = (TextView) to.getChildAt(1);
            ((ImageView)to.getChildAt(0)).setImageResource(R.drawable.shape_to);
            details = (TextView) findViewById(R.id.details);
            content = (LinearLayout) findViewById(R.id.content);
            avatar = (ImageView)findViewById(R.id.avatar);
            name_loading = (ProgressBar) findViewById(R.id.name_loading);
            name = (TextView) findViewById(R.id.driver_name);
            reg_date = (TextView) findViewById(R.id.driver_registration_date);
            last_login = (TextView) findViewById(R.id.driver_active_date);
            reoccur = (ReoccuringWeekDaysView) findViewById(R.id.reoccur);
            row = (RideRowView) findViewById(R.id.row);
            streifenhoernchen = findViewById(R.id.streifenhoernchen);
            Util.fixStreifenhoernchen(streifenhoernchen);
            avatar.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (url != null)
                        new ImageDialog(getContext()).show();
                }
            });
        }

        class ImageDialog extends Dialog implements OnClickListener {
            
            public ImageDialog(Context context) {
                super(context, R.style.ProfilePictureFullscreen);
            }

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                setContentView(R.layout.view_big_image);
                ImageView image = (ImageView) findViewById(R.id.image_large);
                image.setImageResource(R.drawable.ic_call);
                imageLoader.get(url, ImageLoader.getImageListener(image,
                              R.drawable.ic_loading, R.drawable.icn_view_none));
                ScaleAnimation s = new ScaleAnimation(0.3f, 1, 0.3f, 1);
                s.setDuration(300);
                s.setFillAfter(true);
                image.startAnimation(s);
                image.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                dismiss();
            };
        }

        @Override
        public Loader<Cursor> onCreateLoader(int ride_id, Bundle b) {
            return new CursorLoader(getContext(), Uri.parse(
                    "content://de.fahrgemeinschaft/rides/" + ride_id + "/rides")
                    ,null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> l, Cursor c) {
            for (int i = 1; i < c.getCount(); i++) {
                c.moveToPosition(i);
                FrameLayout view = (FrameLayout)
                        LayoutInflater.from(getContext())
                        .inflate(R.layout.view_place_bubble, null, false);
                ((TextView) view.getChildAt(1))
                        .setText("- " + c.getString(COLUMNS.FROM_NAME));
                ((ImageView) view.getChildAt(0))
                        .setImageResource(R.drawable.shape_via);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(lp);
                content.addView(view, i);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> l) {
            Log.d(TAG, "loader " + l.getId() + "reset.");
        }

        @Override
        public void onResponse(JSONObject json) {
            try {
                JSONObject user = json.getJSONObject("user");
                if (user.getString("UserID").equals(userId)) {
                    JSONArray kvp = user.getJSONArray("KeyValuePairs");
                    String firstname = "n/a";
                    String lastname = "n/a";
                    for (int i = 1; i < kvp.length(); i++) {
                        String key = kvp.getJSONObject(i).getString("Key");
                        if (key.equals("firstname"))
                            firstname = kvp.getJSONObject(i).getString("Value");
                        else if (key.equals("lastname"))
                            lastname = kvp.getJSONObject(i).getString("Value");
                    }
                    if (visible) {
                        name.setText(firstname + " " + lastname);
                    } else {
                        name.setText(firstname);
                    }
                    Date since = lrdate.parse(user.getString("RegistrationDate"));
                    Date logon = lrdate.parse(user.getString("LastvisitDate"));
                    reg_date.setText(getContext().getString(
                            R.string.member_since, lwdate.format(since)));
                    last_login.setText(getContext().getString(
                            R.string.last_login, lwhdate.format(logon)));
                    //                  JSONArray rgd = user.getJSONArray("RegistrationDate");
                    //                  reg_date.setText(user.getJSONArray("").getString("Value") + " " 
                    //                          + kvp.getJSONObject(2).getString("Value"));
                    //                  Log.d(TAG, json.toString());

                    name_loading.setVisibility(View.GONE);
                    name.setVisibility(View.VISIBLE);
                    last_login.setVisibility(View.VISIBLE);
                    reg_date.setVisibility(View.VISIBLE);
                    if (!user.isNull("AvatarPhoto")) {
                        JSONObject photo = user.getJSONObject("AvatarPhoto");
                        String id = photo.getString("PhotoID");
                        String path = photo.getString("PathTo");
                        url = "http://service.fahrgemeinschaft.de//"
                                + "ugc/pa/" + path +"/"+ id + "_big.jpg";
                        imageLoader.get(url, ImageLoader.getImageListener(avatar,
                                R.drawable.ic_loading, R.drawable.icn_view_none));
                    }
                }
          } catch (Exception e) {
              e.printStackTrace();
          }
        }
    }

    @Override
    public void onErrorResponse(VolleyError err) {
        Log.d(TAG, err.toString());
        err.printStackTrace();
    }

    private boolean isMyRide(Cursor ride) {
        return (ride.getString(COLUMNS.WHO).equals("") ||
                ride.getString(COLUMNS.WHO).equals(PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("user", "")));
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {}

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}

}
