/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Connector;
import org.teleportr.Place;
import org.teleportr.Ride;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class GPlaces extends Connector {


    private static final String API_KEY = "AIzaSyBefOGpJAYaylGWlUo0qqhATcJSECortuM";
    private static final String TAG = "gPlaces";

    @Override
    public void resolvePlace(Place place, Context ctx) {
        String uri = "https://maps.googleapis.com/maps/api/place/details/json"
                + "?sensor=true&reference=" + place.get("gplace:id");
        Log.d(TAG, uri);
        String jsonResult = httpGet(uri + "&language=de&key=" + API_KEY);
        try {
            JSONObject result = new JSONObject(jsonResult)
                    .getJSONObject("result");
            JSONObject location = result.getJSONObject("geometry")
                    .getJSONObject("location");
            Log.d(TAG, location.toString());
            
            place.latlon(
                    Double.parseDouble(location.getString("lat")),
                    Double.parseDouble(location.getString("lng")))
                    .address(result.getString("formatted_address"))
                    .store(ctx);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        } catch (Exception e) {
            Log.e(TAG, "no internet?", e);
        }
    }



    static String httpGet(String url) {
        try {
            HttpURLConnection get = (HttpURLConnection)
                    new URL(url).openConnection();
            return loadString(get);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static class AutocompleteLoader extends AsyncTaskLoader<Cursor> {

        private Cursor places;
        private String text;
        private Cursor prev;

        public AutocompleteLoader(Context context, String text, Cursor prev) {
            super(context);
            this.text = text;
            this.prev = prev;
        }

        private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private static final String OUT_JSON = "/json";

        @Override
        protected void onStartLoading() {
            if (places != null) {
                deliverResult(places);
            } else {
                forceLoad();
            }
        }

        @Override
        public Cursor loadInBackground() {

            StringBuilder sb = new StringBuilder(PLACES_API_BASE
                    + TYPE_AUTOCOMPLETE + OUT_JSON);
            try {
                sb.append("?sensor=false&key=" + API_KEY);
                sb.append("&language=de&location=48.1,11.8&radius=3000");
                sb.append("&input=" + URLEncoder.encode(text, "utf8"));

                String jsonResults = httpGet(sb.toString());

                if (jsonResults == null) {
                    return prev;
                }
                
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                HashSet<String> already = new HashSet<String>();
                for (int i = 0; i < prev.getCount(); i++) {
                    prev.moveToPosition(i);
                    already.add(prev.getString(2));
                }
                MatrixCursor gPlaces = new MatrixCursor(new String[] { "_id",
                        "geohash", "name", "address", "ref" });
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    JSONObject json = predsJsonArray.getJSONObject(i);
                    String address = json.get("description").toString();
                    String[] split = address.split(", ");
                    String name = null;
                    if (split.length > 2)
                        name = split[0] + " " + split[1];
                    else if (split.length > 1)
                        name = split[0];
                    if (name != null && !already.contains(name)) {
                        gPlaces.addRow(new String[] { "" + i,
                                "", name, address,
                                json.getString("reference") });
                    }
                }
                places = new MergeCursor(new Cursor[] { prev, gPlaces });
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (JSONException e) {
                Log.e(TAG, "Cannot process JSON results", e);
            } catch (Exception e) {
                Log.e(TAG, "no internet?", e);
            }
            return places;
        }
    }



    @Override
    public long search(Place from, Place to, Date dep, Date arr) {
        // TODO Auto-generated method stub
        return 0;
    }



    @Override
    public int publish(Ride offer) throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }



}