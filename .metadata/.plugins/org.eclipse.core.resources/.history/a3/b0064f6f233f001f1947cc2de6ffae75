package external;

import java.io.*;
import java.net.*;
import org.json.*;

public class TicketMasterAPI {
	
	private final static String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_TERM = ""; // no restriction
	private static final String API_KEY = System.getenv("TICKETMASTER_API_KEY");
	
	public JSONArray search(double lat, double lon, String term) {
		
		//Encoding term in url
		
		if (term == null) {
			term = DEFAULT_TERM;
		}
		
		try {
			term = java.net.URLEncoder.encode(term, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Convert Lat/Long to Geohash
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		//Construct API query
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, term, 50);
		
		try {
			// Open a HTTP connection between your Java application and TicketMaster based on url
			@SuppressWarnings("deprecation")
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			connection.setRequestMethod("GET");
			
			// Send request to TicketMaster and get response, response code could be
			// returned directly
			// response body is saved in InputStream of connection.
			int responseCode = connection.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + URL + "?" + query);
			System.out.println("Response Code : " + responseCode);
			// Now read response body to get events data
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			
			in.close();
			JSONObject obj = new JSONObject(response.toString());
			
			if (obj.isNull("_embedded")) {
				return new JSONArray();
			}
			
			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			return events;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new JSONArray();		
	}
	
	
	private void queryAPI(double lat, double lon) {
		
		JSONArray events = search(lat, lon, null);
		try {
		    for (int i = 0; i < events.length(); i++) {
		        JSONObject event = events.getJSONObject(i);
		        System.out.println(event);
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}

}
