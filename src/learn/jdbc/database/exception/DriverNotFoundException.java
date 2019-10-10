package learn.jdbc.database.exception;

public class DriverNotFoundException extends RuntimeException
{
  public DriverNotFoundException(String library) {
    super("SQL library " + library + " not  found");
  }
}
