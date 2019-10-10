package learn.jdbc.database.builder;

import learn.jdbc.utility.StringHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SqlQueryBuilder
{
  private enum Action {
    INSERT, SELECT, UPDATE, DELETE
  };

  private enum TablePreposition {
    FROM, INTO
  };

  private Map<String, String> parts = new HashMap<>();

  /**
   * The original column->value bindings
   * Values are added to this only by calling the method 'bindValue()'
   */
  private List<Object> originalBindings = new ArrayList<>();

  /**
   * A re-arranged copy of @property this.originalBindings list
   * Updated whenever sql query string is requested, e.g @method this.getSql() is called for query execution
   *  - Values are re-arranged to match the placeholder (?) indexes in the query string @property this.sql
   * Returned whenever column->value bindings is requested, e.g @method this.getCompiledBindings() for query execution
   */
  private List<Object> bindings = new ArrayList<>();


  public String getSql()
  {
    return this.compileQuery();
  }


  public String toString()
  {
    return this.getSql();
  }


  private String bindValue(String value)
  {
    this.originalBindings.add(value);
    return "?" + this.originalBindings.size();
  }


  public List<Object> getBindings()
  {
    return this.bindings;
  }


  // "SELECT * FROM users WHERE last_name = ? and first_name = ?";
  // E.g: this.select().from(table).columns(columns).where(wheres);
  public SqlQueryBuilder select()
  {
    this.parts.put("action", Action.SELECT.name());
    return this;
  }


  // "INSERT INTO users (last_name, first_name, email, gender, uid, t_and_c, password, middle_name) VALUES (" + stringValues + ")"
  // E.g: this.insert().into(table).columns(columns).where(wheres);
  public SqlQueryBuilder insert()
  {
    this.parts.put("action", Action.INSERT.name());
    return this;
  }


  public SqlQueryBuilder from(String table)
  {
    return this.table(table, TablePreposition.FROM);
  }


  public SqlQueryBuilder into(String table)
  {
    return this.table(table, TablePreposition.INTO);
  }


  private SqlQueryBuilder table(String table, TablePreposition tablePreposition)
  {
    table = StringHelper.backQuote(table);
    String preposition = tablePreposition.name();

    // E.g: this.parts.put("from", String.format("FROM %s", table));
    this.parts.put(preposition.toLowerCase(), preposition + " " + table);
    return this;
  }


  public SqlQueryBuilder columns(List<String> columns)
  {
    String columnsString = "";
    int size = columns.size();

    if(size == 0){
      columnsString = "*";
    }
    else {
      for(String column : columns){
        columnsString = columnsString.concat(StringHelper.backQuote(column));
        if(--size > 0){
          columnsString = columnsString.concat(", ");
        }
      }
    }

    this.parts.put("columns", columnsString);
    return this;
  }


  public SqlQueryBuilder setColumnsValues(Map<String, Object> columnValues)
  {
    List<String> columns = new ArrayList<>(columnValues.keySet());

    String columnsString = "", valuesString = "";

    for(int index = 1, size = columns.size(); index <= size; index++){
      // Get the column name by its array index, then, add it to the 'columns' string
      String column = columns.get(index - 1);
      columnsString = columnsString.concat(StringHelper.backQuote(column));

      // Get the column value by its column name, then, bind the value and add the returned bind-key to the 'values' string
      String bindParamKey = this.bindValue(columnValues.get(column).toString());
      valuesString = valuesString.concat(bindParamKey);

      // Add a trailing comma if there is a next item to concatenate
      if(index < size){
        columnsString = columnsString.concat(", ");
        valuesString = valuesString.concat(", ");
      }
    }

    this.parts.put("columns", columnsString);
    this.parts.put("values", String.format("VALUES (%s)", valuesString));
    return this;
  }


  public SqlQueryBuilder where(Map<String, Object> wheres)
  {
    List<String> whereColumns = new ArrayList<>(wheres.keySet());
    int size = whereColumns.size();
    String whereString = "";

    for(String whereColumn : whereColumns){
      // Get the 'where' value by its column name, then, bind the value and add the returned bind-key to the 'where' string
      String bindParamKey = this.bindValue(wheres.get(whereColumn).toString());
      whereString = whereString.concat(whereColumn + " = " + bindParamKey);

      if(--size > 0){
        whereString = whereString.concat(" AND "); // Only 'AND' conjunction, for now. 'OR', etc, will be added soon
      }
    }

    if(! whereString.isBlank()) {
      this.parts.put("where", String.format("WHERE %s", whereString));
    }

    return this;
  }


  private boolean performs(Action action)
  {
    String currentAction = this.parts.getOrDefault("action", null);
    return currentAction.equals(action.name());
  }


  private String compileQuery()
  {
    String sql = "";

    if(this.performs(Action.SELECT)) {
      String
          columns = this.parts.get("columns"),
          fromTable = this.parts.get("from"),
          where = this.parts.get("where");
      sql = String.format("SELECT %s %s %s", columns, fromTable, where);
    }
    else if(this.performs(Action.INSERT)){
      String
          columns = this.parts.get("columns"),
          intoTable = this.parts.get("into"),
          values = this.parts.get("values");
      sql = String.format("INSERT %s (%s) %s", intoTable, columns, values);
    }

    // Re-arrange bindings and return the new sql with only "?" placeholders
    return this.synchroniseQueryBindings(sql);
  }


  // ToDo: Abstract and move to StringHelper class
  public String synchroniseQueryBindings(CharSequence sql)
  {
    String bindParamKeyRegex = "(\\?)(\\d+)";
    Pattern pattern = Pattern.compile(bindParamKeyRegex);

    Matcher matcher = pattern.matcher(sql);
    StringBuffer stringBuffer = new StringBuffer(sql.length());

    while (matcher.find()) {
      String placeholder = matcher.group(1); // E.g: "?"
      String placeholderRegex = Matcher.quoteReplacement(placeholder); // E.g: "\\?" ("?" quoted/escaped)

      String bindParamNumber = matcher.group(2);    // E.g: "1"
      int matchedIndex = Integer.parseInt(bindParamNumber); // E.g: 1
      this.bindings.add(this.originalBindings.get(matchedIndex - 1).toString());

      matcher.appendReplacement(stringBuffer, placeholderRegex);
    }
    matcher.appendTail(stringBuffer);

    return stringBuffer.toString();
  }

}
