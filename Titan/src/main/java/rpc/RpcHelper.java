package rpc;

import java.io.PrintWriter;

import javax.servlet.http.*;


import org.json.*;


public class RpcHelper {
	
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
