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

    public static final int TYPE_OFFER_REOCCURING = 53;

    private static final String ID = "/id/";
    private static final String PUT = "PUT";
    private static final String POST = "POST";
    private static final String TRIP = "/trip";
    private static final String DELETE = "DELETE";
    private static final String SESSION = "/session";
    private static final String ZERO = "0";
    private static final String EMPTY = "";
    private static final String COMMA = ", ";
    private static final String NULL = "null";
    private static final String NOTIME = "2359";

    private static final String USER = "user";
    private static final String AUTH = "auth";
    private static final String LOGIN = "login";
    private static final String APIKEY = "apikey";
    private static final String PASSWD = "Password";
    private static final String AUTHKEY = "authkey";
    private static final String AUTH_KEY = "AuthKey";

    private static final String FAHRGEMEINSCHAFT_DE
            = "http://service.fahrgemeinschaft.de";
    public String endpoint =  FAHRGEMEINSCHAFT_DE;

    private String startDate;
    static final SimpleDateFormat fulldf =
            new SimpleDateFormat("yyyyMMddHHmm", Locale.GERMAN);
    static final SimpleDateFormat df =
            new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
    public static final String[] DAYS = new String[] { "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    @Override
    public String authenticate(String credential) throws Exception {
        System.out.println("refreshing authtoken");
        HttpURLConnection post = (HttpURLConnection)
                new URL(endpoint + SESSION).openConnection();
        post.setRequestProperty(APIKEY, Secret.APIKEY);
        post.setDoOutput(true);
        post.getOutputStream().write(new JSONObject()
                    .put(EMAIL, get(LOGIN))
                    .put(PASSWD, credential)
                    .toString().getBytes());
        post.getOutputStream().close();
        JSONObject json = loadJson(post);
        if (post.getResponseCode() == 403)
            throw new AuthException();
        JSONObject auth = json.getJSONObject(AUTH);
        set(USER, auth.getString(ID_USER));
        JSONArray kvp = json.getJSONObject(USER)
                .getJSONArray(KEY_VALUE_PAIRS);
        for (int i = 1; i < kvp.length(); i++) {
            String key = kvp.getJSONObject(i).getString(KEY);
            if (key.equals(FIRSTNAME))
                set(FIRSTNAME, kvp.getJSONObject(i).getString(VALUE));
            else if (key.equals(LASTNAME))
                set(LASTNAME, kvp.getJSONObject(i).getString(VALUE));
        }
        return auth.getString(AUTH_KEY);
    }

    private static final String RADIUS_TO = "radius_to";
    private static final String RADIUS_FROM = "radius_from";
    private static final String SEARCH_ORIGIN = "?searchOrigin=";
    private static final String TOLERANCE_RADIUS = "ToleranceRadius";
    private static final String SEARCH_DESTINATION = "&searchDestination=";
    private static final String RESULTS = "results";
    private static final String REOCCUR = "Reoccur";
    private static final String STARTDATE = "Startdate";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";

    @Override
    public long search(Ride query) throws Exception {
        HttpURLConnection get;
        if (query == null) { // myrides
            get = (HttpURLConnection) new URL(endpoint + TRIP).openConnection();
        } else {
            JSONObject from_json = new JSONObject();
            JSONObject to_json = new JSONObject();
            startDate = df.format(query.getDep());
            try {
                from_json.put(LONGITUDE, query.getFrom().getLng());
                from_json.put(LATITUDE, query.getFrom().getLat());
                from_json.put(STARTDATE, df.format(query.getDep()));
                from_json.put(REOCCUR, JSONObject.NULL);
                from_json.put(TOLERANCE_RADIUS, get(RADIUS_FROM));
                // place.put("Starttime", JSONObject.NULL);
                to_json.put(LONGITUDE, query.getTo().getLng());
                to_json.put(LATITUDE, query.getTo().getLat());
                to_json.put(TOLERANCE_RADIUS, get(RADIUS_TO));
                // place.put("ToleranceDays", "3");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            get = (HttpURLConnection) new URL(new StringBuffer()
                    .append(endpoint).append(TRIP)
                    .append(SEARCH_ORIGIN).append(from_json)
                    .append(SEARCH_DESTINATION).append(to_json)
                    .toString()).openConnection();
        }
        get.setRequestProperty(APIKEY, Secret.APIKEY);
        if (getAuth() != null)
            get.setRequestProperty(AUTHKEY, getAuth());
        JSONObject json = loadJson(get);
        if (get.getResponseCode() == 403)
            throw new AuthException();
        if (json != null) {
            JSONArray results = json.getJSONArray(RESULTS);
            System.out.println("FOUND " + results.length() + " rides");

            for (int i = 0; i < results.length(); i++) {
                store(parseRide(results.getJSONObject(i)));
            }
        }
        startDate = null;
        if (query != null)
            return getNextDayMorning(query.getDep());
        else return 0;
    }

    public static long getNextDayMorning(long dep) {
        if (dep != 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dep + 24 * 3600000); // plus one day
            c.set(Calendar.HOUR_OF_DAY, 0); // reset
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } else return 0;
    }

    private static final String KEY = "Key";
    private static final String VALUE = "Value";
    private static final String LASTNAME = "lastname";
    private static final String FIRSTNAME = "firstname";
    private static final String KEY_VALUE_PAIRS = "KeyValuePairs";
    private static final String ID_USER = "IDuser";

    private static final String OFFER = "offer";
    private static final String TRIP_ID = "TripID";
    private static final String TRIP_ID_WITH_SMALL_t = "tripID"; //wtf!
    private static final String TRIPTYPE = "Triptype";

    private static final String EMAIL = "Email";
    private static final String MOBILE = "Mobile";
    private static final String LANDLINE = "Landline";
    private static final String PLATE = "NumberPlate";
    private static final String CONTACTMAIL = "Contactmail";
    private static final String CONTACTMOBILE = "Contactmobile";
    private static final String CONTACTLANDLINE = "Contactlandline";
    private static final String ADDRESS = "Address";
    private static final String STARTTIME = "Starttime";
    private static final String DESTINATION = "Destination";
    private static final String ORIGIN = "Origin";
    private static final String ROUTINGS = "Routings";
    private static final String PRICE = "Price";
    private static final String BAHN = "Bahn";
    private static final String RELEVANCE = "Relevance";
    private static final String COMMENT = "Comment";
    private static final String DESCRIPTION = "Description";
    private static final String PRIVACY = "Privacy";
    private static final String PLACES = "Places";
    private static final String DE = "DE";
    private static final String GEO = "geo";
    private static final String PLACETYPE = "Placetype";
    private static final String DEUTSCHLAND = "Deutschland";
    private static final String COUNTRY_CODE = "CountryCode";
    private static final String COUNTRY_NAME = "CountryName";
    private static final String ROUTING_INDEX = "RoutingIndex";
    

    private Ride parseRide(JSONObject json)  throws JSONException {

        Ride ride = new Ride().type(Ride.OFFER).mode(Mode.CAR);
        if (startDate == null) ride.marked(); // myrides
        ride.who(json.getString(ID_USER));
        String value = json.getString(CONTACTMAIL);
        if (!value.equals(EMPTY) && !value.equals(NULL))
            ride.set(EMAIL, value);
        value = json.getString(CONTACTMOBILE);
        if (!value.equals(EMPTY) && !value.equals(NULL))
            ride.set(MOBILE, value);
        value = json.getString(CONTACTLANDLINE);
        if (!value.equals(EMPTY) && !value.equals(NULL))
            ride.set(LANDLINE, value);
        value = json.getString(PLATE);
        if (!value.equals(EMPTY) && !value.equals(NULL)) {
            ride.set(PLATE, value);
            if (value.equals(BAHN))
                ride.mode(Mode.TRAIN);
        }
        ride.getDetails().put(PRIVACY, json.getJSONObject(PRIVACY));
        ride.set(COMMENT, json.getString(DESCRIPTION));
        if (json.getInt(RELEVANCE) == 10) {
            ride.activate();
        } else {
            ride.deactivate();
        }
        ride.ref(json.getString(TRIP_ID));
        ride.seats(json.getInt(PLACES));
        if (!json.isNull(PRICE)) {
            ride.price((int) Double.parseDouble(
                    json.getString(PRICE)) * 100);
        } else {
            ride.price(-1);
        }

        JSONObject reoccur = json.getJSONObject(REOCCUR);
        boolean isReoccuring = isReoccuring(reoccur);
        boolean isMyride = startDate == null;
        String time = parseTime(json);

        if (isReoccuring) {
            ride.getDetails().put(REOCCUR, reoccur);
        }
        if (isReoccuring && isMyride) {
            ride.type(TYPE_OFFER_REOCCURING);
        }
        if (isMyride) {
            ride.dep(parseDate(json.getString(STARTDATE) + time));
        } else {
            ride.dep(parseDate(startDate + time));
        }

        JSONArray routings = json.getJSONArray(ROUTINGS);

        ride.from(store(parsePlace(routings.getJSONObject(0)
                .getJSONObject(ORIGIN))));

        for (int j = 1; j < routings.length(); j++) {
            ride.via(store(parsePlace(routings.getJSONObject(j)
                    .getJSONObject(DESTINATION))));
        }

        ride.to(store(parsePlace(routings.getJSONObject(0)
                .getJSONObject(DESTINATION))));
        return ride;
    }

    public boolean isReoccuring(JSONObject reoccur) throws JSONException {
        boolean isReoccuring = false;
        for (int i = 0; i < 7; i++) {
            if (reoccur.getBoolean(FahrgemeinschaftConnector.DAYS[i])) {
                isReoccuring = true;
                break;
            }
        }
        return isReoccuring;
    }

    private Place parsePlace(JSONObject json) throws JSONException {
        String[] split = json.getString(ADDRESS).split(COMMA);
        return new Place(
                    Double.parseDouble(json.getString(LATITUDE)),
                    Double.parseDouble(json.getString(LONGITUDE)))
                .address(json.getString(ADDRESS))
                .name((split.length > 0)? split[0] : EMPTY);
    }

    private String parseTime(JSONObject json) throws JSONException {
//              new Date(Long.parseLong(ride.getString("Enterdate"));
        String time = NOTIME;
        if (!json.isNull(STARTTIME)) {
            time = json.getString(STARTTIME);
            if (time.length() == 3)
                time = ZERO + time;
            if (time.length() != 4)
                time = NOTIME;
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
            post = (HttpURLConnection) new URL(endpoint + TRIP)
                .openConnection();
            post.setRequestMethod(POST);
        } else {
            post = (HttpURLConnection) new URL(new StringBuffer()
                    .append(endpoint).append(TRIP).append(ID)
                    .append(offer.getRef()).toString()).openConnection();
            post.setRequestMethod(PUT);
        }
        post.setRequestProperty(APIKEY, Secret.APIKEY);
        if (getAuth() != null)
            post.setRequestProperty(AUTHKEY, getAuth());
        post.setDoOutput(true);
        JSONObject json = new JSONObject();
//        json.put("Smoker", "no");
        json.put(TRIPTYPE, OFFER);
        json.put(TRIP_ID, offer.getRef());
        json.put(ID_USER, get(USER));
        if (offer.getMode() != null && offer.getMode().equals(Mode.TRAIN)) {
            json.put(PLATE, BAHN);
        } else {
            json.put(PLATE, offer.get(PLATE));
        }
        if (offer.isActive()) {
            json.put(RELEVANCE, 10);
        } else {
            json.put(RELEVANCE, 0);
        }
        json.put(PLACES, offer.getSeats());
        json.put(PRICE, offer.getPrice() / 100);
        json.put(CONTACTMAIL, offer.get(EMAIL));
        json.put(CONTACTMOBILE, offer.get(MOBILE));
        json.put(CONTACTLANDLINE, offer.get(LANDLINE));
        String dep = fulldf.format(offer.getDep());
        json.put(STARTDATE, dep.subSequence(0, 8));
        json.put(STARTTIME, dep.subSequence(8, 12));
        json.put(DESCRIPTION, offer.get(COMMENT));
        if (!offer.getDetails().isNull(PRIVACY))
            json.put(PRIVACY, offer.getDetails().getJSONObject(PRIVACY));
        if (!offer.getDetails().isNull(REOCCUR))
            json.put(REOCCUR, offer.getDetails().getJSONObject(REOCCUR));
        ArrayList<JSONObject> routings = new ArrayList<JSONObject>();
        List<Place> stops = offer.getPlaces();
        int max = stops.size() - 1;
        for (int dest = max; dest >= 0 ; dest--) {
            for (int orig = 0; orig < dest; orig++) {
                int idx = (orig == 0? (dest == max? 0 : dest) : - dest);
                JSONObject route = new JSONObject();
                route.put(ROUTING_INDEX, idx);
                route.put(ORIGIN, place(stops.get(orig)));
                route.put(DESTINATION, place(stops.get(dest)));
                routings.add(route);
            }
        }
        json.put(ROUTINGS, new JSONArray(routings));
        OutputStreamWriter out = new OutputStreamWriter(post.getOutputStream());
        out.write(json.toString());
        out.flush();
        out.close();
        JSONObject response = loadJson(post);
        if (!response.isNull(TRIP_ID_WITH_SMALL_t)) {
            offer.ref(response.getString(TRIP_ID_WITH_SMALL_t));
        }
        return offer.getRef();
    }

    private JSONObject place(Place from) throws JSONException {
        JSONObject place = new JSONObject();
        place.put(LATITUDE, from.getLat());
        place.put(LONGITUDE, from.getLng());
        place.put(ADDRESS, from.getAddress());
        place.put(COUNTRY_NAME, DEUTSCHLAND);
        place.put(COUNTRY_CODE, DE);
        place.put(PLACETYPE, GEO);
        return place;
    }

    @Override
    public String delete(Ride offer) throws Exception {
        HttpURLConnection delete = (HttpURLConnection) new URL(
                new StringBuffer().append(endpoint).append(TRIP)
                    .append(ID).append(offer.getRef())
                    .toString()).openConnection();
        delete.setRequestMethod(DELETE);
        delete.setRequestProperty(AUTHKEY, getAuth());
        delete.setRequestProperty(APIKEY, Secret.APIKEY);
        return loadJson(delete).toString();
    }
}
