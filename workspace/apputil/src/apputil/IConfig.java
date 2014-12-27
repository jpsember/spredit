package apputil;

import org.json.*;

public interface IConfig {

  /**
   * Give client an opportunity to restore configuration state from JSON object
   * 
   * @param map
   * @throws JSONException
   */
  public void readFrom(JSONObject map) throws JSONException;

  /**
   * Give client an opportunity to save configuration state to JSON object
   * 
   * @param map
   * @throws JSONException
   */
  public void writeTo(JSONObject map) throws JSONException;
}
