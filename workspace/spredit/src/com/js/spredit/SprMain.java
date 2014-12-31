package com.js.spredit;

import java.awt.*;
import java.io.*;
import static com.js.basic.Tools.*;

import javax.swing.*;

import com.js.basic.CmdLineArgs;
import com.js.basic.Files;
import com.js.geometry.IPoint;

import apputil.*;
import tex.*;

public class SprMain implements IApplication {

  public static void main(String[] args) {
    try {
      CmdLineArgs ca = new CmdLineArgs();
      sArgs = ca;
      ca.banner("Compile texture atlas");

      ca.add("build").setString()
          .desc("Build texture atlas <projectpath[.txp]>");
      ca.add("write").desc("Write texture atlas to .png file");
      ca.add("palette").desc("Include standard palette in atlas");
      ca.add("font").setString().setArray(3)
          .desc("Build atlas from Java font; <fontname> <size> <outputpath>");
      ca.add("showfont").desc("Display the built font");
      ca.add("showfonts").desc("Display fonts available on this machine");
      ca.add("size").setInt().setArray(2)
          .desc("Set size of atlas for font <width> <height>");
      ca.add("frames").desc("Plot frames around sprites");
      ca.add("nosort").desc("Disable font sorting");
      ca.add("nocrop").desc("Disable font cropping");
      ca.add("horzpixels").def(0)
          .desc("Add extra horizontal pixels between characters");
      ca.add("verbose").desc("Verbose messages");
      ca.add("bold").desc("Set bold attribute");
      ca.add("italics").desc("Set italics attribute");
      ca.add("resolutions").setDouble().setArray()
          .desc("Set resolutions to compile");

      ca.parse(args);

      mVerbose = ca.get("verbose");

      if (ca.get("showfonts")) {
        mRunGUI = false;
        String[] fn = Builder.getFontNames();
        for (int i = 0; i < fn.length; i++) {
          String s = fn[i];
          s = s.replaceAll(" ", "_");
          pr(s);
        }
      }
      mShowAtlas = ca.get("showfont");
      if (ca.hasValue("font")) {
        String[] s = ca.getStrings("font");
        mRunGUI = false;
        mFontName = s[0];
        mFontName = mFontName.replaceAll("_", " ");
        mFontSize = Integer.parseInt(s[1]);
        mFontPath = new File(s[2]);
      }

      if (ca.hasValue("size")) {
        int[] v = ca.getInts("size");
        mAtlasSize = new IPoint(v[0], v[1]);
      }
      if (ca.hasValue("build")) {
        mRunGUI = false;
        File path = new File(ca.getString("build"));
        mBuildPath = Files.setExtension(path.getAbsoluteFile(),
            TexProject.SRC_EXT);
      }

      if (ca.hasValue("resolutions")) {
        mResolutions = ca.getFloats("resolutions");
      }

      if (mRunGUI) {
        AppTools.startApplication(new SprMain());
      } else {
        AppTools.runAsCmdLine();

        if (mFontPath != null) {
          buildFont();
          return;
        }

        if (mBuildPath != null) {
          if (mVerbose)
            pr("Building atlas: " + mBuildPath);

          TexProject tp = new TexProject(mBuildPath);

          Builder b = new Builder();
          b.setProject(tp);
          b.setVerbose(mVerbose);
          b.setSort(!sArgs.get("nosort"));
          b.setPlotFrames(sArgs.get("frames"));
          if (sArgs.get("palette"))
            b.includePalette();

          b.gatherSprites();

          float[] res = getResolutions();

          for (int i = 0; i < res.length; i++) {
            float resolution = res[i];

            File f = tp.atlasFile();
            f = SprUtils.addResolutionSuffix(f, res, i);

            pr("building atlas [" + f + "]");

            Atlas at = b.build(f, mAtlasSize, null, resolution);

            if (sArgs.get("write")) {
              at.debugWriteToPNG();
            }

            if (i == 0) {
              mAtlasFile = at.dataFile();
              if (mShowAtlas)
                laterShowNewAtlas();
            }
          }
          return;
        }
      }
    } catch (CmdLineArgs.Exception e) {
      System.out.println(e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void createAndShowGUI(JFrame frame) {
    try {
      mConfigSet = new ConfigSet(null) //
          .add(apputil.MyFrame.CONFIG) //
          .add(SpriteEditor.CONFIG) //
          .restore();
    } catch (Throwable e) {
      AppTools.showError("reading defaults file", e);
    }

    SpriteEditor.init((JComponent) frame.getContentPane());
  }

  @Override
  public String getName() {
    return "SprEdit";
  }

  @Override
  public JFrame getFrame() {
    return new MyFrame(getName());
  }

  @Override
  public boolean exitProgram() {
    if (!SpriteEditor.doCloseProject())
      return false;
    writeDefaults();
    return true;
  }

  private void writeDefaults() {
    try {
      mConfigSet.save();
    } catch (IOException e) {
      AppTools.showError("writing defaults file", e);
    }
  }

  private static void buildFont() throws IOException {
    final boolean db = false;

    float[] res = getResolutions();
    for (int slot = 0; slot < res.length; slot++) {
      float scl = res[slot];

      Builder atlasBuilder = new Builder();
      atlasBuilder.setVerbose(mVerbose);
      atlasBuilder.setSort(!sArgs.get("nosort"));
      atlasBuilder.setPlotFrames(sArgs.get("frames"));

      if (sArgs.get("palette")) {
        atlasBuilder.includePalette();
      }

      int style = 0;
      if (sArgs.get("italics"))
        style |= Font.ITALIC;
      if (sArgs.get("bold"))
        style |= Font.BOLD;

      int scaledSize = Math.round(mFontSize * scl);
      FontExtractor fimg = new FontExtractor(new Font(mFontName, style,
          mFontSize));
      fimg.setHorizontalSpacing(sArgs.getInt("horzpixels"));
      fimg.setCropping(!sArgs.get("nocrop"));

      FontExtractor fimg2 = null;
      if (scaledSize != mFontSize) {
        fimg2 = new FontExtractor(new Font(mFontName, style, scaledSize));
        fimg2.setHorizontalSpacing(sArgs.getInt("horzpixels"));
        fimg2.setCropping(!sArgs.get("nocrop"));
      }

      for (char c = ' '; c < 128; c++) {
        fimg.render(c);
        if (fimg2 != null) {
          fimg2.render(c);
        }

        SpriteInfo si = new SpriteInfo(Integer.toString(c), fimg.getClip(),
            fimg.getCP());
        si.setSourceImage(fimg.getImage());
        warning("compressed images no longer supported");
        // if (fimg2 != null)
        // si.setCompressedImage(fimg2.getImage(), // fimg2.getClip(),
        // fimg2.getCP());

        atlasBuilder.addSprite(si, si.id());

      }
      int[] fd = fimg.getFontInfo();
      fimg = null;

      File fp = SprUtils.addResolutionSuffix(mFontPath, res, slot);
      if (db)
        pr(" writing to " + fp);

      Atlas at = atlasBuilder.build(fp, null, fd, res[slot]);

      if (at == null) {
        pr("*** Could not build atlas " + fp);
        System.exit(1);
      }
      if (mVerbose)
        pr("built atlas " + at);

      if (sArgs.get("write"))
        at.debugWriteToPNG();

      if (slot == 0) {
        mAtlasFile = at.dataFile();

        if (mShowAtlas)
          laterShowNewAtlas();
      }
    }
  }

  private static void laterShowNewAtlas() {
    // display font in atlas display
    // Schedule a job for the event-dispatching thread:
    // calling an application object's run() method.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        displayNewAtlas();
      }
    });
  }

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void displayNewAtlas() {
    AtlasDisplay a = new AtlasDisplay(mAtlasFile);
    a.setVisible(true);
  }

  private static float[] getResolutions() {
    if (mResolutions == null) {
      mResolutions = new float[1];
      mResolutions[0] = 1;
    }
    return mResolutions;
  }

  private static ConfigSet mConfigSet;
  private static IPoint mAtlasSize;
  private static File mBuildPath;
  private static File mFontPath;
  private static String mFontName;
  private static int mFontSize;
  private static boolean mVerbose;
  private static boolean mRunGUI = true;
  private static boolean mShowAtlas;
  private static File mAtlasFile;
  private static float[] mResolutions;
  private static CmdLineArgs sArgs;

}
