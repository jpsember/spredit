package com.js.basic;

import java.io.*;
import static com.js.basic.Tools.*;

/**
 * Utility class for streams.
 */
public class Streams {

  /**
   * Get a buffered InputStream for reading from a file.
   * 
   * @param path
   *          path of file
   * @return OutputStream
   * @throws IOException
   * @deprecated use File version
   */
  public static InputStream inputStream(String path) throws IOException {
    ASSERT(path != null);
    return new BufferedInputStream(new FileInputStream(path));
  }

  /**
   * Get a buffered Reader for a file
   * 
   * @param path
   *          String path of file
   * @return Reader
   */
  private static Reader reader(String path) throws IOException {
    return new InputStreamReader(inputStream(path));
  }

  /**
   * Get a buffered OutputStream for writing to a file
   * 
   * @param path
   *          path of file
   * @return OutputStream
   * @throws IOException
   * @deprecated use File version
   */
  public static OutputStream outputStream(String path) throws IOException {
    ASSERT(path != null);
    OutputStream os = new FileOutputStream(path);
    return new BufferedOutputStream(os);
  }

  /**
   * Read a string from a reader, then close the reader
   * 
   * @param r
   *          reader
   * @return string read
   * @throws IOException
   */
  public static String readStringFrom(Reader r) throws IOException {
    StringBuilder sb = new StringBuilder();
    char[] buff = new char[100];

    while (true) {
      int cnt = r.read(buff);
      if (cnt < 0)
        break;
      sb.append(buff, 0, cnt);
    }
    r.close();
    return sb.toString();
  }

  /**
   * @return home directory (from "user.home" property)
   */
  public static File homeDirectory() {
    if (sHomeDir == null) {
      setHomeDirectory(new File(System.getProperty("user.home")));
    }
    return sHomeDir;
  }

  /**
   * Write text file if it has changed, or does not exist
   * 
   * @param filename
   *          name of file, relative to root directory
   * @param content
   *          new contents of file
   * @throws IOException
   */
  public static boolean writeIfChanged(File path, String content)
      throws IOException {
    final boolean db = false;

    if (db)
      pr("Streams.writeIfChanged path:" + path);

    boolean changed = false;

    String txOld = null;

    if (path.exists()) {
      if (db)
        pr(" attempting to read old from: " + path.getPath());

      txOld = Streams.readTextFile(path.getPath());
    }

    if (txOld == null || !content.equals(txOld)) {
      changed = true;
      Streams.writeTextFile(path, content);
    }
    return changed;
  }

  /**
   * Define the home directory for the server. We use this as the base for files
   * that may not be visible to the client, for example, backup files, example
   * photos, calendar scripts.
   * 
   * @param dir
   *          : home directory; if null, reads it from user.home system property
   */
  public static void setHomeDirectory(File dir) {
    sHomeDir = dir;
    // in case it's null, read it back so it is set to user.home
    homeDirectory();
  }

  @Deprecated
  // use File version
  public static byte[] readBinaryFile(String path) throws IOException {
    FileInputStream r = new FileInputStream(path);
    ByteArrayOutputStream w = new ByteArrayOutputStream();

    byte[] b = new byte[2000];
    while (true) {
      int len = r.read(b);
      if (len < 0)
        break;
      w.write(b, 0, len);
    }
    b = w.toByteArray();
    r.close();
    return b;
  }

  /**
   * Read a file into a string
   * 
   * @param file
   *          File to read
   * @return String
   * @deprecated use File version
   */
  public static String readTextFile(String path) throws IOException {
    StringBuilder sb = new StringBuilder();
    Reader r = reader(path);
    while (true) {
      int c = r.read();
      if (c < 0) {
        break;
      }

      sb.append((char) c);
    }
    r.close();
    return sb.toString();
  }

  public static void writeTextFile(File file, String content)
      throws IOException {
    BufferedWriter w = new BufferedWriter(new FileWriter(file));
    w.write(content);
    w.close();
  }

  /**
   * Read string from reader, then close it
   * 
   * @param s
   *          reader
   * @return string read
   * @throws IOException
   */
  public static String readTextFile(Reader s) throws IOException {
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = s.read();
      if (c < 0)
        break;
      sb.append((char) c);
    }
    s.close();
    return sb.toString();
  }

  @Deprecated
  // Use File version
  public static boolean hasExtension(String path) {
    return (getExtension(path).length() != 0);
  }

  @Deprecated
  // Use File version
  public static String addExtension(String path, String ext) {
    if (!hasExtension(path)) {
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
   * @deprecated // Use File version
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

  @Deprecated
  // Use File version
  public static String getParent(String f) {
    int i = f.lastIndexOf('/');
    if (i < 0) {
      i = 0;
    }
    return f.substring(0, i);
  }

  /**
   * Get the user directory
   * 
   * @return path of user directory, or empty string if running as an applet
   */
  public static String getUserDir() {
    String h = System.getProperty("user.dir");
    return h;
  }

  public static String relativeToUserHome(File path) {
    File userDir = Streams.homeDirectory();

    String sHome = userDir.toString();
    String sPath = path.toString();

    String sUser = getUserDir();
    if (!sUser.endsWith("/"))
      sUser = sUser + "/";

    if (sPath.startsWith(sUser)) {
      sPath = sPath.substring(sUser.length());
    } else if (sPath.startsWith(sHome)) {
      sPath = "~" + sPath.substring(sHome.length());
    }
    return sPath;
  }

  /**
   * Convert string to an extension; add '.' if necessary; if '.' already
   * exists, remove everything to left of it
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
   * @param file
   *          : File
   * @return String containing extension, empty if it has none (or is a
   *         directory)
   */
  public static String getExtension(File file) {
    String ext = "";
    String f = file.getName();
    int extPos = f.lastIndexOf('.');
    if (extPos >= 0) {
      ext = f.substring(extPos + 1);
    }
    return ext;
  }

  public static boolean hasExtension(File file) {
    return getExtension(file).length() > 0;
  }

  // Use File versions
  @Deprecated
  public static String getExtension(String path) {
    return getExtension(new File(path));
  }

  /**
   * Remove extension, if any, from path
   * 
   * @deprecated
   */
  public static String removeExt(String path) {
    int extPos = path.lastIndexOf('.');
    if (extPos >= 0) {
      return path.substring(0, extPos);
    }
    return path;
  }

  private static File sHomeDir;
}
