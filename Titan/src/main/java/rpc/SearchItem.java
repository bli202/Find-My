package rpc;
import java.util.*;
import java.io.*;

import org.json.*;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		


		// TODO Auto-generated method stub
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		// Term can be empty or null.
		String term = request.getParameter("term");
		String userId = request.getParameter("user_id");

		
		DBConnection connection = DBConnectionFactory.getDBConnection();
		List<Item> items = connection.searchItems(lat, lon, term);
		List<JSONObject> list = new ArrayList<>();
		Set<String> favorite = connection.getFavoriteItemIds(userId);

		try {
			
			for (Item item : items) {
				// Add a thin version of item object
				JSONObject obj = item.toJSONObject();
				
				// This field is required by frontend to correctly display favorite items.
				obj.put("favorite", favorite.contains(item.getItemId()));
				
				list.add(obj);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JSONArray array = new JSONArray(list);
		RpcHelper.writeJsonArray(response, array);

	}



	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
