package learn.jdbc.database.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

class MysqlDbConnection extends DbConnection
{
  public Connection connect()
  {
    try {
      Map<String, Object> db = this.getParameters();

      String library = (String) db.get(LIBRARY);
      String driver = (String) db.get(DRIVER);
      String dbHost = (String) db.get(HOST);
      String dbPort = (String) db.get(PORT);
      String dbName = (String) db.get(DATABASE);
      String dbUsername = (String) db.get(USERNAME);
      String dbPassword = (String) db.get(PASSWORD);
      String dbTimezone = (String) db.get(TIMEZONE);

      // E.g: jdbc:mysql://localhost:3306/itf0
      String dbUrl = String.format("%s:%s://%s:%s/%s", library, driver, dbHost, dbPort, dbName);

      System.out.println("Connecting to MySQL Database: " + dbUrl + " by user: " + dbUsername);

      return DriverManager.getConnection(dbUrl + "?serverTimezone=" + dbTimezone, dbUsername, dbPassword);
    }
    catch (SQLException exc){
      handleException(exc);
    }

    return null;
  }

}
