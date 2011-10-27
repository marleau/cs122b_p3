package Fabflix;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		if (Login.kickNonAdmin(request, response)) {return;}
		HttpSession session = request.getSession();
		session.setAttribute("title", "Add Movie");
		session.setAttribute("addMovie_err", false);
		response.sendRedirect("addmovie.jsp");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			
			if (title == null || year == null || director == null || genre == null || first_name == null || last_name == null) {
				session.setAttribute("addMovie_err", true);
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
