package com.js.scredit;

import java.io.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Streams;

import apputil.*;
import static com.js.basic.Tools.*;

public class ScriptSet {

  public ScriptSet(File projectBase, int nLayers, int currentLayer,
      int fgndStart) {
    this.base = projectBase;
    this.size = nLayers;
    this.current = currentLayer;
    this.fg = fgndStart;
    this.files = new File[size];
  }

  public ScriptSet(File projectBase, String s) {
    this.base = projectBase;
    // this. contents = s;
    parse(s);
  }

  public ScriptSet(File projectBase, File f) throws IOException {
    this.base = projectBase;
    // this. contents = ;
    parse(Streams.readTextFile(f.toString()));
  }

  public File file(int n) {
    return files[n];
  }

  public int nLayers() {
    return size;
  }

  public int getFgnd() {
    return fg;
  }

  public int getCurrent() {
    return current;
  }

  public void setFile(int i, File f) {
    files[i] = f;
  }

  public String encode() {
    JSONObject map = new JSONObject();
    try {
      // map.put("size", nLayers());
      map.put("current", current);
      map.put("fg", fg);
      JSONArray a = new JSONArray();
      for (int i = 0; i < nLayers(); i++) {
        File f = file(i);
        if (f == null)
          a.put("!");
        else
          a.put(new RelPath(base, f).toString());
      }
      map.put("paths", a);
    } catch (JSONException e) {
      die(e);
    }
    return map.toString();
  }

  private void parse(String contents) {
    try {
      JSONObject map = new JSONObject(contents);
      JSONArray a = map.getJSONArray("paths");

      size = a.length();
      current = map.getInt("current");
      fg = map.getInt("fg");

      files = new File[a.length()];
      for (int i = 0; i < a.length(); i++) {
        String relPathString = a.getString(i);
        if (!relPathString.equals("!")) {
          files[i] = new RelPath(base, relPathString).file();
        }
      }
    } catch (JSONException e) {
      die(e);
    }
  }

  private File base;
  private int size;
  private int current;
  private int fg;
  // private String contents;
  private File[] files;

}
