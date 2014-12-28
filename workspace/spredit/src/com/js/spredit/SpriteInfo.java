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
import com.js.geometry.*;

import static com.js.basic.Tools.*;

public class SpriteInfo {

  public static final int IMG_SOURCE = 0, IMG_THUMBNAIL = 1,
      IMG_UNUSEDWORK = 2, IMG_COMPILED = 3, IMG_TOTAL = 4;

  public static final int THUMB_SIZE = 80;

  private static final String META_DIR_NAME = "_1";

  private void readMeta() {
    final boolean db = true;

    if (db)
      pr("SpriteInfo.readMeta " + mMetaPath);

    try {
      mLastFileContents = Streams.readTextFile(mMetaPath.toString());
      JSONObject map = new JSONObject(mLastFileContents);
      setCenterPoint(IPoint.parseJSON(map.getJSONArray("CP")));
      setCropRect(IRect.parseJSON(map.getJSONArray("CLIP")));
      mWorkImageSize = IPoint.parseJSON(map.getJSONArray("SIZE"));
      pr("crop " + mCropRect + "\n workImageSize " + mWorkImageSize
          + "\n centerpt " + mCenterpoint);

      // mSprite.setCompression((float) map.optDouble("COMPRESS", 1));
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
  public SpriteInfo(String id, IRect clip, Point centerPoint) {
    this.mSprite = new Sprite(id);
    this.setCenterPoint(new IPoint(centerPoint));
    this.setCropRect(clip);
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

    this.setAliasSprite(orig);

    // initialize imageSize by getting the image
    getSourceImage();
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

      // if (mSprite.compressionFactor() != 1)
      // map.put("COMPRESS", mSprite.compressionFactor());
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
  }

  public IPoint centerPoint() {
    return mCenterpoint;
  }

  public IRect cropRect() {
    return mCropRect;
  }

  public void setCropRect(IRect r) {
    if (!r.equals(mCropRect)) {
      setImg(IMG_COMPILED, null);
    }
    mCropRect = new IRect(r);
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
    if (getSourceImage() == null) {
      warning("can't verify meta data, no image for " + this);
      return;
    }

    IRect bounds = new IRect(new Rect(0, 0, mWorkImageSize.x, mWorkImageSize.y));
    if (!bounds.contains(mCropRect)) {

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
    BufferedImage img = getSourceImage();
    setCropRect(new IRect(0, 0, mWorkImageSize.x, mWorkImageSize.y));
    IRect ub = new IRect(ImgUtil.calcUsedBounds(img, 0));
    ub.y = SprTools.flipYAxis(mWorkImageSize.y, ub);
    setCropRect(ub);
    resetCenterPoint();
  }

  private static final String META_SPRITE_EXT = "spi";
  private static final String THUMB_EXT = "png";
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
   */
  public void setSourceImage(BufferedImage img) {
    setImg(IMG_SOURCE, img);
    mImageLocked = true;
  }

  public BufferedImage getSourceImage() {
    if (img(IMG_SOURCE) == null) {
      if (aliasSprite != null) {
        setImg(IMG_SOURCE, aliasSprite.getSourceImage());
      } else
        try {
          setImg(IMG_SOURCE, ImgUtil.read(mImgPath));
        } catch (IOException e) {
          AppTools.showError("reading image", e);
        }
    }
    return img(IMG_SOURCE);
  }

  // /**
  // * Get work image. This is the original source image after undergoing any
  // * scaling, but before any clipping or compression is applied.
  // *
  // * @return image
  // */
  // public BufferedImage workImage() {
  // unimp("work image is same as source image");
  // do {
  // if (img(IMG_WORK) != null)
  // break;
  // getSourceImage();
  // if (img(IMG_SOURCE) == null)
  // break;
  // setImg(IMG_WORK, img(IMG_SOURCE));
  // mWorkImageSize = new IPoint(ImgUtil.size(img(IMG_WORK)));
  // } while (false);
  // return img(IMG_WORK);
  // }

  /**
   * Get BufferedImage for sprite as it will appear in an atlas. This is the
   * work image after it has been cropped
   * 
   * @return BufferedImage
   */
  public BufferedImage getCompiledImage() {
    BufferedImage img = null;
    do {
      img = img(IMG_COMPILED);
      if (img != null)
        break;

      // get origin-oriented version of work image:
      // one whose clip rectangle position is at the origin.

      img = getSourceImage();
      img = SprTools.subImage(img, mCropRect);
      // IRect wRect = new IRect(0, 0, mCropRect.width, mCropRect.height);
      // Point wcp = new Point(mCenterpoint.x - mCropRect.x, mCenterpoint.y
      // - mCropRect.y);

      // // calculate centerpoint-oriented work rectangle
      // // after undergoing compression
      //
      // float c = compressionFactor();
      // float x1 = -wcp.x * c;
      // float y1 = -wcp.y * c;
      // float x2 = x1 + wRect.width * c;
      // float y2 = y1 + wRect.height * c;
      //
      // // expand sides so they are at pixel boundaries
      // float cxadj = x1 - floorf(x1);
      // float cyadj = y1 - floorf(y1);
      //
      // x1 -= cxadj;
      // y1 -= cyadj;
      // x2 = ceilf(x2);
      // y2 = ceilf(y2);
      //
      // // calculate centerpoint of origin-oriented compressed image
      // mCompressedCenterpoint = new Point(-x1 - cxadj, -y1 - cyadj);
      //
      // img = ImgEffects.scaleToFitExact(img, new Dimension((int) (x2 - x1),
      // (int) (y2 - y1)));

      setImg(IMG_COMPILED, img);
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
    if (i == IMG_COMPILED && im == null)
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

  // public float compressionFactor() {
  // return mSprite.compressionFactor();
  // }

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
    if (mImgTextureId == 0 && img(IMG_SOURCE) != null) {
      BufferedImage img = getCompiledImage();
      mImgTextureId = TextureLoader.getTexture(panel.glContext(), img,
          mImgTextureSize);
    }

    if (mImgTextureId == 0)
      return;

    // construct sprite for plotting.
    // Its clip bounds is that of a centerpoint-oriented work image,
    // and its translation if for the compressed image's texture
    Sprite s = new Sprite(mSprite.id());
    s.setBounds(mCropRect);
    // s.setTranslate(new IPoint(mCenterpoint));
    warning("make centerpoints floats again");
    //
    // s.setBounds(new IRect(mCropRect.x - mCenterpoint.x, mCropRect.y
    // - mCenterpoint.y, mCropRect.width, mCropRect.height));
    // s.setTranslate(new IPoint(compressedCenterPoint()));
    // s.setCompression(mSprite.compressionFactor());
    // if (db)
    // pr("plotTexture " + this + " clip=" + this.mCropRect + " cp="
    // + this.mCenterpoint + " compcp=" + compressedCenterPoint());

    panel.plotSprite(mImgTextureId, mImgTextureSize, s, location.x, location.y);
  }

  // /**
  // * Get centerpoint of compressed image
  // *
  // * @return centerpoint of origin-oriented compressed image
  // */
  // public Point compressedCenterPoint() {
  // getCompiledImage();
  // return mCompressedCenterpoint;
  // }

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

  // size of scaled image (BEFORE compressing)
  private IPoint mWorkImageSize;

  private IPoint mCenterpoint = new IPoint();
  private IRect mCropRect = new IRect();

  private BufferedImage[] mImg = new BufferedImage[IMG_TOTAL];
  private boolean mImageLocked;
  // private BufferedImage mPreCompressedImg;
  // private Point mPreCompressedCenterpoint;
}
