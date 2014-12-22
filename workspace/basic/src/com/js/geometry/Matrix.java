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

  public static Matrix multiply(Matrix m1, Matrix m2, Matrix dest) {
    if (dest == null)
      dest = new Matrix();

    float na = m1.a * m2.a + m1.c * m2.b + m1.tx * 0;
    float nc = m1.a * m2.c + m1.c * m2.d + m1.tx * 0;
    float ntx = m1.a * m2.tx + m1.c * m2.ty + m1.tx * 1;

    float nb = m1.b * m2.a + m1.d * m2.b + m1.ty * 0;
    float nd = m1.b * m2.c + m1.d * m2.d + m1.ty * 0;
    float nty = m1.b * m2.tx + m1.d * m2.ty + m1.ty * 1;

    dest.a = na;
    dest.b = nb;
    dest.c = nc;
    dest.d = nd;
    dest.tx = ntx;
    dest.ty = nty;

    return dest;
  }

  public Matrix invert(Matrix dest) {
    if (dest == null)
      dest = new Matrix();

    MyMath.testForZero(d);
    float e = 1 / d;
    float h = c * e;
    float g = b * h;
    float ag = a - g;
    MyMath.testForZero(ag);
    float f = 1 / ag;
    float j = h * ty - tx;

    float na = f;
    float nc = -h * f;
    float ntx = f * j;
    float nb = -b * e * f;
    float nd = e * (1 + g * f);
    float nty = e * (-ty - b * f * j);
    dest.a = na;
    dest.c = nc;
    dest.tx = ntx;
    dest.b = nb;
    dest.d = nd;
    dest.ty = nty;

    return dest;
  }

}
