package com.js.scredit;

import com.js.geometry.MyMath;
import com.js.geometry.Point;

public class Circle {
  public Circle(Point origin, float radius) {
    this.origin = origin;
    this.radius = radius;
  }
  public boolean contains(Point pt) {
    return MyMath.squaredDistanceBetween(origin, pt) < radius * radius;
  }
  private Point origin;
  private float radius;
  public float getRadius() {
    return radius;
  }
  public Point getOrigin() {
    return origin;
  }
  /**
   * Calculate the circumcenter of three points.
   * @param a first point
   * @param b second point
   * @param c third point
   */
  public static Circle calcCircumCenter(Point a, Point b, Point c) {

    float rad = 0;
    Point dest = new Point();

    if (Math.abs(b.x - a.x) > Math.abs(b.y - a.y)) {
      dest.y = (((c.x * c.x - a.x * a.x) + (c.y * c.y - a.y * a.y))
          * (b.x - a.x) + (a.x - c.x)
          * ((b.y * b.y - a.y * a.y) + (b.x * b.x - a.x * a.x)))
          / (2 * (c.y - a.y) * (b.x - a.x) + 2 * (c.x - a.x) * (a.y - b.y));
      dest.x = ((b.y * b.y - a.y * a.y) + (b.x * b.x - a.x * a.x) + 2
          * (a.y - b.y) * dest.y)
          / (2 * (b.x - a.x));
    } else {
      dest.x = (((c.y * c.y - a.y * a.y) + (c.x * c.x - a.x * a.x))
          * (b.y - a.y) + (a.y - c.y)
          * ((b.x * b.x - a.x * a.x) + (b.y * b.y - a.y * a.y)))
          / (2 * (c.x - a.x) * (b.y - a.y) + 2 * (c.y - a.y) * (a.x - b.x));
      dest.y = ((b.x * b.x - a.x * a.x) + (b.y * b.y - a.y * a.y) + 2
          * (a.x - b.x) * dest.x)
          / (2 * (b.y - a.y));
    }
    rad = MyMath.distanceBetween(a, dest);
    return new Circle(dest, rad);
  }

}
