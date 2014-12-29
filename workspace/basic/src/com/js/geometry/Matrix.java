package com.js.geometry;

import static com.js.basic.Tools.*;

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
    sb.append(d(a));
    sb.append(d(c));
    sb.append(d(tx));
    sb.append("]\n");
    sb.append("[");
    sb.append(d(b));
    sb.append(d(d));
    sb.append(d(ty));
    sb.append("]\n");
    sb.append("[");
    sb.append(d(0.0f));
    sb.append(d(0.0f));
    sb.append(d(1.0f));
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

  public Point apply(Point source, Point destination) {
    return apply(source.x, source.y, destination);
  }

  public Point apply(Point source) {
    return apply(source, null);
  }

  public static Matrix getTranslate(Point translate) {
    return getTranslate(translate.x, translate.y);
  }

  public static Matrix getTranslate(float tx, float ty) {
    Matrix m = new Matrix();
    m.tx = tx;
    m.ty = ty;
    return m;
  }

  public static Matrix getRotate(float angleInRadians) {
    Matrix matrix = new Matrix();

    float c = MyMath.cos(angleInRadians), s = MyMath.sin(angleInRadians);
    matrix.a = c;
    matrix.c = -s;
    matrix.b = s;
    matrix.d = c;
    return matrix;
  }

  public static Matrix getScale(float scaleFactor) {
    Matrix m = new Matrix();
    m.a = scaleFactor;
    m.d = scaleFactor;
    return m;
  }

  /**
   * Multiply two matrices. Note that if v is a vector, and T1 and T2 are
   * matrices, then to construct a matrix T3 such that T3[v] = T2[T1[v]], then
   * T3 = T2 * T1 (not T1 * T2). In other words, the order matters; matrix
   * multiplication is not commutative.
   * 
   * @param m1
   * @param m2
   * @param dest
   *          where to store result; if null, constructs new one; can also be
   *          either m1 or m2
   * @return result
   */
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

  public static Matrix multiply(Matrix m1, Matrix m2) {
    return multiply(m1, m2, null);
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

  /**
   * Get a matrix that flips a view's origin between top left and bottom left
   * 
   * @param height
   *          height of view; 0 to just negate the y component with no further
   *          adjustment
   */
  public static Matrix getFlipVertically(float height) {
    Matrix m = new Matrix();
    m.d = -1;
    m.ty = height;
    return m;
  }

}
