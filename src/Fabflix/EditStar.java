package Fabflix;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class EditStar
 */
public class EditStar extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public EditStar() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in
		if (Login.kickNonAdmin(request, response)){return;}// kick if not admin

		response.setContentType("text/html"); // Response mime type

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String starID = request.getParameter("starID");

		//Scrub Args
		if (value != null){
			value = Database.cleanSQL(value);
		}
		
		if (starID == null) {
			response.sendRedirect("index.jsp");
			return;
		}else{
			starID = Database.cleanSQL(starID);
		}

		try {
			Connection dbcon = Database.openConnection();
			Statement statement = dbcon.createStatement();

			if (action != null && field != null && value != null) {
				if (action.equals("delete")) {// ==========DELETE
					if (field.equals("movie")) {
						String query = "DELETE FROM stars_in_movies WHERE star_id = '" + starID + "' AND movie_id = '" + value + "'";
						statement.executeUpdate(query);
					}
				} else if (action.equals("add")) {// ==========ADD
					if (field.equals("movie")) {
						String query = "INSERT INTO stars_in_movies VALUES(" + starID + ", " + value + ");";
						statement.executeUpdate(query);
					}
				} else if (action.equals("edit")) {// ==========EDIT
					if (field.equals("first_name") || field.equals("last_name") || field.equals("dob") || field.equals("photo_url")) {
						if (field.equals("dob") && !Database.isValidDate(value)) {
							response.sendRedirect("StarDetails?id=" + starID + "&edit=true");
							return;
						}
						String query = "UPDATE stars SET " + field + " = '" + value + "' WHERE id = '" + starID + "'";
						statement.executeUpdate(query);
					}
				}
			}
			dbcon.close();
		} catch (NamingException e) {
		} catch (SQLException e) {
		}

		response.sendRedirect("StarDetails?id=" + starID + "&edit=true");

	}
	
	public static void editStarLink(PrintWriter out, Integer starID, String oldVal, String field) {
		out.println("<form method=\"post\" action=\"EditStar\">" +
				"<input type=\"text\" name=\"value\" value=\""+oldVal+"\" />" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"edit\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=starID VALUE=\""+ starID+"\">" +
				"<button type=\"submit\" value=\"submit\">Change "+field+"</button>" +
				"</form>");
	}

	public static void addMovieLink(PrintWriter out, Integer starID, String field) {
		out.println("<form method=\"post\" action=\"EditStar\">" +
				"<input type=\"text\" name=\"value\" />" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"add\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=starID VALUE=\""+ starID+"\">" +
				"<button type=\"submit\" value=\"submit\">Add "+field+" ID</button>" +
				"</form>");
	}

	public static void removeMovieLink(PrintWriter out, Integer starID, Integer delID, String name) {
		out.println("<form method=\"post\" action=\"EditStar\">" +
				"<input type=\"HIDDEN\" name=\"value\" value=\""+ delID +"\"/>" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"movie\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=starID VALUE=\""+ starID+"\">" +
				"<button type=\"submit\" value=\"submit\">Remove "+name+"</button>" +
				"</form>");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("index.jsp");
	}
}
