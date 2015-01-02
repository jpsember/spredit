package apputil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Files;

import static com.js.basic.Tools.*;

/**
 * Maintains a queue of recently-used files, e.g. to display in an application
 * menu (or combo box)
 */
public class RecentFiles {

  private static final int MAX = 8;

  public static interface Listener {
    public void mostRecentFileChanged(RecentFiles recentFiles);
  }

  /**
   * Constructor
   * 
   * @param projectDirectory
   *          base of project directory tree, or null
   */
  public RecentFiles(File projectDirectory) {
    if (projectDirectory != null) {
      mProjectDirectory = projectDirectory;
    }
  }

  public void addListener(Listener listener) {
    mListeners.add(listener);
  }

  public void removeListener(Listener listener) {
    mListeners.remove(listener);
  }

  /**
   * Get current file
   * 
   * @return current file, or null if not active
   */
  public File getCurrentFile() {
    return mFileActive ? get(0) : null;
  }

  /**
   * Specify current file; if not null, moves it to the front of the list (and
   * bumps one if the list is too full)
   */
  public void setCurrentFile(File file) {
    if (db)
      pr("RecentFiles.setCurrentFile " + file);
    mFileActive = false;
    if (file == null)
      return;
    File previousCurrentFile = getCurrentFile();
    file = Files.fileWithinDirectory(file, getProjectDirectory());
    if (db)
      pr(" setting to canonical form " + file);
    if (file.equals(previousCurrentFile))
      return;

    int j = mFileList.indexOf(file);
    if (j >= 0) {
      mFileList.remove(j);
    }
    mFileList.add(0, file);
    mFileActive = true;
    while (mFileList.size() > MAX)
      mFileList.remove(mFileList.size() - 1);

    if (db)
      pr("mFileList now " + d(mFileList));
    for (Listener listener : mListeners) {
      listener.mostRecentFileChanged(this);
    }

  }

  /**
   * Get number of files in list
   */
  public int size() {
    return mFileList.size();
  }

  /**
   * Get nth most recently used file
   */
  public File get(int n) {
    return mFileList.get(n);
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
   * Clear history
   */
  public void clear() {
    mFileList.clear();
    mFileActive = false;
  }

  /**
   * Get root of project tree, or null if none was given
   */
  public File getProjectDirectory() {
    return mProjectDirectory;
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
    decode(recentFilesMap);
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
      // RelPath rp = new RelPath(mProjectBase, a.getString(c++));
      if (db)
        pr("adding recent file " + f);
      mFileList.add(f);
    }
  }

  // private class ComboBoxItem {
  // public ComboBoxItem(File f) {
  // pr("building ComboBoxItem for file " + f);
  // mFile = f;
  // // this.p = new RelPath(mProjectBase, f);
  // }
  //
  // public String toString() {
  // pr("ComboBoxItem to string, returning " + mFile);
  // return mFile.getPath();
  // // String s = p.display(); // toString();
  // // if (p.withinProjectTree()) //s.startsWith(">"))
  // // s = s.substring(1);
  // // return s;
  // }
  //
  // private File mFile;
  // // private RelPath p;
  // }

  // /**
  // * Connect this object to a JComboBox
  // *
  // * @param mComboBox
  // */
  // public void setComboBox(JComboBox cbx) {
  // pr("RecentFiles setComboBox " + cbx);
  // mComboBox = cbx;
  //
  // mComboBox.addActionListener(new ActionListener() {
  // public void actionPerformed(ActionEvent e) {
  // ComboBoxItem itm = (ComboBoxItem) mComboBox.getSelectedItem();
  // // RelPath rp = (RelPath) cb.getSelectedItem();
  // if (!mRebuildingBox) {
  // if (itm != null) {
  // setCurrentFile(itm.mFile); // itm.p.file());
  // }
  // }
  // }
  // });
  // rebuildComboBox();
  // }
  //
  // private void rebuildComboBox() {
  // mComboBox.removeAllItems();
  // for (int i = 0; i < size(); i++) {
  // mComboBox.addItem(new ComboBoxItem(get(i)));
  // }
  // }
  // private JComboBox mComboBox;
  // private boolean mRebuildingBox;

  // The project directory as given (possibly null)
  private File mProjectDirectory;
  private ArrayList<File> mFileList = new ArrayList();
  private boolean mFileActive;
  private Set<Listener> mListeners = new HashSet();
}
