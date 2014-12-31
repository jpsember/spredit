package com.js.basic;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class Files {

  public static void writeStringToFileIfChanged(File file, String content)
      throws IOException {
    if (file.isFile()) {
      String currentContents = FileUtils.readFileToString(file);
      if (currentContents.equals(content))
        return;
    }
    FileUtils.writeStringToFile(file, content);
  }

  /**
   * Get an input stream to a resource, which is stored in the class folder (or
   * one of its subfolders)
   * 
   * @param resourceName
   *          name of resource
   * @return BufferedInputStream
   * @throws IOException
   */
  public static BufferedInputStream openResource(Class c, String resourceName)
      throws IOException {
    InputStream is = c.getResourceAsStream(resourceName);
    if (is == null) {
      throw new FileNotFoundException("openResource failed: " + resourceName);
    }
    return new BufferedInputStream(is);
  }

  /**
   * @deprecated use Apache Commons IO
   */
  public static String addExtension(String path, String ext) {
    if (!hasExtension(new File(path))) {
      path = changeExtension(path, ext);
    }
    return path;
  }

  /**
   * Change extension of a file.
   * 
   * @param name
   *          current filename
   * @param ext
   *          new extension, "" for none
   * @return String representing new filename
   * @deprecated use Apache Commons IO
   */
  public static String changeExtension(String name, String ext) {
    ext = extString(ext);
    String out = name;
    String currExt = extString(name);
    if (!currExt.equalsIgnoreCase(ext)) {
      out = removeExt(name);
      if (ext.length() > 0) {
        out = out + "." + ext;
      }
    }
    return out;
  }

  /**
   * Replace the extension of a file
   * 
   * @param f
   *          existing File
   * @param ext
   *          new extension
   * @return File with replaced extension
   * @deprecated use Apache Commons IO
   */
  public static File changeExtension(File f, String ext) {

    // get the name from the file; we will change its extension.
    String name = f.getName();
    if (name.length() == 0) {
      throw new RuntimeException("Set extension of empty name: " + f);
    }

    String parent = f.getParent();
    return new File(parent, changeExtension(name, ext));
  }

  /**
   * Convert string to an extension; add '.' if necessary; if '.' already
   * exists, remove everything to left of it
   * 
   * @deprecated use Apache Commons IO
   */
  private static String extString(String s) {
    String out = null;
    int pos = s.lastIndexOf('.');
    out = s.substring(pos + 1);
    return out;
  }

  /**
   * Get extension of a file
   * 
   * @return String containing extension, empty if it has none
   */
  public static String getExtension(File file) {
    return FilenameUtils.getExtension(file.getPath());
  }

  public static boolean hasExtension(File file) {
    return !FilenameUtils.getExtension(file.getPath()).isEmpty();
  }

  /**
   * Remove extension, if any, from path
   * 
   * @deprecated use Apache Commons IO
   */
  public static String removeExt(String path) {
    int extPos = path.lastIndexOf('.');
    if (extPos >= 0) {
      return path.substring(0, extPos);
    }
    return path;
  }

}
