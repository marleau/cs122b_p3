// JDBC Example - printing a database's metadata
// Coded by Chaitanya Desai Spring'08


import java.sql.*;				// Enable SQL processing

public class JDBC
{

	public static void main(String[] arg) throws Exception
	{

		// Incorporate mySQL driver
		Class.forName("com.mysql.jdbc.Driver").newInstance();

		 // Connect to MySQL as root
		Connection connection = DriverManager.getConnection("jdbc:mysql://","root", "rootpass");
		
		// Create and execute an SQL statement to get all the database names
		Statement myDBStm = connection.createStatement();
		ResultSet resultDB = myDBStm.executeQuery("show databases");
		String dbName;
		while (resultDB.next())
		{
			dbName = resultDB.getString(1);
			//only get information inside the "moviedb" database
			if (dbName.compareTo("moviedb")==0)
			{
				//first, we need to switch to this database;
				Statement mySWStm = connection.createStatement();
				mySWStm.execute("use moviedb");
				//Create and execute an SQL statement to get all the table names in moviedb
				Statement myTBStm = connection.createStatement();
				ResultSet resultTB = myTBStm.executeQuery("show tables");
				//myStm.close();			
				String tblName;
				ResultSet ColData;
				Statement myColStm;
				while (resultTB.next())
				{
					tblName = resultTB.getString(1);
					System.out.println("\n**Table Name:** " + tblName + "\n");
					System.out.println("Metadata about columns in this table:\n");
					System.out.println("==== Field Name ==== Field Type ===== Null Allowed ?");
					System.out.println("----------------------------------------------------");
					myColStm = connection.createStatement();
					// Create and execute an SQL statement to get all the column names for this table
					ColData = myColStm.executeQuery("describe "+tblName);
					//myStm.close();
					while (ColData.next())
					{
						System.out.print("==== "+ColData.getString(1));
						System.out.print("==== "+ColData.getString(2));
						System.out.println("==== "+ColData.getString(3));
					}					
				}			
			}
		}
			
	}
}
