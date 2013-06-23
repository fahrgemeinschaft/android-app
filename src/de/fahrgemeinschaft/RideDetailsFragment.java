/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Ride;
import org.teleportr.Ride.COLUMNS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.ResponseDelivery;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.calciumion.widget.BasePagerAdapter;

import de.fahrgemeinschaft.EditRideFragment3.VisibilityView;

public class RideDetailsFragment extends SherlockFragment
        implements Response.ErrorListener {

    private static final String TAG = "Details";
    private static final SimpleDateFormat day =
            new SimpleDateFormat("EE", Locale.GERMANY);
    private static final SimpleDateFormat date =
            new SimpleDateFormat("dd.MM.", Locale.GERMANY);
    private static SimpleDateFormat time =
            new SimpleDateFormat("HH:mm", Locale.GERMANY);
    private ViewPager pager;
    private Cursor cursor;
    private RequestQueue queue;
    private static ImageLoader imageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        
        final LruCache<String, Bitmap> mImageCache =
                new LruCache<String, Bitmap>(20);

        ImageCache imageCache = new ImageCache() {
            @Override
            public void putBitmap(String key, Bitmap value) {
                mImageCache.put(key, value);
            }

            @Override
            public Bitmap getBitmap(String key) {
                return mImageCache.get(key);
            }
        };

        queue = Volley.newRequestQueue(getActivity());
        imageLoader = new ImageLoader(queue, imageCache);
        queue.start();
    }

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_details, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle savedInstanceState) {
        super.onViewCreated(layout, savedInstanceState);
        Log.d(TAG, "on create detail view " + savedInstanceState);
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
                RideView view = (RideView) v;
                
                if (view.content.getChildCount() > 4)
                    view.content.removeViews(1, view.content.getChildCount()-4);

                cursor.moveToPosition((Integer) position);

                view.userId = cursor.getString(COLUMNS.WHO);
                view.name.setText("");
                view.from_place.setText(cursor.getString(COLUMNS.FROM_ADDRESS));
                view.to_place.setText(cursor.getString(COLUMNS.TO_ADDRESS));

                Date timestamp = new Date(cursor.getLong(COLUMNS.DEPARTURE));
                view.day.setText(day.format(timestamp));
                view.date.setText(date.format(timestamp));
                view.time.setText(time.format(timestamp));

                view.price.setText("" + (cursor.getInt(COLUMNS.PRICE) / 100));
                switch(cursor.getInt(COLUMNS.SEATS)){
                case 0:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_full);
                    break;
                case 1:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_1);
                    break;
                case 2:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_2);
                    break;
                case 3:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_3);
                    break;
                default:
                    ((ImageView) v.findViewById(R.id.seats_icon))
                            .setImageResource(R.drawable.icn_seats_white_many);
                    break;
                }
                view.details.setText(Ride.get("details",
                        cursor.getString(COLUMNS.DETAILS)));

                getActivity().getSupportLoaderManager()
                    .initLoader((int) cursor.getLong(0), null, view);
                
                view.avatar.setImageResource(R.drawable.ic_launcher);
                
                queue.add(new ProfileRequest(cursor.getString(COLUMNS.WHO),
                        view, RideDetailsFragment.this));
                
                return view;
            }
            
            @Override
            protected Object getItem(int position) {
                return position;
            }
        });
    }

    public void setCursor(Cursor cursor) {
        Log.d(TAG, "change cursor");
        this.cursor = cursor;
        if (pager != null)
            pager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        pager.setCurrentItem(((ResultsActivity) getActivity()).selected);
        pager.setOnPageChangeListener((OnPageChangeListener) getActivity());
    }

    static class RideView extends RelativeLayout
        implements LoaderCallbacks<Cursor>, Response.Listener<JSONObject> {

        TextView from_place;
        TextView to_place;
        TextView price;
        TextView day;
        TextView date;
        TextView time;
        TextView details;
        LinearLayout content;
        ImageView avatar;
        String userId;
        TextView name;

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
            price = (TextView) findViewById(R.id.price);
            day = (TextView) findViewById(R.id.day);
            date = (TextView) findViewById(R.id.date);
            time = (TextView) findViewById(R.id.time);
            details = (TextView) findViewById(R.id.details);
            content = (LinearLayout) findViewById(R.id.content);
            avatar = (ImageView)findViewById(R.id.avatar);
            name = (TextView) findViewById(R.id.driver_name);
        }
        @Override
        public Loader<Cursor> onCreateLoader(int ride_id, Bundle b) {
            Log.d(TAG, "loading subrides for ride " + ride_id);
            return new CursorLoader(getContext(), Uri.parse(
                    "content://de.fahrgemeinschaft/rides/" + ride_id + "/rides")
                    ,null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> l, Cursor c) {
            Log.d(TAG, "finished loading subrides " + l.getId());
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
                Log.d(TAG, c.getString(COLUMNS.FROM_NAME)
                        + " --> " + c.getString(COLUMNS.TO_NAME));
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
              Log.d(TAG, "profile downloaded " + user.get("UserID"));
              if (user.getString("UserID").equals(userId)) {
                  JSONArray kvp = user.getJSONArray("KeyValuePairs");
                  name.setText(kvp.getJSONObject(1).getString("Value") + " " 
                          + kvp.getJSONObject(2).getString("Value"));
//                  Log.d(TAG, kvp.toString());
                  if (!user.isNull("AvatarPhoto")) {
                      JSONObject photo = user.getJSONObject("AvatarPhoto");
                      String id = photo.getString("PhotoID");
                      String path = photo.getString("PathTo");
                      String url = "http://service.fahrgemeinschaft.de//"
                              + "ugc/pa/" + path +"/"+ id + "_small.jpg";
                      imageLoader.get(url, ImageLoader.getImageListener(avatar,
                              R.drawable.ic_loading, R.drawable.ic_launcher_fab));
                  }
              }
          } catch (JSONException e) {
              e.printStackTrace();
          }
        }
    }

    @Override
    public void onErrorResponse(VolleyError err) {
        Log.d(TAG, err.toString());
        err.printStackTrace();
    }

    static class ProfileRequest extends JsonObjectRequest { 

        private static HashMap<String, String> headers;

        static {
            headers = new HashMap<String, String>();  
            headers.put("apikey", FahrgemeinschaftConnector.APIKEY);  
        }

        public ProfileRequest( String userid,
                Listener<JSONObject> listener, ErrorListener errorListener) {
            super(Method.GET, "http://service.fahrgemeinschaft.de/user/"
                + userid, null, listener, errorListener);
            setShouldCache(Boolean.TRUE);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return headers;
        };
        
        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse res) {
            return Response.success(super.parseNetworkResponse(res).result,
                    parseIgnoreCacheHeaders(res));
            
        }
    }

    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;
        long serverDate = 0;
        String serverEtag = null;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
//            serverDate = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
        final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;

        return entry;
    }
    
}
