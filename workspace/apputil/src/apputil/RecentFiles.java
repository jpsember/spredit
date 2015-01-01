package apputil;

import java.io.*;
import java.util.ArrayList;

import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.event.*;
import static com.js.basic.Tools.*;


/**
 * Maintains a list of recent files to display in an application menu
 */
public class RecentFiles {

  private static final int MAX = 8;

  /**
   * Constructor
   * 
   * @param projectBase
   *          base of project tree, or null if none (absolute paths)
   */
  public RecentFiles(File projectBase) {
    mProjectBase = projectBase;
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
    File c = getCurrentFile();
    mFileActive = false;
    if (file == null)
      return;

    file = fileRelativeToDirectory(file, mProjectBase);

    int j = mFileList.indexOf(file);
    if (j >= 0) {
      mFileList.remove(j);
    }
    mFileList.add(0, file);
    mFileActive = true;
    while (mFileList.size() > MAX)
      mFileList.remove(mFileList.size() - 1);

    if (!file.equals(c)) {
      if (mComboBox != null) {
        mRebuildingBox = true;
        rebuildComboBox();
        mRebuildingBox = false;
      }
    }
  }

  /**
   * Get number of files in list
   * 
   * @return number of files
   */
  int size() {
    return mFileList.size();
  }

  /**
   * Get nth most recently used file
   */
  File get(int n) {
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
   * Get root of project tree, or null if none specified
   * 
   * @return directory
   */
  public File getProjectBase() {
    return mProjectBase;
  }

  /**
   * Encode recent files object to JSONObject
   */
  public JSONObject encode() throws JSONException {
    JSONObject map = new JSONObject();
    map.put("active", mFileActive);
    JSONArray a = new JSONArray();
    for (int i = 0; i < size(); i++) {
      File f = get(i);
      // RelPath rp = new RelPath(mProjectBase, f);
      a.put(f.getPath());
      // a.put(rp.toString());
    }
    map.put("list", a);
    return map;
  }

  public void decode(String mapAsJSONString) throws JSONException {
    JSONObject map = new JSONObject(mapAsJSONString);
    decode(map);
  }

  /**
   * Decode object from JSON, if map exists
   */
  public void decode(JSONObject map) throws JSONException {
    clear();
    if (map == null)
      return;
    mFileActive = map.getBoolean("active");
    JSONArray a = map.getJSONArray("list");
    int c = 0;
    while (c < a.length()) {
      File f = new File(a.getString(c++));
      // RelPath rp = new RelPath(mProjectBase, a.getString(c++));
      pr("adding recent file " + f);
      mFileList.add(f);
    }
  }

  private class ComboBoxItem {
    public ComboBoxItem(File f) {
      mFile = f;
      // this.p = new RelPath(mProjectBase, f);
    }

    public String toString() {
      return mFile.getPath();
      // String s = p.display(); // toString();
      // if (p.withinProjectTree()) //s.startsWith(">"))
      // s = s.substring(1);
      // return s;
    }

    private File mFile;
    // private RelPath p;
  }

  /**
   * Connect this object to a JComboBox
   * 
   * @param mComboBox
   */
  public void setComboBox(JComboBox cbx) {
    this.mComboBox = cbx;

    mComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ComboBoxItem itm = (ComboBoxItem) mComboBox.getSelectedItem();
        // RelPath rp = (RelPath) cb.getSelectedItem();
        if (!mRebuildingBox) {
          if (itm != null) {
            setCurrentFile(itm.mFile); // itm.p.file());
          }
        }
      }
    });
    rebuildComboBox();
  }

  public File fileRelativeToDirectory(File file, File directory) {
    if (directory == null)
      return file.getAbsoluteFile();

    String dirPath = directory.getAbsolutePath();
    String filePath = file.getAbsolutePath();
    if (!filePath.startsWith(dirPath))
      return file.getAbsoluteFile();

    String suffix = filePath.substring(dirPath.length());
    if (suffix.startsWith(File.separator)) {
      suffix = suffix.substring(File.separator.length());
    }
    return new File(suffix);
  }

  public String displayRelativeToProjectBase(File file) {
    return fileRelativeToDirectory(file, mProjectBase).getPath();
  }

  //
  // String filePath = file.getPath();
  // if (mProjectBase == null)
  // return filePath;
  // String projectPath = mProjectBase.getPath();
  // if (!filePath.startsWith(projectPath))
  // return filePath;
  // String suffix = filePath.substring(projectPath.length());
  // if (suffix.startsWith(File.separator)) {
  // suffix = suffix.substring(File.separator.length());
  // }
  // return suffix;
  // }

  private void rebuildComboBox() {
    mComboBox.removeAllItems();
    for (int i = 0; i < size(); i++) {
      mComboBox.addItem(new ComboBoxItem(get(i)));
    }
  }

  private JComboBox mComboBox;
  private File mProjectBase;
  private ArrayList<File> mFileList = new ArrayList();
  private boolean mRebuildingBox;
  private boolean mFileActive;

}
