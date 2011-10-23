package Fabflix;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Database {
	
	public static Connection openConnection() throws NamingException, SQLException {
		// Open context for mySQL pooling
		Context initCtx = new InitialContext();

		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		if (envCtx == null)
			System.err.println("envCtx is NULL");

		// Look up our data source in context.xml
		DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

		if (ds == null)
			System.err.println("ds is null.");

		Connection dbcon = ds.getConnection();
		if (dbcon == null)
			System.err.println("dbcon is null.");
		
		// connection is now open
		return dbcon;
	}
}