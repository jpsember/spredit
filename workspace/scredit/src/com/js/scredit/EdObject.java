package com.js.scredit;

import static com.js.basic.Tools.*;

import java.awt.Color;

import com.js.basic.Freezable;
import com.js.geometry.Matrix;
import com.js.geometry.MyMath;
import com.js.geometry.Point;
import com.js.geometry.Rect;
import com.js.myopengl.GLPanel;

import tex.*;

public abstract class EdObject extends Freezable.Mutable {
  private static final int FLAG_SELECTED = (1 << 31);
  private static final int FLAG_EDITABLE = (1 << 30);

  public EdObject(EdObject source) {
    if (source != null)
      mFlags = source.mFlags;
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
   * Determine if object is well defined. If not, and editing complete, it gets
   * deleted.
   */
  public boolean isWellDefined() {
    return true;
  }

  /**
   * Add or clear flags
   * 
   * @param flags
   *          flags to modify
   * @param value
   *          true to set, false to clear
   */
  private void setFlags(int flags, boolean value) {
    if (!value)
      clearFlags(flags);
    else
      addFlags(flags);
  }

  /**
   * Turn specific flags on
   * 
   * @param f
   *          flags to turn on
   */
  public void addFlags(int f) {
    setFlags(mFlags | f);
  }

  /**
   * Determine if a set of flags are set
   * 
   * @param f
   *          flags to test
   * @return true if every one of these flags is set
   */
  public boolean hasFlags(int f) {
    return (mFlags & f) == f;
  }

  /**
   * Turn specific flags off
   * 
   * @param f
   *          flags to turn off
   */
  public void clearFlags(int f) {
    setFlags(mFlags & ~f);
  }

  /**
   * Get current flags
   * 
   * @return flags
   */
  public int flags() {
    return mFlags;
  }

  // /**
  // * Determine if object is selected
  // *
  // * @deprecated use slot list instead
  // */
  // public boolean isSelected() {
  // return hasFlags(FLAG_SELECTED);
  // }

  /**
   * Set object's selected state
   */
  void setSelected(boolean f) {
    int flags = FLAG_SELECTED;
    if (!f)
      flags |= FLAG_EDITABLE;
    setFlags(flags, f);
  }

  public boolean isEditable() {
    return hasFlags(FLAG_EDITABLE);
  }

  void setEditable(boolean f) {
    int flags = FLAG_EDITABLE;
    if (f)
      flags |= FLAG_SELECTED;
    setFlags(flags, f);
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
  public abstract void render(GLPanel panel, boolean isSelected,
      boolean isEditable);

  public EdObject applyColor(Color color) {
    return this;
  }

  public EdObject snapToGrid() {
    return this;
  }

  public abstract void setLocation(Point pt);

  public EdObject flip(boolean horz, Point newLocation) {
    EdObject newObj = mutableCopyOf(this);
    newObj.setLocation(newLocation);
    return newObj;
  }

  public abstract void setRotation(float angle);

  public abstract float rotation();

  public abstract void setScale(float scale);

  public abstract float scale();

  public abstract Point location();

  /**
   * Replace existing flags with new ones
   */
  public void setFlags(int f) {
    mutate();
    mFlags = f;
  }

  /**
   * @return
   */
  public abstract Rect boundingRect();

  /**
   * Apply a transformation to this object
   */
  public void applyTransform(Matrix m) {
    throw new UnsupportedOperationException();
  }

  private int mFlags;
}
