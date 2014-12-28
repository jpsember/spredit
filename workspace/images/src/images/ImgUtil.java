package images;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import static com.js.basic.Tools.*;

import com.js.basic.Streams;
import com.js.geometry.IPoint;
import com.js.geometry.Rect;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import java.awt.*;

public class ImgUtil {

  public static Rect bounds(BufferedImage img) {
    return new Rect(0, 0, img.getWidth(), img.getHeight());
  }

  public static IPoint size(BufferedImage img) {
    return new IPoint(img.getWidth(), img.getHeight());
  }

  //  private static void printlist(String names[], String title) {
  //    pr(title);
  //    for (int i = 0, n = names.length; i < n; i++) {
  //      pr("\t" + names[i]);
  //    }
  //  }

  public static void main(String[] args) throws IOException {

    //    if (args.length == 0) {
    //
    //      String readerNames[] = ImageIO.getReaderFormatNames();
    //      printlist(readerNames, "Reader names:");
    //      String readerMimes[] = ImageIO.getReaderMIMETypes();
    //      printlist(readerMimes, "Reader MIME types:");
    //      String writerNames[] = ImageIO.getWriterFormatNames();
    //      printlist(writerNames, "Writer names:");
    //      String writerMimes[] = ImageIO.getWriterMIMETypes();
    //      printlist(writerMimes, "Writer MIME types:");
    //
    //      return;
    //    }
    //
    String path = args[0];
    //float quality = Float.parseFloat(args[1]);
    int size = Integer.parseInt(args[1]);
    BufferedImage img = read(new File(path));
    img = ImgEffects.scaleToFit(img, new Dimension(500, 500));

    for (int p = 0; p <= 8; p++) {
      double f = -.1 * p + 1;
      int s2 = (int) (size * f);

      pr("p=" + p + " s2=" + s2);
      byte[] jpeg = toJPEG(img, s2);
      pr("image size=" + jpeg.length + " for max=" + s2);
      ImgUtil.write(jpeg, new File("size" + p + ".jpg"));

    }

  }

  private static final String JPEG_EXT = "jpg";

  /**
   * Load an image.
   * 
   * @param src :
   *          Image to load
   * @return BufferedImage
   */
  public static BufferedImage read(File src) throws IOException {
    final boolean db = false; //true;

    if (db)
      pr("\n\nImgUtil.read " + src);

    BufferedImage img = ImageIO.read(src);

    
    
    // if it's an indexed color model, convert it.
    {
      if (img.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
        int newType =  BufferedImage.TYPE_INT_RGB;
        if (img.getColorModel().hasAlpha())
          newType = BufferedImage.TYPE_INT_ARGB;
        
        BufferedImage b = new BufferedImage(img.getWidth(), img.getHeight(),
            newType);
        Graphics2D g2 = b.createGraphics();
        g2.drawImage(img, null, null);
        img = b;
      }
    }

//    switch (img.getType()) {
//    case BufferedImage.TYPE_INT_ARGB:
//    case BufferedImage.TYPE_INT_RGB:
//      break;
//    }
    
    if (db)
      pr(" returning:\n" + img + "\n\n");

    return img;
  }

  //  /**
  //   * Load an image.
  //   * 
  //   * @param stream : stream to read from
  //   * @return BufferedImage
  //   */
  //  public static BufferedImage read(InputStream stream) throws IOException {
  //    return read(stream, null, null, null);
  //  }

  /**
   * Read a JPEG from a stream using JAI codec.
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

  //  //Returns the format name of the image in the object 'o'.
  //  // 'o' can be either a File or InputStream object.
  //  // Returns null if the format is not known.
  //  private static String getFormatName(Object o) {
  //    try {
  //      // Create an image input stream on the image
  //      ImageInputStream iis = ImageIO.createImageInputStream(o);
  //
  //      // Find all image readers that recognize the image format
  //      Iterator iter = ImageIO.getImageReaders(iis);
  //      if (!iter.hasNext()) {
  //        // No readers found
  //        return null;
  //      }
  //
  //      // Use the first reader
  //      ImageReader reader = (ImageReader) iter.next();
  //
  //      // Close stream
  //      iis.close();
  //
  //      // Return the format name
  //      return reader.getFormatName();
  //    } catch (IOException e) {
  //      System.out.println(e);
  //      //  Streams.report(e);
  //    }
  //    // The image could not be read
  //    return null;
  //  }

  /**
   * Load an image.
   * 
   * @param stream  stream to read from
   * @param format   type of image ("jpeg","bmp")
   * @param minSize if not null, ensures original bounds contains this rectangle
   * @param maxPixels if not zero, ensures # pixels doesn't exceed this value
   * @return BufferedImage
   */
  public static BufferedImage read(InputStream stream, String format)
      throws IOException {

    ImgReader reader = null;
    BufferedImage img = null;

    try {
      reader = new ImgReader(stream, format);

      //      Dimension srcSize = reader.size();
      //
      //      if ((minSize != null && (srcSize.width < minSize.width || srcSize.height < minSize.height))
      //          || (maxPixels != 0 && (srcSize.width * srcSize.height > maxPixels)))
      //      //(maxSize != null && (srcSize.width > maxSize.width || srcSize.height > maxSize.height))) {
      //      {
      //        throw new ImgSizeException("Bad image bounds: " + srcSize);
      //      }

      img = reader.getImageReader().read(0);

      //      warn("How do we detect if an image has transparency?");
      //      if (format.equals("gif") || format.equals("png"))
      //        img = removeTransparency(img);

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

  //  /**
  //   * Load an image.
  //   * 
  //   * @param srcPath :
  //   *          Image to load
  //   * @param minSize :
  //   *          if not null, ensures original bounds contains this rectangle
  //   * @param maxSize :
  //   *          if not null, ensures original bounds fits within this rectangle
  //   * @return BufferedImage
  //   */
  //  public static BufferedImage read(File src) //, Dimension minSize, long maxPixels)
  //      throws IOException {
  //
  //    String fmtName = null;
  //    {
  //      InputStream s = new FileInputStream(src);
  //      fmtName = getFormatName(s);
  //      s.close();
  //    }
  //
  //    return read(new FileInputStream(src), fmtName); //, minSize, maxPixels);
  //  }

  /**
   * Convert an image to a JPEG
   * 
   * @param img :
   *          BufferedImage
   * @return byte[] array representing JPEG image
   */
  public static byte[] toJPEG(BufferedImage img) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    ////	 Set the compression quality
    //    ImageWriteParam iwparam = new MyImageWriteParam();
    //    iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
    //    iwparam.setCompressionQuality(compressionQuality);
    //
    //

    ImageIO.write(img, "jpg", outStream); // This was "jpeg" instead!
    return outStream.toByteArray();
  }

  /**
   * Write image to file as a JPEG
   * 
   * @param byte[] JPEG image to write
   * @param dest path to write to
   */
  public static void write(byte[] img, File dest) throws IOException {
    // String destPath = dest.getPath();
    // destPath = Path.addExtension(destPath, "jpg");
    // dest =  Streams.changeExtension(dest, JPEG_EXT);

    OutputStream os = Streams.outputStream(dest.getPath());
    try {
      os.write(img);
    } finally {
      os.close();
    }
  }

  /**
   * Write image to file as a JPEG
   * @param img
   * @param dest
   * @throws IOException
   */
  public static void writeJPG(BufferedImage img, File dest) throws IOException {
    dest = Streams.changeExtension(dest, JPEG_EXT);
    ImageIO.write(img, "jpg", dest); // was "jpeg"
  }

  /**
   * Write image to file as a PNG
   * 
   * @param img
   * @param dest path to write to
   */
  public static void writePNG(BufferedImage img, File dest) throws IOException {
    ImageIO.write(img, "png", dest);
  }

  /**
   * Get a string describing a BufferedImage
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

  //  /**
  //   * Dump a .jpg 
  //   * @param src
  //   * @param full
  //   * @throws IOException
  //   * @deprecated
  //   */
  //  public static void dumpJPEG(File src, boolean full) throws IOException {
  //    pr(src.toString() + ":");
  //    if (!full)
  //      Streams.out.print("        ");
  //
  //    Z_JPEGInputStream r = new Z_JPEGInputStream(src);
  //
  //    while (true) {
  //      byte[] b = r.readSegment();
  //      if (b == null)
  //        break;
  //
  //      if (r.segType() >= 0xd0 && r.segType() <= 0xd7)
  //        continue;
  //
  //      if (full)
  //        pr(Z_JPEGInputStream.toString(b));
  //      else
  //        Streams.out.print(" " + Z_JPEGInputStream.typeString(r.segType()));
  //    }
  //    if (!full)
  //      pr();
  //    r.close();
  //  }

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
   * @param image
   * @param compressionQuality : quality from 0 to 1
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
    //		else {
    //
    //			// Set the compression quality
    //			//		ImageWriteParam 
    //			param = new MyImageWriteParam();
    //			param.setCompressionQuality(compressionQuality);
    //			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    //		}

    if (false) {
      String[] qd = param.getCompressionQualityDescriptions();
      float[] qv = param.getCompressionQualityValues();
      for (int i = 0; i < qv.length; i++)
        pr("quality " + d(qv[i]) + " = " + qd[i]);
    }

    // Write the image
    writer.write(null, new IIOImage(image, null, null), param);

    // Cleanup
    ios.flush();
    writer.dispose();
    ios.close();
    return bs.toByteArray();
  }

  //	// This class overrides the setCompressionQuality() method to workaround
  //	// a problem in compressing JPEG images using the javax.imageio package.
  //	private static class MyImageWriteParam extends JPEGImageWriteParam {
  //		public MyImageWriteParam() {
  //			super(Locale.getDefault());
  //		}
  //
  //		// This method accepts quality levels between 0 (lowest) and 1 (highest) and simply converts
  //		// it to a range between 0 and 256; this is not a correct conversion algorithm.
  //		// However, a proper alternative is a lot more complicated.
  //		// This should do until the bug is fixed.
  //		public void setCompressionQuality(float quality) {
  //			if (quality < 0.0F || quality > 1.0F) {
  //				throw new IllegalArgumentException("Quality out-of-bounds!");
  //			}
  //			this.compressionQuality = 256 - (quality * 256);
  //		}
  //	}

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
   * Calculate cropping rectangle that eliminates as many empty rows and columns as possible
   * @param img 
   * @param padding number of transparent pixels to retain to each side
   * @return cropped rectangle (not flipped to OpenGL coordinate system)
   */
  public static Rect calcUsedBounds(BufferedImage img, int padding) {

    int x0 = 0, x1 = img.getWidth();
    int y0 = 0, y1 = img.getHeight();

    while (x0 < x1 && isTransparentColumn(img, x0, y0, y1)) {
      x0++;
    }
    if (x0 == x1) {
      x1 = x0 = img.getWidth() / 2;
      y1 = y0 = img.getHeight() / 2;
    } else {
      while (isTransparentColumn(img, x1 - 1, y0, y1)) {
        x1--;
      }
      while (isTransparentRow(img, y0, x0, x1)) {
        y0++;

      }
      while (isTransparentRow(img, y1 - 1, x0, x1)) {
        y1--;
      }
    }
    x0 = Math.max(x0 - padding, 0);
    x1 = Math.min(x1 + padding, img.getWidth());
    y0 = Math.max(y0 - padding, 0);
    y1 = Math.min(y1 + padding, img.getHeight());

    return new Rect(x0, y0, x1 - x0, y1 - y0);
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
