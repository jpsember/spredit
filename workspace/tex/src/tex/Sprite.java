package tex;

import com.js.geometry.Point;
import com.js.geometry.Rect;

/**
 * Sprite fields explained
 * -----------------------
 * id : string containing A-Z, _, 0-9
 * bounds : bounds of sprite in 'world' space, where centerpoint is at the origin
 * translate : location of pixel at (0,0) within atlas
 * compression : scaling factor (0..1] applied to image to occupy less space in atlas
 *
 * Example:
 * If sprite has:
 *  bounds -10,-20 ... 30,40
 *  translate 200, 80
 *  compression  .5
 *  
 * Then crop rectangle within atlas is
 *  (-10 * .5) + 200 = 195,  (-20 * .5) + 80 = 70  
 *    ...
 *  (30 * .5) + 200 = 215, (40 * .5) + 80 = 100
 */
public class Sprite {

  /**
   * Constructor.  Sets compression to 1.
   * @param id 
   */
  public Sprite(String id) {
    this.id = id;
    this.compressionFactor = 1;
    this.translate = new Point();
    this.bounds = new Rect();
  }

  /**
   * Copy constructor
   * @param srcSprite
   */
  public Sprite(Sprite srcSprite) {
    this(srcSprite.id);
    bounds = new Rect(srcSprite.bounds);
    translate = new Point(srcSprite.translate);
    compressionFactor = srcSprite.compressionFactor;
  }

  public String toString() {
    return id;
  }

  /**
   * Get compression factor
   * @return factor
   */
  public float compressionFactor() {
    return compressionFactor;
  }
  /**
   * Set compression factor
   * @param factor factor 
   */
  public void setCompression(float factor) {
    compressionFactor = factor;
  }

  /**
   * Set translate
   * @param t
   */
  public void setTranslate(Point t) {
    translate.setTo(t);
  }

  /**
   * Get bounds
   * @return bounds (original, not a copy)
   */
  public Rect bounds() {
    return bounds;
  }

  /**
   * Get translation 
   * @return translation (original, not a copy)
   */
  public Point translate() {
    return translate;
  }

  /**
   * Get id
   * @return id
   */
  public String id() {
    return id;
  }

  /**
   * Set bounds
   * @param r
   */
  public void setBounds(Rect r) {
    bounds.setTo(r);
  }

  /**
   * Set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  private Rect bounds;
  private float compressionFactor;
  private Point translate;
  private String id;
}
