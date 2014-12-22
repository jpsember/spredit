package com.js.geometry;

import com.js.basic.Tools;

/**
 * Matrix for 2D affine transformations; modelled after iOS CGAffineTransform
 * 
 * <pre>
 * 
 * It represents the following matrix M:
 * 
 *   | a  c  tx |
 *   | b  d  ty |
 *   | 0  0  1  |
 * 
 * If (x,y) is a point, we will use the notation (x,y)' to refer to the column vector:
 * 
 *   | x |
 *   | y |
 * 
 * </pre>
 */
public class Matrix {

  public float a = 1, b, c, d = 1, tx, ty;

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(Tools.d(a));
    sb.append(Tools.d(c));
    sb.append(Tools.d(0.0f));
    sb.append("]\n");
    sb.append("[");
    sb.append(Tools.d(b));
    sb.append(Tools.d(d));
    sb.append(Tools.d(0.0f));
    sb.append("]\n");
    sb.append("[");
    sb.append(Tools.d(0.0f));
    sb.append(Tools.d(0.0f));
    sb.append(Tools.d(1.0f));
    sb.append("]\n");
    return sb.toString();
  }

  /**
   * Given a point p = (x,y,1)', calculate r = M * p = (rx,ry,1)'
   * 
   * @param destination
   *          where to store the result; if null, allocates a new point
   * @return destination
   */
  public Point apply(float x, float y, Point destination) {
    if (destination == null)
      destination = new Point();
    destination.x = a * x + c * y + tx;
    destination.y = b * x + d * y + ty;
    return destination;
  }

  public static Matrix getTranslate(Point translate) {
    Matrix m = new Matrix();
    m.tx = translate.x;
    m.ty = translate.y;
    return m;
  }

  // public Point apply(float x, float y) {
  // return apply(x, y, null);
  // }

}
