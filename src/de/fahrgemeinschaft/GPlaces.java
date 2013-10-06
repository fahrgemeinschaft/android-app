/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class GPlaces extends Connector {

    private static final String FORMATTED_ADDRESS = "formatted_address";
    private static final String DESCRIPTION = "description";
    private static final String PREDICTIONS = "predictions";
    private static final String REFERENCE = "reference";
    public static final String GPLACES_ID = "gplace:id";
    private static final String GEOMETRY = "geometry";
    private static final String LOCATION = "location";
    private static final String RESULT = "result";
    private static final String TAG = "gPlaces";
    private static final String KOMMA = ", ";
    private static final String LNG = "lng";
    private static final String LAT = "lat";
    private static final String SPACE = " ";
    private static final String EMPTY = "";

    private static final String GPLACES_BASE_URL =
            "https://maps.googleapis.com/maps/api/place";

    private static final String URL_PARAMS =
            "?sensor=false&language=de&key=" + Secret.API_KEY;

    private static final String LOC = // TODO users location
            "&location=48.1,11.8&radius=3000";

    private static final String GPLACES_AUTOCOMPLETE_URL =
            GPLACES_BASE_URL + "/autocomplete/json" + URL_PARAMS + LOC;

    private static final String GPLACES_DETAILS_URL =
            GPLACES_BASE_URL + "/details/json" + URL_PARAMS;

    private static final String REF = "&reference=";
    private static final String INPUT = "&input=";
    private static final String UTF8 = "utf8";

    private static final String[] PLACE_COLUMNS = new String[] {
        BaseColumns._ID, Place.GEOHASH, Place.NAME, Place.ADDRESS, GPLACES_ID };


    @Override
    public void resolvePlace(Place place, Context ctx) throws Exception {
        StringBuffer url = new StringBuffer(GPLACES_DETAILS_URL);
        url.append(REF).append(place.get(GPLACES_ID));
        String jsonResult = httpGet(url.toString());
        JSONObject result = new JSONObject(jsonResult).getJSONObject(RESULT);
        JSONObject location = result.getJSONObject(GEOMETRY)
                .getJSONObject(LOCATION);
        place.latlon(
                Double.parseDouble(location.getString(LAT)),
                Double.parseDouble(location.getString(LNG)))
                .address(result.getString(FORMATTED_ADDRESS))
                .store(ctx);
    }

    private static String httpGet(String url) throws Exception {
        HttpURLConnection get = (HttpURLConnection)
                new URL(url).openConnection();
        return loadString(get);
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

            MatrixCursor gPlaces = new MatrixCursor(PLACE_COLUMNS);
            try {
                StringBuilder url = new StringBuilder(GPLACES_AUTOCOMPLETE_URL);
                url.append(INPUT).append(URLEncoder.encode(text, UTF8));
                String jsonResults = httpGet(url.toString());
                if (jsonResults == null) {
                    gPlaces.close();
                    return prev;
                }
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray(PREDICTIONS);
                HashSet<String> cached = new HashSet<String>();
                for (int i = 0; i < prev.getCount(); i++) {
                    prev.moveToPosition(i);
                    cached.add(prev.getString(2));
                }
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    JSONObject json = predsJsonArray.getJSONObject(i);
                    String address = json.get(DESCRIPTION).toString();
                    String[] split = address.split(KOMMA);
                    String name = null;
                    if (split.length > 2)
                        name = split[0] + SPACE + split[1];
                    else if (split.length > 1)
                        name = split[0];
                    if (name != null && !cached.contains(name)) {
                        gPlaces.addRow(new String[] { String.valueOf(i), EMPTY,
                                name, address, json.getString(REFERENCE) });
                    }
                }

            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (JSONException e) {
                Log.e(TAG, "Cannot process JSON results", e);
            } catch (Exception e) {
                Log.e(TAG, "no internet?", e);
            }
            places = new MergeCursor(new Cursor[] { prev, gPlaces });
            return places;
        }
    }



    @Override
    public long search(Ride query) {
        return 0;
    }

    @Override
    public String publish(Ride offer) throws Exception {
        return null;
    }

    @Override
    public String delete(Ride ride) throws Exception {
        return null;
    }
}