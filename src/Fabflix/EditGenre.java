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
		if (LoginPage.kickNonUsers(request, response)) {
			return;
		}// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		HttpSession session = request.getSession();
		Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String genreID = request.getParameter("genreID");

		// Scrub Args
		value = ListResults.cleanSQL(value);

		// Kick non admins
		if (genreID == null || isAdmin == null || !isAdmin) {
			response.sendRedirect("index.jsp");
			return;
		}

		try {
			Connection dbcon = ListResults.openConnection();

			Statement statement = dbcon.createStatement();

			//TODO genre editing
			
			
			
			
			dbcon.close();
		} catch (NamingException e) {
		} catch (SQLException e) {
		}

		response.sendRedirect("CheckDB");// TODO correct return EditGenre.dest

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
