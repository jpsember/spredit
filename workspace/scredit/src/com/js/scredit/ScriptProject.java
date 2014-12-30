package com.js.scredit;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.JSONTools;
import com.js.basic.Streams;

import apputil.*;
import tex.*;
import static com.js.basic.Tools.*;

public class ScriptProject {

  public static final String PROJ_EXT = "scp";
  public static MyFileFilter FILES_ONLY = new MyFileFilter(
      "Script project files", PROJ_EXT, false, null);
  public static MyFileFilter FILES_AND_DIRS = new MyFileFilter(
      "Script project files", PROJ_EXT, true, null);

  // /**
  // * @deprecated
  // * @param f
  // * @return
  // */
  // public static File findProject(File f) {
  // final boolean db = false;
  // File ret = null;
  // if (db)
  // pr("base null, looking at last project path " + f);
  //
  // if (f == null)
  // f = new File(Streams.getUserDir());
  //
  // while (true) {
  // if (db)
  // pr("looking in ancestor of current directory: " + f);
  // if (f == null || !f.isDirectory())
  // break;
  //
  // File[] f3 = FILES_ONLY.list(f); //)f.listFiles(FILES_ONLY);
  // if (f3.length != 0) {
  // ret = f3[0];
  // if (db)
  // pr("found in ancestor");
  // break;
  // }
  // f = f.getParentFile();
  // }
  // return ret;
  // }

  public ScriptProject(File f) throws IOException {

    final boolean db = false;

    if (db)
      pr("ScriptProject constructor: " + f);

    this.projectDir = f.getParentFile();
    this.projectFile = f;
    this.prefix = projectFile.getParentFile().getAbsolutePath()
        + File.separator;
    this.recentScripts = new RecentFiles(projectDir);
    this.recentAtlases = new RecentFiles(projectDir);
    recentScriptSets = new RecentFiles(projectDir);
    if (projectFile.exists()) {
      try {
        read();
      } catch (FileNotFoundException e) {
        throw new IOException(e);
      }
    }

    flush();
  }

  private static final String DEF_RECENTSCRIPTS = "RECENTSCRIPTS";
  private static final String DEF_RECENTATLASES = "RECENTATLAS";
  private static final String DEF_RECENTSETS = "RECENTSCRIPTSETS";

  public void flush() throws IOException {
    try {
      JSONObject projectMap = new JSONObject();
      storeDefaults(DEF_RECENTSCRIPTS, recentScripts.encode());
      storeDefaults(DEF_RECENTATLASES, recentAtlases.encode());
      storeDefaults(DEF_RECENTSETS, recentScriptSets.encode());

      for (String key : JSONTools.iterable(defaults.keySet())) {
        projectMap.put(key, defaults.get(key));
      }
      String content = projectMap.toString();
      Streams.writeIfChanged(projectFile, content);
    } catch (JSONException e) {
      die(e);
    }
  }

  private void read() throws IOException {
    try {
      String content;
      content = Streams.readTextFile(projectFile.getPath());
      JSONObject defaultsMap = new JSONObject(content);
      for (String key : JSONTools.keys(defaultsMap)) {
        defaults.put(key, defaultsMap.getString(key));
      }
      recentScripts.decode(getDefaults(DEF_RECENTSCRIPTS, null));
      recentAtlases.decode(getDefaults(DEF_RECENTATLASES, null));
      recentScriptSets.decode(getDefaults(DEF_RECENTSETS, null));
    } catch (JSONException e) {
      die(e);
    }
  }

  public File file() {
    return projectFile;
  }

  public File getPath(String key, File defaultValue) {
    String val = getDefaults(key, null);
    File rp = defaultValue;
    if (val != null) {
      unimp("labelToString no longer used here... maybe JSON?");
      // String s = AppTools.labelToString(val);
      rp = new RelPath(directory(), val).file();
    }
    return rp;
  }

  public String getDefaults(String key, String defaultValue) {
    String val = (String) defaults.get(key);
    if (val == null) {
      val = defaultValue;
    }
    return val;
  }

  public void storeDefaults(String key, File path) {
    Object value = null;
    if (path != null) {
      String s = new RelPath(directory(), path).toString();
      value = AppTools.stringToLabel(s);
    }
    storeDefaults(key, value);
  }

  public void storeDefaults(String key, Object value) {
    if (value == null)
      defaults.remove(key);
    else
      defaults.put(key, value.toString());
  }

  public boolean isDescendant(File f) {
    return f.getAbsolutePath().startsWith(prefix);
  }

  public File directory() {
    return projectDir;
  }

  private String prefix;
  // Use TreeMap so keys have natural sort order
  private Map<String, String> defaults = new TreeMap();
  private File projectFile;
  private File projectDir;

  /**
   * Replace a file with the project root directory, if it is null
   * 
   * @param f
   *          file
   * @return non-null f
   */
  public File replaceIfMissing(File f) {
    final boolean db = false;

    if (db)
      pr("ScriptProject replaceIfMissing file=" + f);

    if (f == null) {
      f = new File(projectDir, File.separator);
      if (db)
        pr(" is null or doesn't exist, replacing with project dir=" + f);
    }
    return f;
  }

  public void setLastScriptPath(File f) {
    recentScripts.setCurrentFile(f);
  }

  public void setLastSetPath(File f) {
    recentScriptSets.setCurrentFile(f);
  }

  private RecentFiles recentScripts, recentAtlases, recentScriptSets;

  public RecentFiles recentAtlases() {
    return recentAtlases;
  }

  public RecentFiles recentScripts() {
    return recentScripts;
  }

  public RecentFiles recentScriptSets() {
    return recentScriptSets;
  }

  /**
   * @param f
   * @return
   * @throws IOException
   */
  public Atlas getAtlas(File f) throws IOException {
    final boolean db = false;
    Atlas a = (Atlas) atlasMap.get(f);
    if (a == null) {
      a = new Atlas(f);

      if (db)
        pr("attempting load atlas image...");

      BufferedImage img = a.image();
      if (db)
        pr("img = " + img);

      if (img == null)
        throw new IOException("no atlas available");
      storeAtlas(f, a);
    }
    return a;
  }

  private void storeAtlas(File f, Atlas at) {
    atlasMap.put(f, at);
  }

  private Map atlasMap = new HashMap();

}
