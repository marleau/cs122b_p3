package Fabflix;

import java.io.IOException;
import java.sql.*;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class EditMovie
 */
public class EditMovie extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EditMovie() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (LoginPage.kickNonUsers(request, response)){return;}// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		HttpSession session = request.getSession();
		Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String movieID = request.getParameter("movieID");

		//Scrub Args
		value = ListResults.cleanSQL(value);
		
		
		//Kick non admins
		if (movieID == null || isAdmin == null || !isAdmin) {
			response.sendRedirect("index.jsp");
		}

		try {
			Connection dbcon = ListResults.openConnection();

			Statement statement = dbcon.createStatement();

			if (action != null && field != null && value != null) {
				if (action.equals("delete")) {// ==========DELETE
					if (field.equals("genre")) {
						String query = "DELETE FROM genres_in_movies WHERE genre_id = '" + value + "' AND movie_id = '" + movieID + "'";
						statement.executeUpdate(query);
					} else if (field.equals("star")) {
						String query = "DELETE FROM stars_in_movies WHERE star_id = '" + value + "' AND movie_id = '" + movieID + "'";
						statement.executeUpdate(query);
					}
				} else if (action.equals("add")) {// ==========ADD
					if (field.equals("genre")) {
						//TODO Add genre based on name and merge with similar, because ID is not shown
						String query = "INSERT INTO genres_in_movies VALUES(" + value + ", " + movieID + ");";
						statement.executeUpdate(query);
					} else if (field.equals("star")) {
						String query = "INSERT INTO stars_in_movies VALUES(" + value + ", " + movieID + ");";
						statement.executeUpdate(query);
					}
				} else if (action.equals("edit")) {// ==========EDIT
					if (field.equals("title") || field.equals("year") || field.equals("director") || field.equals("banner_url") || field.equals("trailer_url")) {
						if (field.equals("year")) {
							try {
								Integer year = Integer.valueOf(value);
								value = year.toString();
							} catch (Exception e) {
								response.sendRedirect("MovieDetails?id=" + movieID + "&edit=true");
								return;
							}
						}
						String query = "UPDATE movies SET " + field + " = '" + value + "' WHERE id = '" + movieID + "'";
						statement.executeUpdate(query);
					}
				}
			}
			dbcon.close();
		} catch (NamingException e) {
		} catch (SQLException e) {
		}

		response.sendRedirect("MovieDetails?id=" + movieID + "&edit=true");

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp");
	}
}
