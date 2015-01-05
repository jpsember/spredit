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

  // ------------- class elements

  public static MyFileFilter FILES = new MyFileFilter("Script files", "scr");
  public static MyFileFilter SET_FILES = new MyFileFilter(
      "Script project files", "set");

  public static EdObjectFactory factoryFor(String tag) {
    return (EdObjectFactory) sFactories.get(tag);
  }

  public static void addObjectFactory(EdObjectFactory f) {
    sFactories.put(f.getTag(), f);
  }

  private static Map sFactories = new HashMap();

  // --------------- instance elements

  /**
   * Create a new script
   */
  public Script(ScriptProject project) {
    this.mProject = project;
  }

  // Shouldn't have 'path' field for Script
  @Deprecated
  public void setPath(File p) {
    this.mPath = p;
  }

  /**
   * Read script
   * 
   * @param baseDir
   *          location of base directory containing project file; if no project
   *          file found here, creates it
   * @throws IOException
   * @deprecated shouldn't include path information
   */
  public Script(ScriptProject project, File path) throws IOException {

    final boolean db = false;

    if (db)
      pr("Script constructor: " + path);
    this.mProject = project;

    this.mPath = path;

    File f = path;
    if (f.exists()) {
      try {
        read();
      } catch (FileNotFoundException e) {
        throw new IOException(e);
      }
    }
  }

  private static final String ITEMS_TAG = "ITEMS";

  public void flush() throws IOException {
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
    if (mPath == null)
      throw new IllegalStateException("path undefined");

    Files.writeStringToFileIfChanged(mPath, content);
  }

  @Deprecated
  public File path() {
    return mPath;
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

  public ObjArray items() {
    return mObjects;
  }

  public ScriptProject project() {
    return mProject;
  }

  private void read() throws IOException {
    String content = FileUtils.readFileToString(mPath);
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

  private Atlas mLastAtlas;
  private File mPath;
  private Map mDefaults = new HashMap();
  private ObjArray mObjects = new ObjArray();
  private ScriptProject mProject;

}
