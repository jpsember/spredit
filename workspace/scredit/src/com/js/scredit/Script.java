package com.js.scredit;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.json.*;

import com.js.basic.*;

import tex.*;
import static com.js.basic.Tools.*;

public class Script {

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

  private static final String ITEMS_TAG = "items";

  /**
   * Write script's contents to its file
   */
  public void write() throws IOException, JSONException {
    assertHasName();
    JSONObject scriptMap = new JSONObject();
    {
      JSONArray itemsArray = new JSONArray();
      for (EdObject obj : mObjects) {
        EdObjectFactory f = obj.getFactory();
        JSONObject itemMap = new JSONObject();
        f.write(this, itemMap, obj);
        itemsArray.put(f.getTag());
        itemsArray.put(itemMap);
      }
      scriptMap.put(ITEMS_TAG, itemsArray);
    }
    FileUtils.write(getFile(), scriptMap.toString(2));
  }

  public Atlas lastAtlas() {
    return mLastAtlas;
  }

  public void setAtlas(Atlas a) {
    mLastAtlas = a;
  }

  public void setItems(EdObjectArray items) {
    mObjects = frozen(items);
  }

  /**
   * Get the objects comprising this script
   */
  public EdObjectArray items() {
    return mObjects;
  }

  public ScriptProject project() {
    return mProject;
  }

  public void read() throws IOException, JSONException {
    assertHasName();
    String content = Files.readString(getFile());
    JSONObject scriptMap = new JSONObject(content);
    JSONArray itemsArray = scriptMap.getJSONArray(ITEMS_TAG);
    int cursor = 0;
    while (cursor < itemsArray.length()) {
      String itemTag = itemsArray.getString(cursor++);
      JSONObject itemMap = itemsArray.getJSONObject(cursor++);
      EdObjectFactory factory = factoryFor(itemTag);
      if (factory == null) {
        throw new JSONException("unrecognized object type: " + itemTag);
      }
      EdObject obj = factory.parse(this, itemMap);
      mObjects.add(obj);
    }
  }

  private void assertHasName() {
    if (!hasName())
      throw new IllegalStateException("script has no name");
  }

  private static Map<String, EdObjectFactory> sFactories = new HashMap();

  private Atlas mLastAtlas;
  private EdObjectArray mObjects = new EdObjectArray();
  private ScriptProject mProject;
  private File mFile;
}
