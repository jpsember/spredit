package apputil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Files;

import static com.js.basic.Tools.*;

/**
 * <pre>
 * 
 * Maintains a queue of recently-used files, e.g. to display in a menu
 * 
 * It has two objects:
 * 
 * 1) a queue of recently used files, from most recently- to least recently- used
 * 2) an optional root directory file
 * 
 * If a root directory file is included, then it will be an absolute file path,
 * and each of the recently used files will i) lie within the root directory,
 * and ii) be expressed as files relative to the root directory.  Otherwise,
 * each recently used file will be an absolute file.
 * 
 * A RecentFiles object also has an optional alias, which if defined, redirects
 * method calls to the alias object.  This allows a RecentFiles object to act as 
 * (dynamic) pointer to another.   *** TODO: consider not using alias, and instead
 * have a method in the listener to support redirecting to another object.
 * 
 * </pre>
 */
public class RecentFiles {

  public static interface Listener {
    public void mostRecentFileChanged(RecentFiles recentFiles);
  }

  public RecentFiles(File rootDirectory) {
    if (rootDirectory != null) {
      Files.verifyAbsolute(rootDirectory);
      mRootDirectory = rootDirectory;
    }
  }

  public void setAlias(RecentFiles alias) {
    mAlias = alias;
  }

  public RecentFiles getAlias() {
    if (mAlias != null)
      return mAlias;
    return this;
  }

  public void addListener(Listener listener) {
    mListeners.add(listener);
  }

  public void removeListener(Listener listener) {
    mListeners.remove(listener);
  }

  /**
   * Get current file, if one exists, as an absolute file
   */
  public File getCurrentFile() {
    RecentFiles a = getAlias();
    if (!a.mFileActive)
      return null;
    File file = a.get(0);
    if (a.hasRootDirectory())
      file = a.getAbsoluteFile(file);
    return file;
  }

  /**
   * Get current file, if one exists, relative to root directory (or absolute,
   * if no root directory defined)
   */
  private File getCurrentFileRelative() {
    RecentFiles a = getAlias();
    if (!a.mFileActive)
      return null;
    File file = a.get(0);
    return file;
  }

  /**
   * Specify current file; if not null, moves it to the front of the list (and
   * bumps one if the list is too full)
   * 
   * @param file
   *          current file (or null); if an absolute file, must lie within root
   *          directory (if a root directory was defined)
   */
  public void setCurrentFile(File file) {
    getAlias().setCurrentFileAux(file, this);
  }

  /**
   * Convert a file, if it's not absolute, to an absolute file by prepending
   * root directory (which must exist in this case)
   */
  private File getAbsoluteFile(File absOrRelativeFile) {
    if (!absOrRelativeFile.isAbsolute())
      return new File(mRootDirectory, absOrRelativeFile.getPath());
    return absOrRelativeFile;
  }

  private void setCurrentFileAux(File file, RecentFiles original) {
    if (db)
      pr("RecentFiles.setCurrentFile " + file);

    mFileActive = false;
    if (file == null)
      return;

    if (hasRootDirectory()) {
      if (file.isAbsolute()) {
        File relativeFile = Files.fileWithinDirectory(file, mRootDirectory);
        if (relativeFile.isAbsolute())
          throw new IllegalArgumentException("file '" + file
              + "' is not within root directory tree '" + mRootDirectory + "'");
        file = relativeFile;
      }
    } else {
      Files.verifyAbsolute(file);
    }

    if (file.equals(getCurrentFileRelative()))
      return;

    int j = mFileList.indexOf(file);
    if (j >= 0) {
      mFileList.remove(j);
    }
    mFileList.add(0, file);
    mFileActive = true;
    while (mFileList.size() > MAXIMUM_RECENT_FILES)
      mFileList.remove(mFileList.size() - 1);

    if (db)
      pr("mFileList now " + d(mFileList, true));
    pr("setCurrentFile to " + file + " for " + nameOf(this)
        + "; notifying listeners:\n"
        + d(mListeners, true));
    for (Listener listener : original.mListeners) {
      listener.mostRecentFileChanged(original);
    }
  }

  /**
   * Get list of files, optionally omitting the current file (if there is one)
   */
  public List<File> getList(boolean omitCurrentFile) {
    return getAlias().getListAux(omitCurrentFile);
  }

  private List<File> getListAux(boolean omitCurrentFile) {
    int firstItem = (omitCurrentFile && getCurrentFile() != null) ? 1 : 0;
    return mFileList.subList(firstItem, mFileList.size());
    // ArrayList<File> displayList = new ArrayList();
    // int cursor = getCurrentFile() == null ? 0 : 1;
    // while (cursor < mFileList.size()) {
    // displayList.add(mFileList.get(cursor));
    // cursor++;
    // }
    // return displayList;
  }

  /**
   * Get number of files in list
   */
  private int size() {
    return getAlias().mFileList.size();
  }

  /**
   * Get nth most recently used file
   */
  private File get(int n) {
    return getAlias().mFileList.get(n);
  }

  /**
   * Get the most recently used file, or null if the list is empty
   */
  public File getMostRecentFile() {
    File file = null;
    if (!getAlias().mFileList.isEmpty())
      file = getAlias().mFileList.get(0);
    return file;
  }

  /**
   * Clear history
   */
  public void clear() {
    RecentFiles a = getAlias();
    a.mFileList.clear();
    a.mFileActive = false;
  }

  /**
   * Get root directory, or null if none was given
   */
  public File getRootDirectory() {
    return getAlias().mRootDirectory;
  }

  /**
   * Store within JSON map
   */
  public void put(JSONObject map, String key) throws JSONException {
    map.put(key, getAlias().encode());
  }

  private JSONObject encode() throws JSONException {
    return getAlias().encodeAux();
  }

  private JSONObject encodeAux() throws JSONException {
    JSONObject map = new JSONObject();
    map.put("active", mFileActive);
    JSONArray a = new JSONArray();
    for (int i = 0; i < size(); i++) {
      File f = get(i);
      a.put(f.getPath());
    }
    map.put("list", a);
    if (db)
      pr("RecentFiles.encode produced:\n" + d(map));
    return map;
  }

  /**
   * Decode file entries from JSON map, if found; otherwise, clear
   */
  public void restore(JSONObject map, String key) throws JSONException {
    JSONObject recentFilesMap = map.optJSONObject(key);
    getAlias().decode(recentFilesMap);
  }

  private void decode(JSONObject map) throws JSONException {
    if (db)
      pr("RecentFiles decode:\n" + d(map));
    clear();
    if (map == null)
      return;
    mFileActive = map.getBoolean("active");
    JSONArray a = map.getJSONArray("list");
    int c = 0;
    while (c < a.length()) {
      File f = new File(a.getString(c++));
      if (f.isAbsolute() == hasRootDirectory()) {
        warning("ignoring absolute/relative file mismatch: " + f);
        continue;
      }
      if (db)
        pr("adding recent file " + f);
      mFileList.add(f);
    }
    if (mFileList.isEmpty())
      mFileActive = false;
  }

  private boolean hasRootDirectory() {
    return mRootDirectory != null;
  }

  private static final int MAXIMUM_RECENT_FILES = 8;

  private File mRootDirectory;
  private ArrayList<File> mFileList = new ArrayList();
  private boolean mFileActive;
  private Set<Listener> mListeners = new HashSet();
  private RecentFiles mAlias;
}
