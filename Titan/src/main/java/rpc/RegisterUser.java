package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class RegisterUser
 */
@WebServlet("/register")
public class RegisterUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterUser() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		try {
			String userId = request.getParameter("user_id");
	        JSONObject obj = new JSONObject();
	        
	        DBConnection conn = DBConnectionFactory.getDBConnection();
	        
	        if (userId != null && !userId.isEmpty()) {
	            if (conn.isUserExists(userId)) {
	                obj.put("status", "Username already exists");
	            } else {
	                obj.put("status", "Username available");
	            }
	        } else {
	            obj.put("status", "Invalid input");
	        }
	        
	        RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
            JSONObject input = RpcHelper.readJsonObject(request);
            String userId = input.getString("user_id");
            String password = input.getString("password");
            String firstName = input.getString("first_name");
            String lastName = input.getString("last_name");
            
            System.out.println(password);

            DBConnection conn = DBConnectionFactory.getDBConnection();
            JSONObject obj = new JSONObject();

            if (conn.registerUser(userId, password, firstName, lastName)) {
                obj.put("status", "OK");
            } else {
                obj.put("status", "User creation failed. Username might already exist.");
                response.setStatus(400); // Bad Request
            }

            RpcHelper.writeJsonObject(response, obj);
        } catch (Exception e) {
            // Catch any exceptions and print the stack trace for debugging
            e.printStackTrace();
        }
	}

}
