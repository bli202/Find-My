package db.mysql;

import java.sql.*;
import java.util.*;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection{
	
	private Connection conn;
	
	public MySQLConnection() {
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (conn != null) {
			
			try {
				
				conn.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return;
		}
		
		String query = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
	  
	    try {
	            PreparedStatement statement = conn.prepareStatement(query);
	            for (String itemId : itemIds) {
	        	  
	                statement.setString(1, userId);
	                statement.setString(2, itemId);
	                statement.execute();
	              
	            }
	          
	    } catch (SQLException e) {
	            e.printStackTrace();
	    }
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		
		if (conn == null) {
			return;
		}
		
		String query = "DELETE FROM history WHERE user_id = ? and item_id = ?";
		
		try {
			
			PreparedStatement statement = conn.prepareStatement(query);
			
			for (String itemId : itemIds) {
				
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.execute();
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		
		if (conn == null) {
			return new HashSet<>();
		}
		
		Set<String> favoriteItems = new HashSet<>();
		
	    try {
	    	
	      String sql = "SELECT item_id from history WHERE user_id = ?";
	      PreparedStatement statement = conn.prepareStatement(sql);
	      statement.setString(1, userId);
	      ResultSet rs = statement.executeQuery();
	      
	      while (rs.next()) {
	    	  
	        String itemId = rs.getString("item_id");
	        favoriteItems.add(itemId);
	        
	      }
	      
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    
	    return favoriteItems;

	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		
		// TODO Auto-generated method stub
		if (conn == null) {
            return new HashSet<>();
		}
		
		Set<String> itemIds = getFavoriteItemIds(userId);
		Set<Item> favoriteItems = new HashSet<>();
		
		try {
			
			for (String itemId : itemIds) {
				
				String sql = "SELECT * from items WHERE item_id = ? ";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();
				ItemBuilder builder = new ItemBuilder();
		
				// Because itemId is unique and given one item id there should
				// have
				// only one result returned.
				if (rs.next()) {
					
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setRating(rs.getDouble("rating"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					
				}
				
				favoriteItems.add(builder.build());
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItems;

	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return null;
		}
		
		Set<String> categories = new HashSet<>();
		
		try {
			
			String sql = "SELECT category from categories WHERE item_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				
				categories.add(rs.getString("category"));
				
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return categories;

	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		
		// TODO Auto-generated method stub
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		
		for (Item item : items) {
			// Save the item into our own db.
			saveItem(item);
			
		}
		
		return items;
	}

	@Override
	public void saveItem(Item item) {
		// TODO Auto-generated method stub
		
		if (conn == null) {
			return;
		}
		
		try {
			// First, insert into items table
			String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?,?,?)";

			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setDouble(3, item.getRating());
			statement.setString(4, item.getAddress());
			statement.setString(5, item.getImageUrl());
			statement.setString(6, item.getUrl());
			statement.setDouble(7, item.getDistance());
			statement.execute();

			// Second, update categories table for each category.
			sql = "INSERT IGNORE INTO categories VALUES (?,?)";
			
			for (String category : item.getCategories()) {
				
				statement = conn.prepareStatement(sql);
				statement.setString(1, item.getItemId());
				statement.setString(2, category);
				statement.execute();
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return null;
		}
		
		String name = "";
		
		try {
			
			String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			
			if (rs.next()) {
				name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return name;

	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		
		// TODO Auto-generated method stub
		
		if (conn == null) {
			return false;
		}
		
		try {
			
			String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();

	        if (rs.next()) {
	            return true;
	        }
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return false;
	}
	
	@Override
	public boolean registerUser(String userId, String password, String firstName, String lastName) {
		
		if (conn == null) {
			return false;
		}
		
		String sql = "INSERT INTO users (user_id, password, first_name, last_name) VALUES (?, ?, ?, ?)";
		
		try {
	        PreparedStatement statement = conn.prepareStatement(sql);
	        statement.setString(1, userId);
	        // Use a hashing method like MD5, SHA, or bcrypt here for security
	        statement.setString(2, password);
	        statement.setString(3, firstName);
	        statement.setString(4, lastName);
	        return statement.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
    
    @Override
    public boolean isUserExists(String userId) {
        if (conn == null) {
            return false;
        }

        String sql = "SELECT user_id FROM users WHERE user_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            return rs.next(); // If there's a result, the user exists
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
