package learn.jdbc.database.connection;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;


abstract class DbConnection
{
  final String HOST = "host";
  final String PORT = "port";
  final String DRIVER = "driver";
  final String LIBRARY = "library";
  final String DATABASE = "name";
  final String USERNAME = "user";
  final String PASSWORD = "password";
  final String TIMEZONE = "timezone";

  /**
   * Database Connection parameters: {
   *   host, user, password, name, ...
   * }
   */
  private Map<String, Object> parameters;


  abstract public Connection connect() throws SQLException;
  

  public void setParameters(Map<String, Object> parameters)
  {
    this.parameters = parameters;
  }


  public Map<String, Object> getParameters()
  {
    return this.parameters;
  }


  public void handleException(Exception exc)
  {
    exc.printStackTrace();
  }

}
