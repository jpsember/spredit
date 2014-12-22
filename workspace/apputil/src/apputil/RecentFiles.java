package apputil;

import java.io.*;
import javax.swing.*;
import scanning.*;
import base.*;
import java.awt.event.*;
import static com.js.basic.Tools.*;

/**
 * Maintains list of recent files to display in application menu
 */
public class RecentFiles {

  private static final int MAX = 8;

  /**
   * Constructor
   * @param projectBase base of project tree, or null if none (absolute paths)
   */
  public RecentFiles(File projectBase) {
    this.projectBase = projectBase;
  }

  /**
   * Get current file
   * @return current file, or null if not active
   */
  public File current() {
    return fileActive ? get(0) : null;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("================\n");
    sb.append("RecentFiles");
    sb.append(" projectBase=" + projectBase);
    sb.append(" fileActive=" + fileActive);
    sb.append('\n');
    for (int i = 0; i < size(); i++)
      sb.append(" " + get(i) + "\n");
    sb.append("================\n");
    return sb.toString();
  }

  private boolean fileActive;

  /**
   * Specify current file (if not null, adds or moves file to front of list)
   * @param f file
   * @return true if current file has changed
   */
  public boolean use(File f) {

    boolean changed = false;
    File c = current();
    fileActive = false;
    if (f != null) {
      
      int j = files.indexOf(f);
      if (j >= 0) {
        files.remove(j);
      }
      files.add(0, f);
      fileActive = true;
      while (files.size() > MAX)
        files.pop();

      if (!f.equals(c)) {
        changed = true;
        if (cb != null) {
          rebuildingBox = true;
          rebuildComboBox();
          rebuildingBox = false;
        }
      }

    } else
      if (c != null) changed = true;
    return changed;
  }

  /**
   * Get number of files in list
   * @return number of files
   */
  public int size() {
    return files.size();
  }

  /**
   * Get nth most recently used file
   * @param n (0=most recent)
   * @return nth last used file 
   */
  public File get(int n) {
    return (File) files.get(n);
  }

  /**
   * Clear history
   */
  public void clear() {
    files.clear();
    fileActive = false;
  }

  /**
   * Get root of project tree, or null if none specified
   * @return directory 
   */
  public File getProjectBase() {
    return projectBase;
  }

  /**
   * Get position of file within tree
   * @param file
   * @return position 0...size()-1, or -1 if not found
   */
  public int indexOf(File file) {
    return files.indexOf(file);
  }

  /**
   * Encode object to string
   * @return
   */
  public String encode() {
    final boolean db = false;
    
    if (db) 
      pr("RecentFiles, encode; projectBase="+projectBase);
          
    DefBuilder sb = new DefBuilder();
    sb.append(size());
    sb.append(fileActive);
    for (int i = 0; i < size(); i++) {
      File f = get(i);
      RelPath rp = new RelPath(projectBase,f);
      if (db) 
        pr("constructed RelPath for ["+f+"]:\n"+rp);
            
      sb.append(rp);
    }
    sb.addCr();
    return sb.toString();
  }

  /**
   * Decode object from string
   * @param s
   */
  public void decode(String s) {
    clear();
    if (s != null) {
      DefScanner sc = new DefScanner(s);
      int sz = sc.sInt();
      if (sc.peek(IBasic.BOOL))
        fileActive = sc.sBool();
      for (int i = 0; i < sz; i++) {
        File f = sc.sPath(projectBase);
        files.add(f);
        fileActive = true;
      }
    }
  }

  private class CBItem {
    public CBItem(File f) {
      this.p = new RelPath(projectBase, f);
    }
    public String toString() {
      String s = p.display(); //toString();
//      if (p.withinProjectTree()) //s.startsWith(">"))
//        s = s.substring(1);
      return s;
    }
    private RelPath p;
  }

  private JComboBox cb;
  private File projectBase;
  private DArray files = new DArray();
  private boolean rebuildingBox;

  /**
   * Connect this object to a JComboBox
   * @param cb
   */
  public void setComboBox(JComboBox cbx) {
    this.cb = cbx;

    cb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CBItem itm = (CBItem) cb.getSelectedItem();
        //        RelPath rp = (RelPath) cb.getSelectedItem();
        if (!rebuildingBox) {
          if (itm != null) {
            use(itm.p.file());
          }
        }
      }
    });
    rebuildComboBox();
  }
  private void rebuildComboBox() {
    cb.removeAllItems();
    for (int i = 0; i < size(); i++) {
      cb.addItem(new CBItem(get(i))); //RelPath(projectBase, get(i)));
    }
  }

}
