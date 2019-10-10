package learn.jdbc.utility;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;


public class CustomProperties
{
  private int currentHashCode;
  private Properties properties;

  private Map<String, Object> propertiesObj;

  //  Load/detect file path from a provider/config
  private final AtomicReference<String> propertiesFilePath = new AtomicReference<>("demo.properties");

  public CustomProperties()
  {
    lazyLoadProperties();
  }

  /**
   * Returns all properties whose keys are specified in @param propKeys list
   */
  public Map<String, String> getProperties(List<String> propKeys)
  {
    Map<String, String> values = new HashMap<>();

    for(String propKey: propKeys){
      values.put(propKey, properties.getProperty(propKey));
    }

    return values;
  }

  /**
   * E.g if @param subsetKey is "db", returns all properties starting with the string "db."
   */
  public Map<String, Object> getPropertiesSubset(String subsetKey)
  {
    Map<String, Object> values = new HashMap<>();

    for (Object prop : properties.keySet()) {
      String existingProp = prop.toString();

      if(existingProp.startsWith(subsetKey)){
        String newProp = existingProp.replaceFirst(subsetKey + ".", "");
        values.put(newProp, properties.get(existingProp));
      }
    }

    return values;
  }


  private void lazyLoadProperties()
  {
    try {
      properties = new Properties();
      properties.load( getPropertiesFileStream());
    }
    catch (IOException exc){
      exc.printStackTrace();
    }
  }


  private boolean changesDetected()
  {
    return properties.hashCode() != currentHashCode;
  }


  private FileInputStream getPropertiesFileStream() throws IOException
  {
    return new FileInputStream(propertiesFilePath.toString());
  }

}
