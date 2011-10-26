package Fabflix;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

// Servlet implementation class CheckDB
public class CheckDB extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CheckDB() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)) {
			return;
		}// kick if not logged in
		if (Login.kickNonAdmin(request, response)) {
			return;
		}// kick if not admin
		HttpSession session = request.getSession();
		ServletContext context = getServletContext();

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
			Integer sub = 0;
			try {
				sub = Integer.valueOf(request.getParameter("sub"));
				if (sub < 0 || sub > 4) {
					sub = 0;
				}
			} catch (NumberFormatException e) {
				sub = 0;
			} catch (NullPointerException e) {
				sub = 0;
			}

			// OPEN HTML
			session.setAttribute("title", "CheckDB");

			// HEADER
			out.println(Page.header(context, session));

			out.println(printOptionMenu());
			if (option > 0) {
				out.println(printSubMenu(option));
			}

			out.println("<BR>");

			switch (option) {
			case 1:
				// Movies Errors
				switch (sub) {
				case 1:
					savePath(request);
					out.println("<H1>Missing Stars:</H1><BR>");
					out.println(printMovieWoStar());
					break;

				case 2:
					savePath(request);
					out.println("<H1>Missing Genres:</H1><BR>");
					out.println(printMovieWoGenres());
					break;

				case 3:
					savePath(request);
					out.println("<H1>Duplicate Movies:</H1><BR>");
					out.println(printSimilarMoivies());
					break;

				default:
					break;
				}
				break;

			case 2:
				// Stars Errors
				switch (sub) {
				case 1:
					savePath(request);
					out.println("<H1>Missing First Or Last Name:</H1><BR>");
					out.println(printStarWoName());
					break;

				case 2:
					savePath(request);
					out.println("<H1>Date Of Birth Flagged:</H1><BR>");
					out.println(printInvlaidDOB());
					break;

				case 3:
					savePath(request);
					out.println("<H1>Missing Movies:</H1><BR>");
					out.println(printStarWoMovie());
					break;

				case 4:
					savePath(request);
					out.println("<H1>Duplicate Stars:</H1><BR>");
					out.println(printSimilarStar());
					break;

				default:
					break;
				}
				break;

			case 3:
				// Genre Errors
				switch (sub) {
				case 1:
					savePath(request);
					out.println("<H1>Empty Genres:</H1><BR>");
					out.println(printGenreWoMovie());
					break;

				case 2:
					savePath(request);
					out.println("<H1>Duplicate Genres:</H1><BR>");
					out.println(printSimilarGenres());
					break;

				default:
					savePath(request);
					out.println("<H1>All Genres:</H1><BR>");
					out.println(printGenres());
					break;
				}
				break;

			case 4:
				// Customer Errors
				switch (sub) {
				case 1:
					savePath(request);
					out.println("<H1>Invalid Email:</H1><BR>");
					out.println(printInvalidEmails());
					break;

				case 2:
					savePath(request);
					out.println("<H1>Expired Credit Card:</H1><BR>");
					out.println(printExpiredCC());
					break;

				default:
					break;
				}
				break;

			default:
				break;
			}

			// FOOTER

			Connection dbcon = Database.openConnection();
			Page.footer(out);

			dbcon.close();

		} catch (SQLException ex) {
			out.println(Page.header(context, session));
			while (ex != null) {
				out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} // end while
			out.println("</DIV></BODY></HTML>");
		} // end catch SQLException
		catch (java.lang.Exception ex) {
			out.println(Page.header(context, session));
			out.println("<P>SQL error in doGet: " + ex.getMessage() + "<br>" + ex.toString() + "</P></DIV></BODY></HTML>");
			return;
		}
		out.close();
	}

	private String printGenres() throws NamingException, SQLException, UnsupportedEncodingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM genres ORDER BY name";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each genre, DISPLAY INFORMATION
			String name = searchResults.getString("name");
			int genreID = searchResults.getInt("id");

			rtn += "<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(name, "UTF-8") + "\">" + name + "</a><BR>ID: " + genreID + "<BR>";

			rtn += EditGenre.renameGenreLink(name, genreID);

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printOptionMenu() {
		return "<div class=\"menu\">" + "	<ul class=\"main\">" + "		<li><a href=\"CheckDB?option=1\">Movie Warnings</a></li>"
				+ "		<li><a href=\"CheckDB?option=2\">Star Warnings</a></li>" + "		<li><a href=\"CheckDB?option=3\">Genre Management</a></li>"
				+ "		<li><a href=\"CheckDB?option=4\">Customer Warnings</a></li>" + "   </ul>" + "</div>";
	}

	private String printSubMenu(Integer option) {
		String rtn = "";
		switch (option) {
		case 1:
			rtn = "<div class=\"admin-menu\">" + "	<ul class=\"admin-main\">" + "		<li>Movie</li>" + "		<li><a href=\"CheckDB?option=1&sub=1\">Missing Star</a></li>"
					+ "		<li><a href=\"CheckDB?option=1&sub=2\">Missing Genre</a></li>" + "		<li><a href=\"CheckDB?option=1&sub=3\">Duplicate Movies</a></li>"
					+ "   </ul>" + "</div>";
			break;
		case 2:
			rtn = "<div class=\"admin-menu\">" + "	<ul class=\"admin-main\">" + "		<li>Star</li>" + "		<li><a href=\"CheckDB?option=2&sub=1\">Missing Name</a></li>"
					+ "		<li><a href=\"CheckDB?option=2&sub=2\">DOB Warning</a></li>" + "		<li><a href=\"CheckDB?option=2&sub=3\">Missing Movies</a></li>"
					+ "		<li><a href=\"CheckDB?option=2&sub=4\">Duplicate Stars</a></li>" + "   </ul>" + "</div>";
			break;
		case 3:
			rtn = "<div class=\"admin-menu\">" + "	<ul class=\"admin-main\">" + "		<li>Genre</li>" + "		<li><a href=\"CheckDB?option=3&sub=1\">Empty Genre</a></li>"
					+ "		<li><a href=\"CheckDB?option=3&sub=2\">Duplicate Genre</a></li>" + "   </ul>" + "</div>";
			break;
		case 4:
			rtn = "<div class=\"admin-menu\">" + "	<ul class=\"admin-main\">" + "		<li>Customer</li>" + "		<li><a href=\"CheckDB?option=4&sub=1\">Invalid Email</a></li>"
					+ "		<li><a href=\"CheckDB?option=4&sub=2\">Invalid CC</a></li>" + "   </ul>" + "</div>";
			break;

		default:
			break;
		}
		return rtn;
	}

	private String printMovieWoStar() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM movies WHERE id NOT IN (SELECT movie_id FROM stars_in_movies) ORDER BY title";
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

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printStarWoMovie() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM stars WHERE id NOT IN (SELECT star_id FROM stars_in_movies) ORDER BY last_name";
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

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printGenreWoMovie() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM genres WHERE id NOT IN (SELECT genre_id FROM genres_in_movies) ORDER BY name";
		ResultSet searchResults = statement.executeQuery(query);
		searchResults.last();
		if (searchResults.getRow() > 0) {
			rtn += EditGenre.deleteAllEmptyGenreLink();
		}
		searchResults.beforeFirst();
		rtn += "<BR><BR>";
		while (searchResults.next()) {// For each genre, DISPLAY INFORMATION
			String name = searchResults.getString("name");
			int id = searchResults.getInt("id");

			rtn += name + "<BR>ID: " + id + " ";
			rtn += EditGenre.deleteGenreLink(id, name);

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printSimilarGenres() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT *, COUNT(name) cnt FROM genres GROUP BY SOUNDEX(name) HAVING COUNT(SOUNDEX(name)) > 1 ORDER BY name";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each genre, DISPLAY INFORMATION
			String name = searchResults.getString("name");
			int count = searchResults.getInt("cnt");

			rtn += name + "<BR>Count: " + count + "<BR>";

			rtn += EditGenre.mergeGenreLink(name);

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printMovieWoGenres() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM movies WHERE id NOT IN (SELECT movie_id FROM genres_in_movies) ORDER BY title";
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

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printSimilarMoivies() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT *,COUNT(*) cnt FROM movies GROUP BY SOUNDEX(title),year HAVING COUNT(SOUNDEX(title)) > 1 ORDER BY title";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each movie, DISPLAY INFORMATION
			Integer movieID = searchResults.getInt("id");
			String title = searchResults.getString("title");
			Integer year = searchResults.getInt("year");
			String bannerURL = searchResults.getString("banner_url");
			Integer count = searchResults.getInt("cnt");

			rtn += printSimilarMovieSummary(movieID, title, year, bannerURL, count);
			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printStarWoName() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM stars s WHERE first_name = '' OR last_name = '' OR first_name IS NULL OR last_name IS NULL ORDER BY last_name";
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

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printSimilarStar() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT *,COUNT(*) cnt FROM stars GROUP BY SOUNDEX(first_name),SOUNDEX(last_name),dob HAVING COUNT(SOUNDEX(first_name)) > 1 ORDER BY last_name";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each star, DISPLAY INFORMATION
			Integer starID = searchResults.getInt("id");
			String first_name = searchResults.getString("first_name");
			String last_name = searchResults.getString("last_name");
			String photoURL = searchResults.getString("photo_url");
			String dob = searchResults.getString("dob");
			Integer count = searchResults.getInt("cnt");

			rtn += printSimilarStarSummary(starID, first_name, last_name, photoURL, dob, count);

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printExpiredCC() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM customers c LEFT OUTER JOIN creditcards cc ON c.cc_id=cc.id WHERE cc_id IN (SELECT id FROM creditcards WHERE expiration <= CURDATE() ) OR cc_id IS NULL ORDER BY c.last_name";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each genre, DISPLAY INFORMATION
			String first_name = searchResults.getString("c.first_name");
			String last_name = searchResults.getString("c.last_name");
			String ccfirst_name = searchResults.getString("cc.first_name");
			String cclast_name = searchResults.getString("cc.last_name");
			String expiration = searchResults.getString("expiration");
			Integer customerID = searchResults.getInt("id");
			String email = searchResults.getString("email");
			String cc_id = searchResults.getString("cc_id");
			String address = searchResults.getString("address");

			rtn += printCustomerSummary(customerID, first_name, last_name, email, address);

			rtn += printCreditCardSummary(cc_id, ccfirst_name, cclast_name, expiration);
			rtn += EditCustomer.editCreditCardLink(customerID, first_name + " " + last_name);

			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printInvlaidDOB() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM stars s WHERE dob <= '1900/01/01' OR dob >= CURDATE() OR dob IS NULL";
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

			rtn += "<BR><BR>";

		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	private String printInvalidEmails() throws SQLException, NamingException {
		String rtn = "";
		Connection dbcon = Database.openConnection();
		Statement statement = dbcon.createStatement();
		String query = "SELECT * FROM customers WHERE id NOT IN (SELECT id FROM customers WHERE email REGEXP '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$')";
		ResultSet searchResults = statement.executeQuery(query);
		while (searchResults.next()) {// For each genre, DISPLAY INFORMATION
			String first_name = searchResults.getString("first_name");
			String last_name = searchResults.getString("last_name");
			Integer customerID = searchResults.getInt("id");
			String email = searchResults.getString("email");
			String cc_id = searchResults.getString("cc_id");
			String address = searchResults.getString("address");

			rtn += printCustomerSummary(customerID, first_name, last_name, email, address);
			
			rtn += "<BR><BR>";
		}

		searchResults.close();
		statement.close();
		dbcon.close();
		return rtn;
	}

	public String printCustomerSummary(Integer customerID, String first_name, String last_name, String email, String address) {
		return "ID: " + customerID + "<BR>Name: " + first_name + " " + last_name + "<BR>Email: " + email + "<BR>Address: " + address + EditCustomer.editCustomerLink(customerID, first_name + " " + last_name);
	}

	public String printCreditCardSummary(String cc_id, String first_name, String last_name, String expiration) {
		return "ID: " + cc_id + "<BR>Name: " + first_name + " " + last_name + "<BR>Expiration: " + expiration;
	}

	public String printStarSummary(Integer starID, String first_name, String last_name, String photoURL) {
		return "<a href=\"StarDetails?id=" + starID + "\"><img src=\"" + photoURL + "\" height=\"60\"><BR>" + first_name + " " + last_name + "</a><BR> ID: " + starID;
	}

	public String printSimilarStarSummary(Integer starID, String first_name, String last_name, String photoURL, String dob, Integer count) {
		return "<img src=\"" + photoURL + "\" height=\"60\"><BR>" + first_name + " " + last_name + "<BR> Date of Birth: " + dob + "<BR> Count: " + count + "<BR>"
				+ EditStar.mergeStarLink(starID);
	}

	public String printMovieSummary(Integer movieID, String title, Integer year, String bannerURL) {
		return "<a href=\"MovieDetails?id=" + movieID + "\"><img src=\"" + bannerURL + "\" height=\"60\"><BR>" + title + " (" + year + ")</a><BR> ID: " + movieID;
	}

	public String printSimilarMovieSummary(Integer movieID, String title, Integer year, String bannerURL, Integer count) {
		return "<img src=\"" + bannerURL + "\" height=\"60\"><BR>" + title + " (" + year + ")<BR> Count: " + count
				+ "<BR>" + EditMovie.mergeMovieLink(movieID);
	}

	public static void savePath(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String URL = request.getRequestURL().toString();
		String qs = request.getQueryString();
		if (qs != null) {
			URL += "?" + qs;
		}
		// Save destination
		session.setAttribute("CheckDB.dest", URL);
	}

	public static void returnPath(HttpSession session, HttpServletResponse response) throws IOException {
		try {
			String target = (String) session.getAttribute("CheckDB.dest");
			if (target != null) {
				session.removeAttribute("CheckDB.dest");
				response.sendRedirect(target);
				return;
			}
		} catch (Exception ignored) {
		}
		response.sendRedirect("CheckDB");
	}
}
