package Fabflix;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.sql.*;

/**
 * Servlet implementation class MovieDetails
 */
public class MovieDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public MovieDetails() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Login.kickNonUsers(request, response);// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();
		ServletContext context = getServletContext();
		HttpSession session = request.getSession();

		try {
			Connection dbcon = Database.openConnection();
			
			// READ movieID
			Integer movieID;
			try {
				movieID = Integer.valueOf(request.getParameter("id"));
			} catch (Exception e) {
				movieID = 0;
			}

			Boolean edit = false; // trigger edit mode
			if (Page.isAdmin(request)) {
				try {
					edit = Boolean.valueOf(request.getParameter("edit"));
				} catch (Exception e) {
					edit = false;
				}
			}
			

			// Declare our statement
			Statement statement = dbcon.createStatement();
			String query = "SELECT DISTINCT * FROM movies m " + "WHERE m.id ='" + movieID + "'";
			ResultSet rs = statement.executeQuery(query);
			
			if (rs.next()) {

				String title = rs.getString("title");
				Integer year = rs.getInt("year");
				String director = rs.getString("director");
				String bannerURL = rs.getString("banner_url");
				String trailerURL = rs.getString("trailer_url");

				session.setAttribute("title", title + " ("+year+")");
				out.println(Page.header(context, session));
				out.println("<div class=\"movie-detail\">");
				
				if (Page.isAdmin(request)) {
					if (edit) {
						out.println("<div class=\"editing\">You are currently editing "+title+ " ("+year+"). To stop, click <a href=\"MovieDetails?id="+movieID+"&edit=false\">here</a>.</div>");
					} else {
						out.println("<div class=\"editing\">To edit "+title+ " ("+year+"), click <a href=\"MovieDetails?id="+movieID+"&edit=true\">here</a>.</div>");
					}

					if (session.getAttribute("movieError") != null) {
						out.println("<p class=\"error\">" + session.getAttribute("movieError") + "</p>");
						session.removeAttribute("movieError");
					}
					if (session.getAttribute("movieSuccess") != null){
						out.println("<p class=\"success\">" + session.getAttribute("movieSuccess") + "</p>");
						session.removeAttribute("movieSuccess");
					}
				}

				// Movie Info
				out.println("<H1>" + title + " ("+year+")");
				if (!edit){
					Page.addToCart(out, movieID);
				} else {
					EditMovie.deleteMovieLink(out, movieID, title);
				}
				out.println("</H1>");
								
				out.println("<a href=\"" + trailerURL + "\"><img src=\"" + bannerURL + "\" width=\"200\"></a>");
				
				out.println("<div class=\"info\"><ul>");
				out.println("<li>ID</li>\n<li>"+movieID+"</li>");
				out.println("</li></ul><ul>");
				if (edit) {
					out.println("<li>Title</li><li>");
					EditMovie.editMovieLink(out, movieID, title, "title");
					out.println("</li><li>Banner URL</li>");
					EditMovie.editMovieLink(out, movieID, bannerURL, "banner_url");
					out.println("</li><li>Trailer</li>\n<li>");
					EditMovie.editMovieLink(out, movieID, trailerURL, "trailer_url");
					out.println("</li></ul>");
				} else {
					out.println("<li>Trailer</li>\n<li>");
					out.println("<a href=\"" + trailerURL + "\">View</a>");
					out.println("</li></ul>");
				}
				
				out.println("<ul><li>Year</li>\n<li>");
				if (edit){
					EditMovie.editMovieLink(out, movieID, year.toString(), "year");
				} else {
					ListResults.listByYearLink(out, year, 0);
				}
				out.println("</li></ul>");
				
				out.println("<ul><li>Director</li>\n<li>");
				if (edit){
					EditMovie.editMovieLink(out, movieID, director, "director");
				} else {
					ListResults.listByDirectorLink(out, director, 0);
				}
				out.println("</li></ul>");

				out.println("<ul><li>Genres</li>\n<li>");
				ListResults.listGenres(out, dbcon, 0, movieID, edit);
				out.println("</li>");
				out.println("</ul>");

				out.println("<ul><li>Stars</li>\n<li>");
				ListResults.listStarsIMG(out, dbcon, 0, movieID, edit);
				out.println("</li></ul>");
				
				out.println("</div>");
			out.println("</div>");
			
			} else {
				session.setAttribute("title", "FabFlix -- Movie Not Found");
				out.println(Page.header(context, session));
				out.println("<H1>Movie Not Found</H1>");

				if (session.getAttribute("movieError") != null) {
					out.println("<p class=\"error\">" + session.getAttribute("movieError") + "</p>");
					session.removeAttribute("movieError");
				}
				if (session.getAttribute("movieSuccess") != null){
					out.println("<p class=\"success\">" + session.getAttribute("movieSuccess") + "</p>");
					session.removeAttribute("movieSuccess");
				}
			}

			// Footer
			Page.footer(out);

			rs.close();
			statement.close();
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
			out.println("<P>SQL error in doGet: " + ex.getMessage() + "<br>"
					+ ex.toString() + "</P></DIV></BODY></HTML>");
			return;
		}
		out.close();
	}

}
