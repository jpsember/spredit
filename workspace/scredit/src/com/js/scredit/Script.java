package com.js.scredit;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.json.*;

import apputil.*;
import com.js.basic.*;

import static com.js.basic.Tools.*;
import tex.*;

public class Script {

  public static MyFileFilter FILES = new MyFileFilter("Script files", "scr");
  public static MyFileFilter SET_FILES = new MyFileFilter(
      "Script project files", "set");

  public static EdObjectFactory factoryFor(String tag) {
    return sFactories.get(tag);
  }

  public static void addObjectFactory(EdObjectFactory f) {
    sFactories.put(f.getTag(), f);
  }

  /**
   * Constructor
   * 
   * @param project
   *          the ScriptProject this script will belong to
   * @param file
   *          an optional file where this script is to be written; if null, it's
   *          considered an 'anonymous' script that has never been saved
   */
  public Script(ScriptProject project, File file) {
    this.mProject = project;
    setFile(file);
  }

  /**
   * Get the script's file, which may be null
   */
  public File getFile() {
    return mFile;
  }

  public boolean hasName() {
    return mFile != null;
  }

  public void setFile(File file) {
    Files.verifyAbsolute(file, true);
    mFile = file;
  }

  private static final String ITEMS_TAG = "ITEMS";

  /**
   * Write script's contents to its file
   * 
   * @throws IOException
   */
  public void write() throws IOException {
    if (!hasName())
      throw new IllegalStateException("script has no name");
    String content = null;
    try {
      JSONObject scriptMap = new JSONObject();
      {
        JSONObject defaultsMap = new JSONObject();
        for (String key : JSONTools.iterable(mDefaults.keySet())) {
          defaultsMap.put(key, mDefaults.get(key));
        }
        scriptMap.put("defaults", defaultsMap);
      }

      {
        JSONArray scriptItems = new JSONArray();
        for (int i = 0; i < mObjects.size(); i++) {
          EdObject obj = mObjects.get(i);
          EdObjectFactory f = obj.getFactory();
          JSONObject itemMap = new JSONObject();
          f.write(this, itemMap, obj);
          scriptItems.put(f.getTag());
          scriptItems.put(itemMap);
        }
        scriptMap.put(ITEMS_TAG, scriptItems);
      }
      content = scriptMap.toString(2);
    } catch (JSONException e) {
      die(e);
    }
    Files.writeStringToFileIfChanged(getFile(), content);
  }

  public Atlas lastAtlas() {
    return mLastAtlas;
  }

  public void setAtlas(Atlas a) {
    mLastAtlas = a;
  }

  public void setItems(ObjArray items) {
    this.mObjects = items;
  }

  /**
   * Get the objects comprising this script
   */
  public ObjArray items() {
    return mObjects;
  }

  public ScriptProject project() {
    return mProject;
  }

  public void read() throws IOException {
    if (!hasName())
      throw new IllegalStateException();
    String content = FileUtils.readFileToString(getFile());
    try {
      JSONObject fileMap = new JSONObject(content);
      {
        JSONObject defaultsMap = fileMap.getJSONObject("defaults");
        for (String key : JSONTools.keys(defaultsMap)) {
          mDefaults.put(key, defaultsMap.get(key));
        }
      }
      boolean problem = false;
      JSONArray itemsList = fileMap.getJSONArray(ITEMS_TAG);
      int cursor = 0;
      while (cursor < itemsList.length()) {
        String itemTag = itemsList.getString(cursor++);
        JSONObject itemMap = itemsList.getJSONObject(cursor++);
        EdObjectFactory f = factoryFor(itemTag);
        if (f == null) {
          if (!problem) {
            problem = true;
            AppTools.showMsg("unknown object type: " + itemTag + " (" + itemMap
                + ")");
          }
          continue;
        }
        EdObject obj = f.parse(this, itemMap);
        mObjects.add(obj);
      }
    } catch (JSONException e) {
      die(e);
    }
  }

  private static Map<String, EdObjectFactory> sFactories = new HashMap();

  private Atlas mLastAtlas;
  private Map mDefaults = new HashMap();
  private ObjArray mObjects = new ObjArray();
  private ScriptProject mProject;
  private File mFile;
}
