package com.js.spredit;

import images.*;

import java.awt.Dimension;
import java.awt.image.*;
import java.io.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.js.myopengl.*;
import apputil.*;
import tex.*;

import com.js.basic.Streams;
import com.js.geometry.IPoint;
import com.js.geometry.IRect;
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
      pr("SpriteInfo.readMeta " + mMetaPath);

    try {
      mLastFileContents = Streams.readTextFile(mMetaPath.toString());
      JSONObject map = new JSONObject(mLastFileContents);
      setCenterPoint(IPoint.parseJSON(map.getJSONArray("CP")));
      setCropRect(IRect.parseJSON(map.getJSONArray("CLIP")));
      mWorkImageSize = IPoint.parseJSON(map.getJSONArray("SIZE"));
      mSprite.setCompression((float) map.optDouble("COMPRESS", 1));
      // don't call setScaleFactor(), since it will modify the clip, cp
      // values
      mScaleFactor = (float) map.optDouble("SCALE", 1);
      String alias = map.optString("ALIAS");
      if (alias != null) {
        mAliasFileRead = new RelPath(mProject.baseDirectory(), alias).file();
      }
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
    this.mSprite = new Sprite(id);
    this.setCenterPoint(new IPoint(centerPoint));
    this.setCropRect(new IRect(clip));
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

    this.mProject = project;

    String sprId = project.extractId(path);

    if (db)
      pr(" id=" + sprId);

    this.mSprite = new Sprite(sprId);

    if (META_FILES_ONLY.accept(path)) {
      if (db)
        pr(" meta file;");

      this.mMetaPath = path;
      readMeta();
    } else {
      this.mImgPath = path;
      this.mMetaPath = createMetaPath();

      if (db)
        pr(" img file; metaPath=" + mMetaPath);

      if (mMetaPath.exists()) {
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

    this.mProject = orig.mProject;
    this.mMetaPath = aliasMetaPath;

    String sprId = mProject.extractId(aliasMetaPath);
    this.mSprite = new Sprite(orig.mSprite);
    this.mSprite.setId(sprId);

    this.mCenterpoint = new IPoint(orig.mCenterpoint);
    this.mCropRect = new IRect(orig.mCropRect);

    this.mScaleFactor = orig.mScaleFactor;

    this.setAliasSprite(orig);

    // initialize imageSize by getting the image
    this.workImage();
  }

  public File metaPath() {
    return mMetaPath;
  }

  public void flush() {
    JSONObject map = new JSONObject();
    try {
      if (isAlias()) {
        map.put("ALIAS",
            new RelPath(mProject.baseDirectory(), imagePath()).toString());
      }
      map.put("SIZE", mWorkImageSize.toJSON());
      map.put("CP", centerPoint().toJSON());
      map.put("CLIP", cropRect().toJSON());

      if (mSprite.compressionFactor() != 1)
        map.put("COMPRESS", mSprite.compressionFactor());
      if (mScaleFactor != 1)
        map.put("SCALE", mScaleFactor);
    } catch (JSONException e) {
      AppTools.showError("encoding SpriteInfo", e);
    }

    String str = map.toString();
    if (!str.equals(mLastFileContents)) {
      try {
        Streams.writeTextFile(mMetaPath, str);
        mLastFileContents = str;
      } catch (IOException e) {
        AppTools.showError("writing SpriteInfo", e);
      }
    }
  }

  public void setCenterPoint(IPoint cp) {
    mCenterpoint = new IPoint(cp);
    // rebuild compressed image
    setImg(IMG_COMPRESSED, null);
  }

  public void setCompressionFactor(float shrink) {
    // unimp("append subfolders to id shown in imgDirectory");

    float prev = mSprite.compressionFactor();
    if (prev != shrink) {
      mSprite.setCompression(shrink);
      // rebuild compressed image
      setImg(IMG_COMPRESSED, null);

    }
  }

  /**
   * Get centerpoint
   * 
   * @return centerpoint
   */
  public IPoint centerPoint() {
    return mCenterpoint;
  }

  /**
   * Get cropping rectangle to be applied to work image
   * 
   * @return cropping rectangle
   */
  public IRect cropRect() {
    return mCropRect;
  }

  /**
   * Set cropping rectangle
   * 
   * @param r
   */
  public void setCropRect(IRect r) {
    mCropRect = new IRect(r);
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

    IRect bounds = new IRect(new Rect(0, 0, mWorkImageSize.x, mWorkImageSize.y));
    if (!bounds.contains(mCropRect)) {
      if (db)
        pr("verifyMetaData, crop rectangle fails: \n" + mCropRect + "\n "
            + bounds);
      // else
      // warn("*** bad crop rectangle, "+sprite.id()+"\n clip:  " + cropRect +
      // "\n bounds:"
      // + bounds);

      if (resetIfProblem) {
        resetClip();
        resetCenterPoint();
      } else {
        int cx = MyMath.clamp(mCropRect.x, 0, mWorkImageSize.x - 1);
        int cy = MyMath.clamp(mCropRect.y, 0, mWorkImageSize.y - 1);
        int cx2 = MyMath.clamp(mCropRect.endX(), cx + 1, mWorkImageSize.x);
        int cy2 = MyMath.clamp(mCropRect.endY(), cy + 1, mWorkImageSize.y);
        IRect cr = new IRect(cx, cy, cx2 - cx, cy2 - cy);
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
    setCenterPoint(new IPoint(mCropRect.midPoint()));
  }

  public void resetClip() {
    final boolean db = this.id().equals("BOOT");

    // setImg(IMG_WORK,
    // workImage = null;

    BufferedImage img = workImage();

    setCropRect(new IRect(0, 0, mWorkImageSize.x, mWorkImageSize.y));

    if (db)
      pr("resetClip to image size=" + this.mCropRect);

    IRect ub = new IRect(ImgUtil.calcUsedBounds(img, 0));

    if (db)
      pr(" calcUsedBounds= " + ub);

    ub.y = SprTools.flipYAxis((int) mWorkImageSize.y, ub);

    if (db)
      pr(" flipped y axis= " + ub);

    setCropRect(ub);
    resetCenterPoint();
  }

  private static final String META_SPRITE_EXT = "spi", THUMB_EXT = "png";
  public static MyFileFilter META_FILES_ONLY = new MyFileFilter(
      "Sprite Meta files", META_SPRITE_EXT, false, null);

  private File createMetaPath() throws IOException {

    File metaDir = new File(mImgPath.getParentFile(), META_DIR_NAME);

    if (!metaDir.exists()) {
      if (!metaDir.mkdir())
        throw new IOException("unable to create meta directory");
    }
    String imgName = Streams.removeExt(mImgPath.getName());
    return new File(metaDir, Streams.addExtension(imgName, META_SPRITE_EXT));
  }

  public Sprite sprite() {
    return mSprite;
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
    mImageLocked = true;
    /*
     * setImg(IMG_COMPRESSED, img); compCP = centerPoint; ASSERT(compCP !=
     * null); imageLocked = true;
     */
  }

  public void setCompressedImage(BufferedImage img, Point cp) {
    mPreCompressedImg = img;
    mPreCompressedCenterpoint = cp;
  }

  private BufferedImage getSourceImage() {
    final boolean db = false;

    if (img(IMG_SOURCE) == null) {

      if (aliasSprite != null) {
        setImg(IMG_SOURCE, aliasSprite.getSourceImage());
      } else
        try {
          if (db)
            pr("getImage for " + this + ", reading from " + mImgPath);

          setImg(IMG_SOURCE, ImgUtil.read(mImgPath));

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
        pr("getImage for " + this + "\n  scaleFactor=" + d(mScaleFactor)
            + " compress=" + d(compressionFactor()));

      getSourceImage();
      if (img(IMG_SOURCE) == null)
        break;

      if (mScaleFactor != 1) {
        setImg(IMG_WORK, ImgEffects.scale(img(IMG_SOURCE), mScaleFactor));
        if (db)
          pr(" scaled by " + mScaleFactor + " to " + img(IMG_WORK));
      } else {
        setImg(IMG_WORK, img(IMG_SOURCE));
      }
      mWorkImageSize = new IPoint(ImgUtil.size(img(IMG_WORK)));

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
      if (mPreCompressedImg != null) {
        img = mPreCompressedImg;
        mCompressedCenterpoint = mPreCompressedCenterpoint;
        break;
      }

      // get origin-oriented version of work image:
      // one whose clip rectangle position is at the origin.

      img = workImage();
      img = SprTools.subImage(img, mCropRect);
      Rect wRect = new Rect(0, 0, mCropRect.width, mCropRect.height);
      Point wcp = new Point(mCenterpoint.x - mCropRect.x, mCenterpoint.y
          - mCropRect.y);

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
      mCompressedCenterpoint = new Point(-x1 - cxadj, -y1 - cyadj);

      img = ImgEffects.scaleToFitExact(img, new Dimension((int) (x2 - x1),
          (int) (y2 - y1)));

      setImg(IMG_COMPRESSED, img);

    } while (false);
    return img;
  }
  private BufferedImage img(int i) {
    return mImg[i];
  }

  private void setImg(int i, BufferedImage im) {
    mImg[i] = im;

    for (int j = i + 1; j < IMG_THUMBNAIL; j++)
      setImg(j, null);
    if (i == IMG_COMPRESSED && im == null)
      mImgTextureId = TextureLoader.deleteTexture(mImgTextureId);
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
              File thumbDir = new File(mProject.baseDirectory(),
                  TexProject.THUMB_DIR);

              if (!thumbDir.exists()) {
                if (!thumbDir.mkdir())
                  throw new IOException("unable to create meta directory");
              }

              thumbPath = new File(thumbDir, Streams.addExtension(mSprite.id(),
                  THUMB_EXT));
            }

            if (db)
              pr(mSprite.id() + " determine if " + thumbPath + " exists");

            // if disk thumb version exists, and is not older than disk
            // original,
            // use it
            if (thumbPath.exists()
                && (thumbPath.lastModified() < mImgPath.lastModified() || (SIMMOD && thumbPath
                    .lastModified() < System.currentTimeMillis() - 10000))) {
              thumbPath.delete();
            }
            if (!thumbPath.exists()) {
              if (db)
                pr(mSprite.id() + " thumbthread: determine if " + thumbPath
                    + " exists");

              BufferedImage img = getSourceImage();

              if (img.getWidth() > THUMB_SIZE || img.getHeight() > THUMB_SIZE)
                setImg(IMG_THUMBNAIL, ImgEffects.scaleToFit(img, new Dimension(
                    THUMB_SIZE, THUMB_SIZE)));
              else
                setImg(IMG_THUMBNAIL, img);

              if (db)
                pr(mSprite.id() + " thumbthread: writing thumbnail to "
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
      if (mImageLocked)
        continue;
      setImg(i, null);
    }
  }

  public String toString() {
    return mSprite.id();
  }

  /**
   * Get size of work image (before any cropping applied)
   * 
   * @return size
   */
  public IPoint workImageSize() {
    return mWorkImageSize;
  }

  public File imagePath() {
    if (aliasSprite != null)
      return aliasSprite.imagePath();
    else if (mAliasFileRead != null)
      return mAliasFileRead;
    else
      return mImgPath;
  }

  public float scaleFactor() {
    return mScaleFactor;
  }

  public float compressionFactor() {
    return mSprite.compressionFactor();
  }

  public void setScaleFactor(float f) {
    final boolean db = false;

    if (f != mScaleFactor) {
      float sRel = f / mScaleFactor;
      mCropRect.scale(sRel);
      mCropRect.snapToGrid(1);

      mCenterpoint.applyScale(sRel);
      mScaleFactor = f;

      // reconstruct work image using new scale factor
      setImg(IMG_WORK, null);

      if (db)
        pr(mSprite.id() + " setScaleFactor to " + d(f) + "\n cropRect now "
            + mCropRect + " workImage.size " + workImageSize());

      // make sure crop rectangle remains legal
      verifyMetaData(false);
    }
  }
  public File getAliasTag() {
    return mAliasFileRead;
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
    mAliasFileRead = null;
    releaseImage();
  }

  public String id() {
    return mSprite.id();
  }

  public void plotTexture(Point location, SpritePanel panel) {
    final boolean db = false;

    if (mImgTextureId == 0 && img(IMG_WORK) != null) {
      BufferedImage img = compressedImage();
      mImgTextureId = TextureLoader.getTexture(panel.glContext(), img,
          mImgTextureSize);
    }

    if (mImgTextureId == 0)
      return;

    // construct sprite for plotting.
    // Its clip bounds is that of a centerpoint-oriented work image,
    // and its translation if for the compressed image's texture
    Sprite s = new Sprite(mSprite.id());
    s.setBounds(new IRect(mCropRect.x - mCenterpoint.x, mCropRect.y
        - mCenterpoint.y, mCropRect.width, mCropRect.height));
    s.setTranslate(new IPoint(compressedCenterPoint()));
    s.setCompression(mSprite.compressionFactor());
    if (db)
      pr("plotTexture " + this + " clip=" + this.mCropRect + " cp="
          + this.mCenterpoint + " compcp=" + compressedCenterPoint());

    panel.plotSprite(mImgTextureId, mImgTextureSize, s, location.x, location.y);
  }

  /**
   * Get centerpoint of compressed image
   * 
   * @return centerpoint of origin-oriented compressed image
   */
  public Point compressedCenterPoint() {
    compressedImage();
    return mCompressedCenterpoint;
  }

  private IPoint mImgTextureSize = new IPoint();
  private int mImgTextureId;

  private TexProject mProject;
  // value of ALIAS tag read from meta file
  private File mAliasFileRead;
  // cached meta file contents, to see if new contents are different
  private String mLastFileContents;
  private File mMetaPath;
  private File mImgPath;
  private Sprite mSprite;

  // centerpoint of origin-oriented compressed image
  private Point mCompressedCenterpoint;

  private float mScaleFactor = 1;

  // size of scaled image (BEFORE compressing)
  private IPoint mWorkImageSize;

  private IPoint mCenterpoint = new IPoint();
  private IRect mCropRect = new IRect();

  private BufferedImage[] mImg = new BufferedImage[IMG_TOTAL];
  private boolean mImageLocked;
  private BufferedImage mPreCompressedImg;
  private Point mPreCompressedCenterpoint;
}
