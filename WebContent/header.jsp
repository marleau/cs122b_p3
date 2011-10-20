<!DOCTYPE html>
<html>
	<head>
        <!--<base href="${pagecontext.request.contextpath}" />-->
        <title>Fabflix - <%= (String) session.getAttribute("title") %></title>
		<!--<link rel="stylesheet" href="css/normalize.css" type="text/css" />-->
		<!--<link rel="stylesheet" href="css/style.css" type="text/css" />-->
		<style>
			<%@ include file="css/style.css" %>
		</style>
	</head>

	<body>
	
	<%@ include file="menu.jsp" %>
	
	<div class="content">
	
	<% Boolean isAdmin = (Boolean) session.getAttribute("isAdmin"); %>
		<% if (isAdmin != null && isAdmin){ %>
			<H3>ADMIN MODE</H3>
		<% } %>