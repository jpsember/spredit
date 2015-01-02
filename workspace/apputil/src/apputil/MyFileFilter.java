package apputil;

import java.io.*;

import org.apache.commons.io.FilenameUtils;

import com.js.basic.Files;
import static com.js.basic.Tools.*;

public class MyFileFilter extends javax.swing.filechooser.FileFilter implements
    FileFilter, FilenameFilter {

  static {
    doNothing();
  }

  /**
   * Constructor
   * 
   * @param description
   *          description to return for getDescription()
   * @param extension
   *          if not null, only files with this extension are included
   */
  public MyFileFilter(String description, String extension) {
    mDescription = description;
    mExtension = extension;
  }

  @Override
  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }
    if (mExtension != null
        && !FilenameUtils.isExtension(f.getPath(), mExtension))
      return false;
    return true;
  }

  @Override
  public String getDescription() {
    return mDescription;
  }

  @Override
  public boolean accept(File dir, String name) {
    return accept(new File(dir, name));
  }

  public File fixExtension(File file) {
    if (mExtension != null) {
      file = Files.setExtension(file, mExtension);
    }
    return file;
  }

  private String mDescription;
  private String mExtension;
}
