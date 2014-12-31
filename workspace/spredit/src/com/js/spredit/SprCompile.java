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
    new SprCompile().run(args);
  }

  private void run(String[] args) {
    if (true) {
      warning("using test args");
      String[] a = {//
      "-v", //
          "-p", "sample", //
      };
      args = a;
    }

    try {
      mArgs = buildCmdLineArgs();
      mArgs.parse(args);

      mVerbose = mArgs.get("verbose");

      if (mArgs.hasValue("size")) {
        int[] v = mArgs.getInts("size");
        mAtlasSize = new IPoint(v[0], v[1]);
      }

      {
        File path = new File(mArgs.getString("project"));
        mBuildPath = Files.setExtension(path, TexProject.SRC_EXT);
      }

      if (mArgs.hasValue("resolutions")) {
        mResolutions = mArgs.getFloats("resolutions");
      }

      buildAtlas();
    } catch (CmdLineArgs.Exception e) {
      System.out.println(e.getMessage());
    } catch (Throwable e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  private void buildAtlas() throws IOException {

    if (mVerbose)
      pr("Building atlas: " + mBuildPath);

    TexProject tp = new TexProject(mBuildPath);

    Builder builder = new Builder();
    builder.setProject(tp);
    builder.setVerbose(mVerbose);
    builder.setSort(!mArgs.get("nosort"));
    builder.setPlotFrames(mArgs.get("frames"));
    if (mArgs.get("palette"))
      builder.includePalette();

    builder.gatherSprites();

    float[] res = mResolutions;

    for (int i = 0; i < res.length; i++) {
      float resolution = res[i];

      File f = tp.atlasFile();
      f = SprUtils.addResolutionSuffix(f, res, i);

      pr("building atlas [" + f + "]");

      Atlas at = builder.build(f, mAtlasSize, null, resolution);

      if (mArgs.get("write")) {
        at.debugWriteToPNG();
      }
    }
  }

  private static float[] sDefaultResolutions = { 1 };

  private IPoint mAtlasSize;
  private File mBuildPath;
  private boolean mVerbose;
  private float[] mResolutions = sDefaultResolutions;
  private CmdLineArgs mArgs;
}
