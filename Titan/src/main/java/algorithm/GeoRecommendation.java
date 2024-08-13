package algorithm;

import java.util.*;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	
	public List<Item> recommendItems(String userId, double lat, double lon) {
		
		List<Item> recommendedItems = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getDBConnection();

		// step 1 Get all favorited items
		Set<String> favoriteItems = conn.getFavoriteItemIds(userId);
		
        // step 2 Get all categories of favorited items, sort by count
		Map<String, Integer> allCategories = new HashMap<>(); // step 2
		
		for (String item : favoriteItems) {
			
			Set<String> categories = conn.getCategories(item); // db queries
			
			for (String category : categories) {
				if (allCategories.containsKey(category)) {
					allCategories.put(category, allCategories.get(category) + 1);
				} else {
					allCategories.put(category, 1);
				}
			}
			
		}
		
		List<Map.Entry<String, Integer>> categoryList = new ArrayList<Map.Entry<String, Integer>>(allCategories.entrySet());
		
		Collections.sort(categoryList, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return Integer.compare(o2.getValue(), o1.getValue());
			}
		});

		// Step 3, do search based on category, filter out favorited events, sort by distance
		Set<Item> visitedItems = new HashSet<>();
		
		for (Map.Entry<String, Integer> category : categoryList) {
			
			List<Item> items = conn.searchItems(lat, lon, category.getKey());
			List<Item> filteredItems = new ArrayList<>();
			
			for (Item item : items) {
				if (!favoriteItems.contains(item.getItemId()) && !visitedItems.contains(item)) {
					filteredItems.add(item);
				}
			}
			
			Collections.sort(filteredItems, new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					// return the increasing order of distance.
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
			});
			
			visitedItems.addAll(items);
			recommendedItems.addAll(filteredItems);
		}

		return recommendedItems;
  }	
}
