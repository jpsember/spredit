package com.js.geometry;

import static com.js.basic.Tools.*;

public class Point3 {

  public static final Point3 ZERO = new Point3();

  public Point3() {
  }

  public Point3(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Point3(Point3 point) {
    this(point.x, point.y, point.z);
  }

  public void setTo(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void clear() {
    setTo(0, 0, 0);
  }

  public void setTo(Point3 source) {
    setTo(source.x, source.y, source.z);
  }

  public  float magnitude() {
    return MyMath.sqrtf(x * x + y * y + z * z);
  }

  public void add(Point3 point3) {
    x += point3.x;
    y += point3.y;
    z += point3.z;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(d(x));
    sb.append(d(y));
    sb.append(d(z));
    return sb.toString();
  }

  public float x, y, z;

}
