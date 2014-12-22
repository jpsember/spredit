package com.js.geometry;

import static com.js.basic.Tools.*;

public class Point {

  public static final Point ZERO = new Point();

  public Point() {
  }

  // public final void apply(Matrix m) {
  // float[] f = new float[9];
  // m.getValues(f);
  // float newX = f[0] * x + f[1] * y + f[2];
  // float newY = f[3] * x + f[4] * y + f[5];
  // this.x = newX;
  // this.y = newY;
  // }

  public Point(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Point(Point point) {
    this(point.x, point.y);
  }

  public Point(IPoint point) {
    this(point.x, point.y);
  }

  public void setTo(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public void clear() {
    setTo(0, 0);
  }

  public void setTo(final Point source) {
    setTo(source.x, source.y);
  }

  public float magnitude() {
    return MyMath.magnitudeOfRay(x, y);
  }

  public void add(Point point) {
    x += point.x;
    y += point.y;
  }

  public void applyScale(float scaleFactor) {
    x *= scaleFactor;
    y *= scaleFactor;
  }

  public static Point sum(Point a, Point b) {
    return new Point(a.x + b.x, a.y + b.y);
  }

  public static Point difference(Point a, Point b) {
    return new Point(a.x - b.x, a.y - b.y);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(d(x));
    sb.append(' ');
    sb.append(d(y));
    return sb.toString();
  }

  public String toStringAsInts() {
    StringBuilder sb = new StringBuilder();
    sb.append(d((int) x, 4));
    sb.append(' ');
    sb.append(d((int) y, 4));
    return sb.toString();
  }

  public String dumpUnlabelled() {
    StringBuilder sb = new StringBuilder();
    sb.append(d(x));
    sb.append(' ');
    sb.append(d(y));
    sb.append(' ');
    return sb.toString();
  }

  public float x;
  public float y;

}
