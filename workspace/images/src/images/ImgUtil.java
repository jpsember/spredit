package images;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.*;

import org.apache.commons.io.FileUtils;

import static com.js.basic.Tools.*;

import com.js.basic.Files;
import com.js.geometry.IPoint;
import com.js.geometry.IRect;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import java.awt.*;

public class ImgUtil {

  public static IRect bounds(BufferedImage img) {
    return new IRect(0, 0, img.getWidth(), img.getHeight());
  }

  public static IPoint size(BufferedImage img) {
    return new IPoint(img.getWidth(), img.getHeight());
  }

  private static final String JPEG_EXT = "jpg";

  /**
   * Load an image.
   * 
   * @param src
   *          : Image to load
   * @return BufferedImage
   */
  public static BufferedImage read(File src) throws IOException {
    BufferedImage img = ImageIO.read(src);

    // if it's an indexed color model, convert it.
    {
      if (img.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
        int newType = BufferedImage.TYPE_INT_RGB;
        if (img.getColorModel().hasAlpha())
          newType = BufferedImage.TYPE_INT_ARGB;

        BufferedImage b = new BufferedImage(img.getWidth(), img.getHeight(),
            newType);
        Graphics2D g2 = b.createGraphics();
        g2.drawImage(img, null, null);
        img = b;
      }
    }
    return img;
  }

  /**
   * Read a JPEG from a stream using JAI codec.
   * 
   * @param stream
   * @return
   * @throws IOException
   */
  public static BufferedImage readJAI(InputStream stream) throws IOException {
    JPEGImageDecoder d = JPEGCodec.createJPEGDecoder(stream);
    return d.decodeAsBufferedImage();
  }

  /**
   * Read a JPEG from a file using JAI codec.
   * 
   * @param stream
   * @return
   * @throws IOException
   */
  public static BufferedImage readJAI(File file) throws IOException {
    InputStream stream = new FileInputStream(file);
    BufferedImage bi = readJAI(stream);
    stream.close();
    return bi;
  }

  /**
   * Load an image.
   * 
   * @param stream
   *          stream to read from
   * @param format
   *          type of image ("jpeg","bmp")
   * @param minSize
   *          if not null, ensures original bounds contains this rectangle
   * @param maxPixels
   *          if not zero, ensures # pixels doesn't exceed this value
   * @return BufferedImage
   */
  public static BufferedImage read(InputStream stream, String format)
      throws IOException {

    ImgReader reader = null;
    BufferedImage img = null;

    try {
      reader = new ImgReader(stream, format);
      img = reader.getImageReader().read(0);
    } finally {
      if (reader != null) {
        reader.dispose();
      }
    }
    return img;
  }

  public static BufferedImage removeTransparency(BufferedImage bi1) {
    int w = bi1.getWidth();
    int h = bi1.getHeight();
    BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = bi2.createGraphics();
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, w, h);
    g.drawRenderedImage(bi1, null);
    g.dispose();
    return bi2;
  }

  /**
   * Convert an image to a JPEG
   * 
   * @param img
   *          : BufferedImage
   * @return byte[] array representing JPEG image
   */
  public static byte[] toJPEG(BufferedImage img) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    ImageIO.write(img, "jpg", outStream);
    return outStream.toByteArray();
  }

  /**
   * Write image to file as a JPEG
   * 
   * @param byte[] JPEG image to write
   * @param dest
   *          path to write to
   */
  public static void write(byte[] img, File dest) throws IOException {
    FileUtils.writeByteArrayToFile(dest, img);
  }

  /**
   * Write image to file as a JPEG
   * 
   * @param img
   * @param dest
   * @throws IOException
   */
  public static void writeJPG(BufferedImage img, File dest) throws IOException {
    dest = Files.changeExtension(dest, JPEG_EXT);
    ImageIO.write(img, "jpg", dest); // was "jpeg"
  }

  /**
   * Write image to file as a PNG
   * 
   * @param img
   * @param dest
   *          path to write to
   */
  public static void writePNG(BufferedImage img, File dest) throws IOException {
    ImageIO.write(img, "png", dest);
  }

  /**
   * Get a string describing a BufferedImage
   * 
   * @param img
   * @return
   */
  public static String toString(BufferedImage img) {
    StringBuilder sb = new StringBuilder();
    sb.append("BufferedImage size " + img.getWidth() + "x" + img.getHeight());
    sb.append("\n");

    String[] pn = img.getPropertyNames();
    if (pn != null) {
      sb.append("Properties:\n");
      for (int i = 0; i < pn.length; i++)
        sb.append(" " + pn[i] + " : " + img.getProperty(pn[i]) + "\n");
    }
    sb.append("Color model: " + img.getColorModel());
    return sb.toString();

  }

  private static float[] imgQualityLarge = { .6f, .45f, .35f },
      imgQualitySmall = { .75f, .50f, .35f };

  public static byte[] toJPEG(BufferedImage image, int maxSize)
      throws IOException {

    final boolean db = false;

    int diagSize = Math.max(image.getWidth(), image.getHeight());

    if (db)
      pr("ImgUtil.toJPEG, diagSize=" + diagSize + " maxSize=" + maxSize);

    byte[] img = null;
    float[] imgQuality = (diagSize > 160) ? imgQualityLarge : imgQualitySmall;
    for (int qi = 0;; qi++) {
      if (qi >= imgQuality.length)
        throw new IOException("unable to compress to desired size");

      float quality = imgQuality[qi];
      img = toJPEGWithQuality(image, quality);
      if (db)
        pr(" quality=" + quality + " size=" + img.length);

      if (img.length < maxSize)
        break;
    }
    return img;
  }

  /**
   * Convert image to JPEG
   * 
   * @param image
   * @param compressionQuality
   *          : quality from 0 to 1
   * @return JPEG image, in form of byte array
   * @throws IOException
   */
  public static byte[] toJPEGWithQuality(BufferedImage image,
      float compressionQuality) throws IOException {
    // Find a jpeg writer
    ImageWriter writer = null;
    Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
    if (iter.hasNext()) {
      writer = (ImageWriter) iter.next();
    }

    ByteArrayOutputStream bs = new ByteArrayOutputStream();

    // Prepare output file
    ImageOutputStream ios = ImageIO.createImageOutputStream(bs);
    writer.setOutput(ios);

    ImageWriteParam param;

    if (true) {
      param = writer.getDefaultWriteParam();
      if (compressionQuality >= 0) {
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(compressionQuality);
      }
    }
    // Write the image
    writer.write(null, new IIOImage(image, null, null), param);

    // Cleanup
    ios.flush();
    writer.dispose();
    ios.close();
    return bs.toByteArray();
  }

  public static String f(BufferedImage img) {
    StringBuilder sb = new StringBuilder("[");
    if (img == null)
      sb.append("null");
    else {
      sb.append(img.getWidth());
      sb.append("x");
      sb.append(img.getHeight());
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Calculate cropping rectangle that eliminates as many empty rows and columns
   * as possible
   * 
   * @param img
   * @param padding
   *          number of transparent pixels to retain to each side
   * @return cropped rectangle (not flipped to OpenGL coordinate system)
   */
  public static IRect calcUsedBounds(BufferedImage img, int padding) {

    int x0 = 0, x1 = img.getWidth();
    int y0 = 0, y1 = img.getHeight();

    while (x0 < x1 && isTransparentColumn(img, x0, y0, y1)) {
      x0++;
    }
    if (x0 == x1) {
      x1 = x0 = img.getWidth() / 2;
      y1 = y0 = img.getHeight() / 2;
    } else {
      while (isTransparentColumn(img, x1 - 1, y0, y1))
        x1--;
      while (isTransparentRow(img, y0, x0, x1))
        y0++;
      while (isTransparentRow(img, y1 - 1, x0, x1))
        y1--;
    }
    x0 = Math.max(x0 - padding, 0);
    x1 = Math.min(x1 + padding, img.getWidth());
    y0 = Math.max(y0 - padding, 0);
    y1 = Math.min(y1 + padding, img.getHeight());

    return new IRect(x0, y0, x1 - x0, y1 - y0);
  }

  private static boolean isTransparentColumn(BufferedImage img, int x, int y0,
      int y1) {
    for (int y = y0; y < y1; y++) {
      int rgba = img.getRGB(x, y);
      if ((rgba & 0xff000000) != 0) {
        return false;
      }
    }
    return true;
  }

  private static boolean isTransparentRow(BufferedImage img, int y, int x0,
      int x1) {
    for (int x = x0; x < x1; x++) {
      int rgba = img.getRGB(x, y);
      if ((rgba & 0xff000000) != 0) {
        return false;
      }
    }
    return true;
  }

}
