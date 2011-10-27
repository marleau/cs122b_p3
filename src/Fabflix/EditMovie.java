package Fabflix;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import javax.servlet.ServletContext;
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
		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in
		if (Login.kickNonAdmin(request, response)){return;}// kick if not admin

		response.setContentType("text/html"); // Response mime type

		HttpSession session = request.getSession();

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String movieID = request.getParameter("movieID");

		//Scrub Args
		if (value != null){
			value = Database.cleanSQL(value);
		}
		

		if (movieID == null ) {
			response.sendRedirect("index.jsp");
			return;
		}else {
			movieID = Database.cleanSQL(movieID);
		}

		try {
			Connection dbcon = Database.openConnection();

			Statement statement = dbcon.createStatement();

			if (action != null && field != null) {
				if (action.equals("delete")) {// ==========DELETE
					if (field.equals("genre") && value != null ) {
						String query = "SELECT * FROM genres WHERE id = '" + value + "'";
						ResultSet nameQ = statement.executeQuery(query);
						nameQ.next();
						
						String name = nameQ.getString("name");

						statement = dbcon.createStatement();
						query = "DELETE FROM genres_in_movies WHERE genre_id = '" + value + "' AND movie_id = '" + movieID + "'";
						statement.executeUpdate(query);
						session.setAttribute("movieSuccess", name + " Deleted!");
					} else if (field.equals("star") && value != null) {
						String query = "SELECT * FROM stars WHERE id = '" + value + "'";
						ResultSet nameQ = statement.executeQuery(query);
						nameQ.next();
						
						String name = nameQ.getString("first_name") + " " + nameQ.getString("last_name");

						statement = dbcon.createStatement();
						query = "DELETE FROM stars_in_movies WHERE star_id = '" + value + "' AND movie_id = '" + movieID + "'";
						statement.executeUpdate(query);
						session.setAttribute("movieSuccess", name + " Deleted!");
					} else if (field.equals("movie")){
						String query = "SELECT * FROM movies WHERE id = '" + movieID + "'";
						ResultSet nameQ = statement.executeQuery(query);
						nameQ.next();
						
						String name = nameQ.getString("title") + " (" + nameQ.getString("year")+")";

						statement = dbcon.createStatement();
						query = "DELETE FROM movies WHERE id = '" + movieID + "' ";
						statement.executeUpdate(query);
						session.setAttribute("movieSuccess", name + " Deleted!");
					}
				} else if (action.equals("add") && value != null) {// ==========ADD
					if (field.equals("genre") && !value.isEmpty()) {
						String genreName = value;
						String genreID = "0";
						String query = "SELECT id FROM genres g WHERE name = '" + genreName + "'";
						ResultSet genreQ = statement.executeQuery(query);
						if (genreQ.next()){
							genreID = genreQ.getString("id");
						}else{
							//create a genre entry
							statement = dbcon.createStatement();
							String update = "INSERT INTO genres VALUES( 0, '" + genreName + "');";
							statement.executeUpdate(update);

							statement = dbcon.createStatement();
							query = "SELECT id FROM genres g WHERE name = '" + genreName + "'";
							genreQ = statement.executeQuery(query);
							genreQ.next();
							genreID = genreQ.getString("id");
						}
						
						
						statement = dbcon.createStatement();
						String update = "INSERT INTO genres_in_movies VALUES(" + genreID + ", " + movieID + ");";
						statement.executeUpdate(update);
						session.setAttribute("movieSuccess", genreName + " Added!");
					} else if (field.equals("star ID")) {
						String query = "SELECT * FROM stars WHERE id = '" + value + "'";
						ResultSet nameQ = statement.executeQuery(query);
						nameQ.next();
						
						String name = nameQ.getString("first_name") + " " + nameQ.getString("last_name");

						statement = dbcon.createStatement();
						query = "INSERT INTO stars_in_movies VALUES(" + value + ", " + movieID + ");";
						statement.executeUpdate(query);

						session.setAttribute("movieSuccess", name + " Added!");
					}
				} else if (action.equals("edit") && value != null) {// ==========EDIT
					if (field.equals("title") || field.equals("year") || field.equals("director") || field.equals("banner_url") || field.equals("trailer_url")) {
						if (field.equals("year")) {
							try {
								Integer year = Integer.valueOf(value);
								value = year.toString();
							} catch (Exception e) {
								session.setAttribute("movieError", "Invalid Year");
								response.sendRedirect("MovieDetails?id=" + movieID + "&edit=true");
								return;
							}
						}
						String query = "UPDATE movies SET " + field + " = '" + value + "' WHERE id = '" + movieID + "'";
						statement.executeUpdate(query);
						session.setAttribute("movieSuccess", field + " Updated!");
					}
				}else if (action.equals("merge")){
					if (field.equals("movie") && !movieID.isEmpty()){
						// Get all similar names
						String query = "SELECT * FROM movies WHERE SOUNDEX(title) = SOUNDEX((SELECT title FROM movies WHERE id = '"+movieID+"')) AND year = (SELECT year FROM movies WHERE id = '"+movieID+"')";
						ResultSet similarNames = statement.executeQuery(query);

						PrintWriter out = response.getWriter();
						ServletContext context = getServletContext();
						out.println(Page.header(context, session));
						out.println("<H1>Pick The Best:</H1>");
						out.println("<FORM ACTION=\"EditMovie\" METHOD=\"POST\">");

						String banner_url;
						int mid=0;
						String title, year, director;
						while (similarNames.next()){
							banner_url = similarNames.getString("banner_url");
							mid = similarNames.getInt("id");
							title = similarNames.getString("title");
							year = similarNames.getString("year");
							director = similarNames.getString("director");
							out.println("<INPUT TYPE=\"RADIO\" NAME=\"movieID\" id=\""+mid+"\" VALUE=\""+mid+"\"><label for=\""+mid+"\"><img src=\""+banner_url+"\" height=\"100\"><BR>"+title +" (" + year+")<BR>Director: "+director+"</label><BR><BR>");
						}

						out.println("<INPUT TYPE=\"Hidden\" NAME=\"action\" VALUE=\"merge\"><INPUT TYPE=\"Hidden\" NAME=\"field\" VALUE=\"onMovie\">");
						out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Submit\"></FORM>");
						
						Page.footer(out);
						out.close();
						dbcon.close();
						return;
						
					} else if (field.equals("onMovie") && !movieID.isEmpty()){
						String query = "SELECT * FROM movies WHERE id = '"+movieID+"'";
						ResultSet starQ = statement.executeQuery(query);
						
						starQ.next();
						String year = starQ.getString("year");
						String title = Database.cleanSQL(starQ.getString("title"));

						//Update all stars to use new movieID
						statement = dbcon.createStatement();
						String update = "UPDATE stars_in_movies SET movie_id = '"+movieID+"' WHERE movie_id IN (SELECT id FROM movies WHERE SOUNDEX(title) = SOUNDEX('"+title+"') AND year = '"+year+"')";
						statement.executeUpdate(update);
						
						//Update all genres to use new movieID
						statement = dbcon.createStatement();
						update = "UPDATE genres_in_movies SET movie_id = '"+movieID+"' WHERE movie_id IN (SELECT id FROM movies WHERE SOUNDEX(title) = SOUNDEX('"+title+"') AND year = '"+year+"')";
						statement.executeUpdate(update);

						//Remove old movies
						statement = dbcon.createStatement();
						update = "DELETE FROM movies WHERE SOUNDEX(title) = SOUNDEX('"+title+"') AND year = '"+year+"' AND id != '"+movieID+"'";
						statement.executeUpdate(update);
						
						session.setAttribute("checkSuccess", title +" ("+ year + ") Merged!");
						CheckDB.returnPath(session, response);
						dbcon.close();
						return;
					}
				}
			}
			dbcon.close();
		} catch (SQLException ex) {
			PrintWriter out = response.getWriter();
			ServletContext context = getServletContext();
			out.println(Page.header(context, session));
			while (ex != null) {
				out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} // end while
			out.println("</DIV></BODY></HTML>");
		} // end catch SQLException
		catch (java.lang.Exception ex) {
			PrintWriter out = response.getWriter();
			ServletContext context = getServletContext();
			out.println(Page.header(context, session));
			out.println("<P>SQL error in doGet: " + ex.getMessage() + "<br>"
					+ ex.toString() + "</P></DIV></BODY></HTML>");
			return;
		}

		response.sendRedirect("MovieDetails?id=" + movieID + "&edit=true");

	}

	public static void editMovieLink(PrintWriter out, Integer movieID, String oldVal, String field) {
		out.println("<form method=\"post\" action=\"EditMovie\">" +
				"<input type=\"text\" name=\"value\" value=\""+oldVal+"\" />" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"edit\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=movieID VALUE=\""+ movieID+"\">" +
				"<button type=\"submit\" value=\"submit\">Change "+field+"</button>" +
				"</form>");
	}

	public static void addStarGenreLink(PrintWriter out, Integer movieID, String field) {
		out.println("<form method=\"post\" action=\"EditMovie\">" +
				"<input type=\"text\" name=\"value\" />" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"add\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=movieID VALUE=\""+ movieID+"\">" +
				"<button type=\"submit\" value=\"submit\">Add "+field+"</button>" +
				"</form>");
	}

	public static String mergeMovieLink(Integer movieID) {
		return "<form method=\"post\" action=\"EditMovie\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"merge\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"movie\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=movieID VALUE=\""+ movieID+"\">" +
				"<button type=\"submit\" value=\"submit\">Merge</button>" +
				"</form>";
	}

	public static void removeStarGenreLink(PrintWriter out, Integer movieID, Integer delID, String field,String name) {
		out.println("<form method=\"post\" action=\"EditMovie\">" +
				"<input type=\"HIDDEN\" name=\"value\" value=\""+ delID +"\"/>" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=movieID VALUE=\""+ movieID+"\">" +
				"<button type=\"submit\" value=\"submit\">Remove "+name+"</button>" +
				"</form>");
	}

	public static void deleteMovieLink(PrintWriter out, Integer movieID, String name) {
		out.println("<form method=\"post\" action=\"EditMovie\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"movie\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=movieID VALUE=\""+ movieID+"\">" +
				"<button type=\"submit\" value=\"submit\">Remove "+name+"</button>" +
				"</form>");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp");
	}
}
