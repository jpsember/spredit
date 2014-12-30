package com.js.scredit;

import java.io.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import apputil.*;
import static com.js.basic.Tools.*;

/**
 * <pre>
 * 
 * Represents a layered sequence of scripts
 * 
 * </pre>
 * 
 */
public class ScriptSet {

  public ScriptSet(File projectBase, int nLayers, int currentLayer,
      int fgndStart) {
    mProjectBase = projectBase;
    mCurrentLayer = currentLayer;
    mForegroundStart = fgndStart;
    mFiles = new File[nLayers];
  }

  public ScriptSet(File projectBase, JSONObject map) throws JSONException {
    mProjectBase = projectBase;
    if (map == null)
      return;

    mCurrentLayer = map.getInt("current");
    mForegroundStart = map.getInt("fg");
    JSONArray array = map.getJSONArray("paths");
    mFiles = new File[array.length()];
    for (int i = 0; i < array.length(); i++) {
      String relPathString = array.getString(i);
      if (!relPathString.isEmpty()) {
        mFiles[i] = new RelPath(mProjectBase, relPathString).file();
      }
    }
  }

  public File file(int n) {
    return mFiles[n];
  }

  public int size() {
    return mFiles.length;
  }

  public int getForegroundLayer() {
    return mForegroundStart;
  }

  public int getCurrentLayer() {
    return mCurrentLayer;
  }

  public void setFile(int i, File f) {
    mFiles[i] = f;
  }

  public JSONObject encode() {
    JSONObject map = new JSONObject();
    try {
      map.put("current", mCurrentLayer);
      map.put("fg", mForegroundStart);
      JSONArray array = new JSONArray();
      for (int i = 0; i < size(); i++) {
        File f = file(i);
        if (f == null)
          array.put("");
        else
          array.put(new RelPath(mProjectBase, f).toString());
      }
      map.put("paths", array);
    } catch (JSONException e) {
      die(e);
    }
    return map;
  }

  private File mProjectBase;
  private int mCurrentLayer;
  private int mForegroundStart;
  private File[] mFiles;
}
