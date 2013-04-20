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

import org.teleportr.Connector;
import org.teleportr.ConnectorService;
import org.teleportr.Place;
import org.teleportr.Ride;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;



public class FahrgemeinschaftConnector implements Connector {

	private static final String APIKEY = "API-KEY"; "API-KEY";

	@Override
	public void getRides(Place from, Place to, Date dep, Date arr) {
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		
		JSONObject from_json = new JSONObject();
		JSONObject to_json = new JSONObject();
		try {
			from_json.put("Longitude", "" + from.getLng());
			from_json.put("Latitude", "" + from.getLat());
			from_json.put("Startdate", df.format(dep));
			from_json.put("Reoccur", JSONObject.NULL);
			//		place.put("Starttime", JSONObject.NULL);
			
			to_json.put("Longitude", "" + to.getLng());
			to_json.put("Latitude", "" + to.getLat());
		} catch (JSONException e) {
			e.printStackTrace();
		}
//		place.put("ToleranceRadius", "25");
//		place.put("ToleranceDays", "3");
		
		
		JSONObject json = loadJson("http://service.fahrgemeinschaft.de/trip?" +
				"searchOrigin=" + from_json + "&searchDestination=" + to_json);
		
		try {
			//System.out.println(json);
			JSONArray results = json.getJSONArray("results");
			System.out.println("FOUND " + results.length() + " rides");
			
			
			for (int i = 0; i < results.length(); i++) {
				JSONObject ride = results.getJSONObject(i);
				String description = ride.getString("Description");
				String tel = ride.getString("Contactlandline");
				JSONArray routings = ride.getJSONArray("Routings");
				JSONObject origin = routings.getJSONObject(0).getJSONObject("Origin");
				JSONObject destination = routings.getJSONObject(routings.length()-1).getJSONObject("Destination");
				
				from = new Place(
						Double.parseDouble(origin.getString("Latitude")),
						Double.parseDouble(origin.getString("Longitude")))
						.name(origin.getString("Address"));
				
				to = new Place(
						Double.parseDouble(destination.getString("Latitude")),
						Double.parseDouble(destination.getString("Longitude")))
						.name(destination.getString("Address"));
				
				
				//JSONArray routings = ride.getJSONArray("Routings");
//				Date enterDate = new Date(Long.parseLong(ride.getString("Enterdate")));

				Date now = new Date(); //df.parse(ride.getString("Startdate"));
				Calendar departure = Calendar.getInstance();
				departure.clear();
				departure.set(now.getYear()+1900, now.getMonth(), now.getDay());
				if (!ride.isNull("Starttime")) {
					String startTime = ride.getString("Starttime");
					int splitIdx = startTime.length() - 2;
					String hours = startTime.substring(0, splitIdx);
					String minutes = startTime.substring(splitIdx);
					departure.set(now.getYear()+1900, now.getMonth(), now.getDay(),
							Integer.parseInt(hours), Integer.parseInt(minutes), 0);
					System.out.println(departure.getTime());
				}
				
				new Ride().type(Ride.OFFER)
					.from(from).to(to).dep(departure.getTime());
				
				System.out.println(to);
				System.out.println(from);
				System.out.println();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void postRide(Place orig, Place dest, Date dep, Date arr) {
		
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


//"Triptype": "offer",
//"Smoker": "no",
//"Startdate": "20130419",
//"Accuracy": {
//  "DistanceDestination": 0,
//  "OverallDistance": 0,
//  "DistanceOrigin": 0
//},
//"TripID": "887b0da0-5a55-6e04-995d-367e06fffc7a",
//"Starttime": "1300",
//"Contactmail": "wuac@me.com",
//"Enterdate": "1366178563",
//"IDuser": "29ae8215-223f-63f4-9982-39b9aca69556",
//"Reoccur": {
//  "Saturday": false,
//  "Thursday": false,
//  "Monday": false,
//  "Tuesday": false,
//  "Wednesday": false,
//  "Friday": false,
//  "Sunday": false
//},
//"Deeplink": null,
//"Places": "3",
//"Prefgender": null,
//"Price": "0",
//"Privacy": {
//  "Name": "1",
//  "Landline": "1",
//  "Email": "1",
//  "Mobile": "1",
//  "NumberPlate": "1"
//},
//"Relevance": "10",
//"Partnername": null,
//"ClientIP": null,
//"NumberPlate": "",
//"Contactlandline": ""
