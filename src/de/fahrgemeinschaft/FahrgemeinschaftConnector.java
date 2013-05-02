package de.fahrgemeinschaft;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.teleportr.Connector;
import org.teleportr.Place;
import org.teleportr.Ride;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FahrgemeinschaftConnector extends Connector {

    private static final String APIKEY = "API-KEY"; 
    static final SimpleDateFormat fulldf = new SimpleDateFormat("yyyyMMddHHmm");
    static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void getRides(Place from, Place to, Date dep, Date arr) {


        JSONObject from_json = new JSONObject();
        JSONObject to_json = new JSONObject();
        try {
            from_json.put("Longitude", "" + from.getLng());
            from_json.put("Latitude", "" + from.getLat());
            from_json.put("Startdate", df.format(dep));
            from_json.put("Reoccur", JSONObject.NULL);
            // place.put("Starttime", JSONObject.NULL);

            to_json.put("Longitude", "" + to.getLng());
            to_json.put("Latitude", "" + to.getLat());
            // place.put("ToleranceRadius", "25");
            // place.put("ToleranceDays", "3");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject json = loadJson("http://service.fahrgemeinschaft.de/trip?"
                + "searchOrigin=" + from_json + "&searchDestination=" + to_json);

        try {
            JSONArray results = json.getJSONArray("results");
            System.out.println("FOUND " + results.length() + " rides");

            String departure;
            String[] split;
            String who;
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject ride = results.getJSONObject(i);
                
                who = "mail=" + ride.getString("Contactmail");
                who += ";mobile=" + ride.getString("Contactmobile");
                who += ";landline=" + ride.getString("Contactlandline");

//              new Date(Long.parseLong(ride.getString("Enterdate"));
                departure = "000000000000";
                if (!ride.isNull("Starttime")) {
                    departure = ride.getString("Starttime");
                    if (departure.length() == 3)
                        departure = "0" + departure;
                    departure = ride.getString("Startdate") + departure;
                } else {
                    System.out.println("no start time!");
                }
                
                long price = 0;
                if (!ride.isNull("Price")) {
                    System.out.println("price : " + ride.getString("Price"));
                    price = Long.parseLong(ride.getString("Price"));
                }
                JSONArray routings = ride.getJSONArray("Routings");
                for (int j = 0; j < routings.length(); j++) {
                    JSONObject r = routings.getJSONObject(j);
                    JSONObject origin = routings.getJSONObject(j)
                            .getJSONObject("Origin");
                    JSONObject destination = routings.getJSONObject(j)
                            .getJSONObject("Destination");
                    System.out.println(origin.getString("Address")
                            +" --->  "+destination.getString("Address"));
                    
                    split = origin.getString("Address").split(", ");
                    from = store(new Place(
                                Double.parseDouble(origin.getString("Latitude")),
                                Double.parseDouble(origin.getString("Longitude")))
                            .address(origin.getString("Address"))
                            .name((split.length > 0)? split[0] : ""));
                    
                    split = destination.getString("Address").split(", ");
                    to = store(new Place(
                                Double.parseDouble(destination.getString("Latitude")),
                                Double.parseDouble(destination.getString("Longitude")))
                            .address(destination.getString("Address"))
                            .name((split.length > 0)? split[0] : ""));
                    
                    store(new Ride()
                        .type(Ride.OFFER)
                        .from(from).to(to)
                        .who(who).price(price)
                        .dep(fulldf.parse(departure))
                        .seats(ride.getLong("Places"))
                        .details(ride.getString("Description")));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
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
