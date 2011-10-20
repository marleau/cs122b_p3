package Fabflix;

import java.io.IOException;
import java.sql.*;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String movieID = request.getParameter("movieID");

		if (movieID == null) {
			response.sendRedirect("index.jsp");
		}
		
		try {
			Connection dbcon = ListResults.openConnection();

			Statement statement = dbcon.createStatement();

			if (action != null) {
				if (action.equals("delete")) {
					// TODO remove statements (genre, star)
				} else if (action.equals("add")) {
					// TODO add statements (genre,star)
				} else if (action.equals("edit")) {
					if (field.equals("title") || field.equals("year") || field.equals("director") || field.equals("banner_url") || field.equals("trailer_url")){
						if (field.equals("year")){
							try{
							Integer year = Integer.valueOf(value);
							value = year.toString();
							}catch(Exception e){
								response.sendRedirect("MovieDetails?id=" + movieID + "&edit=true");
							}
						}
						String query = "UPDATE movies SET "+field+" = '"+value+"' WHERE id = '"+movieID+"'";
						statement.executeUpdate(query);						
						// TODO edit statements
					}
				}
			}

		} catch (NamingException e) {
		} catch (SQLException e) {
		}

		response.sendRedirect("MovieDetails?id=" + movieID + "&edit=true");

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp");
	}
}
