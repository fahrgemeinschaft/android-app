/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Connector;
import org.teleportr.Place;
import org.teleportr.Ride;



public class FahrgemeinschaftConnector extends Connector {

    private String startDate;

    public String endpoint =  "http://service.fahrgemeinschaft.de";

    static final String APIKEY = "API-KEY";
    static final SimpleDateFormat fulldf =
            new SimpleDateFormat("yyyyMMddHHmm", Locale.GERMAN);
    static final SimpleDateFormat df =
            new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);

    @Override
    public String authenticate() throws Exception {
        HttpURLConnection post = (HttpURLConnection)
                new URL(endpoint + "/session").openConnection();
        post.setRequestProperty("apikey", APIKEY);
        post.setDoOutput(true);
        post.getOutputStream().write((
                "{\"Email\": \"" + get("username")
                + "\", \"Password\": \"" + get("password")
                + "\"}").getBytes());
        post.getOutputStream().close();
        JSONObject json = loadJson(post);
        JSONObject auth = json.getJSONObject("auth");
        set("user", auth.getString("IDuser"));
        return auth.getString("AuthKey");
    }

    @Override
    public long search(Place from, Place to, Date dep, Date arr) throws Exception {
        
        startDate = df.format(dep);

        JSONObject from_json = new JSONObject();
        JSONObject to_json = new JSONObject();
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

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint
                             + "/trip?searchOrigin=" + from_json
                            + "&searchDestination=" + to_json).openConnection();
            conn.setRequestProperty("apikey", APIKEY);
            JSONObject json = loadJson(conn);
            if (json != null) {
                JSONArray results = json.getJSONArray("results");
                System.out.println("FOUND " + results.length() + " rides");

                for (int i = 0; i < results.length(); i++) {
                    store(parseRide(results.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dep.getTime() + 24 * 3600 * 1000;
    }

    private Ride parseRide(JSONObject json)  throws JSONException {

        Ride ride = new Ride().type(Ride.OFFER);
        ride.who(json.getString("IDuser"));
        JSONObject p = json.getJSONObject("Privacy");
        String value = json.getString("Contactmail");
        if (!value.equals("") && !value.equals("null"))
            ride.set("mail", p.getInt("Email") + value);
        value = json.getString("Contactmobile");
        if (!value.equals("") && !value.equals("null"))
            ride.set("mobile", p.getInt("Mobile") + value);
        value = json.getString("Contactlandline");
        if (!value.equals("") && !value.equals("null"))
            ride.set("landline", p.getInt("Landline") + value);
        ride.set("comment", json.getString("Description"));
        ride.ref(json.getString("TripID"));
        ride.seats(json.getInt("Places"));
        ride.dep(parseTimestamp(json));
        ride.getDetails().put("reoccur", json.getJSONObject("Reoccur"));

        if (!json.isNull("Price")) {
            ride.price((int) Double.parseDouble(
                    json.getString("Price")) * 100);
        }

        JSONArray routings = json.getJSONArray("Routings");

        ride.from(store(parsePlace(
                routings.getJSONObject(0)
                .getJSONObject("Origin"))));

        for (int j = 1; j < routings.length(); j++) {
            ride.via(store(parsePlace(
                    routings.getJSONObject(j)
                    .getJSONObject("Destination"))));
        }

        ride.to(store(parsePlace(
                routings.getJSONObject(0)
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

    private Date parseTimestamp(JSONObject json) throws JSONException {
//              new Date(Long.parseLong(ride.getString("Enterdate"));
        String departure = "0000";
        if (!json.isNull("Starttime")) {
            departure = json.getString("Starttime");
            if (departure.length() == 3)
                departure = "0" + departure;
//            departure = json.getString("Startdate") + departure;
        }
        try {
            return fulldf.parse(startDate + departure);
        } catch (ParseException e) {
            System.out.println("date/time parse error!");
            e.printStackTrace();
            return new Date(0);
        }
    }

    @Override
    public int publish(Ride offer) throws Exception {
        HttpURLConnection post = (HttpURLConnection)
                new URL(endpoint + "/trip").openConnection();
        post.setRequestMethod("POST");
        post.setRequestProperty("authkey", getAuth());
        post.setRequestProperty("apikey", APIKEY);
        post.setDoOutput(true);
        JSONObject json = new JSONObject();
//        json.put("Smoker", "no");
        json.put("Triptype", "offer");
        json.put("IDuser", get("user"));
        json.put("Places", offer.getSeats());
        json.put("Price", offer.getPrice() / 100);
        json.put("Contactmail", offer.get("mail"));
        json.put("NumberPlate", offer.get("plate"));
        json.put("Contactmobile", offer.get("mobile"));
        json.put("Contactlandline", offer.get("landline"));
        String dep = fulldf.format(offer.getDep());
        json.put("Startdate", dep.subSequence(0, 8));
        json.put("Starttime", dep.subSequence(8, 12));
        json.put("Description", offer.get("comment"));
        json.put("Privacy", offer.getDetails().getJSONObject("privacy"));
        json.put("Reoccur", offer.getDetails().getJSONObject("reoccur"));
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
        System.out.println(json);
        OutputStreamWriter out = new OutputStreamWriter(post.getOutputStream());
        out.write(json.toString());
        out.flush();
        out.close();
        JSONObject response = loadJson(post);
        System.out.println(response.getString("tripID"));
        return 0;
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
}
