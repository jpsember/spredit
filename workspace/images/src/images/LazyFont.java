package images;

import java.awt.*;

public class LazyFont {
  
  public LazyFont(String name, int style, int size) {
    this.font = new Font(name,style,size);
  }
  public Font font() {
    return font;
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Font: ");
    sb.append(font);
    return sb.toString();
  }
  
  public FontMetrics metrics(Graphics g) {
    if (metrics == null)
      metrics = g.getFontMetrics(font);
    return metrics;
  }
  
  public static void setAntiAliasing(Graphics  g) {
    
    Graphics2D g2 = (Graphics2D) g;
    
          // for antialising geometric shapes
          g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON));
          // for antialiasing text
          g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    
 }
  
  private Font font;
  private FontMetrics metrics;
}
