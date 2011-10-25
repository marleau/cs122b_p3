package Fabflix;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;



public class Checkout extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in
		
		HttpSession session = request.getSession();
		if (request.getParameter("updateCart") != null) {
			ShoppingCart.updateCart(request, response);	
			session.setAttribute("updated", 1);
		} else
			session.removeAttribute("updated");	
		try {
			
			Connection dbcon = Database.openConnection();
			
			session.setAttribute("title", "Checkout");
			
			if (session.getAttribute("validCC") == null)
				session.setAttribute("validCC", false);
			else {
				if ((Boolean)session.getAttribute("validCC")) {
					session.removeAttribute("ccError");
					processOrder(request, dbcon);
				}
			}
			
			if (session.getAttribute("ccError") != null && (Boolean)session.getAttribute("ccError")) {
				session.removeAttribute("ccError");
			}
			
			Map<String, Integer> cart = (Map<String, Integer>)session.getAttribute("cart");
			
			if (session.getAttribute("processed") != null) {
				if ((Boolean)session.getAttribute("processed") && !cart.isEmpty()) {
					session.setAttribute("processed", false);
				}
			} else {
				session.setAttribute("processed", false);
			}
			
			dbcon.close();
			response.sendRedirect("checkout.jsp");
		} catch (Exception e) {
			
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Login.kickNonUsers(request, response);
		HttpSession session = request.getSession(true);// Get client session
		session.setAttribute("title", "Checkout");
		Map<String, Integer> cart = (Map<String, Integer>)session.getAttribute("cart");
		
		if (request.getParameter("updateCart") != null) {
			ShoppingCart.updateCart(request, response);	
			session.setAttribute("updated", 1);
		} else
			session.removeAttribute("updated");
		
		if (session.getAttribute("processed") != null) {
			if ((Boolean)session.getAttribute("processed") && !cart.isEmpty()) {
				session.setAttribute("processed", false);
			}
		} else {
			session.setAttribute("processed", false);
		}
		
		try {
			Connection dbcon = Database.openConnection();
			
			// validate credit card
			if (isValid(request)) {
				session.setAttribute("validCC", true);
				session.removeAttribute("ccError");
				processOrder(request, dbcon);
			} else {
				session.setAttribute("validCC", false);
			}
			
			response.sendRedirect("checkout.jsp");
		
		} catch (SQLException e) {} catch (Exception e) {}
	}
	
	public void processOrder(HttpServletRequest request, Connection db) {
		
		try {
			HttpSession session = request.getSession();
			session.setAttribute("validCC", false);
			session.removeAttribute("ccError");
			session.setAttribute("processed", true);
			Map<String,Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
			String userID = (String) session.getAttribute("user.id");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			String curDate = df.format(date);
			
			for (Map.Entry<String, Integer> entry : cart.entrySet()) {
				String movieID = entry.getKey();
				String query = "INSERT INTO sales (customer_id, movie_id, sales_date) VALUES ('" + userID + "', '" + movieID + "', '" + curDate + "');";
				System.out.println(query);
				Statement st = db.createStatement();
				for (int i = 0; i < entry.getValue(); i++) {
					st.executeUpdate(query);
				}
			}
			
			cart.clear();
			
		
		} catch (SQLException ex) {
			while (ex != null) {
				System.out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} 
		}
		
	}

	public static boolean isValid(HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			Connection dbcon = Database.openConnection();
			Statement statement = dbcon.createStatement();
			String firstName = request.getParameter("firstName");
			String lastName = request.getParameter("lastName");
			String id = request.getParameter("id");
			
			String expiration = request.getParameter("year") + "-" + request.getParameter("month") + "-" + request.getParameter("day");
			String query = "SELECT * FROM creditcards WHERE first_name='" + firstName + "' AND last_name='" + lastName + "' AND id='" + id + "' AND expiration='" + expiration + "';";
			ResultSet result = statement.executeQuery(query);
			if (result.next()) {
				session.setAttribute("validCC", true);
				session.removeAttribute("ccError");
				return true;
			} else {
				session.setAttribute("ccError", true);
				session.setAttribute("validCC", false);
				return false;
			}
		} catch (SQLException e) {
			System.out.println("MovieDB: Error\nSQL error in doGet: " + e.getMessage() + "\n" + e.toString());
		} catch (Exception e) {} 
		
		return false;
	}

}
