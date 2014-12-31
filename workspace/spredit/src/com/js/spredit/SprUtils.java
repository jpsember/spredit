package com.js.spredit;

import java.io.*;
import java.util.*;
import javax.swing.*;

import com.js.basic.Files;

import tex.*;
import apputil.*;
import static com.js.basic.Tools.*;

public class SprUtils {

  public static File addResolutionSuffix(File file, float[] res, int slot) {
    if (slot != 0) {
      String s = file.toString();
      String ext = null;
      if (Files.hasExtension(file)) {
        ext = Files.getExtension(file);
        s = Files.removeExtension(file).getPath();
      }
      s = s + "_" + slot;
      if (ext != null)
        s = Files.addExtension(s, ext);
      file = new File(s);
    }
    return file;
  }

  private static void addFiles(ArrayList<File> lst, File f, int max) {
    final boolean db = false;

    if (db)
      pr("addFiles max=" + max + ", count=" + lst.size() + ", adding " + f);

    lst.add(f);
    if (lst.size() < max) {
      if (f.isDirectory()) {
        File[] fl = f.listFiles();
        if (db)
          pr(" processing subfiles: " + fl.length);

        for (int i = 0; i < fl.length; i++) {
          if (db)
            pr("  subfile=" + fl[i]);

          if (lst.size() >= max)
            break;
          addFiles(lst, fl[i], max);
        }
      }
    }
  }

  private static final int MANY_FILES = 200;

  private static boolean manyFiles(File f) {
    ArrayList<File> lst = new ArrayList();
    int max = MANY_FILES;
    addFiles(lst, f, MANY_FILES);
    return lst.size() >= max;
  }

  private static File nestedProjects(File f, File projectFile) {
    File ret = null;
    if (!f.isDirectory()) {
      // warn("disabled");
      if (!f.equals(projectFile) && TexProject.FILES_ONLY.accept(f))
        ret = f;
    } else {
      File[] fl = f.listFiles();
      for (int i = 0; ret == null && i < fl.length; i++) {
        ret = nestedProjects(fl[i], projectFile);
      }
    }
    return ret;
  }

  /**
   * Verify that it is ok to create a sprite project at a location. Warns user
   * if project tree has > 200 files or so; reports error if an existing sprite
   * project is found in the project tree.
   * 
   * @param f
   *          path
   * @return true if ok; false if user aborted, or some error found
   */
  public static boolean verifyCreateTexProject(File f) {

    boolean ok = false;
    do {
      boolean manyFiles = manyFiles(f.getParentFile());

      if (manyFiles) {
        int code = JOptionPane
            .showConfirmDialog(
                AppTools.frame(),
                "There are a lot of files in the project tree.  Create project anyways?",
                "Potential problem", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (code != JOptionPane.YES_OPTION)
          break;
      }

      File pf = nestedProjects(f.getParentFile(), f);
      if (pf != null) {
        AppTools.showMsg("Project tree contains project file: " + pf);
        break;
      }

      ok = true;
    } while (false);
    return ok;
  }

  /**
   * @deprecated moved to AppTools
   * @param f
   * @return
   */
  public static File incrementFile(File f) {
    String name = f.getName();
    String ext = Files.getExtension(f);
    name = Files.removeExtension(f).getName();

    if (name.length() == 0)
      throw new IllegalArgumentException(f.toString());
    int i = name.length();
    while (i > 0) {
      char c = name.charAt(i - 1);
      if (c < '0' || c > '9')
        break;
      i--;
    }
    int number = 0;
    if (i < name.length()) {
      number = 1 + Integer.parseInt(name.substring(i));
      name = name.substring(0, i);
    }
    if (number < 10)
      name = name + "0";
    name = name + number;
    name = name + "." + ext;
    File ret = new File(f.getParent(), name);
    return ret;

  }

  /**
   * Add entries for any image files we find in file tree
   * 
   * @param f
   *          root of file tree
   * @throws IOException
   */
  private static void addEntries(TexProject project, TreeMap entries, File f)
      throws IOException {
    if (!f.isDirectory()) {
      if (project.isTexture(f)) {
        String id = project.extractId(f);
        {
          SpriteInfo si;
          si = new SpriteInfo(project, f);
          si.verifyMetaData(true);
          // Entry ent = new Entry(id);
          // ent.si = si;
          entries.put(id, si);

        }
      }
    } else {
      if (!TexProject.isMetaInfoFolder(f)) {
        File[] fs = f.listFiles();
        for (int i = 0; i < fs.length; i++)
          addEntries(project, entries, fs[i]);
      }
    }
  }

  /**
   * Add entries for any sprites that are aliased to originals. These will not
   * have source image files, so won't have been detected by the previous pass.
   * 
   * @param f
   *          root of file tree
   * @throws IOException
   */
  private static void addAliasEntries(TexProject project, TreeMap entries,
      File f) throws IOException {
    final boolean db = true;

    if (db)
      pr("addAliasEntries " + f);
    do {
      if (!f.isDirectory()) {
        if (!SpriteInfo.META_FILES_ONLY.accept(f))
          break;

        // is there already an entry with this id?
        String id = project.extractId(f);
        SpriteInfo ent = (SpriteInfo) entries.get(id);
        if (db)
          pr(" entry for id=" + id + " is " + ent);

        // if entry exists, already added in first pass;
        // not an aliased entry.
        if (ent != null)
          break;

        // construct entry, read meta file
        ent = new SpriteInfo(project, f);

        // if not an alias, something funny going on;
        // probably the .png file corresponding to this
        // meta file was deleted.

        File aTag = ent.getAliasTag();
        if (aTag == null) {
          // File imgPath = ent.imagePath();
          // if (db)
          // pr(" not an alias; imgPath=[" + imgPath + "]");
          pr("Deleting meta file that has no sprite: " + f);
          f.delete();

          break;
        }

        // find aliased entry
        // File aimg = ent.imagePath();
        String aid = project.extractId(aTag);
        SpriteInfo aent = (SpriteInfo) entries.get(aid);
        if (aent == null) {
          warning("can't find original " + aTag + " for alias " + f);
          f.delete();
          break;
        }

        ent.setAliasSprite(aent);
        entries.put(ent.id(), ent);

        ent.verifyMetaData(true);

      } else {
        if (f.getName().equals(TexProject.THUMB_DIR))
          return;

        File[] fs = f.listFiles();
        for (int i = 0; i < fs.length; i++) {
          addAliasEntries(project, entries, fs[i]);
        }
      }
    } while (false);
  }

  public static TreeMap readSprites(TexProject project) throws IOException {
    TreeMap entries = new TreeMap();
    addEntries(project, entries, project.baseDirectory());
    addAliasEntries(project, entries, project.baseDirectory());
    return entries;
  }

}
