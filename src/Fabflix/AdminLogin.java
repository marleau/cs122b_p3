package Fabflix;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AdminLogin extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static void kickNonAdmins(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		Boolean adminLogin = (Boolean) session.getAttribute("admin_login");
		
		if (adminLogin != null && !adminLogin) {
			String URL = request.getRequestURL().toString();
			String qs = request.getQueryString();
			if (qs != null) {
				URL += "?" + qs;
			}
			// Save destination till after logged in
			session.setAttribute("user.dest", URL);
			// send to login page if not logged in
			response.sendRedirect("admin");		
		}
	}
	
}
