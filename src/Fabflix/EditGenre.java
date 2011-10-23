package Fabflix;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class FixDB
 */
public class EditGenre extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EditGenre() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// if (LoginPage.kickNonUsers(request, response)) {return;}// kick if not logged in
		Login.kickNonUsers(request, response);
		Login.kickNonAdmin(request, response);

		response.setContentType("text/html"); // Response mime type

		HttpSession session = request.getSession();
		Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String genreID = request.getParameter("genreID");

		// Scrub Args
		if (value != null){
			value = ListResults.cleanSQL(value);
		}
		
		// Kick non admins
		if (isAdmin == null || !isAdmin) {
			response.sendRedirect("index.jsp");
			return;
		}

		try {
			Connection dbcon = ListResults.openConnection();

			Statement statement = dbcon.createStatement();

			//TODO genre editing
			if (action != null && field != null) {
				if (action.equals("delete")) {// ==========DELETE
					if (field.equals("genre") && genreID != null) {
						String query = "DELETE FROM genres WHERE id = '" + genreID + "'";
						statement.executeUpdate(query);
					}else if (field.equals("allEmpty")){
						//FIXME Delete from where genre is not in genres_in_movies 
						String query = "DELETE FROM genres WHERE id NOT IN (SELECT genre_id FROM genres_in_movies)";
						statement.executeUpdate(query);
					}
				}
				
				
				
			}
			
			
			
			dbcon.close();
		} catch (NamingException e) {
		} catch (SQLException e) {
		}

		try {
			String target = (String) session.getAttribute("EditGenre.dest");
			if (target != null) {
				session.removeAttribute("EditGenre.dest");
				response.sendRedirect(target);
				return;
			}
		} catch (Exception ignored) {
		}
		
		response.sendRedirect("CheckDB");// TODO correct return EditGenre.dest

	}
	public static String deleteGenreLink(Integer genreID, String name) {
		return "<form method=\"post\" action=\"EditGenre\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"genre\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=genreID VALUE=\""+ genreID+"\">" +
				"<button type=\"submit\" value=\"submit\">Delete "+name+"</button>" +
				"</form>";
	}
	public static String deleteAllEmptyGenreLink() {
		return "<form method=\"post\" action=\"EditGenre\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"allEmpty\">" +
				"<button type=\"submit\" value=\"submit\">Delete All Empty Genres</button>" +
				"</form>";
	}

	public static void savePath(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String URL = request.getRequestURL().toString();
		String qs = request.getQueryString();
		if (qs != null) {
			URL += "?" + qs;
		}
		// Save destination
		session.setAttribute("EditGenre.dest", URL);
	}

}
