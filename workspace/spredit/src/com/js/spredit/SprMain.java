package com.js.spredit;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import static com.js.basic.Tools.*;

import javax.swing.*;

import com.js.basic.CmdLineArgs;
import com.js.geometry.IPoint;

import apputil.*;
import scanning.*;
import streams.*;
import tex.*;

public class SprMain implements IApplication {

  public static IApplication app() {
    if (theApp == null)
      theApp = new SprMain();
    return theApp;
  }

  private static final String APP_NAME = "SprEdit";

  private static final String help = "Compile texture atlas\n"
      + "spredit <options>\n" + "Options: \n"
      + " -b <projectpath[.txp]>  : build texture atlas\n"
      + " -w               : write texture atlas to png file\n"
      + " -p               : include standard palette in atlas\n"
      + " -f <fontname> <size> <outputpath>\n"
      + "                  : build atlas from Java font\n"
      + "                    (replace spaces in font name with underscores)\n"
      + " -F               : display fonts available on this machine\n"
      + " -s <width> <height> : set size of atlas for font\n"
      + " -S               : show built font\n" //
      + " -R               : plot frames around sprites\n" //
      + " -T               : disable font sorting\n" //
      + " -C               : disable font cropping\n" //
      + " -H <pixels>      : add extra horizontal pixels between characters\n" //
      + " -h, --help       : help\n" //
      + " -v               : verbose\n" //
      + " -B               : set bold attribute\n" //
      + " -I               : set italic attribute\n" //
  , //

      defaults = " == --help -h" + " !! " + "   ";

  private static IApplication theApp;

  public static void main(String[] args) {
    runGUI = true;

    try {
      CmdArgs ca = new CmdArgs(args, defaults, help);

      while (true) {
        if (!ca.hasNext()) {
          break;
        }

        if (!ca.nextIsOption())
          ca.exception("Unexpected argument: " + ca.nextValue());

        switch (ca.nextChar()) {
        default:
          ca.unsupported();
          break;

        case 'v':
          verbose = true;
          break;
        case 'F': {
          runGUI = false;
          String[] fn = Builder.getFontNames();
          for (int i = 0; i < fn.length; i++) {
            String s = fn[i];
            s = s.replaceAll(" ", "_");
            pr(s);
          }

        }
          break;
        case 'S':
          showAtlas = true;
          break;
        case 'C':
          fontCropping = false;
          break;
        case 'B':
          bold = true;
          break;
        case 'I':
          italic = true;
          break;
        case 'T':
          disableSorting = true;
          break;
        case 'H':
          horzPixels = ca.nextInt();
          break;
        case 'f': {
          runGUI = false;
          fontName = ca.nextValue();
          fontName = fontName.replaceAll("_", " ");
          fontSize = ca.nextInt();
          fontPath = ca.nextValue();
        }
          break;
        case 'R':
          plotDebugFrames = true;
          break;
        case 's':
          atlasSize = new IPoint(ca.nextInt(), ca.nextInt());
          break;
        case 'b': {
          runGUI = false;
          String p = ca.nextValue();
          p = Streams.addExtension(p, TexProject.SRC_EXT);
          buildPath = new File(p).getAbsoluteFile();
        }
          break;
        case 'p':
          includePalette = true;
          break;
        case 'w':
          writeToPNG = true;
          break;
        case 'r': {
          ArrayList<Float> a = new ArrayList();
          while (ca.nextIsValue()) {
            a.add((float) ca.nextDouble());
          }
          resolutions = new float[a.size()];
          for (int i = 0; i < resolutions.length; i++)
            resolutions[i] = a.get(i);
        }
          break;
        }
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
          b.setSort(!disableSorting);
          b.setPlotFrames(plotDebugFrames);
          if (includePalette)
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

            if (writeToPNG) {
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
      atlasBuilder.setSort(!disableSorting);
      atlasBuilder.setPlotFrames(plotDebugFrames);

      if (includePalette) {
        atlasBuilder.includePalette();
      }

      int style = 0;
      if (italic)
        style |= Font.ITALIC;
      if (bold)
        style |= Font.BOLD;

      int scaledSize = Math.round(fontSize * scl);
      // Font f;

      // f = new Font(fontName, style, fontSize);
      FontExtractor fimg = new FontExtractor(
          new Font(fontName, style, fontSize));
      fimg.setHorizontalSpacing(horzPixels);
      fimg.setCropping(fontCropping);

      FontExtractor fimg2 = null;
      if (scaledSize != fontSize) {
        fimg2 = new FontExtractor(new Font(fontName, style, scaledSize));
        fimg2.setHorizontalSpacing(horzPixels);
        fimg2.setCropping(fontCropping);
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

      if (writeToPNG)
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
  private static boolean runGUI, showAtlas, plotDebugFrames, disableSorting,
      fontCropping = true, bold, italic, includePalette, writeToPNG;
  private static int horzPixels;
  private static File atlasFile;
  private static float[] resolutions;

}

