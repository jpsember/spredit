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
  public static BufferedInputStream openResource(Class theClass,
      String resourceName) throws IOException {
    InputStream is = theClass.getResourceAsStream(resourceName);
    if (is == null) {
      throw new FileNotFoundException("openResource failed: " + resourceName);
    }
    return new BufferedInputStream(is);
  }

  /**
   * Set extension of file (replacing any existing one)
   * 
   * @param extension
   *          new extension; if empty string, just removes existing extension
   */
  public static File setExtension(File file, String extension) {
    String filePath = file.getPath();
    filePath = FilenameUtils.removeExtension(filePath);
    if (!extension.isEmpty())
      filePath += FilenameUtils.EXTENSION_SEPARATOR_STR + extension;
    return new File(filePath);
  }

  /**
   * Get extension of a file
   * 
   * @return String containing extension, empty if it has none
   */
  public static String getExtension(File file) {
    return FilenameUtils.getExtension(file.getPath());
  }

  /**
   * Determine if file has an extension
   */
  public static boolean hasExtension(File file) {
    return !FilenameUtils.getExtension(file.getPath()).isEmpty();
  }

  /**
   * Remove extension, if any, from path
   */
  public static File removeExtension(File file) {
    return setExtension(file, "");
  }

}
