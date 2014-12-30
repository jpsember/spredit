package com.js.basic;

import static com.js.basic.Tools.*;

import java.io.*;

public class Files {

  /**
   * Get an input stream to a data file, which is stored in the class folder (or
   * one of its subfolders)
   * 
   * @param path
   *          String : path to file
   * @return BufferedInputStream
   * @throws IOException
   * @deprecated use File instead of String for path
   */
  public static BufferedInputStream openResource(Class c, String path)
      throws IOException {

    BufferedInputStream out = null;
    if (c == null) {
      out = new BufferedInputStream(new FileInputStream(path));
    } else {
      InputStream is = c.getResourceAsStream(path);
      if (is == null) {
        throw new FileNotFoundException("openResource failed: " + path);
      }
      out = new BufferedInputStream(is);
    }
    return out;
  }

  public static String readTextFile(InputStream stream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    return readTextFile(reader);
  }

  private static String readTextFile(BufferedReader input) throws IOException {
    StringBuilder sb = new StringBuilder();
    try {
      String line = null;
      /*
       * Readline strips newlines, and returns null only for the end of the
       * stream.
       */
      while ((line = input.readLine()) != null) {
        sb.append(line);
        sb.append(getLineSeparator());
      }
    } finally {
      input.close();
    }
    return sb.toString();
  }

  /**
   * Read a file into a string
   * 
   * @param path
   *          file to read
   * @return String
   */
  public static String readTextFile(File file) throws IOException {
    BufferedReader input = new BufferedReader(new FileReader(file));
    return readTextFile(input);
  }

  public static byte[] readBinaryFile(File file) throws IOException {
    RandomAccessFile f = new RandomAccessFile(file, "r");
    byte[] b = new byte[(int) f.length()];
    int bytesRead = f.read(b);
    if (bytesRead != b.length)
      throw new IOException("failed to read all bytes from " + file);
    return b;
  }

  public static byte[] readBytes(InputStream stream) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = stream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }
    buffer.flush();

    return buffer.toByteArray();
  }

  public static void writeBinaryFile(File file, byte[] contents)
      throws IOException {
    FileOutputStream f = new FileOutputStream(file);
    f.write(contents);
    f.close();
  }

  public static void deleteDirectory(File path) {
    if (!path.isDirectory())
      throw new IllegalArgumentException("not a directory: " + path);

    File[] files = path.listFiles();

    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        deleteDirectory(files[i]);
      } else {
        files[i].delete();
      }
    }
    path.delete();
  }

  public static void writeTextFile(File file, String content,
      boolean onlyIfChanged) throws IOException {
    if (onlyIfChanged) {
      if (file.isFile()) {
        String currentContents = readTextFile(file);
        if (currentContents.equals(content))
          return;
      }
    }
    BufferedWriter w = new BufferedWriter(new FileWriter(file));
    w.write(content);
    w.close();
  }

  public static void writeTextFile(File file, String content)
      throws IOException {
    writeTextFile(file, content, false);
  }

  public static void copy(File src, File dst) throws IOException {
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);

    // Transfer bytes from in to out
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }

  /**
   * Get a buffered InputStream for reading from a file.
   * 
   * @param path
   *          path of file
   * @return OutputStream
   * @throws IOException
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
   * @deprecated use File instead of String for path
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
   * @deprecated use File instead of String for path
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

      txOld = readTextFile(path.getPath());
    }

    if (txOld == null || !content.equals(txOld)) {
      changed = true;
      writeTextFile(path, content);
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

  /**
   * @deprecated use File instead of String for path
   */
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
   * @deprecated use File instead of String for path
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

  /**
   * @deprecated use Apache Commons IO
   */
  public static boolean hasExtension(String path) {
    return (getExtension(path).length() != 0);
  }

  /**
   * @deprecated use Apache Commons IO
   */
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
   * @deprecated use Apache Commons IO
   */
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

  /**
   * @param path
   * @return
   * @deprecated use Strings to manipulate paths
   */
  public static String relativeToUserHome(File path) {
    File userDir = Files.homeDirectory();

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
   * @return String containing extension, empty if it has none (or is a
   *         directory)
   * @deprecated use Apache Commons IO
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

  /**
   * @deprecated use Apache Commons IO
   */
  public static boolean hasExtension(File file) {
    return getExtension(file).length() > 0;
  }

  /**
   * @deprecated use Apache Commons IO
   */
  public static String getExtension(String path) {
    return getExtension(new File(path));
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

  private static String getLineSeparator() {
    if (sLineSeparatorString == null) {
      sLineSeparatorString = System.getProperty("line.separator");
    }
    return sLineSeparatorString;
  }

  private static File sHomeDir;
  private static String sLineSeparatorString;

}
