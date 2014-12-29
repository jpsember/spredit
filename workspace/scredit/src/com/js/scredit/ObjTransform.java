package com.js.scredit;

import java.io.*;

import com.js.geometry.Matrix;
import com.js.geometry.Point;

import com.js.geometry.*;
import static com.js.basic.Tools.*;

/**
 * Wrapper class for object transformation matrix, optimized to only recalculate
 * matrix if parameters change; also, maintains inverse of matrix
 */
public class ObjTransform {

  public ObjTransform() {
  }

  /**
   * Construct a transformation matrix
   * 
   * @param location
   *          location of object
   * @param scale
   *          scale of object
   * @param rotation
   *          rotation of object
   * @return matrix
   */
  private static Matrix getTransformMatrix(Point location, float scale,
      float rotation) {
    Matrix mTrans = Matrix.getTranslate(location);
    if (scale != 1) {
      warning("not sure this is correct; maybe add 'scaleBy' as instance method");
      Matrix.multiply(mTrans, Matrix.getScale(scale), mTrans);
    }
    if (rotation != 0) {
      Matrix mRot = Matrix.getRotate(rotation);
      Matrix.multiply(mTrans, mRot, mTrans);
    }
    return mTrans;
  }

  public Matrix matrix() {
    if (matrix == null) {
      matrix = getTransformMatrix(loc, scale, rotation);
    }
    return matrix;
  }

  public Matrix inverse() {
    if (inverse == null)
      inverse = matrix().invert(null);
    return inverse;
  }

  public float scale() {
    return scale;
  }

  public float rotation() {
    return rotation;
  }

  public void setScale(float s) {
    ASSERT(s > 0);
    if (scale != s) {
      scale = s;
      matrix = inverse = null;
      // pr("set scale to "+f(scale));
    }
  }

  public void setRotation(float r) {
    r = MyMath.normalizeAngle(r);
    if (r != rotation) {
      rotation = r;
      matrix = inverse = null;
    }
  }

  public void setLocation(Point location) {
    if (!location.equals(loc)) {
      loc = location;
      matrix = inverse = null;
    }
  }

  public Point location() {
    return loc;
  }

  public ObjTransform(ObjTransform src) {
    loc = src.loc;
    scale = src.scale;
    rotation = src.rotation;
    matrix = src.matrix;
    inverse = src.inverse;
  }

  private static boolean needsFloat(float f) {
    return f != (short) f;
  }

  public void write(DataOutput dw) throws IOException {
    int flags = 0;

    if (needsFloat(loc.x))
      flags |= (1 << 0);

    if (needsFloat(loc.y))
      flags |= (1 << 1);

    if (scale() != 1.0)
      flags |= (1 << 2);

    if (rotation() != 0)
      flags |= (1 << 3);

    dw.writeByte(flags);

    if (0 != (flags & (1 << 0)))
      dw.writeFloat(loc.x);
    else
      dw.writeShort((int) loc.x);

    if (0 != (flags & (1 << 1)))
      dw.writeFloat(loc.y);
    else
      dw.writeShort((int) loc.y);

    if (0 != (flags & (1 << 2)))
      dw.writeFloat(scale());

    if (0 != (flags & (1 << 3)))
      dw.writeFloat(rotation());
  }

  private Point loc = new Point();
  private float scale = 1;
  private float rotation;
  private Matrix matrix, inverse;

}
