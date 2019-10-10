package learn.jdbc;

import java.io.*;
import java.sql.*;
import java.util.*;

import learn.jdbc.database.SqlQuery;
import learn.jdbc.database.builder.SqlQueryBuilder;
import learn.jdbc.database.connection.DbConnectionManager;

/**
 *
 * @author www.luv2code.com
 *
 */
public class JdbcTest
{
  private Connection myConn = null;


  private boolean usePreparedStatement = Math.random() > 0.5;
  private boolean usingTransaction = Math.random() > 0.5;
  private boolean commitLastOperation = !usingTransaction || Math.random() > 0.5;


  public static void main(String[] args) throws SQLException
  {
    JdbcTest jdbcTest = new JdbcTest();

    ResultSet myRs = null;

    try {
      // Get a connection to database
      jdbcTest.openConnection();
      System.out.println("Database connection successful!\n");

      if(jdbcTest.usingTransaction){
        jdbcTest.myConn.setAutoCommit(false);
      }
      System.out.println("Using Transaction: " + jdbcTest.usingTransaction + "\n");

      // Show some DB Metadata
//      jdbcTest.displayDBSchemaInfo();

      String statementType = jdbcTest.usePreparedStatement ? "Prepared Statement" : "Raw SQL";
      System.out.printf("Using %s query!\n", statementType);

      // I N S E R T   Q U E R Y
      int affectedRows = jdbcTest.runSqlInsert("users", jdbcTest.sampleInsertData());

      // U P D A T E   Q U E R Y
//      int affectedRows = jdbcTest.runSqlUpdate(usePreparedStatement);
//      int affectedRows = jdbcTest.writeFileToBlob();
//      int affectedRows = jdbcTest.writeFileToClob();

      if(jdbcTest.usingTransaction){
        jdbcTest.endTransaction(jdbcTest.commitLastOperation);
      }

      if(jdbcTest.commitLastOperation){
        String pluralize = affectedRows > 1 ? "s" : "";
        System.out.printf("Affected %d row%s in database!\n", affectedRows, pluralize).println();
      }

      // S E L E C T   Q U E R Y
      Map<String, Object> wheres = new HashMap<>();
      wheres.put("last_name", "Wright");
      wheres.put("first_name", "Eric");

      myRs = jdbcTest.runSqlSelect("users", wheres);

      // SELECT BLOB and write to file
//      jdbcTest.readBlobToFile();
      jdbcTest.readClobToFile();

      // Process the result set
      if(myRs != null){
        // Result Set Metadata
//        jdbcTest.displayResultSetMetadata(myRs);

        while (myRs.next()) {
          System.out
              .printf("%s, %s, %s", myRs.getString("last_name"), myRs.getString("first_name"), myRs.getString("email"))
              .println();
        }
      }
    }
    catch (Exception exc) {
      jdbcTest.handleException(exc);
    }
    finally {
      jdbcTest.terminateDatabaseOperation(myRs);
    }
  }

  private Map<String, Object> sampleInsertData()
  {
//    List<String> columns = Arrays.asList("last_name", "first_name", "middle_name", "email", "gender", "uid", "t_and_c", "password");
//    List<String> values = Arrays.asList("Agbani", "Chidimma", "K", "cyndy.kate@info.org", "female", "qen2wan7fcr", "1", "password");

    String[][] userInfo = {
        {"last_name", "Agbani"},
        {"first_name", "Chidimma"},
        {"middle_name", "K"},
        {"email", "cyndy.kate@info.org"},
        {"gender", "female"},
        {"uid", "qen2wan7fcr"},
        {"t_and_c", "1"},
        {"password", "password"},
    };

    Map<String, Object> columnValues = new HashMap<>();

    for (String[] info : userInfo) {
      columnValues.put(info[0], info[1]);
    }

    return columnValues;
  }


  private void openConnection()
  {
    DbConnectionManager connectionManager = new DbConnectionManager();
    myConn = connectionManager.getConnection();
  }


  private int writeFileToBlob() throws SQLException, IOException
  {
    int affectedRows = 0;

    PreparedStatement myPrepStmt = null;

    System.out.println("Writing user resume from local file to Db BLOB value!\n");

//    File theFile = new File("hmo-form-copy.pdf"); // File too large; catch/handle Exception
    File theFile = new File("files/sample-resume.pdf");
    FileInputStream input = new FileInputStream(theFile);
    System.out.println("Input file abs path: " + theFile.getAbsolutePath() + "\n");
    System.out.println("Input file size (Bytes): " + theFile.length() + "\n"); // OR: input.available()

    try {
      myPrepStmt = this.myConn.prepareStatement(
          "UPDATE users SET resume = ? WHERE last_name = ? and first_name = ?"
      );
      myPrepStmt.setBinaryStream(1, input);
      myPrepStmt.setString(2, "Wright");
      myPrepStmt.setString(3, "Eric");

      affectedRows = myPrepStmt.executeUpdate();
    }
    catch (Exception exc){
      handleException(exc);
    }
    finally {
      if(myPrepStmt != null){
        myPrepStmt.close();
      }
    }

    return affectedRows;
  }


  private void readBlobToFile() throws SQLException
  {
    File theFile = null;
    FileOutputStream output = null;
    PreparedStatement myPrepStmt = null;

    System.out.println("Writing user resume from Db BLOB value to local PDF file!\n");

    try {
      myPrepStmt = this.myConn.prepareStatement(
          "SELECT resume FROM users WHERE email = ?"
      );
      myPrepStmt.setString(1, "eric.wright@example.com");

      ResultSet myRs = myPrepStmt.executeQuery();

      if(myRs.next()){
        InputStream input = myRs.getBinaryStream("resume");
        theFile = new File("files/resume-from-db-blob.pdf"); // Over-writes existing file
        output = new FileOutputStream(theFile);

//        File theFile2 = new File("from-db-blob-values.pdf");
//        System.out.println("Old file exists: " + theFile2.exists() + " :: " + theFile2.length() + "\n");

        byte[] buffer = new byte[1024];
        while (input.read(buffer) > 0){
          output.write(buffer);
        }
      }

      System.out.println("Output file abs path: " + (output != null ? theFile.getAbsolutePath() : "") + "\n");
      System.out.println("Output file size (Bytes): " + (output != null ? theFile.length() : "") + "\n");
    }
    catch (Exception exc){
      handleException(exc);
    }
    finally {
      if(myPrepStmt != null){
        myPrepStmt.close();
      }
    }
  }


  private int writeFileToClob() throws SQLException, IOException
  {
    int affectedRows = 0;

    PreparedStatement myPrepStmt = null;

    System.out.println("Writing user cover letter from local file to Db CLOB value!\n");

    try {
      File theFile = new File("files/sample-cover-letter.txt");
      FileReader input = new FileReader(theFile);
      System.out.println("Input file abs path: " + theFile.getAbsolutePath() + "\n");
      System.out.println("Input file size (Bytes): " + theFile.length() + "\n");

      myPrepStmt = myConn.prepareStatement(
          "UPDATE users SET cover_letter = ? WHERE last_name = ? and first_name = ?"
      );
      myPrepStmt.setCharacterStream(1, input);
      myPrepStmt.setString(2, "Wright");
      myPrepStmt.setString(3, "Eric");

      affectedRows = myPrepStmt.executeUpdate();
    }
    catch (Exception exc){
      handleException(exc);
    }
    finally {
      if(myPrepStmt != null){
        myPrepStmt.close();
      }
    }

    return affectedRows;
  }


  private void readClobToFile() throws SQLException
  {
    File theFile = null;
    FileWriter output = null;
    PreparedStatement myPrepStmt = null;

    System.out.println("Writing user cover-letter from Db CLOB value to local TXT file!\n");

//    try (PreparedStatement myPrepStmt = myConn.prepareStatement(
//        "SELECT cover_letter FROM users WHERE email = ?"
//    )) {
    try {
      myPrepStmt = this.myConn.prepareStatement(
          "SELECT cover_letter FROM users WHERE email = ?"
      );
      myPrepStmt.setString(1, "eric.wright@example.com");

      ResultSet myRs = myPrepStmt.executeQuery();

      if(myRs.next()){
        Reader input = myRs.getCharacterStream("cover_letter");
        theFile = new File("files/cover-letter-from-db-clob.txt"); // Over-writes existing file
        output = new FileWriter(theFile);

        int theChar;
        while ((theChar = input.read()) > 0){
          output.write(theChar);
        }
      }

      System.out.println("Output file abs path: " + (output != null ? theFile.getAbsolutePath() : "") + "\n");
      System.out.println("Output file size (Bytes): " + (output != null ? theFile.length() : "") + "\n");
    }
    catch (Exception exc){
      handleException(exc);
    }
    finally {
      if(myPrepStmt != null){
        myPrepStmt.close();
      }
    }
  }


//  private int runSqlInsert() throws SQLException
//  {
//    // Prepare back-quoted column names
//    String[] columns = {"last_name", "first_name", "middle_name", "email", "gender", "uid", "t_and_c", "password"};
//
//    String columnsString = "";
//    for(String column : columns){
//      columnsString = columnsString.concat("`" + column + "`");
//    }
//
//    // Sample Data
////    String[] values = {"Wright", "Eric", "", "eric.wright@foo.com", "male", "s84xb467r8bcr", 1, "password"};
//    String[] values = {"Agbani", "Chidimma", "K", "cyndy.kate@info.org", "female", "qen2wan7fcr", "1", "password"};
//
//    int affectedRows;
//
//    System.out.println("Inserting user into database!\n");
//
//    if(false){
//      // Prepare placeholder string to match the number of columns
//      String placeholdersString = "";
//      for(int index = 1; index <= columns.length; index++){
//        // Add "?". Then, if not last index, add ","
//        placeholdersString = placeholdersString.concat("?").concat(index < columns.length ? "," : "");
//      }
//
//      PreparedStatement myPrepStmt = this.myConn.prepareStatement(
//          "INSERT INTO users (" + columnsString + ") VALUES (" + placeholdersString + ")"
//      );
//
//      // ToDo: Use more flexible data structure e.g ArrayList, Collectors, etc, to accommodate integers, etc
////      myPrepStmt.setInt(6, 1);
//      for(int index = 1; index <= values.length; index++){
//        myPrepStmt.setString(index, values[index]);
//      }
//
//      affectedRows = myPrepStmt.executeUpdate();
//    }
//    else {
//      Statement myStmt = this.myConn.createStatement();
//
//      // Prepare single-quoted values
//      String stringValues = "";
//      for(String value : values){
//        stringValues = stringValues.concat("'" + value + "'");
//      }
//
//      affectedRows = myStmt.executeUpdate(
//          "INSERT INTO users (last_name, first_name, email, gender, uid, t_and_c, password, middle_name) " +
//              " VALUES (" + stringValues + ")"
//      );
//    }
//
//    return affectedRows;
//  }
  private int runSqlInsert(String table, Map<String, Object> columnValues) throws SQLException
  {
//    List<String> columns = Arrays.asList("last_name", "first_name", "middle_name", "email", "gender", "uid", "t_and_c", "password");
//    List<String> values = Arrays.asList("Agbani", "Chidimma", "K", "cyndy.kate@info.org", "female", "qen2wan7fcr", "1", "password");

    int affectedRows;

    System.out.println("Inserting user into database!\n");

//    MysqlQueryBuilder queryBuilder = new MysqlQueryBuilder();
//
//    String sqlQuery = queryBuilder
//        .insert()
//        .into(table)
//        .columns(columnValues)
//        .getSql();
//
//    System.out.println("sqlQuery: " + sqlQuery + "\n");
//
//    PreparedStatement myPrepStmt = this.myConn.prepareStatement(sqlQuery);
//
//    for(int index = 1; index <= values.length; index++){
//      myPrepStmt.setString(index, values[index]);
//    }
//
//    affectedRows = myPrepStmt.executeUpdate();
//
//    return affectedRows;

    SqlQuery sqlQuery = new SqlQuery();
    SqlQueryBuilder queryBuilder = sqlQuery.getQueryBuilder();

    return sqlQuery.executeUpdate(
        queryBuilder
            .insert()
            .into(table)
            .setColumnsValues(columnValues)
    );
  }


  // If "columns" is not supplied
  private ResultSet runSqlSelect(String table, Map<String, Object> wheres) throws SQLException
  {
    List<String> columns = new ArrayList<>();
    return this.runSqlSelect(table, wheres, columns);
  }


  private ResultSet runSqlSelect(String table, Map<String, Object> wheres, List<String> columns) throws SQLException
  {
    System.out.println("Selecting users from database!\n");

    SqlQuery sqlQuery = new SqlQuery();
    SqlQueryBuilder queryBuilder = sqlQuery.getQueryBuilder();

    return sqlQuery.executeQuery(
        queryBuilder
            .select()
            .columns(columns)
            .from(table)
            .where(wheres)
    );
  }


  private int runSqlUpdate(boolean usePreparedStatement) throws SQLException
  {
    // Data
//    Map<String, Object> updates = new HashMap<String, Object>();
//    updates.put("email", "eric.wright@myhost.com");
//    String valueString = updates.toString();
//
//    Map<String, Object> wheres = new HashMap<String, Object>();
//    wheres.put("last_name", "Wright");
//    wheres.put("first_name", "Eric");

    int affectedRows;

    System.out.println("Updating user in database!\n");

    if(usePreparedStatement){
      PreparedStatement myPrepStmt = this.myConn.prepareStatement(
          "UPDATE users SET email = ? WHERE last_name = ? and first_name = ?"
      );
      myPrepStmt.setString(1, "eric.wright@myhost.com");
      myPrepStmt.setString(2, "Wright");
      myPrepStmt.setString(3, "Eric");

      affectedRows = myPrepStmt.executeUpdate();
    }
    else {
      Statement myStmt = this.myConn.createStatement();

      affectedRows = myStmt.executeUpdate(
          "UPDATE users SET email = 'eric.wright@example.com' WHERE last_name = 'Wright' and first_name = 'Eric'"
      );
    }

    return affectedRows;
  }


  private void endTransaction(boolean commitLastOperation) throws SQLException
  {
    if (commitLastOperation) {
      System.out.println("DB  Transaction COMMITTED!\n");
      myConn.commit();
    }
    else {
      System.out.println("DB  Transaction ROLLED BACK!\n");
      myConn.rollback();
    }
  }


  private void displayDBSchemaInfo() throws SQLException
  {
    String catalog = null;
    String schemaPattern = null;
    String tableNamePattern = null;
    String columnNamePattern = null;
    String[] types = null;

    ResultSet myRs = null;

    // Get  Metadata
    DatabaseMetaData databaseMetaData = this.myConn.getMetaData();

    // Get List of Tables
    System.out.println("List of Tables");
    System.out.println("--------------");

    myRs = databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);

    while (myRs.next()) {
        System.out.println(myRs.getString("TABLE_NAME"));
    }
    System.out.println();

    // Get List of Columns in a table
    System.out.println("List of Columns");
    System.out.println("--------------");

    tableNamePattern = "users";
    myRs = databaseMetaData.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);

    while (myRs.next()) {
        System.out.println(myRs.getString("COLUMN_NAME"));
    }
    System.out.println();

    myRs.close();
  }


  private void displayResultSetMetadata(ResultSet myRs) throws SQLException
  {
    ResultSetMetaData rsMetaData = myRs.getMetaData();

    // Get  Metadata
    int columnCount = rsMetaData.getColumnCount();

    // Get List of Tables
    System.out.println("Column count: " + columnCount + "\n");

    for (int column = 1; column <= columnCount; column++) {
      System.out.println("Column name: " + rsMetaData.getColumnName(column));
      System.out.println("Column type name: " + rsMetaData.getColumnTypeName(column));
      System.out.println("Column is nullable: " + rsMetaData.isNullable(column));
      System.out.println("Column is auto-increment: " + rsMetaData.isAutoIncrement(column) + "\n");
    }
  }


//  private static void terminateDatabaseOperation(Connection myConn, Statement[] stmts, ResultSet myRs) throws SQLException
  private void terminateDatabaseOperation(ResultSet myRs, Statement... stmts) throws SQLException
  {
    // Free up used resources
    try {
      if (myRs != null) {
        myRs.close();
      }

      if (stmts.length > 0) {
        for(Statement stmt : stmts){
          if(stmt != null && !stmt.isClosed()){
            stmt.close();
          }
        }
      }

      if (myConn != null) {
        myConn.close();
      }
    }
    catch (Exception exc){
      this.handleException(exc);
    }
  }


  private void handleException(Exception exc)
  {
    // Print Exception Stack trace
    exc.printStackTrace();
  }

}
