package Fabflix;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class EditStar
 */
public class EditStar extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EditStar() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginPage.kickNonUsers(request, response);// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		HttpSession session = request.getSession();
		Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String starID = request.getParameter("starID");

		//Scrub Args
		value = value.replace("\'", "\\\'");
		field = field.replace("\'", "\\\'");
		
		
		//Kick non admins
		if (starID == null || isAdmin == null || !isAdmin) {
			response.sendRedirect("index.jsp");
		}

		try {
			Connection dbcon = ListResults.openConnection();

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
						if (field.equals("dob") && !isValidDate(value)) {
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
	
    public static boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date testDate = null;
        try {
            testDate = sdf.parse(date);
        } catch (ParseException e) {
            return false;
        }
        if (!sdf.format(testDate).equals(date)) {
            return false;
        }
        return true;

    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp");
	}
}
