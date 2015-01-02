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
   * @param file
   * @return
   * @throws IOException
   */
  public static TexProject create(File file) throws IOException {
    if (file.exists())
      throw new IOException("File already exists");
    return new TexProject(file, false);
  }

  /**
   * Construct existing project
   * 
   * @param projectFile
   * @throws IOException
   * @throws FileNotFoundException
   */
  public TexProject(File projectFile) throws FileNotFoundException, IOException {
    this(projectFile, true);
  }

  /**
   * Create new project
   * 
   * @param mBaseDirectory
   *          location of base directory containing project file; if no project
   *          file found here, creates it
   * @throws IOException
   */
  private TexProject(File projectFile, boolean mustExist) throws IOException {

    projectFile = projectFile.getAbsoluteFile();
    this.mBaseDirectory = projectFile.getParentFile();
    this.mProjectFile = projectFile;
    this.mProjectName = Files.removeExtension(projectFile).getName();

    if (mProjectFile.exists()) {
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
    String content = FileUtils.readFileToString(mProjectFile);
    mDefaults = new JSONObject(content);
  }

  public JSONObject getDefaults() {
    return mDefaults;
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

  public static MyFileFilter FILES = new MyFileFilter("Sprite project files",
      SRC_EXT);

  public void flush() throws IOException {
    String content = mDefaults.toString();
    Files.writeStringToFileIfChanged(mProjectFile, content);
  }

  public String toString() {
    return mProjectFile.toString();
  }

  /**
   * Get atlas file associated with this project
   * 
   * @return .atl file
   */
  public File atlasFile() {
    if (mAtlasFile == null)
      mAtlasFile = Files.setExtension(mProjectFile, Atlas.ATLAS_EXT);
    return mAtlasFile;
  }

  public File file() {
    return mProjectFile;
  }

  public File baseDirectory() {
    return mBaseDirectory;
  }

  public String shortPath(File f) {
    if (mBasePrefix == null) {
      mBasePrefix = baseDirectory().getAbsolutePath() + "/";
    }
    String p = f.getAbsolutePath();
    if (p.startsWith(mBasePrefix))
      p = p.substring(mBasePrefix.length());
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
    if (db)
      pr("TexProject.extractId texFile '" + texFile + "', baseDirectory "
          + mBaseDirectory);

    File relFile = Files.fileWithinDirectory(texFile, mBaseDirectory);
    if (relFile.isAbsolute())
      throw new IllegalStateException("TexProject '" + texFile
          + "' is not within project tree");

    StringBuilder sb = new StringBuilder();

    relFile = Files.removeExtension(relFile);
    String s = relFile.getPath();

    for (int i = 0; i < s.length(); i++) {
      char c = Character.toUpperCase(s.charAt(i));
      if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')))
        c = '_';

      // if underscore, and start of directory/file, skip
      // this portion
      if (c == '_') {
        if (i == 0 || s.charAt(i - 1) == File.separatorChar) {
          int j = i + 1;
          for (; j < s.length(); j++) {
            char cn = s.charAt(j);
            if (cn == File.separatorChar) {
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
    String idString = sb.toString();
    return idString;
  }

  /**
   * Get atlas corresponding to this project
   * 
   * @return atlas
   * @throws IOException
   */
  public Atlas atlas() throws IOException {
    if (mAtlas == null) {
      mAtlas = new Atlas(atlasFile());
    }
    return mAtlas;
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

    if (mImageFiles == null) {
      ArrayList<File> lst = new ArrayList();
      addImgFiles(lst, mBaseDirectory);
      mImageFiles = lst.toArray(new File[0]);
    }
    return mImageFiles;
  }

  public String name() {
    return mProjectName;
  }

  public void discardAtlas() {
    mAtlas = null;
  }

  private File[] mImageFiles;
  private File mAtlasFile;
  private Atlas mAtlas;
  private File mProjectFile;
  private File mBaseDirectory;
  private String mBasePrefix;
  private String mProjectName;
  private JSONObject mDefaults = new JSONObject();
}
