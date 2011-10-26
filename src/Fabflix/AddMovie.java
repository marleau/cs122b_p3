package Fabflix;

import java.io.IOException;
import java.sql.*;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AddMovie extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public AddMovie() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)) {return;}
		if (Login.kickNonAdmin(request, response)) {return;}
		HttpSession session = request.getSession();
		session.setAttribute("title", "Add Movie");
		session.removeAttribute("addMovie_err");
		response.sendRedirect("addmovie.jsp");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)) {return;}
		if (Login.kickNonAdmin(request, response)) {return;}
		
		try {
			HttpSession session = request.getSession();
			
			String title = request.getParameter("title");
			Integer year = 0;
			try{
				year = Integer.valueOf(request.getParameter("year"));
			}catch(Exception e){
				year = 0;
				session.setAttribute("addMovie_err", "Invalid Year.");
				response.sendRedirect("addmovie.jsp");
				return;
			}
			String director = request.getParameter("director");
			String first_name = request.getParameter("first_name");
			String last_name = request.getParameter("last_name");
			String genre = request.getParameter("genre");
			
			if (title == null || title.isEmpty()){
				session.setAttribute("addMovie_err", "Needs Title.");
				response.sendRedirect("addmovie.jsp");
				return;
			}
			if ( year == 0 ){
				session.setAttribute("addMovie_err", "Needs Year.");
				response.sendRedirect("addmovie.jsp");
				return;
			} 
			if (director == null || director.isEmpty() ){
				session.setAttribute("addMovie_err", "Needs Director.");
				response.sendRedirect("addmovie.jsp");
				return;
			} 
			if (genre == null || genre.isEmpty() ){
				session.setAttribute("addMovie_err", "Needs Genre.");
				response.sendRedirect("addmovie.jsp");
				return;
			}
			if(first_name == null || first_name.isEmpty() ){
				session.setAttribute("addMovie_err", "Needs Star First Name.");
				response.sendRedirect("addmovie.jsp");
				return;
			}
			if(last_name == null || last_name.isEmpty()) {
				session.setAttribute("addMovie_err", "Needs Star Last Name.");
				response.sendRedirect("addmovie.jsp");
				return;
			} 
			
			Connection dbcon = Database.openConnection();
			CallableStatement cst = dbcon.prepareCall("{call add_movie(?,?,?,?,?,?)}");
			cst.setString(1, title);
			cst.setInt(2, year);
			cst.setString(3, director);
			cst.setString(4, first_name);
			cst.setString(5, last_name);
			cst.setString(6, genre);
			cst.execute();
			
			Statement st = dbcon.createStatement();
			//TODO include genre and star name to search for ID for accuracy
			ResultSet rs = st.executeQuery("SELECT id FROM movies WHERE title = '"+title+"' AND year = '"+year+"' AND director = '"+director+"'");
			rs.next();
			int id = rs.getInt("id");
			
			dbcon.close();
			session.removeAttribute("addMovie_err");
			
			//Try to redirect to newly added movie
			response.sendRedirect("MovieDetails?id="+id);
		} catch (NamingException e) {
		} catch (SQLException e) {
		}
	}

}
