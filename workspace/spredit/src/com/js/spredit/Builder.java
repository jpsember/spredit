package com.js.spredit;

import images.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import tex.*;

import com.js.basic.Files;
import com.js.geometry.IPoint;
import com.js.geometry.IRect;
import com.js.geometry.MyMath;
import com.js.geometry.Point;
import com.js.geometry.Rect;

import static com.js.basic.Tools.*;

public class Builder {

  public void setProject(TexProject p) {
    this.project = p;
  }

  public Builder() {
  }

  public void setSort(boolean s) {
    sortBySize = s;
  }

  public void setPlotFrames(boolean p) {
    plotFrames = p;
  }

  private boolean sortBySize = true;

  /**
   * Find space for a rectangle within texture
   * 
   * @param used
   *          list of previous rectangles
   * @param pixSize
   *          size of this rectangle
   * @param texPageSize
   *          size of texture
   * @return rectangle, or null if no space found
   */
  private Rect findSpaceFor(Rect[] used, IPoint pixSize, IPoint texPageSize) {
    final boolean db = false;

    if (db)
      pr("findSpaceFor   size=" + pixSize);

    if (pixSize.x > texPageSize.x || pixSize.y > texPageSize.y)
      return null;

    Rect ri = new Rect(0, 0, pixSize.x, pixSize.y);

    float nextY = 0;

    while (true) {
      boolean collision = false;
      for (int i = 0; used[i] != null; i++) {
        Rect s = used[i];
        if (ri.intersects(s)) {
          collision = true;

          ri.x = s.endX();
          if (nextY <= ri.y || s.endY() < nextY)
            nextY = s.endY();

          if (db)
            pr("collided with " + s + ", x now " + ri.x + ", nextY " + nextY);
          break;
        }
      }
      if (!collision && ri.endX() > texPageSize.x)
        collision = true;

      if (!collision)
        break;

      if (ri.endX() > texPageSize.x) {
        ri.x = 0;
        ri.y = nextY;
        if (db)
          pr("ran out of room in x, y now " + ri.y);
        if (/* ri.y == 0 || */ri.endY() > texPageSize.y)
          return null;
      }
    }
    return ri;
  }

  /**
   * Add sprites from TexProject
   */
  public void gatherSprites() throws IOException {
    if (project == null)
      throw new IllegalStateException();

    if (db)
      pr("gatherSprites");

    TreeMap<String, SpriteInfo> map = SprUtils.readSprites(project);
    ArrayList<SpriteInfo> a = new ArrayList();
    a.addAll(map.values());

    // sprites = readSprites();
    if (db)
      pr("read " + a.size() + " sprites");

    for (SpriteInfo si : a) {
      String id = project.extractId(si.imagePath());
      addSprite(si, id);
    }
  }

  public void addSprite(SpriteInfo si, String id) {
    AEnt ent = new AEnt(si, id);
    ents.add(ent);
  }

  private static final String DBID = null; // "TUX";

  /**
   * Attempt to build an atlas containing the current sprites
   * 
   * @param f
   *          file to save atlas to
   * @param imageSize
   *          size of atlas image, or null to try increasingly larger sizes
   *          until they all fit
   * @param fontData
   *          if not null, ptr to array of fontAscent, fontDescent, fontLeading
   * @param resolution
   *          (1 is standard; else some value (0..1]; -1 if we're to ignore
   *          resolutions (also used for font builder to avoid throwing out
   *          prebuilt image)
   * @return atlas built, or null if not all sprites fit
   * @throws IOException
   */
  public Atlas build(File f, IPoint imageSize, int[] fontData, float resolution)
      throws IOException {
    final boolean db = false;

    if (db)
      pr("building atlas for "
          + ((project != null) ? project.toString() : "arbitrary sprites"));

    if (incPalette) {
      BufferedImage palImg = Palette.build(1);
      SpriteInfo palInfo = new SpriteInfo("_PALETTE", ImgUtil.bounds(palImg),
          new Point());
      palInfo.setSourceImage(palImg);
      addSprite(palInfo, palInfo.id());
    }

    // for (int i = 0; i < ents.size(); i++) {
    // AEnt ent = (AEnt) ents.get(i);
    // if (ent.id().startsWith("_"))
    // continue;
    // ent.setResolution(resolution);
    // }

    // sort sprites by size
    ArrayList<AEnt> sortedEnts = ents;
    if (sortBySize)
      sortedEnts = sortSprites(ents);

    boolean multiPasses = imageSize == null;
    if (multiPasses) {
      imageSize = new IPoint(64, 64);
    }
    if (db) {
      warning("jumping to biggest size");
      if (imageSize.x < 512 || imageSize.y < 512)
        imageSize = new IPoint(512, 512);
    }

    boolean allFit = false;

    // number of pixels of padding to allocate around each texture
    int padding = 1;

    while (true) {
      if (verbose || db)
        pr("attempting to fit atlas in image of size: " + imageSize);

      // now find space for them
      Rect[] used = new Rect[sortedEnts.size()];

      atlas = new Atlas(imageSize);
      if (fontData != null) {
        atlas.setFontAscent(fontData[0]);
        atlas.setFontDescent(fontData[1]);
        atlas.setFontLeading(fontData[2]);
      }
      allFit = true;

      int j = 0;
      for (int i = 0; i < sortedEnts.size(); i++) {
        AEnt ent = (AEnt) sortedEnts.get(i);
        // if (db)
        // pr("sprite #" + i + ": " + ent.id() + " size=" + ent.compImageSize()
        // + " comp=" + ent.si().compressionFactor());
        //
        // if (db && DBID != null && ent.id().equals(DBID)) {
        // SpriteInfo si = ent.si();
        // pr(" clip=" + si.cropRect() + "\n   cp=" + si.centerPoint());
        // pr(" compImageSize=" + ent.compImageSize() + "\n comp cp="
        // + ent.compCP());
        // }

        // find next available slot
        {
          IPoint paddedSize = ent.si().cropRect().size();
          // new IPoint(ent.compImageSize());
          paddedSize.x += padding * 2;
          paddedSize.y += padding * 2;

          Rect dest = findSpaceFor(used, paddedSize, imageSize); // atlas.imageSize());
          if (db)
            pr(" space at " + dest);
          if (dest != null) {
            ent.setLocation(new IPoint(dest.x + padding, dest.y + padding)); // dest.bottomLeft());
            used[j++] = dest;
          } else {
            if (verbose)
              pr("*** No room for: " + ent.si());
            allFit = false;
            break;
          }
        }
      }
      if (allFit || !multiPasses)
        break;

      if (false) {
        warning("keeping textures square");
        if (imageSize.x == 1024)
          break;
        imageSize.x *= 2;
        imageSize.y *= 2;
      } else {
        if (imageSize.x > imageSize.y) {
          int tmp = imageSize.y;
          imageSize.y = imageSize.x;
          imageSize.x = tmp;
        } else {
          if (imageSize.x == 1024)
            break;
          imageSize.x *= 2;
        }
      }
    }

    Atlas ret = null;

    if (allFit) {

      createPage(imageSize);

      for (int i = 0; i < ents.size(); i++) {
        AEnt ent = (AEnt) ents.get(i);
        IPoint dest = ent.loc();
        if (dest == null)
          continue;

        plotSpriteIntoAtlas(ent.si(), dest, padding);
      }

      if (db)
        pr("writing atlas to " + f);

      if (verbose)
        pr("Writing atlas " + f.getName());

      atlas.write(f);

      if (false) {
        File pngPath = Files.setExtension(f, "png");
        warning("writing atlas to " + pngPath);
        ImgUtil.writePNG(atlas.image(), pngPath);
      }

      graf.dispose();
      graf = null;

      ret = atlas;
    }
    return ret;
  }

  private int debNumber;

  /**
   * Plot sprite into atlas
   * 
   * @param si
   *          SpriteInfo
   * @param bottomLeft
   *          location of bottom left pixel within atlas
   */
  private void plotSpriteIntoAtlas(SpriteInfo si, IPoint bottomLeft, int padding) {
    final boolean db = (DBID != null) && si.id().equals(DBID);

    // Sprite srcSprite = si.sprite();

    // construct a new sprite record, one representing its
    // appearance in the atlas
    Sprite destSprite = new Sprite(si.id());
    {
      unimp("use floating point Rect here");
      IRect destClip = new IRect(si.cropRect());
      destClip.translate((int) -si.centerpoint().x, (int) -si.centerpoint().y);
      destSprite.setBounds(destClip);
      // destSprite.setCompression(srcSprite.compressionFactor());
    }
    if (db)
      pr("plotSpriteIntoAtlas " + si.id() + " bottomLeft=" + bottomLeft
          + " padding=" + padding);

    // calculate location of centerpoint in atlas

    Point cpComp = new Point(si.centerpoint());
    destSprite.setTranslate(new IPoint(bottomLeft.x + cpComp.x, bottomLeft.y
        + cpComp.y));

    if (db)
      pr(" destSprite.bounds=" + destSprite.bounds() + "\n .translate="
          + destSprite.translate());

    atlas.addSprite(destSprite);

    BufferedImage imgComp = si.getCompiledImage();

    // calculate bounding rectangle within atlas, with AWT y-axis
    IRect destRect = new IRect(bottomLeft.x, bottomLeft.y, imgComp.getWidth(),
        imgComp.getHeight());
    if (db)
      pr(" destRect, before flip=" + destRect);
    destRect.y = SprTools.flipYAxis(atlas.imageSize().y, destRect);
    if (db)
      pr(" atlas.imageSize=" + atlas.imageSize() + "\n destRect, after  flip="
          + destRect);

    if (plotFrames) {
      debNumber++;
      graf.setColor((debNumber % 2 == 1) ? Color.yellow : Color.blue);
      graf.drawRect(destRect.x, destRect.y, destRect.width - 1,
          destRect.height - 1);
    }
    graf.drawImage(imgComp, destRect.x, destRect.y, null);
    // plot padding pixels if necessary
    if (padding > 0) {

      BufferedImage dimg = atlas.image();
      for (int x = (int) (destRect.x - padding); x < (int) (destRect.endX() + padding); x++) {
        for (int i = 1; i <= padding; i++) {
          plotPaddingPixel(dimg, x, (int) (destRect.y - i), destRect);
          plotPaddingPixel(dimg, x, (int) (destRect.endY() - 1 + i), destRect);
        }
      }

      for (int y = (int) destRect.y; y < (int) destRect.endY(); y++) {
        for (int i = 1; i <= padding; i++) {
          plotPaddingPixel(dimg, (int) (destRect.x - i), y, destRect);
          plotPaddingPixel(dimg, (int) (destRect.endX() - 1 + i), y, destRect);
        }
      }
    }

  }

  private static void plotPaddingPixel(BufferedImage img, int x, int y,
      IRect bounds) {
    int srcx = MyMath.clamp(x, bounds.x, bounds.endX() - 1);
    int srcy = MyMath.clamp(y, bounds.y, bounds.endY() - 1);
    int pixel = img.getRGB(srcx, srcy);
    img.setRGB(x, y, pixel);
  }

  private ArrayList<AEnt> sortSprites(ArrayList<AEnt> ae) {
    ArrayList<AEnt> s = new ArrayList();
    s.addAll(ae);
    Collections.sort(s, new Comparator() {
      @Override
      public int compare(Object arg0, Object arg1) {
        AEnt s0 = (AEnt) arg0, s1 = (AEnt) arg1;

        IPoint r0 = s0.size(), r1 = s1.size();

        int size0 = r0.x * r0.y;
        int size1 = r1.x * r1.y;
        return size1 - size0;
      }
    });
    return s;
  }

  private void createPage(IPoint texPageSize) {
    // IPoint texPageSize = BuildParms.texSize();
    atlas.setImage(new BufferedImage(texPageSize.x, texPageSize.y,
        BufferedImage.TYPE_INT_ARGB));
    graf = atlas.image().createGraphics();
    // graf.setBackground(Color.red);
    // graf.clearRect(0,0,texPageSize.x,texPageSize.y);

    if (false) {
      warning("experimenting with fonts");

      if (false) {
        String[] fn = getFontNames();

        for (int i = 0; i < fn.length; i++)
          System.out.println(fn[i]);
      }

      Font font = new Font("Monaco", Font.PLAIN, 18);
      // pr("font="+font);
      graf.setFont(font);
      graf.setColor(Color.WHITE);
      // graf.fillRect(20,20,100,700);
      graf.drawString("Quick brown foxy girl (floating) [/]", 20,
          texPageSize.y / 4);
    }

  }

  public static String[] getFontNames() {
    // Get the local graphics environment
    GraphicsEnvironment ge;
    ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

    // Get the font names from the graphics environment
    String[] fontNames = ge.getAvailableFontFamilyNames();
    return fontNames;
  }

  public void setVerbose(boolean f) {
    verbose = f;
  }

  public void includePalette() {
    incPalette = true;
  }

  private Graphics2D graf;
  private Atlas atlas;
  private TexProject project;
  private boolean verbose;
  private ArrayList<AEnt> ents = new ArrayList();
  private boolean plotFrames;
  private boolean incPalette;
}
