<%@ page import="Fabflix.*" %>
<% if(Login.kickNonUsers(request, response)){return;} %>
<% if(Login.kickNonAdmin(request, response)){return;} %>
<%@ include file="header.jsp" %>

<div class="add-movie">

	<h2>Add Movie</h2>
	
	<% if (session.getAttribute("addMovie_err") != null && (Boolean)session.getAttribute("addMovie_err") == true) { %>
		<p class="error">You must provide all information.</p>
	<% } %>
	
	<form method="post" action="AddMovie">
	
		<p>Movie Info</p>
		
		<label>Title</label>
		<input type="text" name="title" />
		
		<label>Year</label>
		<input type="text" name="year" />
		
		<label>Director</label>
		<input type="text" name="director" />
		
		<label>Genre</label>
		<input type="text" name="genre" />
		
		<p>Star in Movie</p>
		
		<label>First Name</label>
		<input type="text" name="first_name" />
		
		<label>Last Name</label>
		<input type="text" name="last_name" />
		
		<br>
		
		<button type="submit" value="submit">Add Movie</button>	
	</form>

</div>

<%@ include file="footer.jsp" %>