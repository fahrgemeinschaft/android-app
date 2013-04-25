package de.fahrgemeinschaft;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Place;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class GPlaces {

    private static final String API_KEY = "AIzaSyBefOGpJAYaylGWlUo0qqhATcJSECortuM";
    private static final String TAG = "gPlaces";

    static public class AutocompleteLoader extends AsyncTaskLoader<Cursor> {

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
                sb.append("&language=de&components=country:de");
                sb.append("&input=" + URLEncoder.encode(text, "utf8"));

                String jsonResults = httpGet(sb.toString());

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
                    String[] place = json.get("description").toString()
                            .split(", ");
                    if (place.length > 1 && !already.contains(place[0])) {
                        gPlaces.addRow(new String[] { "" + i, "", place[0],
                                place[1], json.getString("reference") });
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

    static public class DetailsTask extends AsyncTask<Uri, String, Place> {

        @Override
        protected Place doInBackground(Uri... params) {

            String uri = params[0].toString();
            Log.d(TAG, uri);
            String jsonResult = httpGet(uri + "&key=" + API_KEY);
            try {
                JSONObject result = new JSONObject(jsonResult)
                        .getJSONObject("result");
                JSONObject location = result.getJSONObject("geometry")
                        .getJSONObject("location");
                Log.d(TAG, location.toString());
                return new Place(Double.parseDouble(location.getString("lat")),
                        Double.parseDouble(location.getString("lng")))
                        .address(result.getString("formatted_address"));
            } catch (JSONException e) {
                Log.e(TAG, "Cannot process JSON results", e);
            } catch (Exception e) {
                Log.e(TAG, "no internet?", e);
            }
            return null;
        }

    }

    private static String httpGet(String url) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e(TAG, "io exception " + e.getMessage());
            Log.e(TAG, "no internet???", e);
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults.toString();
    }
}