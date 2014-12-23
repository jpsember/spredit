package com.js.spredit;

import images.*;

import java.awt.Dimension;
import java.awt.image.*;
import java.io.*;
import myopengl.*;
import apputil.*;
import streams.*;
import tex.*;

import com.js.geometry.IPoint;
import com.js.geometry.MyMath;
import com.js.geometry.Point;
import com.js.geometry.Rect;

import static com.js.basic.Tools.*;
import static com.js.geometry.MyMath.*;

public class SpriteInfo {

  public static final int IMG_SOURCE = 0, IMG_WORK = 1, IMG_COMPRESSED = 2,
      IMG_THUMBNAIL = 3, IMG_TOTAL = 4;

  public static final int THUMB_SIZE = 80;

  private static final String META_DIR_NAME = "_1";

  private void readMeta() {
    final boolean db = false;

    if (db)
      pr("SpriteInfo.readMeta " + metaPath);

    try {
      lastFileContents = Streams.readTextFile(metaPath.toString());
      DefScanner s = new DefScanner(lastFileContents);
      while (!s.done()) {
        String idStr = s.nextDef();
        if (idStr.equals("CP")) {
          setCenterPoint(s.sFPt());
        } else if (idStr.equals("CLIP")) {
          setCropRect(s.sRect());
        } else if (idStr.equals("SIZE")) {
          workImageSize = s.sFPt(); // s.sIPt();
        } else if (idStr.equals("COMPRESS")) {
          sprite.setCompression(s.sFloat());
        } else if (idStr.equals("SCALE")) {
          // don't call setScaleFactor(), since it will modify the clip, cp
          // values
          scaleFactor = s.sFloat();
        } else if (idStr.equals("ALIAS")) {
          aliasFileRead = s.sPath(project.baseDirectory());
        } else {
          s.exception("unexpected token");
        }
      }

      // verifyMetaData(true);
      /*
       * if (imgPath != null) { // if image is newer than metadata, verify
       * dimensions, etc // warn("always verifying metaData"); if
       * (metaPath.lastModified() < imgPath.lastModified() ) { verifyMetaData();
       * } }
       */
    } catch (Throwable t) {
      warning("problem reading sprinfo: " + t);
      AppTools.showError("reading SpriteInfo", t);
      constructMetaData();
    }
  }

  /**
   * Constructor for item not associated with a project (used to construct
   * fonts)
   */
  public SpriteInfo(String id, Rect clip, Point centerPoint) {
    this.sprite = new Sprite(id);
    this.setCenterPoint(new Point(centerPoint));
    this.setCropRect(new Rect(clip));
  }

  /**
   * Constructor
   * 
   * @param project
   *          TexProject
   * @param path
   *          path of file; either an image (.png, .jpg) or a metainfo file
   *          (META_SPRITE_EXT)
   * @throws IOException
   */
  public SpriteInfo(TexProject project, File path) throws IOException {
    final boolean db = false;

    if (db)
      pr("SpriteInfo.construct path=" + path);

    this.project = project;

    String sprId = project.extractId(path);

    if (db)
      pr(" id=" + sprId);

    this.sprite = new Sprite(sprId);

    if (META_FILES_ONLY.accept(path)) {
      if (db)
        pr(" meta file;");

      this.metaPath = path;
      readMeta();
    } else {
      this.imgPath = path;
      this.metaPath = createMetaPath();

      if (db)
        pr(" img file; metaPath=" + metaPath);

      if (metaPath.exists()) {
        if (db)
          pr("  reading meta data");

        readMeta();
      } else {
        if (db)
          pr("  meta data doesn't exist, constructing");

        constructMetaData();
        flush();
      }
    }
  }

  public SpriteInfo createAlias(File aliasMetaPath) {
    return new SpriteInfo(this, aliasMetaPath);
  }

  /**
   * Construct as alias of another
   * 
   * @param orig
   *          original
   * @param aliasMetaPath
   *          path to save alias' meta information to
   */
  private SpriteInfo(SpriteInfo orig, File aliasMetaPath) {

    this.project = orig.project;
    this.metaPath = aliasMetaPath;

    String sprId = project.extractId(aliasMetaPath);
    this.sprite = new Sprite(orig.sprite);
    this.sprite.setId(sprId);

    this.centerPoint = new Point(orig.centerPoint);
    this.cropRect = new Rect(orig.cropRect);

    this.scaleFactor = orig.scaleFactor;

    this.setAliasSprite(orig);

    // initialize imageSize by getting the image
    this.workImage();
  }

  public File metaPath() {
    return metaPath;
  }

  public void flush() {
    final boolean db = false;

    DefBuilder sb = new DefBuilder();
    if (isAlias()) {
      sb.append("ALIAS");
      sb.append(new RelPath(project.baseDirectory(), imagePath()));
    }
    sb.append("SIZE");
    sb.append(workImageSize);
    sb.addCr();
    sb.append("CP");
    sb.append(centerPoint());
    sb.addCr();
    sb.append("CLIP");
    sb.append(cropRect());
    sb.addCr();
    if (sprite.compressionFactor() != 1) {
      sb.append("COMPRESS");
      sb.append(sprite.compressionFactor());
      sb.addCr();
    }
    if (scaleFactor != 1) {
      sb.append("SCALE");
      sb.append(scaleFactor);
      sb.addCr();
    }
    String str = sb.toString();
    if (!str.equals(lastFileContents)) {
      try {
        if (db)
          pr("writing new version of: " + metaPath);
        Streams.writeTextFile(metaPath, str);
        lastFileContents = str;
      } catch (IOException e) {
        AppTools.showError("writing SpriteInfo", e);
      }
    }
  }

  public void setCenterPoint(Point cp) {
    centerPoint = new Point(cp);
    // rebuild compressed image
    setImg(IMG_COMPRESSED, null);
  }

  // public void setCenterPoint(IPoint2 cp) {
  // setCenterPoint(new FlPoint2(cp));
  // }
  public void setCompressionFactor(float shrink) {
    // unimp("append subfolders to id shown in imgDirectory");

    float prev = sprite.compressionFactor();
    if (prev != shrink) {
      sprite.setCompression(shrink);
      // rebuild compressed image
      setImg(IMG_COMPRESSED, null);

    }
  }

  /**
   * Get centerpoint
   * 
   * @return centerpoint
   */
  public Point centerPoint() {
    return centerPoint;
  }

  /**
   * Get cropping rectangle to be applied to work image
   * 
   * @return cropping rectangle
   */
  public Rect cropRect() {
    return cropRect;
  }

  /**
   * Set cropping rectangle
   * 
   * @param r
   */
  public void setCropRect(Rect r) {
    cropRect = new Rect(r);
    // force rebuild of compressed image
    setImg(IMG_COMPRESSED, null);
  }


  /**
   * Verify that the meta data is valid; specifically, that the crop rectangle
   * is within the image bounds
   * 
   * @param resetIfProblem
   *          if true, and a problem is encountered, resets both crop and
   *          centerpoint to default; otherwise, just fixes crop so it's legal
   */
  public void verifyMetaData(boolean resetIfProblem) {
    final boolean db = false;

    if (workImage() == null) {
      warning("can't verify meta data, no image for " + this);
      return;
    }

    Rect bounds = new Rect(0, 0, workImageSize.x, workImageSize.y);
    if (!bounds.contains(cropRect)) {
      if (db)
        pr("verifyMetaData, crop rectangle fails: \n" + cropRect + "\n "
            + bounds);
      // else
      // warn("*** bad crop rectangle, "+sprite.id()+"\n clip:  " + cropRect +
      // "\n bounds:"
      // + bounds);

      if (resetIfProblem) {
        resetClip();
        resetCenterPoint();
      } else {
        float cx = MyMath.clamp(cropRect.x, 0, workImageSize.x - 1);
        float cy = MyMath.clamp(cropRect.y, 0, workImageSize.y - 1);
        float cx2 = MyMath.clamp(cropRect.endX(), cx + 1, workImageSize.x);
        float cy2 = MyMath.clamp(cropRect.endY(), cy + 1, workImageSize.y);
        Rect cr = new Rect(cx, cy, cx2 - cx, cy2 - cy);
        setCropRect(cr);
        if (db)
          pr("set crop rect to " + cr);
      }
    }
  }

  private void constructMetaData() {
    resetClip();
    resetCenterPoint();
  }

  public void resetCenterPoint() {
    setCenterPoint(cropRect.midPoint()); // new FlPoint2(cropRect.midX(),
                                         // cropRect.midY()));
  }

  public void resetClip() {
    final boolean db = this.id().equals("BOOT");

    // setImg(IMG_WORK,
    // workImage = null;

    BufferedImage img = workImage();

    setCropRect(new Rect(0, 0, workImageSize.x, workImageSize.y));

    if (db)
      pr("resetClip to image size=" + this.cropRect);

    Rect ub = new Rect(ImgUtil.calcUsedBounds(img, 0));

    if (db)
      pr(" calcUsedBounds= " + ub);

    ub.y = SprTools.flipYAxis(workImageSize.y, ub);

    if (db)
      pr(" flipped y axis= " + ub);

    setCropRect(ub);
    resetCenterPoint();
  }

  private static final String META_SPRITE_EXT = "spi", THUMB_EXT = "png";
  public static MyFileFilter META_FILES_ONLY = new MyFileFilter(
      "Sprite Meta files", META_SPRITE_EXT, false, null);

  private File createMetaPath() throws IOException {

    File metaDir = new File(imgPath.getParentFile(), META_DIR_NAME);

    if (!metaDir.exists()) {
      if (!metaDir.mkdir())
        throw new IOException("unable to create meta directory");
    }
    String imgName = Streams.removeExt(imgPath.getName());
    return new File(metaDir, Streams.addExtension(imgName, META_SPRITE_EXT));
  }

  public Sprite sprite() {
    return sprite;
  }

  /**
   * Set source image. Used for constructing fonts (and palettes), where images
   * are not loaded from files.
   * 
   * @param img
   */
  public void setSourceImage(BufferedImage img) {
    // unimp("pass in prescaled version from smaller font size");
    setImg(IMG_SOURCE, img);
    imageLocked = true;
    /*
     * setImg(IMG_COMPRESSED, img); compCP = centerPoint; ASSERT(compCP !=
     * null); imageLocked = true;
     */
  }

  public void setCompressedImage(BufferedImage img, Point cp) {
    preCompressedImg = img;
    preCompressedCP = cp;
  }

  private BufferedImage getSourceImage() {
    final boolean db = false;

    if (img(IMG_SOURCE) == null) {

      if (aliasSprite != null) {
        setImg(IMG_SOURCE, aliasSprite.getSourceImage());
      } else
        try {
          if (db)
            pr("getImage for " + this + ", reading from " + imgPath);

          setImg(IMG_SOURCE, ImgUtil.read(imgPath));

        } catch (IOException e) {
          AppTools.showError("reading image", e);
        }
    }
    return img(IMG_SOURCE);
  }

  /**
   * Get work image. This is the original source image after undergoing any
   * scaling, but before any clipping or compression is applied.
   * 
   * @return image
   */
  public BufferedImage workImage() {
    final boolean db = false;
    do {
      if (img(IMG_WORK) != null)
        break;

      if (db)
        pr("getImage for " + this + "\n  scaleFactor=" + d(scaleFactor)
            + " compress=" + d(compressionFactor()));

      getSourceImage();
      if (img(IMG_SOURCE) == null)
        break;

      if (scaleFactor != 1) {
        setImg(IMG_WORK, ImgEffects.scale(img(IMG_SOURCE), scaleFactor));
        if (db)
          pr(" scaled by " + scaleFactor + " to " + img(IMG_WORK));
      } else {
        setImg(IMG_WORK, img(IMG_SOURCE));
      }
      workImageSize = new Point(ImgUtil.size(img(IMG_WORK)));

      // verifyMetaData();
    } while (false);
    return img(IMG_WORK);
  }

  /**
   * Get BufferedImage for sprite as it will appear in an atlas. This is work
   * image after it has been cropped and scaled by the compression factor.
   * 
   * @return BufferedImage
   */
  public BufferedImage compressedImage() {
    BufferedImage img = null;
    do {
      img = img(IMG_COMPRESSED);
      if (img != null)
        break;

      // if a pre-compressed image is available, use it instead
      if (preCompressedImg != null) {
        img = preCompressedImg;
        compCP = preCompressedCP;
        break;
      }

      // get origin-oriented version of work image:
      // one whose clip rectangle position is at the origin.

      img = workImage();
      img = SprTools.subImage(img, cropRect);
      Rect wRect = new Rect(0, 0, cropRect.width, cropRect.height);
      Point wcp = new Point(centerPoint.x - cropRect.x, centerPoint.y
          - cropRect.y);

      // calculate centerpoint-oriented work rectangle
      // after undergoing compression

      float c = compressionFactor();
      float x1 = -wcp.x * c;
      float y1 = -wcp.y * c;
      float x2 = x1 + wRect.width * c;
      float y2 = y1 + wRect.height * c;

      // expand sides so they are at pixel boundaries
      float cxadj = x1 - floorf(x1);
      float cyadj = y1 - floorf(y1);

      x1 -= cxadj;
      y1 -= cyadj;
      x2 = ceilf(x2);
      y2 = ceilf(y2);

      // calculate centerpoint of origin-oriented compressed image
      compCP = new Point(-x1 - cxadj, -y1 - cyadj);

      img = ImgEffects.scaleToFitExact(img, new Dimension((int) (x2 - x1),
          (int) (y2 - y1)));

      setImg(IMG_COMPRESSED, img);

    } while (false);
    return img;
  }

  private IPoint imgTextureSize = new IPoint();
  private int imgTextureId;

  private BufferedImage img(int i) {
    return img[i];
  }

  private void setImg(int i, BufferedImage im) {
    img[i] = im;

    for (int j = i + 1; j < IMG_THUMBNAIL; j++)
      setImg(j, null);
    if (i == IMG_COMPRESSED && im == null)
      imgTextureId = TextureLoader.deleteTexture(imgTextureId);
  }

  public BufferedImage thumbnailImage() {
    final boolean db = false;
    try {
      if (img(IMG_THUMBNAIL) == null) {
        if (aliasSprite != null) {
          setImg(IMG_THUMBNAIL, aliasSprite.thumbnailImage());
        } else {
          final boolean SIMMOD = false;
          if (SIMMOD)
            warning("simmod = true");

          // if disk thumb version exists, and is older than original,
          // delete it
          {
            File thumbPath;

            {
              File thumbDir = new File(project.baseDirectory(),
                  TexProject.THUMB_DIR);

              if (!thumbDir.exists()) {
                if (!thumbDir.mkdir())
                  throw new IOException("unable to create meta directory");
              }

              thumbPath = new File(thumbDir, Streams.addExtension(sprite.id(),
                  THUMB_EXT));
            }

            if (db)
              pr(sprite.id() + " determine if " + thumbPath + " exists");

            // if disk thumb version exists, and is not older than disk
            // original,
            // use it
            if (thumbPath.exists()
                && (thumbPath.lastModified() < imgPath.lastModified() || (SIMMOD && thumbPath
                    .lastModified() < System.currentTimeMillis() - 10000))) {
              thumbPath.delete();
            }
            if (!thumbPath.exists()) {
              if (db)
                pr(sprite.id() + " thumbthread: determine if " + thumbPath
                    + " exists");

              BufferedImage img = getSourceImage();

              if (img.getWidth() > THUMB_SIZE || img.getHeight() > THUMB_SIZE)
                setImg(IMG_THUMBNAIL, ImgEffects.scaleToFit(img, new Dimension(
                    THUMB_SIZE, THUMB_SIZE)));
              else
                setImg(IMG_THUMBNAIL, img);

              if (db)
                pr(sprite.id() + " thumbthread: writing thumbnail to "
                    + thumbPath);

              ImgUtil.writePNG(img(IMG_THUMBNAIL), thumbPath);
            }
            if (img(IMG_THUMBNAIL) == null) {
              setImg(IMG_THUMBNAIL, ImgUtil.read(thumbPath));
            }
          }
        }
      }
    } catch (Throwable t) {
      AppTools.showError("getting thumbnail", t);
    }
    return img(IMG_THUMBNAIL);
  }

  public void releaseImage() {
    for (int i = 0; i < IMG_TOTAL; i++) {
      // don't throw out locked image (necessary for building fonts)
      if (imageLocked)
        continue;
      setImg(i, null);
    }
  }

  public String toString() {
    return sprite.id();
  }

  /**
   * Get size of work image (before any cropping applied)
   * 
   * @return size
   */
  public Point workImageSize() {
    return workImageSize;
  }

  public File imagePath() {
    if (aliasSprite != null)
      return aliasSprite.imagePath();
    else if (aliasFileRead != null)
      return aliasFileRead;
    else
      return imgPath;
  }

  public float scaleFactor() {
    return scaleFactor;
  }

  public float compressionFactor() {
    return sprite.compressionFactor();
  }

  public void setScaleFactor(float f) {
    final boolean db = false;

    if (f != scaleFactor) {
      float sRel = f / scaleFactor;
      cropRect.scale(sRel);
      cropRect.snapToGrid(1);

      centerPoint.applyScale(sRel);
      scaleFactor = f;

      // reconstruct work image using new scale factor
      setImg(IMG_WORK, null);

      if (db)
        pr(sprite.id() + " setScaleFactor to " + d(f) + "\n cropRect now "
            + cropRect + " workImage.size " + workImageSize());

      // make sure crop rectangle remains legal
      verifyMetaData(false);
    }
  }

  private BufferedImage[] img = new BufferedImage[IMG_TOTAL];
  private boolean imageLocked;
  private BufferedImage preCompressedImg;
  private Point preCompressedCP;

  public File getAliasTag() {
    return aliasFileRead;
  }

  /**
   * @return
   */
  public boolean isAlias() {
    return aliasSprite != null;
  }

  private SpriteInfo aliasSprite;

  /**
   * Alias this sprite to another
   * 
   * @param si
   */
  public void setAliasSprite(SpriteInfo si) {
    aliasSprite = si;
    aliasFileRead = null;
    releaseImage();
  }

  public String id() {
    return sprite.id();
  }

  public void plotTexture(Point location, SpritePanel panel) {
    final boolean db = false;

    if (imgTextureId == 0 && img(IMG_WORK) != null) {
      BufferedImage img = compressedImage();
      imgTextureId = TextureLoader.getTexture(img, imgTextureSize);
    }

    if (imgTextureId == 0)
      return;

    // construct sprite for plotting.
    // Its clip bounds is that of a centerpoint-oriented work image,
    // and its translation if for the compressed image's texture
    Sprite s = new Sprite(sprite.id());
    s.setBounds(new Rect(cropRect.x - centerPoint.x,
        cropRect.y
        - centerPoint.y, cropRect.width, cropRect.height));
    s.setTranslate(compressedCenterPoint());
    s.setCompression(sprite.compressionFactor());
    if (db)
      pr("plotTexture " + this + " clip=" + this.cropRect + " cp="
          + this.centerPoint + " compcp=" + compressedCenterPoint());

    panel.plotSprite(imgTextureId, imgTextureSize, s, location.x, location.y);
  }

  /**
   * Get centerpoint of compressed image
   * 
   * @return centerpoint of origin-oriented compressed image
   */
  public Point compressedCenterPoint() {
    compressedImage();
    return compCP;
  }

  private TexProject project;
  // value of ALIAS tag read from meta file
  private File aliasFileRead;
  // cached meta file contents, to see if new contents are different
  private String lastFileContents;
  private File metaPath;
  private File imgPath;
  private Sprite sprite;

  // centerpoint of origin-oriented compressed image
  private Point compCP;

  private float scaleFactor = 1;

  // size of scaled image (BEFORE compressing)
  private Point workImageSize;

  private Point centerPoint = new Point();
  private Rect cropRect = new Rect();
}
