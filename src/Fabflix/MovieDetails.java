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
//			Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
//			if (isAdmin != null && isAdmin){
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

				session.setAttribute("title", title);
				out.println(Page.header(context, session));


				// Movie Info
				out.println("<H1>" + title + " (" + year + ")");
				if (Page.isAdmin(request)){
					if (edit){
						out.println("(<A HREF=\"MovieDetails?id="+movieID+"&edit=false\">Stop Editing</A>)");
					}else{
						out.println("(<A HREF=\"MovieDetails?id="+movieID+"&edit=true\">Edit</A>)");
					}
				}
				out.println("</H1><BR>");
				
				//TODO add DELETE MOVIE
				
				out.println("<a href=\"" + trailerURL + "\"><img src=\"" + bannerURL + "\" height=\"300\"><BR>Trailer</a><BR><BR>");
				
				if (!edit && !Page.isAdmin(request)){
					Page.addToCart(out, movieID);
					out.println("<BR><BR>");
				}

				out.println("ID: " + movieID + "<BR>");
				
				if (edit){
					EditMovie.editMovieLink(out, movieID, title, "title");
					out.println("<BR>");
				}
				if (edit){
					EditMovie.editMovieLink(out, movieID, bannerURL, "banner_url");
					out.println("<BR>");
				}
				if (edit){
					EditMovie.editMovieLink(out, movieID, trailerURL, "trailer_url");
					out.println("<BR>");
				}
				
				
				ListResults.listByYearLink(out, year, 0);
				
				if (edit){
					EditMovie.editMovieLink(out, movieID, year.toString(), "year");
				}

				out.println("<BR>");
				
				ListResults.listByDirectorLink(out, director, 0);

				if (edit){
					EditMovie.editMovieLink(out, movieID, director, "director");
				}
				
				out.println("<BR>");
				

				ListResults.listGenres(out, dbcon, 0, movieID, edit);

				out.println("<BR><BR>");

				ListResults.listStarsIMG(out, dbcon, 0, movieID, edit);

			} else {
				session.setAttribute("title", "FabFlix -- Movie Not Found");
				out.println(Page.header(context, session));
				out.println("<H1>Movie Not Found</H1>");
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
