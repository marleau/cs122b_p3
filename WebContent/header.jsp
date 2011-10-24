<!DOCTYPE html>
<html>
	<head>
        <title>Fabflix - <%= (String) session.getAttribute("title") %></title>
		<style>
			<%@ include file="css/style.css" %>
		</style>
	</head>

	<body>
	
	<%@ include file="menu.jsp" %>
	
	<% Boolean isAdmin = (Boolean) session.getAttribute("isAdmin"); %>
	<% if (isAdmin != null && isAdmin){ %>
		<%@ include file="admin-menu.jsp" %>
	<% } %>	
		
	<div class="content">
	