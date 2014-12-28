package apputil;

import java.io.*;
import java.util.*;

import com.js.basic.Streams;

import static com.js.basic.Tools.*;

public class MyFileFilter extends javax.swing.filechooser.FileFilter implements
    FileFilter, FilenameFilter {

  public File fixExtension(File f) {
    if (extension != null) {
      f = Streams.changeExtension(f, extension);
    }
    return f;
  }

  public MyFileFilter(String description, String extension, boolean allowDirs,
      File rootDir) {
    this(description, null, extension, allowDirs, rootDir);

  }

  public MyFileFilter(String description, String prefix, String extension,
      boolean allowDirs, File rootDir) {
    this.description = description;
    this.prefix = prefix;
    if (extension != null) {
      this.extension = extension;
      this.perExtension = "." + extension;
    }
    this.allowDirs = allowDirs;
    this.rootDir = rootDir;
    if (rootDir != null)
      rootDirPrefix = rootDir.getAbsolutePath() + File.separator;
  }

  private String prefix;
  private String description;
  private String perExtension;
  private String extension;
  private boolean allowDirs;
  private File rootDir;
  private String rootDirPrefix;

  public boolean isRootDescendant(File f) {
    if (rootDir == null)
      return true;
    return f.getAbsolutePath().startsWith(rootDirPrefix);
  }

  @Override
  public boolean accept(File f) {
    final boolean db = false;

    if (db)
      pr("FileFilter accept " + f);

    boolean allow = false;
    do {
      if (!isRootDescendant(f)) {
        if (db)
          pr(" not a descendant of root: '" + rootDirPrefix + "'");
        break;
      }
      if (f.isDirectory()) {
        allow = allowDirs;
        if (db)
          pr(" is a directory");

        break;
      }
      String name = f.getName();
      if (prefix != null) {
        if (!name.startsWith(prefix))
          break;
      }
      if (!name.endsWith(perExtension))
        break;

      if (db)
        pr(" ends with '" + perExtension + "'");

      allow = true;

    } while (false);
    return allow;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean accept(File dir, String name) {
    return accept(new File(dir, name));
  }

  public File[] list(File dir) {
    if (!dir.isDirectory())
      throw new IllegalArgumentException();
   File[] fs = dir.listFiles((FilenameFilter) this);
   Arrays.sort(fs);
   return fs;
   
  }
}
