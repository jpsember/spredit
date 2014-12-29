package com.js.scredit;

import java.io.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import apputil.*;
import com.js.basic.*;
import static com.js.basic.Tools.*;
import tex.*;

public class Script {
  public static final String SRC_EXT = "scr";
  public static MyFileFilter FILES_ONLY = new MyFileFilter("Script files",
      SRC_EXT, false, null);
  public static MyFileFilter FILES_AND_DIRS = new MyFileFilter("Script files",
      SRC_EXT, true, null);
  public static final String SET_EXT = "set";
  // private static MyFileFilter SET_FILES_ONLY = new MyFileFilter(
  // "Script project files", SET_EXT, false, null);
  public static MyFileFilter SET_FILES_AND_DIRS = new MyFileFilter(
      "Script project files", SET_EXT, true, null);

  /**
   * Create a new script
   */
  public Script(ScriptProject project) {
    this.project = project;
  }

  public void setPath(File p) {
    this.path = p;
  }

  /**
   * Read script
   * 
   * @param baseDir
   *          location of base directory containing project file; if no project
   *          file found here, creates it
   * @throws IOException
   */
  public Script(ScriptProject project, File path) throws IOException {

    final boolean db = false;

    if (db)
      pr("Script constructor: " + path);
    this.project = project;

    this.path = path;

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

  private void read() throws IOException {
    String content = Streams.readTextFile(path.getPath());
    try {
      JSONObject fileMap = new JSONObject(content);
      {
        JSONObject defaultsMap = fileMap.getJSONObject("defaults");
        for (String key : JSONTools.keys(defaultsMap)) {
          defaults.put(key, defaultsMap.get(key));
        }
      }

      // stack for Group parsing
      // ArrayList<GroupObject> groupStack = new ArrayList();

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
        //
        //
        // DefScanner s2 = new DefScanner(itemFields);
        // EdObject obj;
        // try {
        // obj = f.parse(this, s2);
        // } catch (ScanException e) {
        // AppTools.showError("Problem reading script " + path, e);
        // break;
        // }
        // if (obj == null)
        // continue;
        //
        // // if we just started reading a group, push it onto the stack
        // if (obj instanceof GroupObject) {
        // groupStack.add((GroupObject) obj);
        // obj = null;
        // } else {
        // // if we are currently reading a group, add parsed object to it
        // if (!groupStack.isEmpty()) {
        // GroupObject g = last(groupStack);
        // g.addParsedObject(obj);
        //
        // // if the group is now complete, pop it from the stack and
        // // treat it as the object we read
        // if (g.parseComplete()) {
        // pop(groupStack);
        // obj = g;
        // } else {
        // // otherwise, we're not done reading the group; pretend we
        // // haven't read an object yet
        // obj = null;
        // }
        // }
        // }
        //
        // if (obj != null) {
        items.add(obj);
      }
    } catch (JSONException e) {
      die(e);
    }

  }

  public void flush() throws IOException {
    String content = null;
    try {
      JSONObject scriptMap = new JSONObject();
      {
        JSONObject defaultsMap = new JSONObject();
        for (String key : JSONTools.iterable(defaults.keySet())) {
          defaultsMap.put(key, defaults.get(key));
        }
        scriptMap.put("defaults", defaultsMap);
      }

      {
        JSONArray scriptItems = new JSONArray();
        for (int i = 0; i < items.size(); i++) {
          EdObject obj = items.get(i);
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
    if (path == null)
      throw new IllegalStateException("path undefined");

    Streams.writeIfChanged(path, content);
  }

  public File path() {
    return path;
  }

  public Atlas lastAtlas() {
    return lastAtlas;
  }

  public void setAtlas(Atlas a) {
    lastAtlas = a;
  }

  public void setItems(ObjArray items) {
    this.items = items;
  }

  public ObjArray items() {
    return items;
  }

  public ScriptProject project() {
    return project;
  }

  public static EdObjectFactory factoryFor(String tag) {
    return (EdObjectFactory) factories.get(tag);
  }

  public static void addObjectFactory(EdObjectFactory f) {
    factories.put(f.getTag(), f);
  }

  private static Map factories = new HashMap();

  private Atlas lastAtlas;
  private File path;
  // Use a TreeMap so keys are kept in sorted order
  private Map defaults = new TreeMap();
  private ObjArray items = new ObjArray();
  private ScriptProject project;

}
