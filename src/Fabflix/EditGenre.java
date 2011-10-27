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

 // Servlet implementation class FixDB
public class EditGenre extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public EditGenre() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in
		if (Login.kickNonAdmin(request, response)){return;}// kick if not admin

		response.setContentType("text/html"); // Response mime type

		HttpSession session = request.getSession();

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String genreID = request.getParameter("genreID");
		
		String oldName =request.getParameter("oldName");

		// Scrub Args
		if (value != null){
			value = Database.cleanSQL(value);
		}
		if (genreID != null){
			genreID = Database.cleanSQL(genreID);
		}
		if (oldName != null){
			oldName = Database.cleanSQL(oldName);
		}
		

		try {
			Connection dbcon = Database.openConnection();

			Statement statement = dbcon.createStatement();

			if (action != null && field != null) {
				if (action.equals("delete")) {// ==========DELETE
					if (field.equals("genre") && genreID != null) {
						String query = "SELECT * FROM genres WHERE id = '" + genreID + "'";
						ResultSet nameQ = statement.executeQuery(query);
						nameQ.next();
						
						String name = nameQ.getString("name"); 
						
						statement = dbcon.createStatement();
						query = "DELETE FROM genres WHERE id = '" + genreID + "'";
						statement.executeUpdate(query);
						session.setAttribute("checkSuccess", name + " Removed!");
					}else if (field.equals("allEmpty")){
						String query = "DELETE FROM genres WHERE id NOT IN (SELECT genre_id FROM genres_in_movies)";
						statement.executeUpdate(query);
						session.setAttribute("checkSuccess", "All Empty Genres Removed!");
					}
				} else if (action.equals("edit") && value != null && !value.isEmpty() && genreID != null){
					if (field.equals("name")){
						String update = "UPDATE genres SET name = '"+value+"' WHERE id = '"+genreID+"'";
						statement.executeUpdate(update);
						session.setAttribute("checkSuccess", value + " Renamed!");
					}
				} else if (action.equals("merge")){
					if (field.equals("genre") && value != null && !value.isEmpty() && genreID == null){
						// Get all similar names
						String query = "SELECT * FROM genres g WHERE SOUNDEX(name) = SOUNDEX('"+ value +"') GROUP BY name";
						ResultSet similarNames = statement.executeQuery(query);

						PrintWriter out = response.getWriter();
						ServletContext context = getServletContext();
						out.println(Page.header(context, session));
						out.println("<H1>Pick Proper Spelling</H1>");
						out.println("<FORM ACTION=\"EditGenre\" METHOD=\"POST\">");

						String name;
						int gid=0;
						while (similarNames.next()){
							name = similarNames.getString("name");
							gid = similarNames.getInt("id");
							out.println("<INPUT TYPE=\"RADIO\" NAME=\"genreID\" id=\""+gid+"\" VALUE=\""+gid+"\"><label for=\""+gid+"\">"+name+"</label><BR><BR>");
						}
						
						out.println("<INPUT TYPE=\"RADIO\" NAME=\"genreID\" id=\"0\" VALUE=\"0\"><label for=\"0\"> New Name: </label><INPUT TYPE=\"TEXT\" NAME=\"value\" id=\"0\"><BR><BR>");
						
						out.println("<INPUT TYPE=\"Hidden\" NAME=\"oldName\" VALUE=\""+value+"\"><INPUT TYPE=\"Hidden\" NAME=\"action\" VALUE=\"merge\"><INPUT TYPE=\"Hidden\" NAME=\"field\" VALUE=\"setGenre\">");
						out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Submit\"></FORM>");
						
						Page.footer(out);
						out.close();
						dbcon.close();
						return;
						
					} else if (field.equals("setGenre") && genreID != null && !genreID.isEmpty() && !oldName.isEmpty()){
						if (genreID.equals("0") && value.isEmpty()){//No blank name for a genre
							CheckDB.returnPath(session, response);
							return;
						}
						
						String newName = "";
						
						if (genreID.equals("0")){
							String query = "SELECT * FROM genres g WHERE SOUNDEX(name) = SOUNDEX('"+ oldName +"') GROUP BY name";
							ResultSet similarNames = statement.executeQuery(query);
							similarNames.next();
							genreID = similarNames.getString("id");
							newName = value;
						} else {
							String query = "SELECT * FROM genres g WHERE id = '"+genreID+"'";
							ResultSet similarNames = statement.executeQuery(query);
							similarNames.next();
							newName = Database.cleanSQL(similarNames.getString("name"));
						}
						
						//Rename genreID to new name
						statement = dbcon.createStatement();
						String update = "UPDATE genres SET name = '"+newName+"' WHERE id = '"+genreID+"'";
						statement.executeUpdate(update);
						
						//Update all movies to use new genreID
						statement = dbcon.createStatement();
						update = "UPDATE genres_in_movies SET genre_id = '"+genreID+"' WHERE genre_id IN (SELECT id FROM genres g WHERE SOUNDEX(name) = SOUNDEX('"+oldName+"'))";
						statement.executeUpdate(update);

						//Remove old names
						statement = dbcon.createStatement();
						update = "DELETE FROM genres WHERE SOUNDEX(name) = SOUNDEX('"+oldName+"') AND id != '"+genreID+"'";
						statement.executeUpdate(update);
						
						session.setAttribute("checkSuccess", newName + " Merged!");
					}
				}
				
				
				
			}
			
			dbcon.close();
		} catch (NamingException e) {
		} catch (SQLException e) {
		} catch (Exception e) {
		}

		CheckDB.returnPath(session, response);
	}
	public static String mergeGenreLink(String name) {
		return "<form method=\"post\" action=\"EditGenre\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=\"value\" VALUE=\""+name+"\" />" +
				"<INPUT TYPE=\"HIDDEN\" NAME=\"action\" VALUE=\"merge\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=\"field\" VALUE=\"genre\">" +
				"<button type=\"submit\" value=\"submit\">Merge "+name+"</button>" +
				"</form>";
	}
	public static String deleteGenreLink(Integer genreID, String name) {
		return "<form method=\"post\" action=\"EditGenre\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"genre\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=genreID VALUE=\""+ genreID+"\">" +
				"<button type=\"submit\" value=\"submit\">Delete "+name+"</button>" +
				"</form>";
	}
	public static String deleteAllEmptyGenreLink() {
		return "<form method=\"post\" action=\"EditGenre\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"delete\">" +
				"<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"allEmpty\">" +
				"<button type=\"submit\" value=\"submit\">Delete All Empty Genres</button>" +
				"</form>";
	}

	public static String renameGenreLink(String name, int genreID) {
		return "<form method=\"post\" action=\"EditGenre\">" +
		"<input type=\"text\" name=\"value\" />" +
		"<INPUT TYPE=\"HIDDEN\" NAME=\"genreID\" VALUE=\""+genreID+"\" />" +
		"<INPUT TYPE=\"HIDDEN\" NAME=\"action\" VALUE=\"edit\">" +
		"<INPUT TYPE=\"HIDDEN\" NAME=\"field\" VALUE=\"name\">" +
		"<button type=\"submit\" value=\"submit\">Rename "+name+"</button>" +
		"</form>";
	}



}
