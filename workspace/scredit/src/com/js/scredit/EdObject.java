package com.js.scredit;

import static com.js.basic.Tools.*;

import java.awt.Color;

import com.js.basic.Freezable;
import com.js.geometry.Matrix;
import com.js.geometry.Point;
import com.js.geometry.Rect;
import com.js.myopengl.GLPanel;

import tex.*;

public abstract class EdObject extends Freezable.Mutable {

  public EdObject(EdObject source) {
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

  @Deprecated
  void setSelected(boolean f) {
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

  @Deprecated
  public abstract void setLocation(Point pt);

  public EdObject flip(boolean horz, Point newLocation) {
    EdObject newObj = mutableCopyOf(this);
    newObj.setLocation(newLocation);
    return newObj;
  }

  public abstract Point location();

  public abstract Rect boundingRect();

  /**
   * Apply a transformation to this object
   */
  public void applyTransform(Matrix m) {
    throw new UnsupportedOperationException();
  }
}
