package base;

import static base.MyTools.*;
import java.awt.*;
import java.util.ArrayList;

import com.js.geometry.*;
import com.js.geometry.Point;

@Deprecated
public class FlRect {
  public float x, y, width, height;

  public float midX() {
    return (x + width * .5f);
  }
  public float midY() {
    return (y + height * .5f);
  }
  public FlRect(IRect r) {
    this(r.x, r.y, r.width, r.height);
  }

  public boolean equals(FlRect r) {
    unimp("override equals properly?");
    return r != null && r.x == x && r.y == y && r.width == width
        && r.height == height;
  }

  public FlRect(float x, float y, float w, float h) {
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
  }
  public void setTo(FlRect r) {
    setTo(r.x, r.y, r.width, r.height);
  }

  public void setTo(float x, float y, float w, float h) {
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
  }

  public String toString(boolean digitsOnly) {
    StringBuilder sb = new StringBuilder();
    Point loc = new Point(x, y);
    Point size = new Point(width, height);
    if (!digitsOnly)
      sb.append("(pos=");
    sb.append(loc);
    if (!digitsOnly)
      sb.append(" size=");
    sb.append(size);
    if (!digitsOnly)
      sb.append(")");
    return sb.toString();
  }

  public String toString() {
    return toString(false);
  }
  public FlRect() {
  }
  /**
   * Construct smallest rectangle containing two points
   * @param pt1
   * @param pt2
   */
  public FlRect(IPoint pt1, IPoint pt2) {
    this(new Point(pt1), new Point(pt2));
  }

  /**
   * Construct smallest rectangle containing two points
   * @param pt1
   * @param pt2
   */
  public FlRect(Point pt1, Point pt2) {
    x = Math.min(pt1.x, pt2.x);
    y = Math.min(pt1.y, pt2.y);
    width = Math.max(pt1.x, pt2.x) - x;
    height = Math.max(pt1.y, pt2.y) - y;
    //    x = pt1.x;
    //    y = pt1.y;
    //    width = pt2.x - x;
    //    height = pt2.y - y;
    //    if (width < 0 || height < 0)
    //      throw new IllegalArgumentException();
  }
  public FlRect(FlRect r) {
    this(r.x, r.y, r.width, r.height);
  }
  public FlRect(Rectangle r) {
    this(r.x, r.y, r.width, r.height);
  }

  public Point bottomRight() {
    return new Point(endX(), y);
  }

  public Point topLeft() {
    return new Point(x, endY());
  }

  public Point bottomLeft() {
    return new Point(x, y);
  }

  public Point topRight() {
    return new Point(endX(), endY());
  }
  public float endX() {
    return x + width;
  }
  public float endY() {
    return y + height;
  }
  public boolean contains(FlRect r) {
    return x <= r.x && y <= r.y && endX() >= r.endX() && endY() >= r.endY();
  }
  public void include(FlRect r) {
    include(r.topLeft());
    include(r.bottomRight());

    //    float ex = endX(), ey = endY();
    //    x = Math.min(x, r.x);
    //    y = Math.min(y, r.y);
    //    ex = Math.max(ex,r.endX());
    //    ey = Math.max(ey,r.endY());
    //    width = ex-x;
    //    height = ey-y;
  }

  public void include(Point pt) {
    float ex = endX(), ey = endY();
    x = Math.min(x, pt.x);
    y = Math.min(y, pt.y);
    ex = Math.max(ex, pt.x);
    ey = Math.max(ey, pt.y);
    width = ex - x;
    height = ey - y;
  }

  public float distanceFrom(Point pt) {
    float dist = 0;
    if (!contains(pt)) {
      float dx = x - pt.x;
      if (dx < 0)
        dx = pt.x - endX();
      if (dx < 0)
        dx = 0;
      float dy = y - pt.y;
      if (dy < 0)
        dy = pt.y - endY();
      if (dy < 0)
        dy = 0;
      dist = (float) Math.sqrt(dx * dx + dy * dy);
    }
    return dist;
  }

  public void translate(float dx, float dy) {
    x += dx;
    y += dy;
  }

  public Point midPoint() {
    return new Point(midX(), midY());
  }

  public boolean contains(Point pt) {
    return x <= pt.x && y <= pt.y && endX() >= pt.x && endY() >= pt.y;
  }

  public void translate(Point tr) {
    translate(tr.x, tr.y);
  }

  /**
   * Scale x,y,width,height by factor
   * @param f
   */
  public void scale(float f) {
    x *= f;
    y *= f;
    width *= f;
    height *= f;
  }
  public void snapToGrid(float gridSize) {
    float x2 = endX();
    float y2 = endY();
    x = MyMath.snapToGrid(x, gridSize);
    y = MyMath.snapToGrid(y,gridSize);
    width = MyMath.snapToGrid(x2,gridSize) - x;
    height = MyMath.snapToGrid(y2,gridSize) - y;

  }

  public static FlRect boundsForPoints(ArrayList<Point> a) {
    if (a.isEmpty())
      throw new IllegalArgumentException();
    FlRect r = null;
    for (int i = 0; i < a.size(); i++) {
      Point pt = a.get(i);
      if (r == null)
        r = new FlRect(pt, pt);
      else
        r.include(pt);
    }
    return r;
  }

  /**
   * Get point for corner of rectangle
   * @param i corner number (0..3), bottomleft ccw to topleft
   * @return corner
   */
  public Point corner(int i) {
    Point ret = null;

    switch (i) {
    default:
      throw new IllegalArgumentException();
    case 0:
      ret = bottomLeft();
      break;
    case 1:
      ret = bottomRight();
      break;
    case 2:
      ret = topRight();
      break;
    case 3:
      ret = topLeft();
      break;
    }

    return ret;
  }

}
