package base;

import com.js.geometry.*;

/**
 * Methods equals() and hashCode() are not overridden as you might expect; two
 * distinct objects with the same coordinates will return different hash values,
 * and will not be considered equal.
 * 
 * @deprecated
 */
public class FlPoint2 {
  public float x, y;
 
  public static FlPoint2 add(FlPoint2 a, FlPoint2 b, FlPoint2 d) {
    if (d == null)
      d = new FlPoint2();
    d.x = a.x + b.x;
    d.y = a.y + b.y;
    return d;
  }
  
  public static FlPoint2 add(FlPoint2 a, FlPoint2 b) {
    return add(a, b, null);
  }
  public FlPoint2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public FlPoint2 add(FlPoint2 pt) {
    x += pt.x;
    y += pt.y;
    return this;
  }

//  /**
//   * @deprecated use translate with optional sign
//   * @param x
//   * @param y
//   * @return
//   */
//  public FlPoint2 add(float x, float y) {
//    this.x += x;
//    this.y += y;
//    return this;
//  }

  public FlPoint2 translate(float x, float y, boolean neg) {
    if (neg) {
      x = -x;
      y = -y;
    }
    this.x += x;
    this.y += y;
    return this;
  }
  public FlPoint2 translate(FlPoint2 amt) {
    return translate(amt.x, amt.y, false);
  }
  public FlPoint2 translate(float tx, float ty) {
    x += tx;
    y += ty;
    return this;
  }

  public FlPoint2 translate(FlPoint2 amt, boolean neg) {
    float tx = amt.x;
    float ty = amt.y;
    if (neg) {
      tx = -tx;
      ty = -ty;
    }
    return translate(tx, ty);
  }

  public static FlPoint2 difference(FlPoint2 b, FlPoint2 a, FlPoint2 d) {
    if (d == null)
      d = new FlPoint2();
    d.set(b.x - a.x, b.y - a.y);
    return d;
  }
  public static FlPoint2 difference(FlPoint2 b, FlPoint2 a) {
    return difference(b, a, null);
  }
  public static void addMultiple(FlPoint2 a, float mult, FlPoint2 b,
      FlPoint2 dest) {
    dest.x = a.x + mult * b.x;
    dest.y = a.y + mult * b.y;
  }

  public FlPoint2 subtract(float x, float y) {
    this.x -= x;
    this.y -= y;
    return this;
  }

  public float lengthSq() {
    return x * x + y * y;
  }

  public float length() {
    return (float) Math.sqrt(lengthSq());
  }

  public float normalize() {
    float lenSq = lengthSq();
    if (lenSq != 0 && lenSq != 1) {
      lenSq = (float) Math.sqrt(lenSq);
      float scale = 1 / lenSq;
      x *= scale;
      y *= scale;
    }
    return lenSq;
  }

  public FlPoint2 subtract(FlPoint2 pt) {
    return subtract(pt.x, pt.y);
  }

  public FlPoint2(FlPoint2 src) {
    this(src.x, src.y);
  }

  public static FlPoint2 interpolate(FlPoint2 p1, FlPoint2 p2, float t) {
    return new FlPoint2(p1.x + t * (p2.x - p1.x), p1.y + t * (p2.y - p1.y));
  }

  public static FlPoint2 midPoint(FlPoint2 p1, FlPoint2 p2) {
    return interpolate(p1, p2, .5f);
    //  return new FlPoint2(.5 * (p1.x+p2.x),.5*(p1.y+p2.y));
  }

  //  public boolean isValid() {
  //
  //    return !(java.lang.Float.isInfinite(x) || java.lang.Float.isInfinite(y)
  //        || java.lang.Float.isNaN(x) || java.lang.Float.isNaN(y));
  //  }

  //  /**
  //   * Align point to a grid
  //   * @param gridSize : size of grid
  //   */
  //  public void alignToGrid(float gridSize) {
  //    alignToGrid(gridSize, gridSize);
  //  }
  public void set(float x, float y) {
    this.x = x;
    this.y = y;
  }
  public void clear() {x =0; y=0;}

  //  /**
  //   * Align point to a grid
  //   * @param pixelWidth width of pixels in grid
  //   * @param pixelHeight height of pixels in grid
  //   */
  //  public void alignToGrid(float pixelWidth, float pixelHeight) {
  //    float iGrid = 1 / pixelWidth;
  //    x = Math.round(x * iGrid) * pixelWidth;
  //    iGrid = 1 / pixelHeight;
  //    y = Math.round(y * iGrid) * pixelHeight;
  //  }

  public FlPoint2() {
  }

  //  public boolean clamp(float x0, float y0, float x1, float y1) {
  //    boolean valid = true;
  //    if (x < x0 || x > x1) {
  //      valid = false;
  //      x = clamp(x, x0, x1);
  //    }
  //    if (y < y0 || y > y1) {
  //      valid = false;
  //      y = clamp(y, y0, y1);
  //    }
  //    return valid;
  //  }

  public FlPoint2(IPoint2 p) {
    this(p.x, p.y);
  }
  //  public boolean clamp(FRect r) {
  //    return clamp(r.x, r.y, r.x + r.width, r.y + r.height);
  //  }
  public static float distance(FlPoint2 a, FlPoint2 b) {
    return distance(a.x, a.y, b.x, b.y);
  }
  public static float distanceSq(float ax, float ay, float bx, float by) {
    float dx = bx - ax;
    float dy = by - ay;
    return dx * dx + dy * dy;
  }

  public static float distance(float ax, float ay, float bx, float by) {
    return (float) Math.sqrt(distanceSq(ax, ay, bx, by));
  }
  public static float distanceSq(FlPoint2 a, FlPoint2 b) {
    return distanceSq(a.x, a.y, b.x, b.y);
  }

  //   public static float distanceSq (float x1, float y1, float x2, float y2) {
  //    x1 -= x2;
  //    y1 -= y2;
  //    return (x1*x1)+(y1*y1);
  //  }
  //  public static float distance (float x1, float y1, float x2, float y2) {
  //    return Math.sqrt(distanceSq(x1,y1,x2,y2));
  //  }

  //  public String dump(boolean withComma) {
  //    return Tools.f(x) + (withComma ? "," : " ") + Tools.f(y);
  //  }

  //public String dump() { // plotInfo
  //  return dump(false);
  //}

  /**
   * Dump point as x and y (rounded), with leading space before each
   * @return String
   */
  public String toString() {
    return toString(false, true);
  }

  /**
  * Dump point
  * @param allDigits  if true, results are not rounded
  * @param numbersOnly   if true, returns ' xxxxx yyyy '; otherwise, returns '(xxx,yyy)'
  * @return String
  */
  private String toString(boolean allDigits, boolean numbersOnly) {
    if (!numbersOnly) {
      if (allDigits) {
        return "(" + x + "," + y + ")";
      } else {
        return "(" + MyTools.f(x) + "," + MyTools.f(y) + ")";
      }
    } else {
      if (allDigits) {
        return " " + x + " " + y + " ";
      } else {
        return " " + MyTools.f(x) + " " + MyTools.f(y) + " ";
      }
    }
  }

  //  /**
  //   * Dump point
  //   * @param allDigits  if true, results are not rounded
  //   * @return String
  //   */
  //  private String toString(boolean allDigits) {
  //    return toString(true, false);
  //  }

  //  public void clear() {
  //    x = 0;
  //    y = 0;
  //  }

  //  public float get(int y, int x) {
  //    return (y == 1 ? this.y : this.x);
  //  }
  //
  //  public float get(int y) {
  //    return (y == 1 ? this.y : this.x);
  //  }
  //
  //  public int height() {
  //    return 2;
  //  }

  // public void set(FlPoint2 pt) {this.x = pt.x; this.y = pt.y;}
  // public void set(float x, float y) {
  //   this.x = x; this.y = y;
  // }

  //  public void set(int y, int x, float v) {
  //    set(y, v);
  //  }
  //
  //  public void set(int y, float v) {
  //    if (y == 0)
  //      this.x = v;
  //    else {
  //      if (y != 1)
  //        throw new IllegalArgumentException();
  //      this.y = v;
  //    }
  //  }
  //
  //  public int width() {
  //    return 1;
  //  }
  //
  //  public int size() {
  //    return 2;
  //  }
  //
  //  //public float x, y;
  //
  //  public void setX(float x) {
  //    this.x = x;
  //
  //  }
  //
  //  public void setY(float y) {
  //    this.y = y;
  //  }
  //
  //  public void setZ(float z) {
  //    throw new UnsupportedOperationException();
  //  }
  //
  //  public float x() {
  //    return x;
  //  }
  //
  //  public float y() {
  //    return y;
  //  }
  //
  //  public float z() {
  //    throw new UnsupportedOperationException();
  //  }

  public void negate() {
    x = -x;
    y = -y;
  }

  public void scale(float s) {
    x *= s;
    y *= s;
  }

  public FlPoint2 dup() {
    return new FlPoint2(x, y);
  }
  public void setLocation(FlPoint2 cpt) {
    set(cpt.x, cpt.y);
  }
  public void snapToGrid(float g) {
    x = MyMath.snapToGrid(  x,g);
    y = MyMath.snapToGrid(  y,g);
  }

  //  public void setTo(IVector v) {
  //    x = v.x();
  //    y = v.y();
  //  }

  //  /**
  //   * Compare two points lexicographically, so they are sorted
  //   * first by y, then by x (if y's are equal)
  //   * @param pt1
  //   * @param pt2
  //   * @return negative, zero, or positive, if pt1 is less than, equal to,
  //   *  or greater than pt2
  //   */
  //  public static int compareLex(FlPoint2 pt1, FlPoint2 pt2) {
  //    return compareLex(pt1, pt2, true);
  //    //    
  //    //    float res = pt1.y - pt2.y;
  //    //    if (res == 0)
  //    //      res = pt1.x - pt2.x;
  //    //    return MyMath.sign(res);
  //  }
  //  /**
  //   * Compare two points lexicographically
  //   * @param pt1
  //   * @param pt2
  //   * @param yHasPrecedence true to sort by y, and use x to break ties;
  //   *  false to sort by x first
  //   * @return negative, zero, or positive, if pt1 is less than, equal to,
  //   *  or greater than pt2
  //   */
  //  public static int compareLex(FlPoint2 pt1, FlPoint2 pt2,
  //      boolean yHasPrecedence) {
  //    float res;
  //    if (yHasPrecedence) {
  //      res = pt1.y - pt2.y;
  //      if (res == 0)
  //        res = pt1.x - pt2.x;
  //    } else {
  //      res = pt1.x - pt2.x;
  //      if (res == 0)
  //        res = pt1.y - pt2.y;
  //    }
  //    return sign(res);
  //  }
  //
  //  public static final Comparator comparatorYX = new Comparator() {
  //    public int compare(Object arg0, Object arg1) {
  //      FlPoint2 p0 = (FlPoint2) arg0, p1 = (FlPoint2) arg1;
  //      return compareLex(p0, p1, true);
  //    }
  //  };
  //  public static final Comparator comparatorXY = new Comparator() {
  //    public int compare(Object arg0, Object arg1) {
  //      FlPoint2 p0 = (FlPoint2) arg0, p1 = (FlPoint2) arg1;
  //      return compareLex(p0, p1, false);
  //    }
  //  };

}
