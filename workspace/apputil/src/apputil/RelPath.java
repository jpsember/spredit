package apputil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

import static com.js.basic.Tools.*;

/**
 * Standard format for paths: enforce consistent separator char ('/'), and
 * support relative to project base directory (via a '>' prefix)
 */
public class RelPath {
  private static final boolean db = false;
  
  private static final char OUR_SEPARATOR = '/';
  private static final char OUR_PROJECT_CHAR = '>';

  /**
   * Constructor
   * @param projectDir project directory
   * @param file file that may or may not lie within project directory
   */
  public RelPath(File projectDir, File file) {
    if (db)
      pr("RelPath constructor, projectDir=[" + projectDir + "] file=[" + file
          + "]");

    ArrayList<String> dirComp = extractComponents(projectDir);
    ArrayList<String> fileComp = extractComponents(file);
    int i = 0;
    while (i < dirComp.size() && i < fileComp.size()) {
      if (!dirComp.get(i).equals(fileComp.get(i)))
        break;
      i++;
    }
    abstractNames = fileComp;
    if (i == dirComp.size())
      projectDirElements = i;

    if (db)
      pr(" projectDirElements=" + projectDirElements + "\n toString()=" + this);

  }
  /**
   * Get string representation of path for display to user.
   * Returns the 'toString()' value, after removing the '>' prefix if it exists.
   * @return string
   */
  public String display() {
    String s = toString();
    if (withinProjectTree())
      s = s.substring(1);
    return s;
  }

  /**
   * Constructor
   * @param baseDir base directory for project, or null
   * @param abstractPath abstract representation of a path (i.e., the output of RelPath.toString());
   *   if it is relative (starts with '>'), baseDir must not be null;
   *   if it is not relative, and baseDir is defined, determines
   *   if it lies within the project tree and modifies it accordingly
   */
  public RelPath(File baseDir, String abstractPath) {


    if (db)
      pr("RelPath constructor, baseDir=[" + baseDir + "] abstPath=["
          + abstractPath + "]");

    boolean isRel = (abstractPath.length() > 0)
        && (abstractPath.charAt(0) == OUR_PROJECT_CHAR);

    if (db)
      pr(" isRel=" + isRel);

    if (isRel) {
      if (baseDir == null)
        throw new IllegalArgumentException("baseDir is null for relative path "
            + abstractPath);

      abstractNames = extractComponents(baseDir);
      projectDirElements = abstractNames.size();
      extractComponents(abstractPath.substring(1), abstractNames);
      this.abstractFilePath = abstractPath;
    } else {
      abstractNames = new ArrayList();
      extractComponents(abstractPath, abstractNames);

      if (baseDir != null) {
        // see if file lies within project tree

        if (db)
          pr(" seeing if file lies within project tree");

        ArrayList dirComp = extractComponents(baseDir);
        if (db)
          pr(" extracted components from baseDir=\n" + dirComp);

        int i = 0;
        while (i < dirComp.size() && i < abstractNames.size()) {
          if (!dirComp.get(i).equals(abstractNames.get(i)))
            break;
          i++;
        }

        // abstractPath can be lazy constructed!

        if (i == dirComp.size()) {
          projectDirElements = i;

        } else
          abstractFilePath = abstractPath;
      }
    }
  }

  /**
   * Extract names of directories or files from File
   * @param f file
   * @return array of strings
   */
  private static ArrayList<String> extractComponents(File f) {
    if (db)
      pr("RelPath extractComponents for file:[" + f + "]");

    ArrayList<String> a = new ArrayList();
    while (f != null) {
      String nm = f.getName();
      if (db)
        pr(" f=" + f + "\n getName=[" + nm + "]");

      if (nm.length() == 0) {
        nm = f.toString();
        while (true) {
          int i = nm.length() - 1;
          if (i < 0)
            break;
          if (nm.charAt(i) != File.separatorChar)
            break;
          nm = nm.substring(0, i);
        }
        f = null;
      } else {
        f = f.getParentFile();
      }
      a.add(nm);
    }
    int revTotal = (a.size() / 2) - 1;

    for (int j = revTotal; j >= 0; j--)
      Collections.swap(a, j, a.size() - 1 - j);

    return a;
  }

  /**
   * Extract names of directories or files from an abstract path
   * @param absPath abstract path
   * @param dest names of directories/files appended to this array of strings
   */
  private static void extractComponents(String absPath, ArrayList<String> dest) {
    if (db)
      pr("extractComponents from [" + absPath + "]");

    int i = 0;
    while (i < absPath.length()) {
      int j = absPath.indexOf('/', i);
      if (db)
        pr(" i=" + i + " j=" + j);

      if (j < 0)
        j = absPath.length();
      //      if (j - i == 1)
      //        throw new IllegalArgumentException("bad abstract path: [" + absPath
      //            + "]");

      String w = absPath.substring(i, j);
      if (db)
        pr(" adding component i=" + i + " j=" + j + " [" + w + "]");

      dest.add(w);
      i = j + 1;
    }
  }

  /**
   * Get File corresponding to this relative path
   * @return
   */
  public File file() {

    if (systemFile == null) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < abstractNames.size(); i++) {
        if (i != 0)
          sb.append(File.separatorChar);
        sb.append(abstractNames.get(i));
      }
      systemFile = new File(sb.toString());
      if (db)
        pr("RelPath.file()\n  sb=" + sb + "\nsysf=" + systemFile);

    }
    return systemFile;
  }

  public String toString() {
    if (db)
      pr("RelPath.toString");

    if (abstractFilePath == null) {
      if (db)
        pr(" building abstractFilePath");

      StringBuilder sb = new StringBuilder();
      if (withinProjectTree())
        sb.append(OUR_PROJECT_CHAR);
      for (int j = projectDirElements; j < abstractNames.size(); j++) {
        if (j > projectDirElements)
          sb.append(OUR_SEPARATOR);
        sb.append(abstractNames.get(j));
      }
      this.abstractFilePath = sb.toString();
    }
    if (db)
      pr(" returning [" + abstractFilePath + "]");

    return abstractFilePath;
  }

  public static void main(String[] args) {
    if (true) {

      String[] fs = { "/sys/users/jpsember/foo/bar.txt",
          "/sys/users2/jpsember/foo/bar.txt", "/sys/users2/jpsember/",
          "/sys/users2/jpsember", "/sys/users/jpsember/blargh/bosco.txt",
          ">blargh/bosco.txt", };

      File projDir = new File("/sys/users/jpsember");

      for (int k = 0; k < fs.length; k++) {
        String file1 = fs[k];

        RelPath r = new RelPath(projDir, file1);

        pr("proj= " + projDir + "\nfile= " + file1 + "\nrelp= " + r
            + " (in tree=" + r.withinProjectTree() + ")\n" + "file= "
            + r.file());
        pr("\n");

      }
      return;
    }
  }

  public boolean withinProjectTree() {
    return projectDirElements > 0;
  }

  // abstract representation of relative path:
  //  if begins with '>', it's relative to the project directory
  //  names of directories/file separated by '/'
  private String abstractFilePath;

  // names of directories/file comprising file's path
  private ArrayList<String> abstractNames;

  // if 0, file is not within project tree; otherwise, number of path elements comprising project tree
  private int projectDirElements;

  // lazy-initialized File representing this file
  private File systemFile;
}
