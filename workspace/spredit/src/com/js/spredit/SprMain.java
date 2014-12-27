package com.js.spredit;

import java.awt.*;
import java.io.*;
import static com.js.basic.Tools.*;

import javax.swing.*;

import com.js.basic.CmdLineArgs;
import com.js.geometry.IPoint;

import apputil.*;
import streams.*;
import tex.*;

public class SprMain implements IApplication {

  public static IApplication app() {
    if (theApp == null)
      theApp = new SprMain();
    return theApp;
  }

  private static final String APP_NAME = "SprEdit";
  private static IApplication theApp;

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

      verbose = ca.get("verbose");

      if (ca.get("showfonts")) {
        runGUI = false;
        String[] fn = Builder.getFontNames();
        for (int i = 0; i < fn.length; i++) {
          String s = fn[i];
          s = s.replaceAll(" ", "_");
          pr(s);
        }
      }
      showAtlas = ca.get("showfont");
      if (ca.hasValue("font")) {
        String[] s = ca.getStrings("font");
        runGUI = false;
        fontName = s[0];
        fontName = fontName.replaceAll("_", " ");
        fontSize = Integer.parseInt(s[1]);
        fontPath = s[2];
      }

      if (ca.hasValue("size")) {
        int[] v = ca.getInts("size");
        atlasSize = new IPoint(v[0], v[1]);
      }
      if (ca.hasValue("build")) {
        runGUI = false;
        String p = ca.getString("build");
        p = Streams.addExtension(p, TexProject.SRC_EXT);
        buildPath = new File(p).getAbsoluteFile();
      }

      if (ca.hasValue("resolutions")) {
        resolutions = ca.getFloats("resolutions");
      }

      if (runGUI) {
        AppTools.setAppName(APP_NAME);

        // Schedule a job for the event-dispatching thread:
        // calling an application object's run() method.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            createAndShowGUI();
          }
        });
      } else {
        AppTools.runAsCmdLine();

        if (fontPath != null) {
          buildFont();
          return;
        }

        if (buildPath != null) {
          final boolean db = false;

          if (db)
            pr("buildPath=" + buildPath);

          if (verbose)
            pr("Building atlas: " + buildPath);

          TexProject tp = new TexProject(buildPath);

          Builder b = new Builder();
          b.setProject(tp);
          b.setVerbose(verbose);
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

            Atlas at = b.build(f, atlasSize, null, resolution);

            if (db)
              pr("done building");

            if (sArgs.get("write")) {
              at.debugWriteToPNG();
              // File pngPath = Streams.changeExtension(f, "png");
              // pr("writing atlas to " + pngPath);
              // ImgUtil.writePNG(at.image(), pngPath);
            }

            if (i == 0) {

              atlasFile = at.dataFile();
              if (showAtlas)
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

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void createAndShowGUI() {

    config = new ConfigSet(null);
    config.add(apputil.MyFrame.CONFIG);
    config.add(SpriteEditor.CONFIG);
    // config.add(AtlasDisplay.CONFIG);
    try {
      config.readFrom(AppTools.getDefaultsPath("spredit"));
    } catch (Throwable e) {
      AppTools.showError("reading defaults file", e);
    }

    MyFrame frame = new MyFrame("SPRMAIN");
    frame.setTitle(APP_NAME);
    AppTools.setFrame(frame, app());

    SpriteEditor.init((JComponent) frame.getContentPane());

    frame.setVisible(true);
  }

  private static ConfigSet config;

  public boolean exitProgram() {
    final boolean db = false;
    if (db)
      pr("exitProgram");
    boolean quit = false;
    do {

      if (!SpriteEditor.doCloseProject())
        break;

      writeDefaults();
      quit = true;
    } while (false);
    return quit;
  }

  private static void writeDefaults() {
    try {
      config.writeTo(AppTools.getDefaultsPath("spredit"));
    } catch (IOException e) {
      AppTools.showError("writing defaults file", e);
    }
  }

  // private static int scaleUp(int value, float scale) {
  // return Math.round(value / scale);
  // }

  private static void buildFont() throws IOException {
    final boolean db = false;

    float[] res = getResolutions();
    for (int slot = 0; slot < res.length; slot++) {
      float scl = res[slot];

      if (db)
        pr("buildFont resolution #" + slot + "= " + scl);

      Builder atlasBuilder = new Builder();
      atlasBuilder.setVerbose(verbose);
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

      int scaledSize = Math.round(fontSize * scl);
      // Font f;

      // f = new Font(fontName, style, fontSize);
      FontExtractor fimg = new FontExtractor(
          new Font(fontName, style, fontSize));
      fimg.setHorizontalSpacing(sArgs.getInt("horzpixels"));
      fimg.setCropping(!sArgs.get("nocrop"));

      FontExtractor fimg2 = null;
      if (scaledSize != fontSize) {
        fimg2 = new FontExtractor(new Font(fontName, style, scaledSize));
        fimg2.setHorizontalSpacing(sArgs.getInt("horzpixels"));
        fimg2.setCropping(!sArgs.get("nocrop"));
      }

      // if (

      if (db)
        pr(" fontSize=" + fontSize + " scaledSize=" + scaledSize);

      for (char c = ' '; c < 128; c++) {
        fimg.render(c);
        if (fimg2 != null) {
          fimg2.render(c);
        }

        SpriteInfo si = new SpriteInfo(Integer.toString(c), fimg.getClip(),
            fimg.getCP());
        si.setSourceImage(fimg.getImage());
        if (fimg2 != null)
          si.setCompressedImage(fimg2.getImage(), // fimg2.getClip(),
              fimg2.getCP());

        atlasBuilder.addSprite(si, si.id());

      }
      int[] fd = fimg.getFontInfo();
      fimg = null;

      fontPath = Streams.addExtension(fontPath, Atlas.ATLAS_EXT);
      File fp = new File(fontPath);

      fp = SprUtils.addResolutionSuffix(fp, res, slot);
      if (db)
        pr(" writing to " + fp);

      // // use -1 as resolution to ignore it; we don't want it throwing
      // // out our image which we have pre-attached to the SpriteInfo record
      // Atlas at = atlasBuilder.build(fp, null, fd, -1);

      if (db)
        pr(" building atlas, resolution " + res[slot]);

      Atlas at = atlasBuilder.build(fp, null, fd, res[slot]);

      if (at == null) {
        pr("*** Could not build atlas " + fp);
        System.exit(1);
      }
      if (verbose)
        pr("built atlas " + at);

      if (sArgs.get("write"))
        at.debugWriteToPNG();

      if (slot == 0) {
        atlasFile = at.dataFile();

        if (showAtlas)
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
    AtlasDisplay a = new AtlasDisplay(atlasFile);
    a.setVisible(true);
  }

  private static float[] getResolutions() {
    if (resolutions == null) {
      resolutions = new float[1];
      resolutions[0] = 1;
    }
    return resolutions;
  }

  private static IPoint atlasSize;
  private static File buildPath;
  private static String fontPath;
  private static String fontName;
  private static int fontSize;
  private static boolean verbose;
  private static boolean runGUI = true;
  private static boolean showAtlas;
  private static File atlasFile;
  private static float[] resolutions;
  private static CmdLineArgs sArgs;
}
