package apputil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Files;
import com.js.editor.Enableable;

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
    if (!mFileActive)
      return null;
    File file = mFileList.get(0);
    if (hasRootDirectory())
      file = getAbsoluteFile(file);
    return file;
  }

  /**
   * Get current file, if one exists, relative to root directory (or absolute,
   * if no root directory defined)
   */
  private File getCurrentFileRelative() {
    if (!mFileActive)
      return null;
    File file = mFileList.get(0);
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

    for (Listener listener : mListeners) {
      listener.mostRecentFileChanged(this);
    }
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

  /**
   * Get list of files, optionally omitting the current file (if there is one)
   */
  public List<File> getList(boolean omitCurrentFile) {
    int firstItem = (omitCurrentFile && getCurrentFile() != null) ? 1 : 0;
    return mFileList.subList(firstItem, mFileList.size());
  }

  private void clear() {
    mFileList.clear();
    mFileActive = false;
  }

  /**
   * Get the most recently used file, or null if the list is empty
   */
  public File getMostRecentFile() {
    File file = null;
    if (!mFileList.isEmpty())
      file = mFileList.get(0);
    return file;
  }

  /**
   * Store within JSON map
   */
  public void put(JSONObject map, String key) throws JSONException {
    map.put(key, encode());
  }

  private JSONObject encode() throws JSONException {
    JSONObject map = new JSONObject();
    map.put("active", mFileActive);
    JSONArray a = new JSONArray();
    for (File f : mFileList) {
      a.put(f.getPath());
    }
    map.put("list", a);
    return map;
  }

  /**
   * Decode file entries from JSON map, if found; otherwise, clear
   */
  public void restore(JSONObject map, String key) throws JSONException {
    JSONObject recentFilesMap = map.optJSONObject(key);
    decode(recentFilesMap);
  }

  private void decode(JSONObject map) throws JSONException {
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

  /**
   * JMenu subclass for displaying RecentFiles sets
   */
  public static class Menu extends MyMenuBar.Menu implements MenuListener,
      ActionListener, RecentFiles.Listener, Enableable {

    public Menu(String title, RecentFiles recentFiles, ActionHandler evtHandler) {
      super(title);
      setEnableableDelegate(this);
      setRecentFiles(recentFiles);
      mItemHandler = evtHandler;
      addMenuListener(this);
    }

    public void setRecentFiles(RecentFiles recentFiles) {
      mRecentFiles = recentFiles;
      if (mRecentFiles == null)
        return;
      mRecentFiles.addListener(this);
      rebuild();
    }

    @Override
    public boolean shouldBeEnabled() {
      if (mRecentFiles == null)
        return false;
      return !mRecentFiles.getList(true).isEmpty();
    }

    @Override
    public void actionPerformed(ActionEvent arg) {
      MenuItem item = (MenuItem) arg.getSource();
      mRecentFiles.setCurrentFile(item.file());
      mItemHandler.go();
    }

    @Override
    public void menuCanceled(MenuEvent arg0) {
    }

    @Override
    public void menuDeselected(MenuEvent arg0) {
    }

    @Override
    public void menuSelected(MenuEvent arg0) {
      rebuild();
    }

    @Override
    public void mostRecentFileChanged(RecentFiles recentFiles) {
      rebuild();
    }

    private void rebuild() {
      removeAll();
      RecentFiles r = mRecentFiles;
      if (r == null)
        return;
      for (File f : r.getList(true)) {
        String s = f.getPath();
        JMenuItem item = new MenuItem(f, s);
        this.add(item);
        item.addActionListener(this);
      }
    }

    /**
     * JMenuItem subclass representing RecentFiles items
     */
    private static class MenuItem extends JMenuItem {
      public MenuItem(File file, String label) {
        super(label);
        this.mFile = file;
      }

      public File file() {
        return mFile;
      }

      private File mFile;
    }

    private ActionHandler mItemHandler;
    private RecentFiles mRecentFiles;
  }

}
