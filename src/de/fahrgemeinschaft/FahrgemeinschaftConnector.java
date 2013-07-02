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
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Connector;
import org.teleportr.Place;
import org.teleportr.Ride;

import android.content.Context;


public class FahrgemeinschaftConnector extends Connector {

    public FahrgemeinschaftConnector(Context ctx) {
        super(ctx);
    }

    private String startDate;

    public String endpoint =  "http://service.fahrgemeinschaft.de";

    static final String APIKEY = "API-KEY";
    static final SimpleDateFormat fulldf = new SimpleDateFormat("yyyyMMddHHmm", Locale.GERMAN);
    static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);

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
    public long search(Place from, Place to, Date dep, Date arr) {
        
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
        ride.details(json.getString("Description"));
        ride.ref(json.getString("TripID"));
        ride.seats(json.getLong("Places"));
        ride.dep(parseTimestamp(json));

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
        } else {
            System.out.println("no start time!");
        }
        departure = startDate + departure;
        try {
            return fulldf.parse(departure);
        } catch (ParseException e) {
            System.out.println("date/time parse error!");
            e.printStackTrace();
            return new Date(0);
        }
    }

    @Override
    public int publish(Ride offer) throws Exception {
        JSONObject json = new JSONObject();
        json.put("Triptype", "offer");
        json.put("IDuser", get("user"));
        ArrayList<JSONObject> routings = new ArrayList<JSONObject>();
        routings.add(routing(offer.getFrom(), offer.getTo()));
        for (Ride sub : offer.getSubrides()) {
            routing(offer.getFrom(), sub.getTo());
        }
        json.put("Routings", new JSONArray(routings));
        json.put("Reoccur", new JSONArray());
        json.put("Privacy", new JSONArray());
        System.out.println(json);
        HttpURLConnection post = (HttpURLConnection)
                new URL(endpoint + "/trip").openConnection();
        post.setRequestProperty("authkey", getAuth());
        post.setRequestProperty("apikey", APIKEY);
        post.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(post.getOutputStream());
        out.write(json.toString());
        JSONObject response = loadJson(post);
        System.out.println(response);

        return 0;
    }

    private JSONObject routing(Place from, Place to) throws JSONException {
        JSONObject route = new JSONObject();
        route.put("Origin", place(from));
        route.put("Destination", place(to));
        return route;
    }

    private JSONObject place(Place from) throws JSONException {
        JSONObject place = new JSONObject();
        place.put("Latitude", from.getLat());
        place.put("Longitude", from.getLng());
        place.put("Address", from.getAddress());
        place.put("CountryName", "Deutschland");
        place.put("CountryCode", "DE");
        return place;
    }
}

// "Triptype": "offer",
// "Smoker": "no",
// "Startdate": "20130419",
// "Accuracy": {
// "DistanceDestination": 0,
// "OverallDistance": 0,
// "DistanceOrigin": 0
// },
// "TripID": "887b0da0-5a55-6e04-995d-367e06fffc7a",
// "Starttime": "1300",
// "Contactmail": "wuac@me.com",
// "Enterdate": "1366178563",
// "IDuser": "29ae8215-223f-63f4-9982-39b9aca69556",
// "Reoccur": {
// "Saturday": false,
// "Thursday": false,
// "Monday": false,
// "Tuesday": false,
// "Wednesday": false,
// "Friday": false,
// "Sunday": false
// },
// "Deeplink": null,
// "Places": "3",
// "Prefgender": null,
// "Price": "0",
// "Privacy": {
// "Name": "1",
// "Landline": "1",
// "Email": "1",
// "Mobile": "1",
// "NumberPlate": "1"
// },
// "Relevance": "10",
// "Partnername": null,
// "ClientIP": null,
// "NumberPlate": "",
// "Contactlandline": ""
