/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Connector;
import org.teleportr.Place;
import org.teleportr.Ride;

import android.content.Context;


public class FahrgemeinschaftConnector extends Connector {

    private String startDate;

    public FahrgemeinschaftConnector(Context ctx) {
        super(ctx);
    }

    private static final String APIKEY = "API-KEY"; 
    static final SimpleDateFormat fulldf = new SimpleDateFormat("yyyyMMddHHmm");
    static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    @Override
    public long getRides(Place from, Place to, Date dep, Date arr) {
        
        startDate = df.format(dep);

        JSONObject from_json = new JSONObject();
        JSONObject to_json = new JSONObject();
        try {
            from_json.put("Longitude", "" + from.getLng());
            from_json.put("Latitude", "" + from.getLat());
            from_json.put("Startdate", df.format(dep));
            from_json.put("Reoccur", JSONObject.NULL);
            from_json.put("ToleranceRadius", "15");
            // place.put("Starttime", JSONObject.NULL);

            to_json.put("Longitude", "" + to.getLng());
            to_json.put("Latitude", "" + to.getLat());
            to_json.put("ToleranceRadius", "25");
            // place.put("ToleranceDays", "3");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject json = loadJson("http://service.fahrgemeinschaft.de/trip?"
                + "searchOrigin=" + from_json + "&searchDestination=" + to_json);
        if (json != null) {
            try {
                JSONArray results = json.getJSONArray("results");
                System.out.println("FOUND " + results.length() + " rides");
                
                for (int i = 0; i < results.length(); i++) {
                    store(parseRide(results.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dep.getTime() + 24 * 3600 * 1000;
    }

    private Ride parseRide(JSONObject json)  throws JSONException {
        StringBuffer who = new StringBuffer();
        JSONObject p = json.getJSONObject("Privacy");
        String value = json.getString("Contactmail");
        if (!value.equals(""))
            who.append(";mail=").append(p.getInt("Email")).append(value);
        value = json.getString("Contactmobile");
        if (!value.equals(""))
            who.append(";mobile=").append(p.getInt("Mobile")).append(value);
        value = json.getString("Contactlandline");
        if (!value.equals(""))
            who.append(";landline=").append(p.getInt("Landline")).append(value);

        Ride ride = new Ride().type(Ride.OFFER).who(who.toString());
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

    JSONObject loadJson(String url) {
        System.out.println(url);
        HttpURLConnection conn = null;
        StringBuilder result = new StringBuilder();
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("apikey", APIKEY);
            InputStreamReader in = new InputStreamReader(
                    new BufferedInputStream(conn.getInputStream()));
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                result.append(buff, 0, read);
            }
            return new JSONObject(result.toString());
        } catch (JSONException e) {
            System.out.println("json error");
        } catch (MalformedURLException e) {
            System.out.println("url error ");
        } catch (IOException e) {
            System.out.println("io error");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
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
