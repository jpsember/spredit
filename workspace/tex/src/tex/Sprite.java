package tex;

import com.js.geometry.IPoint;
import com.js.geometry.IRect;
import static com.js.basic.Tools.*;

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

  public Sprite(String id) {
    this.id = id;
    this.translate = new IPoint();
    this.bounds = new IRect();
  }

  /**
   * Copy constructor
   * 
   * @param srcSprite
   */
  public Sprite(Sprite srcSprite) {
    this(srcSprite.id);
    bounds = new IRect(srcSprite.bounds);
    translate = new IPoint(srcSprite.translate);
  }

  public void setTranslate(IPoint t) {
    translate.setTo(t);
  }

  /**
   * Get bounds
   * 
   * @return bounds (original, not a copy)
   */
  public IRect bounds() {
    return bounds;
  }

  /**
   * Get translation
   * 
   * @return translation (original, not a copy)
   */
  public IPoint translate() {
    return translate;
  }

  /**
   * Get id
   * 
   * @return id
   */
  public String id() {
    return id;
  }

  /**
   * Set bounds
   * 
   * @param r
   */
  public void setBounds(IRect r) {
    bounds.setTo(r);
  }

  /**
   * Set id
   * 
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    warning("is toString() relied upon to just return the id?");
  //  Tools.die("called sprite.toString()");
    return "Sprite id='" + id + "' bounds=" + bounds + " translate="
        + translate;
  }

  private IRect bounds;
  private IPoint translate;
  private String id;
}
