package apputil;

import java.io.*;
import java.util.ArrayList;

import org.json.*;

import com.js.basic.Files;

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
 * ConfigSet configSet = new ConfigSet(null)  
 *   .add(...ConfigSet.Interface...)
 *   .add(...ConfigSet.Interface...) 
 *   .restore();
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

  /**
   * Constructor
   * 
   * @param file
   *          file to use; if null, uses application name, and looks for
   *          matching file in current directory or one of its ancestors; if not
   *          found, uses current directory
   */
  public ConfigSet(File file) {
    if (file == null) {
      File currentDirectory = new File(System.getProperty("user.dir"));
      String basename = basename();
      file = findFileAsAncestor(basename, currentDirectory);
      if (file == null)
        file = new File(currentDirectory, basename);
    }
    mFile = file;
  }

  public ConfigSet add(Interface c) {
    configs.add(c);
    return this;
  }

  public ConfigSet save() throws IOException {
    try {
      String s = constructJSONString();
      Files.writeStringToFileIfChanged(mFile, s);
    } catch (JSONException e) {
      die(e);
    }
    return this;
  }

  public ConfigSet restore() throws IOException, JSONException {
    JSONObject map = new JSONObject();

    if (mFile.exists()) {
      String s = Files.readString(mFile);
      map = new JSONObject(s);
    }
    for (Interface ic : configs)
      ic.readFrom(map);
    return this;
  }

  private String basename() {
    String appName = AppTools.app().getName();
    String osName = System.getProperty("os.name");
    boolean isWindows = osName.startsWith("Windows");
    if (isWindows)
      return appName + "_defaults.txt";
    else
      return "." + appName + "_defaults";
  }

  private File findFileAsAncestor(String basename, File directory) {
    while (directory != null) {
      File candidate = new File(directory, basename);
      if (candidate.exists())
        return candidate;
      directory = directory.getParentFile();
    }
    return null;
  }

  private String constructJSONString() throws JSONException {
    JSONObject map = new JSONObject();
    for (Interface ic : configs)
      ic.writeTo(map);
    return map.toString(2);
  }

  private ArrayList<Interface> configs = new ArrayList();
  private File mFile;
}
