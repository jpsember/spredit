package com.js.scredit;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Files;

import apputil.*;
import tex.*;
import static com.js.basic.Tools.*;

public class ScriptProject {

  public static final String PROJ_EXT = "scp";
  public static MyFileFilter FILES = new MyFileFilter("Script project files",
      PROJ_EXT);

  public ScriptProject(File f) throws IOException {
    mProjectDir = f.getParentFile();
    mProjectFile = f;
    mPrefix = mProjectFile.getParentFile().getAbsolutePath() + File.separator;
    mRecentScripts = new RecentFiles(mProjectDir);
    mRecentAtlases = new RecentFiles(mProjectDir);
    mRecentScriptSets = new RecentFiles(mProjectDir);
    if (mProjectFile.exists()) {
      try {
        read();
      } catch (JSONException e) {
        throw new IOException(e);
      } catch (FileNotFoundException e) {
        throw new IOException(e);
      }
    }
    flush();
  }

  private static final String KEY_RECENTSCRIPTS = "recentScripts";
  private static final String KEY_RECENTATLASES = "recentAtlases";
  private static final String KEY_RECENTSCRIPTSETS = "recentScriptSets";

  public void flush() throws IOException {
    try {
      getDefaults().put(KEY_RECENTSCRIPTS, mRecentScripts.encode());
      getDefaults().put(KEY_RECENTATLASES, mRecentAtlases.encode());
      getDefaults().put(KEY_RECENTSCRIPTSETS, mRecentScriptSets.encode());

      // JSON objects (maps) are unordered by definition, but if we can assume
      // they are at least deterministically ordered, then this 'write only if
      // changed' test is still useful.
      String content = getDefaults().toString(2);
      Files.writeStringToFileIfChanged(mProjectFile, content);
    } catch (JSONException e) {
      die(e);
    }
  }

  private void read() throws IOException, JSONException {
    String content = FileUtils.readFileToString(mProjectFile);
    mProjectDefaults = new JSONObject(content);
    mRecentScripts.decode(getDefaults().optJSONObject(KEY_RECENTSCRIPTS));
    mRecentAtlases.decode(getDefaults().optJSONObject(KEY_RECENTATLASES));
    mRecentScriptSets.decode(getDefaults().optJSONObject(KEY_RECENTSCRIPTSETS));
  }

  public File file() {
    return mProjectFile;
  }

  public JSONObject getDefaults() {
    return mProjectDefaults;
  }

  public boolean isDescendant(File f) {
    return f.getAbsolutePath().startsWith(mPrefix);
  }

  public File directory() {
    return mProjectDir;
  }

  /**
   * Replace a file with the project root directory, if it is null
   */
  public File replaceIfMissing(File f) {
    if (f == null) {
      f = new File(mProjectDir, File.separator);
    }
    return f;
  }

  public void setLastScriptPath(File f) {
    mRecentScripts.setCurrentFile(f);
  }

  public void setLastSetPath(File f) {
    mRecentScriptSets.setCurrentFile(f);
  }

  public RecentFiles recentAtlases() {
    return mRecentAtlases;
  }

  public RecentFiles recentScripts() {
    return mRecentScripts;
  }

  public RecentFiles recentScriptSets() {
    return mRecentScriptSets;
  }

  public Atlas getAtlas(File f) throws IOException {
    Atlas a = (Atlas) mAtlasMap.get(f);
    if (a == null) {
      a = new Atlas(f);
      BufferedImage img = a.image();
      if (img == null)
        throw new IOException("no atlas available");
      storeAtlas(f, a);
    }
    return a;
  }

  private void storeAtlas(File f, Atlas at) {
    mAtlasMap.put(f, at);
  }

  private Map mAtlasMap = new HashMap();
  private String mPrefix;
  private JSONObject mProjectDefaults = new JSONObject();
  private File mProjectFile;
  private File mProjectDir;
  private RecentFiles mRecentScripts, mRecentAtlases, mRecentScriptSets;

}
