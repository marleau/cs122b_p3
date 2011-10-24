
<%@page import="Fabflix.ListResults"%>
<% if (session.getAttribute("isAdmin") != null && !(Boolean)session.getAttribute("isAdmin")) { %>
	<BR><HR>
	<%= ListResults.browseGenres(0) %>
	<BR><HR>
	<%= ListResults.browseTitles(0) %>
<% } %>
		</div>
	</body>

</html>