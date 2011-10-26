<%@ page import="Fabflix.*, java.util.*" %>

<% if(Login.kickNonUsers(request, response)){return;} %>
<% ShoppingCart.initCart(request, response); %>
<% Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart"); %>

<%@ include file="header.jsp" %>
<%@page import="java.sql.*"%>
<%@page import="java.text.*"%>


<%@page import="java.util.Date"%><h1>Checkout</h1>

<% if (!(Boolean)session.getAttribute("processed")) { %>

<div class="cart">
	
<ul class="cart">
<% if (ShoppingCart.isCartEmpty(request, response)) { %>
	<li>Your cart is empty.</li>
<% } else { %>
	<form class="cart" action="cart" method="post"><% for (Map.Entry entry : cart.entrySet() ) { %>
		<li>
			<ul class="item">
				<li class="first"><a href="MovieDetails?id=<%= entry.getKey() %>"><%= ShoppingCart.getMovieTitle(request, response, (String)entry.getKey()) %></a></li>
				<li><label>Quantity</label><input class="qty" type="text" name="<%= entry.getKey() %>" value="<%= entry.getValue() %>"></li>
				<li><a href="cart?remove=<%= entry.getKey() %>">Remove</a></li>
			</ul>
		</li>
	<% } %>
</ul>
	<br><br>
	<div style="clear: both;"></div>
	<a style="float: right; margin: 10px;" href="cart?clear=1">Empty cart</a>
	<input style="margin: 10px;" type="hidden" name="updateCart" value="1">
	<input style="margin: 10px;" type="submit" value="Update">
	</form>
<% } %>
</div>
<!-- TODO Hide CC form from admin logins -->

<% if ((Boolean)session.getAttribute("isAdmin") != null && !(Boolean)session.getAttribute("isAdmin")) { %>
	<div class="ccinfo">
		
		<br><br>
	
		<form method="post" action="checkout">
		<% Connection dbcon = Database.openConnection();
			Statement st = dbcon.createStatement();
			ResultSet cust = st.executeQuery("SELECT * FROM customers c LEFT OUTER JOIN creditcards cc ON c.cc_id=cc.id WHERE c.id = '"+session.getAttribute("user.id")+"'");
			cust.next();
			String first_name = cust.getString("cc.first_name");
			String last_name = cust.getString("cc.last_name");
			String cc_id = cust.getString("cc_id");
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		    Date expiration = (Date)formatter.parse(cust.getString("expiration"));
		    Date currentDate = new Date();
		    String year = "";
		    String day = "";
		    String month = "";
		    if ( expiration.after(currentDate) ){
				SimpleDateFormat yearF = new SimpleDateFormat("yyyy");
				SimpleDateFormat monthF = new SimpleDateFormat("MM");
				SimpleDateFormat dayF = new SimpleDateFormat("dd");
				year = yearF.format(expiration);
				day = dayF.format(expiration);
				month = monthF.format(expiration);
		    }else {
		    	//Invalid CC
		    	cc_id = "";
				first_name = cust.getString("c.first_name");
				last_name = cust.getString("c.last_name");
		    	%><p class="error">Your credit card on file has expired.<BR>Please enter a new one.</p><% 
		    }
		    dbcon.close();
		%>
			<h3>Credit Card Information</h3>
			
			<% if (session.getAttribute("ccError") != null) { %>
				<p class="error">Your credit card information is not valid.</p>
			<% } %>
			
			<label>First Name</label><input type="text" name="firstName" value="<%= first_name %>"/>
			
			<br>
			
			<label>Last Name</label><input type="text" name="lastName" value="<%= last_name %>" />
			
			<br>
			
			<label>Card Number</label><input type="text" name="id"  value="<%= cc_id %>"/>
			
			<br>
			
			<p><b>Expiration Date</b></p>
			
			<label>Month</label><input type=text" name="month" value="<%= month %>"/>
			
			<br>
			
			<label>Day</label><input type=text" name="day" value="<%= day %>"/>
			
			<br>
			
			<label>Year</label><input type=text" name="year" value="<%= year %>" />
			
			<br>
			
			<button type="submit" value="submit">Process Order</button>

			<button type="reset" value="reset">Reset Form</button>
		
		</form>
	</div>
<% } %>
<% } else { %>
	<p class="success">Your order has been processed.</p>
<% } %>


<%@ include file="footer.jsp" %>
