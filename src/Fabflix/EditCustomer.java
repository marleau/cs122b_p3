package Fabflix;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class EditCustomer
 */
public class EditCustomer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EditCustomer() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)) {
			return;
		}// kick if not logged in
		if (Login.kickNonAdmin(request, response)) {
			return;
		}// kick if not admin

		response.setContentType("text/html"); // Response mime type

		String customerID = request.getParameter("customerID");

		if (customerID == null) {
			response.sendRedirect("index.jsp");
			return;
		} else {
			customerID = Database.cleanSQL(customerID);
		}

		try {
			Connection dbcon = Database.openConnection();
			Statement statement = dbcon.createStatement();
			String query = "SELECT * FROM customers c LEFT OUTER JOIN creditcards cc ON c.cc_id=cc.id WHERE c.id = '" + customerID + "'";
			ResultSet customer = statement.executeQuery(query);

			if (customer.next()) {
				ServletContext context = getServletContext();
				HttpSession session = request.getSession();
				PrintWriter out = response.getWriter();
				out.println(Page.header(context, session));
				out.println("<H1>Customer ID #" + customerID + "</H1>");
				out.println("<FORM ACTION=\"EditCustomer\" METHOD=\"POST\">");
				String first_name = customer.getString("c.first_name");
				String last_name = customer.getString("c.last_name");
				String ccfirst_name = customer.getString("cc.first_name");
				String cclast_name = customer.getString("cc.last_name");
				String cc_id = customer.getString("cc_id");
				String expiration = customer.getString("expiration");
				String address = customer.getString("address");
				String email = customer.getString("email");
				String password = customer.getString("password");

				if (session.getAttribute("custError") != null) {
					out.println("<p class=\"error\">" + session.getAttribute("custError") + "</p>");
					session.removeAttribute("custError");
				}
				if (session.getAttribute("custSuccess") != null){
					out.println("<p class=\"success\">" + session.getAttribute("custSuccess") + "</p>");
					session.removeAttribute("custSuccess");
				}

				out.println("First Name: " + first_name + "<BR>");
				editFieldLink(out, customerID, first_name, "first_name");
				out.println("<BR>Last Name: " + last_name + "<BR>");
				editFieldLink(out, customerID, last_name, "last_name");
				out.println("<BR>Address: " + address + "<BR>");
				editFieldLink(out, customerID, address, "address");
				out.println("<BR>Email: " + email + "<BR>");
				editFieldLink(out, customerID, email, "email");
				out.println("<BR>Password: " + password + "<BR>");
				editFieldLink(out, customerID, password, "password");

				out.println("<BR><HR>");
				out.println(CheckDB.printCreditCardSummary(cc_id, ccfirst_name, cclast_name, expiration));
				out.println(editCreditCardLink(customerID, first_name + " " + last_name));

				Page.footer(out);
				out.close();
				dbcon.close();
				return;
			}
		} catch (SQLException ex) {
			PrintWriter out = response.getWriter();
			ServletContext context = getServletContext();
			HttpSession session = request.getSession();
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
			HttpSession session = request.getSession();
			out.println(Page.header(context, session));
			out.println("<P>SQL error in doGet: " + ex.getMessage() + "<br>" + ex.toString() + "</P></DIV></BODY></HTML>");
			return;
		}

		HttpSession session = request.getSession();
		CheckDB.returnPath(session, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Login.kickNonUsers(request, response)) {
			return;
		}// kick if not logged in
		if (Login.kickNonAdmin(request, response)) {
			return;
		}// kick if not admin

		response.setContentType("text/html"); // Response mime type

		HttpSession session = request.getSession();

		String value = request.getParameter("value");
		String action = request.getParameter("action");
		String field = request.getParameter("field");
		String customerID = request.getParameter("customerID");

		// Scrub Args
		if (value != null) {
			value = Database.cleanSQL(value);
		}

		if (customerID == null) {
			response.sendRedirect("index.jsp");
			return;
		} else {
			customerID = Database.cleanSQL(customerID);
		}

		try {
			Connection dbcon = Database.openConnection();
			Statement statement = dbcon.createStatement();

			if (action != null && field != null) {
				if (action.equals("edit")) {// ==========EDIT
					if ( field.equals("first_name") || field.equals("last_name") || field.equals("address") || field.equals("email") || field.equals("password") ) {
						if (value != null && !value.isEmpty()){
							String query = "UPDATE customers SET " + field + " = '" + value + "' WHERE id = '" + customerID + "'";
							statement.executeUpdate(query);
							session.setAttribute("custSuccess", field+ " Updated!");
							response.sendRedirect("EditCustomer?customerID=" + customerID);
							return;
						}else {
							session.setAttribute("custError", "Missing "+field);
							backToEdit(response, customerID);
							dbcon.close();
							return;
						}
					}
				} else if (action.equals("update")) {
					if (field.equals("cc_id")) {
						String cc_id = request.getParameter("cc_id");
						String expiration = request.getParameter("expiration");
						String first_name = request.getParameter("first_name");
						String last_name = request.getParameter("last_name");

						if (first_name == null || first_name.isEmpty()) {
							session.setAttribute("custError", "Missing First Name");
							backToEdit(response, customerID);
							dbcon.close();
							return;
						}
						if (last_name == null || last_name.isEmpty()) {
							session.setAttribute("custError", "Missing Last Name");
							backToEdit(response, customerID);
							dbcon.close();
							return;
						}
						if (cc_id == null || cc_id.isEmpty()) {
							session.setAttribute("custError", "Missing Credit Card Number.");
							backToEdit(response, customerID);
							dbcon.close();
							return;
						}
						if (expiration == null || expiration.isEmpty() || !Database.isValidDate(expiration)) {
							session.setAttribute("custError", "Missing Expiration Date");
							backToEdit(response, customerID);
							dbcon.close();
							return;
						}

						String query = "SELECT * FROM creditcards WHERE id = '" + cc_id + "' AND first_name = '" + first_name + "' AND last_name = '" + last_name + "' AND expiration = '" + expiration + "'";
						ResultSet ccLookup = statement.executeQuery(query);
						if (ccLookup.next()) {
							statement = dbcon.createStatement();
							query = "UPDATE customers c SET cc_id = '" + cc_id + "' WHERE id = '" + customerID + "'";
							statement.executeUpdate(query);

							session.setAttribute("custSuccess", "Credit Card Updated!");
							response.sendRedirect("EditCustomer?customerID=" + customerID);
							dbcon.close();
							return;
						}else {
							session.setAttribute("custError", "Invalid Credit Card");
							backToEdit(response, customerID);
							dbcon.close();
							return;
						}
					}
				}
			} else if (action.equals("ccForm")) {
				PrintWriter out = response.getWriter();
				ServletContext context = getServletContext();
				out.println(Page.header(context, session));

				out.println("<H1>Customer ID #" + customerID + "</H1>");
				out.println("<H2>Enter New Credit Card Information:</H2>");
				out.println("<FORM ACTION=\"EditCustomer\" METHOD=\"POST\">");

				String query = "SELECT * FROM customers c LEFT OUTER JOIN creditcards cc ON c.cc_id=cc.id WHERE c.id = '" + customerID + "'";
				ResultSet customer = statement.executeQuery(query);
				customer.next();

				String cc_id = customer.getString("cc_id");
				String expiration = customer.getString("expiration");
				String first_name = customer.getString("cc.first_name");
				String last_name = customer.getString("cc.last_name");

				out.println("<form method=\"post\" action=\"EditCustomer\">");
				out.println("<INPUT TYPE=\"HIDDEN\" NAME=customerID VALUE=\"" + customerID + "\">");
				out.println("<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"update\">");
				out.println("<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"cc_id\">");
				out.println("First Name:<BR>");
				out.println("<INPUT TYPE=\"TEXT\" NAME=first_name VALUE=\"" + first_name + "\">");
				out.println("<BR>");
				out.println("Last Name:<BR>");
				out.println("<INPUT TYPE=\"TEXT\" NAME=last_name VALUE=\"" + last_name + "\">");
				out.println("<BR>");
				out.println("Credit Card:<BR>");
				out.println("<INPUT TYPE=\"TEXT\" NAME=cc_id VALUE=\"" + cc_id + "\">");
				out.println("<BR>");
				out.println("Expiration (yyyy-MM-dd):<BR>");
				out.println("<INPUT TYPE=\"TEXT\" NAME=expiration VALUE=\"" + expiration + "\">");
				out.println("<BR><BR>");
				out.println("<button type=\"submit\" value=\"submit\">Update Credit Card</button></form>");
				Page.footer(out);
				out.close();
				dbcon.close();
				return;
			}

			dbcon.close();
		} catch (SQLException ex) {
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

		CheckDB.returnPath(session, response);
	}

	private void backToEdit(HttpServletResponse response, String customerID)
			throws IOException, UnsupportedEncodingException {
		response.sendRedirect("EditCustomer?customerID=" + java.net.URLEncoder.encode(customerID, "UTF-8") );
	}

	public static String editCustomerLink(String customerID, String buttonName) {
		return "<form method=\"get\" action=\"EditCustomer\">" + "<INPUT TYPE=\"HIDDEN\" NAME=customerID VALUE=\"" + customerID + "\">" + "<button type=\"submit\" value=\"submit\">Edit " + buttonName + "</button>" + "</form>";
	}

	public static String editCustomerIDLink() {
		return "<form method=\"get\" action=\"EditCustomer\">" + "<INPUT TYPE=\"TEXT\" NAME=customerID \">" + "<button type=\"submit\" value=\"submit\">Lookup Customer By ID</button>" + "</form>";
	}

	public static String editCreditCardLink(String customerID, String buttonName) {
		return "<form method=\"post\" action=\"EditCustomer\">" + "<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"ccForm\">" + "<INPUT TYPE=\"HIDDEN\" NAME=customerID VALUE=\"" + customerID + "\">" + "<button type=\"submit\" value=\"submit\">Edit " + buttonName + "'s Credit Card</button>" + "</form>";
	}

	public static void editFieldLink(PrintWriter out, String customerID, String oldVal, String field) {
		out.println("<form method=\"post\" action=\"EditCustomer\">" + "<input type=\"text\" name=\"value\" value=\"" + oldVal + "\" />" + "<INPUT TYPE=\"HIDDEN\" NAME=action VALUE=\"edit\">" + "<INPUT TYPE=\"HIDDEN\" NAME=field VALUE=\"" + field + "\">" + "<INPUT TYPE=\"HIDDEN\" NAME=customerID VALUE=\"" + customerID + "\">" + "<button type=\"submit\" value=\"submit\">Change " + field + "</button>" + "</form>");
	}
}
