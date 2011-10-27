package Fabflix;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class StarDetails
 */
public class StarDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StarDetails() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();
		ServletContext context = getServletContext();
		HttpSession session = request.getSession();
		try {
			
			Connection dbcon = Database.openConnection();
			
			// READ STAR ID
			Integer starID;
			try {
				starID = Integer.valueOf(request.getParameter("id"));
			} catch (Exception e) {
				starID = 0;
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
			String query = "SELECT DISTINCT * FROM stars WHERE id ='" + starID + "'";

			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) {// Get star if ID exists
				String first_name = rs.getString("first_name");
				String last_name = rs.getString("last_name");
				String starName = first_name + " " + last_name;
				String starIMG = rs.getString("photo_url");
				String dob = rs.getString("dob");

				session.setAttribute("title", starName);
				out.println(Page.header(context, session));
				out.println("<div class=\"star-detail\">");
				
				if (Page.isAdmin(request)) {
					if (edit) {
						out.println("<div class=\"editing\">You are currently editing "+starName+". To stop, click <a href=\"StarDetails?id="+starID+"&edit=false\">here</a>.</div>");
					} else {
						out.println("<div class=\"editing\">To edit "+starName+", click <a href=\"StarDetails?id="+starID+"&edit=true\">here</a>.</div>");
					}
					if (session.getAttribute("starError") != null) {
						out.println("<p class=\"error\">" + session.getAttribute("starError") + "</p>");
						session.removeAttribute("starError");
					}
					if (session.getAttribute("starSuccess") != null){
						out.println("<p class=\"success\">" + session.getAttribute("starSuccess") + "</p>");
						session.removeAttribute("starSuccess");
					}
				}
				
				// Star Details
				out.println("<H1>" + starName + "</H1>" );
				if(edit){
					EditStar.deleteStarLink(out, starID, starName);
				}
				out.println("<BR>");
				//TODO add DELETE STAR
				
				out.println("<img src=\"" + starIMG + "\" height=\"300\"><BR><BR>");
				
				out.println("ID: " + starID + "<BR>");// STAR DETAILS
				if (edit){
					EditStar.editStarLink(out, starID, first_name, "first_name");
					out.println("<BR>");
				}
				if (edit){
					EditStar.editStarLink(out, starID, last_name, "last_name");
					out.println("<BR>");
				}
				if (edit){
					EditStar.editStarLink(out, starID, starIMG, "photo_url");
					out.println("<BR>");
				}
				
				out.println("Date of Birth: " + dob);
				if (edit){
					out.println(" (yyyy-MM-dd)");
					EditStar.editStarLink(out, starID, dob, "dob");
					out.println("<BR>");
				}

				out.println("<BR><BR>");
				
				ListResults.listMoviesIMG(out, dbcon, 0, starID, edit);

				out.println("</div>");
				
			} else {// starID didn't return a star
				session.setAttribute("title", "FabFlix -- Star Not Found");
				out.println(Page.header(context, session));
				out.println("<H1>Star Not Found</H1>");

				if (session.getAttribute("starError") != null) {
					out.println("<p class=\"error\">" + session.getAttribute("starError") + "</p>");
					session.removeAttribute("starError");
				}
				if (session.getAttribute("starSuccess") != null){
					out.println("<p class=\"success\">" + session.getAttribute("starSuccess") + "</p>");
					session.removeAttribute("starSuccess");
				}
				
			}
			// Footer

			Page.footer(out);

			rs.close();
			statement.close();
			dbcon.close();
			
		}  catch (SQLException ex) {
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
