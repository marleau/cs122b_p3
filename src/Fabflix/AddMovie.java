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
		CallableStatement cst = null;
		Connection dbcon = null;
		try {
			HttpSession session = request.getSession();
			
			String title = request.getParameter("title");
			System.out.println(title);
			Integer year = Integer.valueOf(request.getParameter("year"));
			System.out.println(year);
			String director = request.getParameter("director");
			System.out.println(director);
			String first_name = request.getParameter("first_name");
			System.out.println(first_name);
			String last_name = request.getParameter("last_name");
			System.out.println(last_name);
			String genre = request.getParameter("genre");
			System.out.println(genre);
			
			if (title == null || title.isEmpty()){
				session.setAttribute("addMovie_err", "Needs Title.");
				response.sendRedirect("addmovie.jsp");
				return;
			}

			try{
				year = Integer.valueOf(request.getParameter("year"));
			}catch(Exception e){
				year = 0;
				session.setAttribute("addMovie_err", "Invalid Year.");
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
			
			System.out.println("after if");
			
			dbcon = Database.openConnection();
			cst = dbcon.prepareCall("{call add_movie(?, ?, ?, ?, ?, ?)}");
			System.out.println("before");
			cst.setString(1, title);
			cst.setInt(2, year);
			cst.setString(3, director);
			cst.setString(4, first_name);
			cst.setString(5, last_name);
			cst.setString(6, genre);
			cst.execute();
			System.out.println("after");
			
			session.setAttribute("addMovie_err", false);
			
			Statement st = dbcon.createStatement();
			ResultSet rs = st.executeQuery("SELECT * from movies where title='" + title + "';");
			if (rs.next()) {
				Integer id = rs.getInt("id");
				response.sendRedirect("MovieDetails?id=" + id);
			}
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
