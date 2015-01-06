package com.js.scredit;

import static com.js.basic.Tools.*;

import java.awt.Color;

import com.js.geometry.MyMath;
import com.js.geometry.Point;
import com.js.geometry.Rect;
import com.js.myopengl.GLPanel;

import tex.*;

public abstract class EdObject implements Cloneable {
  public static final int COLOR_UNDEFINED = -2;

  /**
   * Clone the object
   */
  public Object clone() {
    try {
      EdObject e = (EdObject) super.clone();
      return e;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public <T extends EdObject> T getCopy() {
    return (T) this.clone();
  }

  public Color getColor() {
    return null;
  }

  /**
   * Get atlas associated with this object, if any
   * 
   * @return
   */
  public Atlas getAtlas() {
    return null;
  }

  public String getInfoMsg() {
    return null;
  }

  /**
   * Determine if object is selected
   * 
   * @return true if so
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * Determine if object is well defined. If not, and editing complete, it gets
   * deleted.
   */
  public boolean isWellDefined() {
    return true;
  }

  /**
   * Set object's selected state
   * 
   * @param f
   *          new state
   */
  public void setSelected(boolean f) {
    selected = f;
  }

  public void toggleSelected() {
    selected ^= true;
  }

  /**
   * Rotate and/or scale an object
   * 
   * @param origObject
   *          object before rotate/scale operation began
   * @param scaleFactor
   *          scale factor to apply
   * @param origin
   *          origin of rotation
   * @param rotAngle
   *          rotation angle to apply
   */
  public void rotAndScale(EdObject origObject, float scaleFactor, Point origin,
      float rotAngle) {
    final boolean db = false;
    if (db)
      pr("rotAndScale " + this + "\n scaleFactor=" + scaleFactor + "\n origin="
          + origin + "\n rotAngle=" + da(rotAngle));

    Point oldLoc = origObject.location();
    float oldRot = origObject.rotation();
    float oldScale = origObject.scale();

    if (db)
      pr(" oldLoc=" + oldLoc + " oldRot=" + da(oldRot));

    float rad = MyMath.distanceBetween(origin, oldLoc);
    float posAngle = MyMath.polarAngleOfSegment(origin, oldLoc);

    Point newLoc = MyMath.pointOnCircle(origin, posAngle + rotAngle, rad
        * scaleFactor);
    setLocation(newLoc);

    float newRot = MyMath.normalizeAngle(oldRot + rotAngle);
    if (db)
      pr(" setLocation to new value " + newLoc + ", rotation to " + da(newRot));
    setRotation(newRot);
    setScale(oldScale * scaleFactor);

  }

  /**
   * Determine if boundary of object contains point
   * 
   * @param pt
   * @return true if so
   */
  public abstract boolean contains(Point pt);

  /**
   * Determine if object can be grabbed at a particular point. Default
   * implementation just tests if the object contains() the point.
   * 
   * @param pt
   * @return true if so
   */
  public boolean isGrabPoint(Point pt) {
    return contains(pt);
  }

  public boolean isContainedBy(Rect r) {
    return r.contains(boundingRect());
  }

  /**
   * Get factory responsible for making these objects
   * 
   * @return factory
   */
  public abstract EdObjectFactory getFactory();

  /**
   * Render object within editor.
   */
  public abstract void render(GLPanel panel);

  public EdObject applyColor(Color color) {
    return this;
  }

  public EdObject snapToGrid() {
    return this;
  }

  public abstract void setLocation(Point pt);

  public EdObject flip(boolean horz, Point newLocation) {
    EdObject newObj = (EdObject) this.clone();
    newObj.setLocation(newLocation);
    return newObj;
  }

  public abstract void setRotation(float angle);

  public abstract float rotation();

  public abstract void setScale(float scale);

  public abstract float scale();

  public abstract Point location();

  private boolean selected;

  /**
   * @return
   */
  public abstract Rect boundingRect();
}
