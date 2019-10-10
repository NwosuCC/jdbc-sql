package learn.jdbc.utility;

public class StringHelper
{
  public static String backQuote(String value)
  {
    return "`" + value + "`";
  }


  public static String singleQuote(String value)
  {
    return "'" + value + "'";
  }


  public static String doubleQuote(String value)
  {
    return '"' + value + '"';
  }

}
