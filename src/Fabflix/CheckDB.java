package Fabflix;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class CheckDB
 */
public class CheckDB extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CheckDB() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		LoginPage.kickNonUsers(request, response);// kick if not logged in

		ServletContext context = getServletContext();
		HttpSession session = request.getSession();
		
		// Kick non admins
		Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
		if (isAdmin == null || !isAdmin){
			response.sendRedirect("index.jsp");
		}

		response.setContentType("text/html"); // Response mime type

		PrintWriter out = response.getWriter(); // Setup output

		try {
			Integer option = 0;
			try {
				option = Integer.valueOf(request.getParameter("option"));
				if (option < 0 || option > 4) {
					option = 0;
				}
			} catch (NumberFormatException e) {
				option = 0;
			} catch (NullPointerException e) {
				option = 0;
			}

			// OPEN HTML
			session.setAttribute("title", "CheckDB");

			// HEADER
			out.println(ListResults.header(context, session));

			out.println(printOptionMenu());

			out.println("<BR>");
			
			String output;
			
			switch (option) {
			case 1:
				// Movies Errors
				output = printMovieWoStar();
				if (!output.isEmpty()){
					out.println("<H1>Missing Stars:</H1><BR>");
					out.println(output);
				}
				
				output = printMovieWoGenres();
				if (!output.isEmpty()){
					out.println("<H1>Missing Genres:</H1><BR>");
					out.println(output);
				}

				out.println("<H1>*Movies that are the same or almost the same.</H1><BR>");
				//TODO compare movies
				
				break;

			case 2:
				// Stars Errors
				output = printStarWoName();
				if (!output.isEmpty()){
					out.println("<H1>Missing First Or Last Name:</H1><BR>");
					out.println(output);
				}
				
				output = printInvlaidDOB();
				if (!output.isEmpty()){
					out.println("<H1>Date Of Birth Flagged:</H1><BR>");
					out.println(output);
				}
				
				output = printStarWoMovie();
				if (!output.isEmpty()){
					out.println("<H1>Missing Movies:</H1><BR>");
					out.println(output);
				}

				out.println("<H1>*Stars that are the same or almost the same.</H1><BR>");
				//TODO compare stars
				
				break;

			case 3:
				//Genre Errors
				output = printGenreWoMovie();
				if (!output.isEmpty()){
					out.println("<H1>Empty Genres:</H1><BR>");
					out.println(output);
				}

				out.println("<H1>*Genres that are the same or almost the same.</H1><BR>");
				//TODO compare genres
				
				break;

			case 4:
				//Customer Errors
				output  = printInvalidEmails();
				if (!output.isEmpty()){
					out.println("<H1>Invalid Email:</H1><BR>");
					out.println(output);
				}				

				output = printExpiredCC();
				if (!output.isEmpty()){
					out.println("<H1>Expired Credit Card:</H1><BR>");
					out.println(output);
				}
				
				//TODO check sales date in future
				
				break;

			default:
				break;
			}

			// FOOTER

			Connection dbcon = ListResults.openConnection();
			ListResults.footer(out, dbcon, 0);

			dbcon.close();

		}  catch (SQLException ex) {
			out.println(ListResults.header(context, session));
			while (ex != null) {
				out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} // end while
			out.println("</DIV></BODY></HTML>");
		} // end catch SQLException
		catch (java.lang.Exception ex) {
			out.println(ListResults.header(context, session));
			out.println("<P>SQL error in doGet: " + ex.getMessage() + "<br>"
					+ ex.toString() + "</P></DIV></BODY></HTML>");
			return;
		}
		out.close();
	}

	private String printOptionMenu() {
		// TODO reorganize menu with fewer options; e.g. Movie, Star, Genre,
		// Customer

		
		return "<div class=\"menu\">" +
				"	<ul class=\"main\">" +
				"		<li><a href=\"CheckDB?option=1\">Movie Warnings</a></li>" +
				"		<li><a href=\"CheckDB?option=2\">Star Warnings</a></li>" +
				"		<li><a href=\"CheckDB?option=3\">Genre Warnings</a></li>" +
				"		<li><a href=\"CheckDB?option=4\">Customer Warnings</a></li>" +
				"   </ul>" +
				"</div>";

	}

	private String printMovieWoStar() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = ListResults.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM movies m LEFT OUTER JOIN stars_in_movies s ON s.movie_id=m.id LEFT OUTER JOIN stars st ON s.star_id=st.id WHERE star_id IS NULL OR movie_id IS NULL";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each movie, DISPLAY INFORMATION
			Integer movieID;
			try {
				movieID = Integer.valueOf(searchResults.getString("id"));
			} catch (Exception e) {
				movieID = 0;
			}
			String title = searchResults.getString("title");
			Integer year = searchResults.getInt("year");
			String bannerURL = searchResults.getString("banner_url");

			rtn += printMovieSummary(movieID, title, year, bannerURL);

			// TODO add star to movie from movie page

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		return rtn;
	}

	private String printStarWoMovie() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = ListResults.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM stars s LEFT OUTER JOIN stars_in_movies si ON si.star_id=s.id LEFT OUTER JOIN movies m ON si.movie_id=m.id WHERE star_id IS NULL OR movie_id IS NULL";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each star, DISPLAY INFORMATION
			Integer starID;
			try {
				starID = Integer.valueOf(searchResults.getString("id"));
			} catch (Exception e) {
				starID = 0;
			}
			String first_name = searchResults.getString("first_name");
			String last_name = searchResults.getString("last_name");
			String photoURL = searchResults.getString("photo_url");

			rtn += printStarSummary(starID, first_name, last_name, photoURL);

			// TODO add movie to star from star page

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		return rtn;
	}

	private String printGenreWoMovie() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = ListResults.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM genres g LEFT OUTER JOIN genres_in_movies gi ON gi.genre_id=g.id LEFT OUTER JOIN movies m ON gi.movie_id=m.id WHERE genre_id IS NULL OR movie_id IS NULL";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each genre, DISPLAY INFORMATION
			String name = searchResults.getString("name");
			String id = searchResults.getString("id");

			// TODO make remove button remove empty genre
			// TODO make a remove all empty genres button
			rtn += "ID: " + id + "<BR>" + name + "<BR>Remove";

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		
		return rtn;
	}

	private String printMovieWoGenres() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = ListResults.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM movies m LEFT OUTER JOIN genres_in_movies g ON g.movie_id=m.id LEFT OUTER JOIN genres ge ON g.genre_id=ge.id WHERE movie_id IS NULL or genre_id IS NULL";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each movie, DISPLAY INFORMATION
			Integer movieID;
			try {
				movieID = Integer.valueOf(searchResults.getString("id"));
			} catch (Exception e) {
				movieID = 0;
			}
			String title = searchResults.getString("title");
			Integer year = searchResults.getInt("year");
			String bannerURL = searchResults.getString("banner_url");

			rtn += printMovieSummary(movieID, title, year, bannerURL);

			// TODO add genre to movie from movie page

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		
		return rtn;
	}

	private String printStarWoName() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = ListResults.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM stars s WHERE first_name = '' OR last_name = '' OR first_name IS NULL OR last_name IS NULL";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each star, DISPLAY INFORMATION
			Integer starID;
			try {
				starID = Integer.valueOf(searchResults.getString("id"));
			} catch (Exception e) {
				starID = 0;
			}
			String first_name = searchResults.getString("first_name");
			String last_name = searchResults.getString("last_name");
			String photoURL = searchResults.getString("photo_url");

			rtn += printStarSummary(starID, first_name, last_name, photoURL);

			// TODO add edit name to star page

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		
		return rtn;
	}

	private String printExpiredCC() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = ListResults.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM customers WHERE cc_id IN (SELECT id FROM creditcards WHERE expiration <= CURDATE() )";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each genre, DISPLAY INFORMATION
			String first_name = searchResults.getString("first_name");
			String last_name = searchResults.getString("last_name");
			String id = searchResults.getString("id");
			String email = searchResults.getString("email");
			String cc_id = searchResults.getString("cc_id");
			String address = searchResults.getString("address");

			// TODO edit CC for customer; maybe customer edit page?

			rtn += printCustomerSummary(id, first_name, last_name, email, cc_id, address);

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		
		return rtn;
	}

	private String printInvlaidDOB() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = ListResults.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM stars s WHERE dob <= '1900/01/01' OR dob >= CURDATE()";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each star, DISPLAY INFORMATION
			Integer starID;
			try {
				starID = Integer.valueOf(searchResults.getString("id"));
			} catch (Exception e) {
				starID = 0;
			}
			String first_name = searchResults.getString("first_name");
			String last_name = searchResults.getString("last_name");
			String photoURL = searchResults.getString("photo_url");

			// TODO add edit name to star page
			
			rtn += printStarSummary(starID, first_name, last_name, photoURL);

			rtn += "<BR><BR>";

		}

		searchResults.close();
		statement.close();
		
		return rtn;
	}

	private String printInvalidEmails() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = ListResults.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM customers WHERE id NOT IN (SELECT id FROM customers WHERE email REGEXP '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$')";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each genre, DISPLAY INFORMATION
			String first_name = searchResults.getString("first_name");
			String last_name = searchResults.getString("last_name");
			String id = searchResults.getString("id");
			String email = searchResults.getString("email");
			String cc_id = searchResults.getString("cc_id");
			String address = searchResults.getString("address");

			// TODO edit email for customer; maybe customer edit page?

			rtn += printCustomerSummary(id, first_name, last_name, email, cc_id, address);

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		
		return rtn;
	}

	public String printCustomerSummary(String id, String first_name, String last_name, String email, String cc_id, String address) {
		//TODO remove button
		return "ID: " + id + "<BR>Name: " + first_name + " " + last_name + "<BR>Email: " + email + "<BR>CC: " + cc_id + "<BR>Address: " + address;
	}

	public String printStarSummary(Integer starID, String first_name, String last_name, String photoURL) {
		//TODO remove and mergeWith buttons
		return "<a href=\"StarDetails?id=" + starID + "\"><img src=\"" + photoURL + "\" height=\"60\"> " + first_name + " " + last_name + "</a> ID: <a href=\"StarDetails?id=" + starID + "\">" + starID + "</a>";
	}

	public String printMovieSummary(Integer movieID, String title, Integer year, String bannerURL) {
		//TODO remove and mergeWith buttons
		return "<a href=\"MovieDetails?id=" + movieID + "\"><img src=\"" + bannerURL + "\" height=\"60\"> " + title + " (" + year + ")</a> ID: <a href=\"MovieDetails?id=" + movieID + "\">" + movieID + "</a>";
	}
}
