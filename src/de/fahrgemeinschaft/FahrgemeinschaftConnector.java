/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.AuthException;
import org.teleportr.Connector;
import org.teleportr.Place;
import org.teleportr.Ride;
import org.teleportr.Ride.Mode;



public class FahrgemeinschaftConnector extends Connector {


    private String startDate;

    public String endpoint =  "http://test.service.fahrgemeinschaft.de";

    static final SimpleDateFormat fulldf =
            new SimpleDateFormat("yyyyMMddHHmm", Locale.GERMAN);
    static final SimpleDateFormat df =
            new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
    public static final long reoccuring = 2555452800000l;
    static final String XMAS2050 = "20501224";
    public static final String[] DAYS = new String[] { "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    @Override
    public String authenticate(String credential) throws Exception {
        System.out.println("refreshing authtoken");
        HttpURLConnection post = (HttpURLConnection)
                new URL(endpoint + "/session").openConnection();
        post.setRequestProperty("apikey", Secret.APIKEY);
        post.setDoOutput(true);
        post.getOutputStream().write((
                "{\"Email\": \"" + get("login")
                + "\", \"Password\": \"" + credential
                + "\"}").getBytes());
        post.getOutputStream().close();
        JSONObject json = loadJson(post);
        if (post.getResponseCode() == 403)
            throw new AuthException();
        JSONObject auth = json.getJSONObject("auth");
        set("user", auth.getString("IDuser"));
        JSONArray kvp = json.getJSONObject("user")
                .getJSONArray("KeyValuePairs");
        for (int i = 1; i < kvp.length(); i++) {
            String key = kvp.getJSONObject(i).getString("Key");
            if (key.equals("firstname"))
                set("firstname", kvp.getJSONObject(i).getString("Value"));
            else if (key.equals("lastname"))
                set("lastname", kvp.getJSONObject(i).getString("Value"));
        }
        return auth.getString("AuthKey");
    }

    @Override
    public long search(Place from, Place to, Date dep, Date arr) throws Exception {
        HttpURLConnection get;
        if (from == null && to == null) { // myrides
            get = (HttpURLConnection) new URL(endpoint + "/trip").openConnection();
        } else {
            JSONObject from_json = new JSONObject();
            JSONObject to_json = new JSONObject();
            startDate = df.format(dep);
            try {
                from_json.put("Longitude", "" + from.getLng());
                from_json.put("Latitude", "" + from.getLat());
                from_json.put("Startdate", df.format(dep));
                from_json.put("Reoccur", JSONObject.NULL);
                from_json.put("ToleranceRadius", get("radius_from"));
                // place.put("Starttime", JSONObject.NULL);
                to_json.put("Longitude", "" + to.getLng());
                to_json.put("Latitude", "" + to.getLat());
                to_json.put("ToleranceRadius", get("radius_to"));
                // place.put("ToleranceDays", "3");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            get = (HttpURLConnection) new URL(endpoint
                    + "/trip?searchOrigin=" + from_json
                    + "&searchDestination=" + to_json).openConnection();
        }
        get.setRequestProperty("apikey", Secret.APIKEY);
        if (getAuth() != null)
            get.setRequestProperty("authkey", getAuth());
        JSONObject json = loadJson(get);
        if (get.getResponseCode() == 403)
            throw new AuthException();
        if (json != null) {
            JSONArray results = json.getJSONArray("results");
            System.out.println("FOUND " + results.length() + " rides");

            for (int i = 0; i < results.length(); i++) {
                store(parseRide(results.getJSONObject(i)));
            }
        }
        startDate = null;
        return getNextDayMorning(dep);
    }

    public static long getNextDayMorning(Date dep) {
        if (dep != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dep.getTime() + 24 * 3600000); // plus one day
            c.set(Calendar.HOUR_OF_DAY, 0); // reset
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            return c.getTimeInMillis();
        } else return 0;
    }

    private static final String EMAIL = "Email";
    private static final String MOBILE = "Mobile";
    private static final String LANDLINE = "Landline";
    private static final String PLATE = "NumberPlate";

    private Ride parseRide(JSONObject json)  throws JSONException {

        Ride ride = new Ride().type(Ride.OFFER);
        if (startDate == null) ride.marked(); // myrides
        ride.who(json.getString("IDuser"));
        String value = json.getString("Contactmail");
        if (!value.equals("") && !value.equals("null"))
            ride.set(EMAIL, value);
        value = json.getString("Contactmobile");
        if (!value.equals("") && !value.equals("null"))
            ride.set(MOBILE, value);
        value = json.getString("Contactlandline");
        if (!value.equals("") && !value.equals("null"))
            ride.set(LANDLINE, value);
        value = json.getString(PLATE);
        if (!value.equals("") && !value.equals("null")) {
            ride.set(PLATE, value);
            if (value.equals("Bahn"))
                ride.mode(Mode.TRAIN);
        }
        ride.getDetails().put("Privacy", json.getJSONObject("Privacy"));
        ride.set("Comment", json.getString("Description"));
        if (json.getInt("Relevance") == 10) {
            ride.activate();
        } else {
            ride.deactivate();
        }
        ride.ref(json.getString("TripID"));
        ride.seats(json.getInt("Places"));

        JSONObject reoccur = json.getJSONObject("Reoccur");
        boolean isReoccuring = false;
        for (int i = 0; i < 7; i++) {
            if (reoccur.getBoolean(FahrgemeinschaftConnector.DAYS[i])) {
                isReoccuring = true;
                break;
            }
        }
        if (isReoccuring) {
            ride.arr(new Date(reoccuring));
            ride.getDetails().put("Reoccur", reoccur);
        }
        String time = parseTime(json);
        if (startDate != null) {
            ride.dep(parseDate(startDate + time));
        } else { // myrides
            if (isReoccuring) {
                ride.dep(parseDate(XMAS2050 + time));
            } else {
                ride.dep(parseDate(json.getString("Startdate") + time));
            }
        }

        if (!json.isNull("Price")) {
            ride.price((int) Double.parseDouble(
                    json.getString("Price")) * 100);
        }

        JSONArray routings = json.getJSONArray("Routings");

        ride.from(store(parsePlace(routings.getJSONObject(0)
                .getJSONObject("Origin"))));

        for (int j = 1; j < routings.length(); j++) {
            ride.via(store(parsePlace(routings.getJSONObject(j)
                    .getJSONObject("Destination"))));
        }

        ride.to(store(parsePlace(routings.getJSONObject(0)
                .getJSONObject("Destination"))));
        return ride;
    }

    private Place parsePlace(JSONObject json) throws JSONException {
        String[] split = json.getString("Address").split(", ");
        return new Place(
                    Double.parseDouble(json.getString("Latitude")),
                    Double.parseDouble(json.getString("Longitude")))
                .address(json.getString("Address"))
                .name((split.length > 0)? split[0] : "");
    }

    private String parseTime(JSONObject json) throws JSONException {
//              new Date(Long.parseLong(ride.getString("Enterdate"));
        String time = "2359";
        if (!json.isNull("Starttime")) {
            time = json.getString("Starttime");
            if (time.length() == 3)
                time = "0" + time;
            if (time.length() != 4)
                time = "2359";
        }
        return time;
    }

    private Date parseDate(String date) {
        try {
            return fulldf.parse(date);
        } catch (ParseException e) {
            System.out.println("date/time parse error!");
            e.printStackTrace();
            return new Date();
        }

    }

    @Override
    public String publish(Ride offer) throws Exception {
        HttpURLConnection post;
        if (offer.getRef() == null) {
            post = (HttpURLConnection) new URL(endpoint + "/trip")
                .openConnection();
            post.setRequestMethod("POST");
        } else {
            post = (HttpURLConnection) new URL(endpoint + "/trip/id/"
                    + offer.getRef()).openConnection();
            post.setRequestMethod("PUT");
        }
        post.setRequestProperty("apikey", Secret.APIKEY);
        if (getAuth() != null)
            post.setRequestProperty("authkey", getAuth());
        post.setDoOutput(true);
        JSONObject json = new JSONObject();
//        json.put("Smoker", "no");
        json.put("Triptype", "offer");
        json.put("TripID", offer.getRef());
        json.put("IDuser", get("user"));
        if (offer.getMode().equals(Mode.TRAIN)) {
            json.put(PLATE, "Bahn");
        } else {
            json.put(PLATE, offer.get(PLATE));
        }
        if (offer.isActive()) {
            json.put("Relevance", 10);
        } else {
            json.put("Relevance", 0);
        }
        json.put("Places", offer.getSeats());
        json.put("Price", offer.getPrice() / 100);
        json.put("Contactmail", offer.get(EMAIL));
        json.put("Contactmobile", offer.get(MOBILE));
        json.put("Contactlandline", offer.get(LANDLINE));
        String dep = fulldf.format(offer.getDep());
        json.put("Startdate", dep.subSequence(0, 8));
        json.put("Starttime", dep.subSequence(8, 12));
        json.put("Description", offer.get("Comment"));
        if (!offer.getDetails().isNull("Privacy"))
            json.put("Privacy", offer.getDetails().getJSONObject("Privacy"));
        if (!offer.getDetails().isNull("Reoccur"))
            json.put("Reoccur", offer.getDetails().getJSONObject("Reoccur"));
        ArrayList<JSONObject> routings = new ArrayList<JSONObject>();
        List<Place> stops = offer.getPlaces();
        int max = stops.size() - 1;
        for (int dest = max; dest >= 0 ; dest--) {
            for (int orig = 0; orig < dest; orig++) {
                int idx = (orig == 0? (dest == max? 0 : dest) : - dest);
                JSONObject route = new JSONObject();
                route.put("RoutingIndex", idx);
                route.put("Origin", place(stops.get(orig)));
                route.put("Destination", place(stops.get(dest)));
                routings.add(route);
            }
        }
        json.put("Routings", new JSONArray(routings));
        OutputStreamWriter out = new OutputStreamWriter(post.getOutputStream());
        out.write(json.toString());
        out.flush();
        out.close();
        JSONObject response = loadJson(post);
        if (!response.isNull("tripID")) {
            return response.getString("tripID");
        } else return offer.getRef();
    }

    private JSONObject place(Place from) throws JSONException {
        JSONObject place = new JSONObject();
        place.put("Latitude", from.getLat());
        place.put("Longitude", from.getLng());
        place.put("Address", from.getAddress());
        place.put("CountryName", "Deutschland");
        place.put("CountryCode", "DE");
        place.put("Placetype", "geo");
        return place;
    }

    @Override
    public String delete(Ride offer) throws Exception {
        HttpURLConnection delete = (HttpURLConnection) new URL(
                endpoint + "/trip/id/" + offer.getRef()).openConnection();
        delete.setRequestMethod("DELETE");
        delete.setRequestProperty("authkey", getAuth());
        delete.setRequestProperty("apikey", Secret.APIKEY);
        return loadJson(delete).toString();
    }
}
