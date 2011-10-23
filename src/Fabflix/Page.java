package Fabflix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

public class Page {
	
	public static String header(ServletContext context, HttpSession session) {
		String rtn = "		<html>" + "			<head>" + "		        <title>Fabflix - " + session.getAttribute("title") + "</title>" + "			</head>" + "				<style>"
				+ readStyle(context) + "				</style>" + "			<body>" + readMenu(context, session) + "					<div class=\"content\">";
		return rtn;
	}
	
	public static String readStyle(ServletContext context){
		//Automatically reads style.css from file; YAY no more replacing!
		
		InputStream is = context.getResourceAsStream("css/style.css");
		String rtn = "";
		if (is != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			try {
				while (reader.ready()) {
					rtn += reader.readLine() + "\n";
				}
			} catch (IOException e) {
				// If error... use old style.css
				rtn = "/* NORMALIZE */* {	margin: 0;	padding: 0;}ul {	list-style-type: none;}/* GLOBAL */body {    font-family: Helvetica;    font-size: 16 px;    color: #666666;}/* MENU */div.menu {	width: 100%;	height: 40 px;	background-color: #333333;	color: #eeeeee;	overflow: hidden;}div.menu a {	display: block;	text-decoration: none;	padding: 10px;	color: #999999;}div.menu a:hover {	color: #ffffff;}div.menu a.first {	color: #1e9184;}div.menu ul {	list-style-type: none;}div.menu ul li {	padding: 10 px;	background-color: #333333;}div.menu ul.main {	height: 39px;}div.menu ul.main li {	float: left;	display: inline;	border-right: 1px solid #999999;}div.menu li.first {	padding-right: 100px;}div.menu li.last {	float: right;	border: 0;}div.menu form {	padding: 10px;}div.menu input {}div.menu button {}/*div.menu ul.sub {	float: right;	background-color: #333333;}div.menu ul.sub li {	float: left;	display: inline;} *//* CONTENT */div.content {	clear: both;	padding: 20px;	line-height: 150%;}div.content form {	width:400px;}/*div.content label{	display:block;	text-align:right;	width:140px;	float:left;}div.content input{	float:left;	font-size:12px;	padding:4px 2px;	border:solid 1px #95e1d8;	width:200px;	margin:2px 0 20px 10px;}div.content button{	clear:both;	float: left;	margin-left:150px;	width:125px;	height:31px;	background:#666666;	border: 0;	text-align:center;	line-height:31px;	color:#FFFFFF;}*/h1, h2, h3 {	margin-bottom: 15px;}ul.cart {	margin-left: 20px;}p {	margin-bottom: 10px;}.error {	margin: 15px;	text-align: center;	width: 400px;	padding: 10px;	background: #fdd5d3;	border: 1px solid #f26a63;}.success {	margin: 15px;	text-align: center;	width: 400px;	padding: 10px;	background: #d4fcd9;	border: 1px solid #6af263;}div.cart {	/* border: 1px dotted green; */	width: 600px;}div.cart form {	width: 600px;}div.cart label {	margin-right: 10px;}div.cart input {	float: right;}div.cart input.qty {	width: 20px;	float: none;}div.cart li {	float: left;	display: inline;	padding-left: 10px;}div.cart li.first {	width: 250px;	padding-left: 0 px;}div.content a {	color: #1e9184;	text-decoration: none;	font-weight: bold;}div.content a:hover {	color: #f6b546;	text-decoration: underline;}hr {	height: 1px;	background: #1e9184;	border: 0;	margin: 20px 0px;}div.ccinfo label, div.ccinfo input {	margin: 10px;}";
			}
		}
		return rtn;
	}
	
	public static String readMenu(ServletContext context, HttpSession session){
		
		InputStream is = context.getResourceAsStream("menu.jsp");
		String rtn = "";
		if (is != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));			
			try {
				while (reader.ready()) {
					rtn += reader.readLine();
				}
			} catch (IOException e) {
				//If error... use old menu
				rtn = "<div class=\"menu\">	<ul class=\"main\">		<li class=\"first\"><a href=\"/project3_10\" class=\"first\">Fabflix</a></li>		<li><a href=\"ListResults\">Browse</a></li>		<li><FORM ACTION=\"ListResults\" METHOD=\"GET\">				<INPUT TYPE=\"TEXT\" NAME=\"arg\">				<INPUT TYPE=\"HIDDEN\" NAME=rpp VALUE=\"5\">				<input TYPE=\"SUBMIT\" VALUE=\"Search Movies\">			</FORM>		</li>		<li class=\"last\"><a href=\"AdvancedSearch\">Advanced Search</a></li>		<li><a href=\"cart\">View Cart</a></li>		<li><a href=\"checkout\">Check out</a></li>		<li><a href=\"logout\">Logout</a></li>	</ul></div>";
			}
		}
		
		// if admin, include admin menu
		Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
		if (isAdmin != null && isAdmin){
			is = context.getResourceAsStream("admin-menu.jsp");
		
			if (is != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));			
				try {
					while (reader.ready()) {
						rtn += reader.readLine();
					}
				} catch (IOException e) {}
			}
		}
		return rtn;
	}
	
	public static void footer(PrintWriter out, Connection dbcon, Integer resultsPerPage) throws SQLException, UnsupportedEncodingException {
		out.println("<hr>");
		ListResults.browseGenres(out, dbcon, resultsPerPage);
		out.println("<hr>");
		ListResults.browseTitles(out, resultsPerPage);
		out.println("</div></body></html>");
	}	
	
}