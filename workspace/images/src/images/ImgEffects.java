package images;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.*;
import java.awt.image.*;
import com.js.geometry.*;

public class ImgEffects {
  public static BufferedImage flipVert(BufferedImage src) {
    AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
    tx.translate(0, -src.getHeight(null));
    AffineTransformOp op = new AffineTransformOp(tx,
        AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    return op.filter(src, null);
  }

  public static BufferedImage adjustBrightness(BufferedImage src,
      double scaleFactor) {
    RescaleOp op = new RescaleOp((float) scaleFactor, 0, null);
    return op.filter(src, null);
  }

  public static BufferedImage sharpen(BufferedImage src) {
    Kernel kernel = new Kernel(3, 3, new float[] { -1, -1, -1, -1, 9, -1, -1,
        -1, -1 });
    BufferedImageOp op = new ConvolveOp(kernel);
    return op.filter(src, null);

  }

  public static BufferedImage blur(BufferedImage src) {
    Kernel kernel = new Kernel(3, 3, new float[] { 1f / 9f, 1f / 9f, 1f / 9f,
        1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f });

    BufferedImageOp op = new ConvolveOp(kernel);
    return op.filter(src, null);

  }

  public static BufferedImage emboss(BufferedImage src) {
    Kernel kernel = new Kernel(3, 3, new float[] { -2, 0, 0, 0, 1, 0, 0, 0, 2 });

    BufferedImageOp op = new ConvolveOp(kernel);
    return op.filter(src, null);
  }

  public static BufferedImage grayScale(BufferedImage src) {
    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    ColorConvertOp op = new ColorConvertOp(cs, null);
    return op.filter(src, null);
  }

  public static BufferedImage crop(BufferedImage src, int top, int right,
      int bottom, int left) {
    int w = src.getWidth(), h = src.getHeight();
    int x = (left * w) / 100;
    int y = (top * h) / 100;
    int x2 = w - (right * w) / 100;
    int y2 = h - (bottom * h) / 100;
    if (x2 <= x || y2 <= y)
      throw new IllegalArgumentException("bad crop args");
    if (x == 0 && y == 0 && x2 == w && y2 == h)
      return src;

    return src.getSubimage(x, y, x2 - x, y2 - y);
  }

  /**
   * Rotate an image
   * @param src
   * @param amt
   * @return
   */
  public static BufferedImage rotate(BufferedImage src, double amt) {
    AffineTransform tx = new AffineTransform();
    tx.rotate((float) amt, src.getWidth() / 2, src.getHeight() / 2);

    AffineTransformOp op = new AffineTransformOp(tx,
        AffineTransformOp.TYPE_BILINEAR);
    return op.filter(src, null);
  }
  /**
   * Convert an image to a standard type which we know we can manipulate.
   * 
   * @param src
   * @return
   */
  public static BufferedImage toStandardType(BufferedImage src) {
    BufferedImage dest = src;

    final int preferredType = BufferedImage.TYPE_INT_RGB;

    if (src.getType() != preferredType) {

      // Create an RGB buffered image
      BufferedImage bimage = new BufferedImage(src.getWidth(), src.getHeight(),
          preferredType);

      // Copy non-RGB image to the RGB buffered image
      Graphics2D g = bimage.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.dispose();
      dest = bimage;
    }
    return dest;
  }

  public static BufferedImage threshold(BufferedImage src) {

    short[] t = new short[256];
    for (int i = 0; i < 256; i++) {
      int k = i;
      if (i < 64)
        k = 0;
      else if (i > 192)
        k = 255;
      t[i] = (short) k;
    }

    short[][] t2 = new short[][] { t, t, t };

    BufferedImageOp thresholdOp = new LookupOp(new ShortLookupTable(0, t2),
        null);
    BufferedImage destination = thresholdOp.filter(src, null);

    return destination;
  }

  /**
   * Scale an image.
   * 
   * @param image  image to scale
   * @param desiredSize  
   *          size to scale to; scales image bounds to largest rectangle that
   *          fits within this rectangle, while preserving its aspect ratio
   * @return BufferedImage; if no scaling was necessary, returns original image
   */
  public static BufferedImage scaleToFit(BufferedImage img,
      Dimension desiredSize) { //throws IOException {
    return scaleToFit(img, desiredSize, true);
  }

  public static BufferedImage copy(BufferedImage img) {
    BufferedImage cvtImg = new BufferedImage(img.getWidth(), img.getHeight(),
        img.getType());

    img.copyData(cvtImg.getRaster());
    return cvtImg;

  }

  /**
   * Determine size of image after it has been scaled
   * @param img
   * @param scaleFactor
   * @return size of scaled image
   */
  public static IPoint scaledSize(IPoint size, float scaleFactor) {

    float sx = size.x * scaleFactor;
    float sy = size.y * scaleFactor;
    sx = (float) Math.ceil(sx);
    sy = (float) Math.ceil(sy);
    return new IPoint(sx, sy);

  }

  /**
   * Scale an image.
   * 
   * @param img  image to scale
   * @param scaleFactor scale factor
   * @return BufferedImage; if no scaling was necessary, returns original image
   */
  public static BufferedImage scale(BufferedImage img, double scaleFactor) {
    if (scaleFactor == 1.0)
      return img;

    IPoint sclSize = scaledSize(new IPoint(img.getWidth(), img.getHeight()),
        (float) scaleFactor);

    //    double sx = img.getWidth() * scaleFactor;
    //    double sy = img.getHeight() * scaleFactor;
    //    sx = Math.ceil(sx);
    //    sy = Math.ceil(sy);

    AffineTransform atx = AffineTransform.getScaleInstance(scaleFactor,
        scaleFactor);

    AffineTransformOp op = new AffineTransformOp(atx,
        AffineTransformOp.TYPE_BILINEAR);

    // construct a buffer for the image that has no alpha component.
    BufferedImage cvtImg = new BufferedImage(sclSize.x, sclSize.y,
        img.getType());

    op.filter(img, cvtImg);
    return cvtImg;
  }

  /**
   * Scale an image.
   * 
   * @param image :
   *          image to scale
   * @param desiredSize :
   *          size to scale to; scales image bounds to largest rectangle that
   *          fits within this rectangle, while preserving its aspect ratio
   * @param scaleUp :
   *          if false, and image is smaller than desired size, returns original
   *          image
   * @return BufferedImage; if no scaling was necessary, returns original image
   */
  public static BufferedImage scaleToFit(BufferedImage img,
      Dimension desiredSize, boolean scaleUp) { //throws IOException {

    Dimension srcSize = new Dimension(img.getWidth(), img.getHeight());

    double srcAspectRatio = srcSize.width / (double) srcSize.height;
    double desiredAspectRatio = desiredSize.width / (double) desiredSize.height;

    double scaleFactor = 0;
    if (srcAspectRatio <= desiredAspectRatio) {
      scaleFactor = desiredSize.height / (double) srcSize.height;
    } else {
      scaleFactor = desiredSize.width / (double) srcSize.width;
    }

    if (!scaleUp && scaleFactor >= 1) {
      return img;
    }

    //    if (scaleFactor < 1.0 || (scaleUp && scaleFactor > 1.0)) {
    //      AffineTransform atx = new AffineTransform();
    //
    //      atx.concatenate(AffineTransform
    //          .getScaleInstance(scaleFactor, scaleFactor));
    //
    //      AffineTransformOp op = new AffineTransformOp(atx,
    //          AffineTransformOp.TYPE_BILINEAR);
    //
    //      // construct a buffer for the image that has no alpha component.
    //      cvtImg = new BufferedImage((int) (srcSize.width * scaleFactor),
    //          (int) (srcSize.height * scaleFactor), img.getType());
    //
    //      op.filter(img, cvtImg);

    return scaleToFitExact(img, new Dimension(
        (int) (srcSize.width * scaleFactor),
        (int) (srcSize.height * scaleFactor)));

  }

  /**
   * Scale an image.
   * @param image  image to scale
   * @param desiredSize  size to scale to 
   * @return BufferedImage; if no scaling was necessary, returns original image
   */
  public static BufferedImage scaleToFitExact(BufferedImage img,
      Dimension desiredSize) { // throws IOException {

    BufferedImage cvtImg = img;

    double xScale = desiredSize.getWidth() / (double) img.getWidth();
    double yScale = desiredSize.getHeight() / (double) img.getHeight();
    if (xScale != 1 || yScale != 1) {

      //    if (scaleFactor < 1.0 || (scaleUp && scaleFactor > 1.0)) {
      AffineTransform atx = new AffineTransform();

      atx.concatenate(AffineTransform.getScaleInstance(xScale, yScale));

      AffineTransformOp op = new AffineTransformOp(atx,
          AffineTransformOp.TYPE_BILINEAR);

      cvtImg = new BufferedImage(desiredSize.width, desiredSize.height,
          img.getType());

      op.filter(img, cvtImg);
    }
    return cvtImg;

//    return scaleToFit(img, desiredSize, true);
  }

}
