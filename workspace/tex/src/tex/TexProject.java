package tex;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Files;

import apputil.*;
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
    return new TexProject(f, false);
  }

  /**
   * Construct existing project
   * 
   * @param f
   * @throws IOException
   * @throws FileNotFoundException
   */
  public TexProject(File f) throws FileNotFoundException, IOException {
    this(f, true);
  }

  /**
   * Create new project
   * 
   * @param baseDir
   *          location of base directory containing project file; if no project
   *          file found here, creates it
   * @throws IOException
   */
  private TexProject(File f, boolean mustExist) throws IOException {

    final boolean db = false;

    if (db)
      pr("TexProject constructor: " + f);

    this.baseDir = f.getParentFile();
    this.projectFile = f;
    this.name = Files.removeExt(f.getName());

    if (db)
      pr("baseDir=" + baseDir + "\nname=" + name);

    if (projectFile.exists()) {
      try {
        read();
      } catch (JSONException e) {
        die(e);
      }
    } else {
      if (mustExist)
        throw new FileNotFoundException();
    }
    flush();
  }

  private void read() throws IOException, JSONException {
    String content = FileUtils.readFileToString(projectFile);
    defaults = new JSONObject(content);
  }

  public JSONObject getDefaults() {
    return defaults;
  }

  private static String[] imgExt = { "png", "jpg", };

  public boolean isTexture(File f) {
    if (f.isDirectory())
      return false;

    // if (Atlas.DATA_FILES_ONLY.accept(f)) return false;

    String fileName = f.getName();
    // if name starts with "ATLAS_" prefix, ignore.
    // if (!Atlas.ONEFILE && fileName.startsWith(Atlas.PREFIX))
    // return false;
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
    String content = defaults.toString();
    Files.writeStringToFileIfChanged(projectFile, content);
  }

  public String toString() {
    return projectFile.toString();
  }

  /**
   * Get atlas file associated with this project
   * 
   * @return .atl file
   */
  public File atlasFile() {
    if (atlasFile == null)
      atlasFile = Files.changeExtension(projectFile, Atlas.ATLAS_EXT);
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
   * 
   * @param texFile
   *          image file
   * @return full id
   */
  public String extractId(File texFile) {

    RelPath rp = new RelPath(baseDir, texFile);
    if (!rp.withinProjectTree())
      throw new IllegalStateException("TexProject " + rp
          + " is not within project tree");

    String s = rp.toString();

    StringBuilder sb = new StringBuilder();
    s = Files.removeExt(s);

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
   * 
   * @return atlas
   * @throws IOException
   */
  public Atlas atlas() throws IOException {
    if (atlas == null) {
      atlas = new Atlas(atlasFile());
    }
    return atlas;
  }

  private void addImgFiles(ArrayList<File> imgFiles, File dir) {
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
      ArrayList<File> lst = new ArrayList();
      addImgFiles(lst, baseDir);
      imgFiles = lst.toArray(new File[0]);
    }
    return imgFiles;
  }

  private File[] imgFiles;

  private Atlas atlas;
  private File projectFile;
  private File baseDir;
  private String basePrefix;
  private String name;

  public String name() {
    return name;
  }

  private JSONObject defaults = new JSONObject();

  // private Map defaults = new HashMap();

  public void discardAtlas() {
    atlas = null;
  }

}
