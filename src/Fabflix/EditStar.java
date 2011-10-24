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
 * Servlet implementation class EditStar
 */
public class EditStar extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public EditStar() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in
		if (Login.kickNonAdmin(request, response)){return;}// kick if not admin

		response.setContentType("text/html"); // Response mime type

		HttpSession session = request.getSession();

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String starID = request.getParameter("starID");

		//Scrub Args
		if (value != null){
			value = Database.cleanSQL(value);
		}
		
		if (starID == null) {
			response.sendRedirect("index.jsp");
			return;
		}else{
			starID = Database.cleanSQL(starID);
		}

		try {
			Connection dbcon = Database.openConnection();
			Statement statement = dbcon.createStatement();

			if (action != null && field != null) {
				if (action.equals("delete") && value != null) {// ==========DELETE
					if (field.equals("movie")) {
						String query = "DELETE FROM stars_in_movies WHERE star_id = '" + starID + "' AND movie_id = '" + value + "'";
						statement.executeUpdate(query);
					}
				} else if (action.equals("add") && value != null) {// ==========ADD
					if (field.equals("movie")) {
						String query = "INSERT INTO stars_in_movies VALUES(" + starID + ", " + value + ");";
						statement.executeUpdate(query);
					}
				} else if (action.equals("edit") && value != null) {// ==========EDIT
					if (field.equals("first_name") || field.equals("last_name") || field.equals("dob") || field.equals("photo_url")) {
						if (field.equals("dob") && !Database.isValidDate(value)) {
							response.sendRedirect("StarDetails?id=" + starID + "&edit=true");
							return;
						}
						String query = "UPDATE stars SET " + field + " = '" + value + "' WHERE id = '" + starID + "'";
						statement.executeUpdate(query);
					}
				} else if (action.equals("merge")){
					if (field.equals("star") && !starID.isEmpty()){
						// Get all similar names
						String query = "SELECT * FROM stars WHERE SOUNDEX(first_name) = SOUNDEX((SELECT first_name FROM stars WHERE id = '"+starID+"')) AND SOUNDEX(last_name) = SOUNDEX((SELECT last_name FROM stars WHERE id = '"+starID+"')) AND dob = (SELECT dob FROM stars WHERE id = '"+starID+"')";
						ResultSet similarNames = statement.executeQuery(query);

						PrintWriter out = response.getWriter();
						ServletContext context = getServletContext();
						out.println(Page.header(context, session));
						out.println("<H1>Pick The Best:</H1>");
						out.println("<FORM ACTION=\"EditStar\" METHOD=\"POST\">");

						String photo_url;
						int sid=0;
						String first_name, last_name;
						while (similarNames.next()){
							photo_url = similarNames.getString("photo_url");
							sid = similarNames.getInt("id");
							first_name = similarNames.getString("first_name");
							last_name = similarNames.getString("last_name");
							out.println("<INPUT TYPE=\"RADIO\" NAME=\"starID\" VALUE=\""+sid+"\"><img src=\""+photo_url+"\"> "+first_name +" " + last_name+"<BR><BR>");
						}
						
						out.println("<INPUT TYPE=\"Hidden\" NAME=\"action\" VALUE=\"merge\"><INPUT TYPE=\"Hidden\" NAME=\"field\" VALUE=\"onStar\">");
						out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Submit\"></FORM>");
						
						Page.footer(out);
						out.close();
						dbcon.close();
						return;
						
					} else if (field.equals("onStar") && !starID.isEmpty()){
						String query = "SELECT * FROM stars WHERE id = '"+starID+"'";
						ResultSet starQ = statement.executeQuery(query);
						
						starQ.next();
						String dob = starQ.getString("dob");
						String first_name = starQ.getString("first_name");
						String last_name = starQ.getString("last_name");
						
						//Update all movies to use new starID
						statement = dbcon.createStatement();
						String update = "UPDATE stars_in_movies SET star_id = '"+starID+"' WHERE star_id IN (SELECT id FROM stars WHERE SOUNDEX(first_name) = SOUNDEX('"+first_name+"') AND SOUNDEX(last_name) = SOUNDEX('"+last_name+"') AND dob = '"+dob+"')";
						statement.executeUpdate(update);

						//Remove old names
						statement = dbcon.createStatement();
						update = "DELETE FROM stars WHERE SOUNDEX(first_name) = SOUNDEX('"+first_name+"') AND SOUNDEX(last_name) = SOUNDEX('"+last_name+"') AND dob = '"+dob+"' AND id != '"+starID+"'";
						statement.executeUpdate(update);
						
						CheckDB.returnPath(session, response);
						dbcon.close();
						return;
					}
					
					
				}
			}
			dbcon.close();
		}catch (SQLException ex) {
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
			out.println("<P>SQL error in doGet: " + ex.getMessage() + "<br>" + ex.toString() + "</P></DIV></BODY></HTML>");
			return;
		}

		response.sendRedirect("StarDetails?id=" + starID + "&edit=true");

	}
	
	public static void editStarLink(PrintWriter out, Integer starID, String oldVal, String field) {
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

	public static String mergeStarLink(Integer starID) {
		return "<form method=\"post\" action=\"EditStar\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"merge\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"star\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=starID VALUE=\""+ starID+"\">" +
				"<button type=\"submit\" value=\"submit\">Merge</button>" +
				"</form>";
	}

	public static void removeMovieLink(PrintWriter out, Integer starID, Integer delID, String name) {
		out.println("<form method=\"post\" action=\"EditStar\">" +
				"<input type=\"HIDDEN\" name=\"value\" value=\""+ delID +"\"/>" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"movie\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=starID VALUE=\""+ starID+"\">" +
				"<button type=\"submit\" value=\"submit\">Remove "+name+"</button>" +
				"</form>");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("index.jsp");
	}
}
