package rpc;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.http.*;


import org.json.*;


public class RpcHelper {
	
	// Parses a JSONObject from http request.
		public static JSONObject readJsonObject(HttpServletRequest request) {
			
			StringBuffer sb = new StringBuffer();
			String line = null;
			
			try {
				
				BufferedReader reader = request.getReader();
				
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				
				reader.close();
				return new JSONObject(sb.toString());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}

	
	//Write a JSONObject to http response
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) {
		
		
		try {
			
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			
			PrintWriter out = response.getWriter();
			
			out.print(obj);
			out.flush();
			out.close();
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
	}
	
	//Write a JSONArray to http response
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) {
		
		try {
			
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			
			PrintWriter out = response.getWriter();
			
			out.print(array);
			out.flush();
			out.close();
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
	}

}
