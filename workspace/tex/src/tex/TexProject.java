package tex;

import java.io.*;
import java.util.*;
import apputil.*;
import base.*;
import streams.*;
import static com.js.basic.Tools.*;

public class TexProject {
  public static final String SRC_EXT = "txp";
  public static final String THUMB_DIR = "_thumbnails";

  /**
   * @param f
   * @return
   * @throws IOException
   */
  public static TexProject create(File f) throws IOException {
    if (f.exists())
      throw new IOException("File already exists");

    TexProject p = new TexProject(f, false);

    return p;
  }

  /**
   * Construct existing project
   * @param f
   * @throws IOException 
   * @throws FileNotFoundException 
   */
  public TexProject(File f) throws FileNotFoundException, IOException {
    this(f, true);
  }

  /**
   * Create new project 
   * @param baseDir location of base directory containing project file;
   *   if no project file found here, creates it
   * @throws IOException 
   */
  private TexProject(File f, boolean mustExist) throws IOException,
      FileNotFoundException {

    final boolean db = false;

    if (db)
      pr("TexProject constructor: " + f);

    this.baseDir = f.getParentFile();
    this.projectFile = f;
    this.name = Streams.removeExt(f.getName());

    if (db)
      pr("baseDir=" + baseDir + "\nname=" + name);

    if (projectFile.exists()) {
      read();

    } else {
      if (mustExist)
        throw new FileNotFoundException();
    }

    flush();
  }

  private void read() throws FileNotFoundException {
    DefScanner sc = new DefScanner(projectFile);
    while (!sc.done()) {
      String item = sc.nextDef();
      String str = sc.readLine();
      defaults.put(item, str);
    }
  }

  public String getDefaults(String key, String defaultValue) {
    String val = (String) defaults.get(key);
    if (val == null) {
      if (defaultValue == null)
        defaultValue = "";
      val = defaultValue;
    }
    return val;
  }
  public void storeDefaults(String key, Object value) {
    if (value == null)
      value = "";
    defaults.put(key, value.toString());
  }

  private static String[] imgExt = { "png", "jpg", };

  public boolean isTexture(File f) {
    if (f.isDirectory())
      return false;

    //    if (Atlas.DATA_FILES_ONLY.accept(f)) return false;

    String fileName = f.getName();
    // if name starts with "ATLAS_" prefix, ignore.
    //    if (!Atlas.ONEFILE && fileName.startsWith(Atlas.PREFIX))
    //      return false;
    for (int i = 0; i < imgExt.length; i++)
      if (fileName.endsWith(imgExt[i]))
        return true;
    return false;
  }

  public static MyFileFilter FILES_ONLY = new MyFileFilter(
      "Sprite project files", SRC_EXT, false, null);
  public static MyFileFilter FILES_AND_DIRS = new MyFileFilter(
      "Sprite project files", SRC_EXT, true, null);

  public void flush() throws IOException {

    DefBuilder sb = new DefBuilder();
    {
      DArray k = new DArray();
      k.addAll(defaults.keySet());
      k.sort(String.CASE_INSENSITIVE_ORDER);
      for (int i = 0; i < k.size(); i++) {
        String key = k.getString(i);
        sb.append(key);
        sb.append(defaults.get(key));
        sb.addCr();
      }
    }
    String content = sb.toString();
    Streams.writeIfChanged(projectFile, content);

  }

  public String toString() {
    return projectFile.toString();
  }

  /**
   * Get atlas file associated with this project
   * @return .atl file
   */
  public File atlasFile() {
    if (atlasFile == null)
      atlasFile = Streams.changeExtension(projectFile, Atlas.ATLAS_EXT);
    return atlasFile;
  }

  private File atlasFile;

  public File file() {
    return projectFile;
  }
  public File baseDirectory() {
    return baseDir;
  }

  public String shortPath(File f) {
    if (basePrefix == null) {
      basePrefix = baseDirectory().getAbsolutePath() + "/";
    }
    String p = f.getAbsolutePath();
    if (p.startsWith(basePrefix))
      p = p.substring(basePrefix.length());
    return p;
  }

  /**
   * Extract id from image filename
   * @param texFile image file
   * @return full id
   */
  public String extractId(File texFile) {

    RelPath rp = new RelPath(baseDir, texFile);
    if (!rp.withinProjectTree())
      throw new IllegalStateException();

    String s = rp.toString();

    StringBuilder sb = new StringBuilder();
    s = Streams.removeExt(s);

    for (int i = 1; i < s.length(); i++) {
      char c = Character.toUpperCase(s.charAt(i));
      if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')))
        c = '_';

      // if underscore, and start of directory/file, skip 
      // this portion 
      if (c == '_') {
        char cp = s.charAt(i - 1);
        if (cp == '>' || cp == '/') {
          int j = i + 1;
          for (; j < s.length(); j++) {
            char cn = s.charAt(j);
            if (cn == '/') {
              j++;
              break;
            }
          }
          i = j - 1;
          continue;
        }
      }

      // if first character of id, and it's a digit, prefix with "_"
      if (sb.length() == 0 && (c >= '0' && c <= '9'))
        sb.append('_');

      sb.append(Character.toUpperCase(c));
    }
    return sb.toString();
  }

  /**
   * Get atlas corresponding to this project
   * @return atlas
   * @throws IOException
   */
  public Atlas atlas() throws IOException {
    if (atlas == null) {
      atlas = new Atlas(atlasFile());
    }
    return atlas;
  }

  private void addImgFiles(DArray imgFiles, File dir) {
    File[] f = dir.listFiles();
    Arrays.sort(f);

    // first pass: images
    // second pass: folders

    for (int pass = 0; pass < 2; pass++) {
      for (int i = 0; i < f.length; i++) {
        File e = f[i];
        if (pass == 0) {
          if (!e.isFile())
            continue;

          if (!isTexture(e))
            continue;
          imgFiles.add(e);
        } else {
          if (!e.isDirectory())
            continue;
          if (isMetaInfoFolder(e))
            continue;
          addImgFiles(imgFiles, e);
        }
      }
    }
  }

  public static boolean isMetaInfoFolder(File f) {
    return (f.isDirectory() && f.getName().startsWith("_"));
  }

  /**
   * @return
   */
  public File[] getImageFiles() {

    if (imgFiles == null) {
      DArray lst = new DArray();
      addImgFiles(lst, baseDir);
      imgFiles = (File[]) lst.toArray(File.class);
    }
    return imgFiles;
  }

  //  private static class Entry {
  //    public Entry(String id) {
  //      this.id = id;
  //    }
  //    public SpriteInfo si;
  //    public String id;
  //  };
  //
  //  private Entry entryFor(String id) {
  //    return (Entry) entries.get(id);
  //  }
  //
  //  private void addEntries(File f) {
  //    if (!f.isDirectory()) {
  //      if (isTexture(f)) {
  //
  //        // is there already an entry with this id?
  //        String id = extractId(f);
  //        Entry ent = entryFor(id);
  //        if (ent == null) {
  //          
  //        }
  //      }
  //    } else {
  //    }
  //  }
  //
  //  public void readEntries() {
  //    if (entries == null) {
  //      entries = new TreeMap();
  //      addEntries(baseDir);
  //    }
  //
  //  }
  //
  //  private TreeMap entries;

  private File[] imgFiles;

  private Atlas atlas;
  private File projectFile;
  private File baseDir;
  private String basePrefix;
  private String name;

  public String name() {
    return name;
  }
  //  private boolean simulate;
  //  private int maxFilesRemaining;
  //  private String simProblem;

  private Map defaults = new HashMap();

  //  public void setSimulation() {
  //    simulate = true;
  //    maxFilesRemaining = 300;
  ////    if (true) {
  ////      warn("using small # files");
  ////      maxFilesRemaining = 15;
  ////    }
  //  }

  public void discardAtlas() {
    atlas = null;
  }

}