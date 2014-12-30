package apputil;

import java.io.*;
import java.util.ArrayList;

import org.json.*;

import com.js.basic.Streams;

import static com.js.basic.Tools.*;

/**
 * <pre>
 * 
 * For persisting application preferences to the file system
 * 
 * Usage:
 * 
 * 1) Construct, add listeners, and restore from file system:
 * 
 * ConfigSet configSet = new ConfigSet(...File...)  
 * .add(...ConfigSet.Interface...)
 * .add(...ConfigSet.Interface...) 
 * .restore();
 * 
 * 2) Save:
 * 
 * configSet.save();
 * 
 * </pre>
 */
public class ConfigSet {

  public interface Interface {
    /**
     * Give client an opportunity to restore configuration state from JSON
     * object
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

  public ConfigSet(File file) {
    mFile = file;
  }

  public ConfigSet add(Interface c) {
    configs.add(c);
    return this;
  }

  public ConfigSet save() throws IOException {
    String s = write();
    Streams.writeIfChanged(mFile, s);
    return this;
  }

  private String write() {
    JSONObject map = new JSONObject();
    try {
      for (Interface ic : configs)
        ic.writeTo(map);
    } catch (JSONException e) {
      die(e);
    }
    return map.toString();
  }

  public ConfigSet restore() throws IOException, JSONException {
    JSONObject map = new JSONObject();

    if (mFile.exists()) {
      String s = Streams.readTextFile(mFile.getPath());
      map = new JSONObject(s);
    }
    for (Interface ic : configs)
      ic.readFrom(map);
    return this;
  }

  private ArrayList<Interface> configs = new ArrayList();
  private File mFile;
}
