package de.fahrgemeinschaft;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Connector;
import org.teleportr.Place;

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
                + "?sensor=true&reference=" + place.get("gplace:id", ctx);
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
                    if (split.length > 1 && !already.contains(split[0])) {
                        gPlaces.addRow(new String[] { "" + i,
                                "", split[0], address,
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



}