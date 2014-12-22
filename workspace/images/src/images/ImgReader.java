package images;

import java.awt.Dimension;
import java.io.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.*;

/**
 * Image reader.
 * 
 * Encapsulates ImageReader and ImageInputStream objects.
 *
 */
public class ImgReader {

  public ImgReader(InputStream stream, String fmtName) throws IOException {

    if (fmtName == null)
      fmtName = "jpeg";

    Iterator readers = ImageIO.getImageReadersByFormatName(fmtName);

    if (!readers.hasNext()) {
      throw new IOException("no reader found for fmt: " + fmtName);
    }

    reader = (ImageReader) readers.next();

    // For some reason, this line causes icon to appear in dock
    // when running scredit as a compiler (i.e. no gui):

    // Solution: run application in 'headless' mode; 
    // added AppTools.runAsCmdLine() method.
    // See "http://java.sun.com/developer/technicalArticles/J2SE/Desktop/headless/"

    ImageInputStream iis = ImageIO.createImageInputStream(stream);

    reader.setInput(iis);
  }

  /**
   * Constructor.  Every object constructed should be closed with a call to dispose() to
   * free up resources.
   * 
   * @param srcPath : image to read
   * @throws IOException
   */
  public ImgReader(File file) throws IOException {
    this(new FileInputStream(file), null);
  }

  /**
   * Get size of image
   * @return Dimension
   * @throws IOException
   */
  public Dimension size() throws IOException {
    return new Dimension(reader.getWidth(0), reader.getHeight(0));
  }

  /**
   * Dispose of reader (free up resources)
   * @throws IOException
   */
  public void dispose() throws IOException {
    if (reader != null) {
      reader.dispose();
      reader = null;
    }

    if (stream != null) {
      ImageInputStream temp = stream;
      stream = null;
      temp.close();
    }
  }

  protected void finalize() throws Throwable {
    if (reader != null) {
      System.out.println("ImgReader not dispose()'d");
    }
    try {
      dispose();
    } finally {
      super.finalize();
    }
  }

  public ImageReader getImageReader() {
    return reader;
  }

  public ImageInputStream getImageInputStream() {
    return stream;
  }

  private ImageReader reader;

  private ImageInputStream stream;

}
