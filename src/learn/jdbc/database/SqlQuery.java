package learn.jdbc.database;

import learn.jdbc.database.connection.DbConnectionManager;
import learn.jdbc.database.builder.SqlQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SqlQuery
{
  private DbConnectionManager dbConnectionManager;
  private SqlQueryBuilder queryBuilder;


  public SqlQuery()
  {
    this.dbConnectionManager = new DbConnectionManager();
    this.queryBuilder = this.dbConnectionManager.getQueryBuilder();
  }


  public SqlQueryBuilder getQueryBuilder()
  {
    return this.queryBuilder;
  }


  public ResultSet executeQuery(SqlQueryBuilder queryBuilder) throws SQLException
  {
    return this.getStatement(queryBuilder).executeQuery();
  }


  public int executeUpdate(SqlQueryBuilder queryBuilder) throws SQLException
  {
    return this.getStatement(queryBuilder).executeUpdate();
  }


  private Connection getConnection()
  {
    return this.dbConnectionManager.getConnection();
  }


  private PreparedStatement getStatement(SqlQueryBuilder queryBuilder) throws SQLException
  {
    String sqlQuery = queryBuilder.getSql();

    List<Object> bindings = queryBuilder.getBindings();

    PreparedStatement preparedStatement = this.getConnection().prepareStatement(sqlQuery);

    // ToDo: Use more flexible data structure e.g ArrayList, Collectors, etc, to accommodate integers, etc
    // E.g: preparedStatement.setInt(6, 1);
    for(int index = 1; index <= bindings.size(); index++){
      preparedStatement.setString(index, bindings.get(index - 1).toString());
    }

    return preparedStatement;
  }

}
