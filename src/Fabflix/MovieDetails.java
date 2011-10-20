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

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MovieDetails() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		LoginPage.kickNonUsers(request, response);// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			Connection dbcon = ListResults.openConnection();
			
			// READ movieID
			Integer movieID;
			try {
				movieID = Integer.valueOf(request.getParameter("id"));
			} catch (Exception e) {
				movieID = 0;
			}

			ServletContext context = getServletContext();
			HttpSession session = request.getSession();
			Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
			
			Boolean edit = false; // trigger edit mode
			if (isAdmin != null && isAdmin){
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

				session.setAttribute("title", title);
				out.println(ListResults.header(context, session));


				// Movie Info
				out.println("<H2>" + title + " (" + year + ")");
				
				if (isAdmin != null && isAdmin){
					if (edit){
						out.println("(<A HREF=\"MovieDetails?id="+movieID+"&edit=false\">Stop Editing</A>)");
					}else{
						out.println("(<A HREF=\"MovieDetails?id="+movieID+"&edit=true\">Edit</A>)");
					}
				}
				
				out.println("</H2><BR>");
				
				out.println("<a href=\"" + trailerURL + "\"><img src=\"" + bannerURL + "\"><br>Trailer</a><BR><BR>");
				
				if (!edit){
					ListResults.addToCart(out, movieID);
					out.println("<BR>");
				}
				
				if (edit){
					editMovieLink(out, movieID, title, "title");
					out.println("<BR>");
				}
				if (edit){
					editMovieLink(out, movieID, bannerURL, "banner_url");
					out.println("<BR>");
				}
				if (edit){
					editMovieLink(out, movieID, trailerURL, "trailer_url");
					out.println("<BR>");
				}
				
				out.println("<BR>ID: " + movieID + "<BR>");
				
				ListResults.listByYearLink(out, year);
				
				if (edit){
					editMovieLink(out, movieID, year.toString(), "year");
				}

				out.println("<BR>");
				
				ListResults.listByDirectorLink(out, director);

				if (edit){
					editMovieLink(out, movieID, director, "director");
				}
				
				out.println("<BR>");
				
				ListResults.listGenres(out, dbcon, movieID);

				out.println("<BR><BR>");

				ListResults.listStarsIMG(out, dbcon, movieID);

			} else {
				session.setAttribute("title", "FabFlix -- Movie Not Found");
				out.println(ListResults.header(context, session));
				out.println("<H1>Movie Not Found</H1>");
			}

			// Footer

			ListResults.footer(out, dbcon, 0);

			rs.close();
			statement.close();
			dbcon.close();
			
		} catch (SQLException ex) {
			//TODO header and footer
			out.println("<HTML><HEAD><TITLE>MovieDB: Error</TITLE></HEAD><BODY>");
			while (ex != null) {
				out.println("SQL Exception:  " + ex.getMessage());
				ex = ex.getNextException();
			} // end while
			out.println("</BODY></HTML>");
		} // end catch SQLException
		catch (java.lang.Exception ex) {
			//TODO header and footer
			out.println("<HTML>" + "<HEAD><TITLE>" + "MovieDB: Error" + "</TITLE></HEAD>\n<BODY>" + "<P>SQL error in doGet: " + ex.getMessage() + "<br>"
					+ ex.toString() + "</P></BODY></HTML>");
			return;
		}
		out.close();
	}

	private void editMovieLink(PrintWriter out, Integer movieID, String oldVal, String field) {
		out.println("<form method=\"post\" action=\"EditMovie\">" +
				"<input type=\"text\" name=\"value\" value=\""+oldVal+"\" />" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"edit\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=movieID VALUE=\""+ movieID+"\">" +
				"<button type=\"submit\" value=\"submit\">Change "+field+"</button>" +
				"</form>");
	}

}
