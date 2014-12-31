package com.js.spredit;

import java.io.*;

import static com.js.basic.Tools.*;

import com.js.basic.CmdLineArgs;
import com.js.basic.Files;
import com.js.geometry.IPoint;

import tex.*;

public class SprCompile {

  private static CmdLineArgs buildCmdLineArgs() {
    CmdLineArgs c = new CmdLineArgs();
    c.banner("Compile texture atlas");

    c.add("project").setString().desc("Sprite project to build atlas for");
    c.add("write").desc("Write texture atlas to .png file");
    c.add("palette").desc("Include standard palette in atlas");
    c.add("size").setInt().setArray(2)
        .desc("Set size of atlas for font <width> <height>");
    c.add("frames").desc("Plot frames around sprites");
    c.add("nosort").desc("Disable font sorting");
    c.add("nocrop").desc("Disable font cropping");
    c.add("horzpixels").def(0)
        .desc("Add extra horizontal pixels between characters");
    c.add("verbose").desc("Verbose messages");
    c.add("resolutions").setDouble().setArray()
        .desc("Set resolutions to compile");
    return c;
  }

  public static void main(String[] args) {
    if (true) {
      warning("using test args");
      String[] a = { "-p", "sample", "-v" };
      args = a;
    }

    try {
      ca = buildCmdLineArgs();

      ca.parse(args);

      mVerbose = ca.get("verbose");

      if (ca.hasValue("size")) {
        int[] v = ca.getInts("size");
        mAtlasSize = new IPoint(v[0], v[1]);
      }

      {
        File path = new File(ca.getString("project"));
        warning("what happens if no 'project' specified?");
        mBuildPath = Files.setExtension(path, TexProject.SRC_EXT);
      }

      if (ca.hasValue("resolutions")) {
        mResolutions = ca.getFloats("resolutions");
      }

      buildAtlas();
    } catch (CmdLineArgs.Exception e) {
      System.out.println(e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private static void buildAtlas() throws IOException {

    if (mVerbose)
      pr("Building atlas: " + mBuildPath);

    TexProject tp = new TexProject(mBuildPath);

    Builder b = new Builder();
    b.setProject(tp);
    b.setVerbose(mVerbose);
    b.setSort(!ca.get("nosort"));
    b.setPlotFrames(ca.get("frames"));
    if (ca.get("palette"))
      b.includePalette();

    b.gatherSprites();

    float[] res = mResolutions;

    for (int i = 0; i < res.length; i++) {
      float resolution = res[i];

      File f = tp.atlasFile();
      f = SprUtils.addResolutionSuffix(f, res, i);

      pr("building atlas [" + f + "]");

      Atlas at = b.build(f, mAtlasSize, null, resolution);

      if (ca.get("write")) {
        at.debugWriteToPNG();
      }
    }

  }

  private static float[] sDefaultResolutions = { 1 };

  private static IPoint mAtlasSize;
  private static File mBuildPath;
  private static boolean mVerbose;
  private static float[] mResolutions = sDefaultResolutions;
  private static CmdLineArgs ca;
}
