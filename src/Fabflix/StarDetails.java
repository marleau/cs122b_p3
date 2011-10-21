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
		LoginPage.kickNonUsers(request, response);// kick if not logged in

		response.setContentType("text/html"); // Response mime type

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();
		ServletContext context = getServletContext();
		HttpSession session = request.getSession();
		try {
			
			Connection dbcon = ListResults.openConnection();
			
			// READ STAR ID
			Integer starID;
			try {
				starID = Integer.valueOf(request.getParameter("id"));
			} catch (Exception e) {
				starID = 0;
			}
			
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
			String query = "SELECT DISTINCT * FROM stars WHERE id ='" + starID + "'";

			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) {// Get star if ID exists
				String first_name = rs.getString("first_name");
				String last_name = rs.getString("last_name");
				String starName = first_name + " " + last_name;
				String starIMG = rs.getString("photo_url");
				String dob = rs.getString("dob");



				session.setAttribute("title", starName);
				out.println(ListResults.header(context, session));

				// Star Details
				out.println("<H1>" + starName + " " );
				if (isAdmin != null && isAdmin){
					if (edit){
						out.println("(<A HREF=\"StarDetails?id="+starID+"&edit=false\">Stop Editing</A>)");
					}else{
						out.println("(<A HREF=\"StarDetails?id="+starID+"&edit=true\">Edit</A>)");
					}
				}
				out.println("</H1><BR>");
				//TODO add DELETE STAR
				
				out.println("<img src=\"" + starIMG + "\" height=\"300\"><BR><BR>");
				
				out.println("ID: " + starID + "<BR>");// STAR DETAILS
				if (edit){
					editStarLink(out, starID, first_name, "first_name");
					out.println("<BR>");
				}
				if (edit){
					editStarLink(out, starID, last_name, "last_name");
					out.println("<BR>");
				}
				if (edit){
					editStarLink(out, starID, starIMG, "photo_url");
					out.println("<BR>");
				}
				
				out.println("Date of Birth: " + dob);
				if (edit){
					out.println(" (yyyy-MM-dd)");
					editStarLink(out, starID, dob, "dob");
					out.println("<BR>");
				}

				out.println("<BR><BR>");
				
				ListResults.listMoviesIMG(out, dbcon, 0, starID, edit);

				
			} else {// starID didn't return a star
				session.setAttribute("title", "FabFlix -- Star Not Found");
				out.println(ListResults.header(context, session));
				out.println("<H1>Star Not Found</H1>");
				
			}
			// Footer

			ListResults.footer(out, dbcon, 0);

			rs.close();
			statement.close();
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

	
	private void editStarLink(PrintWriter out, Integer starID, String oldVal, String field) {
		out.println("<form method=\"post\" action=\"EditStar\">" +
				"<input type=\"text\" name=\"value\" value=\""+oldVal+"\" />" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"edit\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=starID VALUE=\""+ starID+"\">" +
				"<button type=\"submit\" value=\"submit\">Change "+field+"</button>" +
				"</form>");
	}

	public static void addMovieLink(PrintWriter out, Integer starID, String field) {
		out.println("<form method=\"post\" action=\"EditStar\">" +
				"<input type=\"text\" name=\"value\" />" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"add\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=starID VALUE=\""+ starID+"\">" +
				"<button type=\"submit\" value=\"submit\">Add "+field+" ID</button>" +
				"</form>");
	}

	public static void deleteMovieLink(PrintWriter out, Integer starID, Integer delID, String field,String name) {
		out.println("<form method=\"post\" action=\"EditStar\">" +
				"<input type=\"HIDDEN\" name=\"value\" value=\""+ delID +"\"/>" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\""+field+"\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=starID VALUE=\""+ starID+"\">" +
				"<button type=\"submit\" value=\"submit\">Remove "+name+"</button>" +
				"</form>");
	}
}
