package images;

import java.awt.image.*;
import java.io.*;

public class LazyImg {
  public static void setArtPath(File dir) {
    artDir = dir;
  }
  private static File artDir;
  
  public LazyImg(String path) {
    this.path = path;
  }
  public BufferedImage get() {
    if (img == null) {
      File f = null;
     try {
        if (artDir != null)
          f = new File(artDir, path);
        else
          f = new File(path);
      img = ImgUtil.read(f);
      } catch (IOException e) {
        throw new RuntimeException("Can't read "+f+": "+e);
      }
    }
    return img;
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Img: ");
    sb.append(path);
    get();
    sb.append(" (");
    sb.append(img.getWidth());
    sb.append('x');
    sb.append(img.getHeight());
    sb.append(")");
    return sb.toString();
  }
  private String path;
  private BufferedImage img;
}
