package learn.jdbc.database.connection;

import learn.jdbc.utility.CustomProperties;
import learn.jdbc.database.exception.DriverNotFoundException;
import learn.jdbc.database.builder.MysqlQueryBuilder;
import learn.jdbc.database.builder.SqlQueryBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DbConnectionManager
{
  private DbConnection dbConnection = null;

  private Connection connection = null;

  private Map<String, Object> parameters;


  public DbConnectionManager()
  {
    CustomProperties customProperties = new CustomProperties();

    this.parameters = customProperties.getPropertiesSubset("db");

    this.dbConnection = this.getDbConnectionImplementation(this.getDbConnectionDriver());

    this.dbConnection.setParameters(this.parameters);
  }


  public SqlQueryBuilder getQueryBuilder()
  {
    String driver = this.getDbConnectionDriver();

    switch (driver){
      case "mysql": return new MysqlQueryBuilder();
    }

    throw new DriverNotFoundException(driver);
  }


  public String getDbConnectionDriver()
  {
    return (String) this.parameters.getOrDefault("driver", null);
  }


  public void setConnection(Connection connection)
  {
    this.connection = connection;
  }


  public Connection getConnection()
  {
    try {
      if(this.connection == null || this.connection.isClosed()){
        this.setConnection(this.dbConnection.connect());
      }
      return this.connection;
    }
    catch (SQLException exc) {
      this.handleException(exc);
      return null;
    }
    // ToDo: will this work? (for automatic connection close after use, garbage collection, etc)
//    finally {
//      this.terminateConnection();
//    }
  }


  public void terminateConnection()
  {
    try {
      if(this.connection != null){
        this.connection.close();
        this.connection = null;
      }
    }
    catch (SQLException exc) {
      this.handleException(exc);
    }
  }


  public DbConnection getDbConnectionImplementation(String driver) throws DriverNotFoundException
  {
    switch (driver){
      case "mysql": return new MysqlDbConnection();
    }

    throw new DriverNotFoundException(driver);
  }


  private void handleException(Exception exc)
  {
    exc.printStackTrace();
  }

}
