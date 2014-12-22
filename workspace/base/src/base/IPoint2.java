package base;

import static com.js.basic.Tools.*;

@Deprecated
public class IPoint2 {
  public int x, y;
  public int comp(int index) {
    return index == 0 ? x : y;
  }
  public void setComp(int index, int val) {
    if (index == 0)
      x = val;
    else
      y = val;
  }
  public void addToComp(int index, int val) {
    setComp(index, val + comp(index));
  }

  public IPoint2(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public IPoint2 translate(int x, int y, boolean neg) {
    if (neg) {
      x = -x;
      y = -y;
    }
    this.x += x;
    this.y += y;
    return this;
  }
  public IPoint2 translate(IPoint2 amt) {
    return translate(amt.x, amt.y, false);
  }
  public IPoint2 translate(int tx, int ty) {
    return translate(tx, ty, false);
  }

  public IPoint2 translate(IPoint2 amt, boolean neg) {
    return translate(amt.x, amt.y, neg);
  }

  public static IPoint2 add(IPoint2 a, IPoint2 b, IPoint2 d) {
    if (d == null)
      d = new IPoint2();
    d.x = a.x + b.x;
    d.y = a.y + b.y;
    return d;
  }
  public static IPoint2 add(IPoint2 a, IPoint2 b) {
    return add(a, b, null);
  }
  public IPoint2(IPoint2 src) {
    this(src.x, src.y);
  }
  public void scale(float f) {
    this.x = Math.round(x * f);
    this.y = Math.round(y * f);
  }

  public void set(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public IPoint2() {
  }

  public IPoint2(FlPoint2 fpt) {
    this((int) fpt.x, (int) fpt.y);
  }
  public IPoint2(double x, double y) {
    this((int) Math.round(x), (int) Math.round(y));
  }

  public String toString() {
    return toString(true);
  }

  /**
  * Dump point
  * @param numbersOnly   if true, returns ' xxxxx yyyy '; otherwise, returns '(xxx,yyy)'
  * @return String
  */
  private String toString(boolean numbersOnly) {
    StringBuilder sb = new StringBuilder();
    if (!numbersOnly) {
      sb.append('(');

      sb.append(d(x));
      sb.append(',');
      sb.append(d(y));
      sb.append(')');
    } else {
      sb.append(d(x));
      sb.append(d(y));
    }
    return sb.toString();
  }

  public static float distance(IPoint2 a, IPoint2 b) {
    return distance(a.x, a.y, b.x, b.y);
  }
  public static int distanceSq(int ax, int ay, int bx, int by) {
    int dx = bx - ax;
    int dy = by - ay;
    return dx * dx + dy * dy;
  }

  public static float distance(int ax, int ay, int bx, int by) {
    return (float) Math.sqrt(distanceSq(ax, ay, bx, by));
  }
  public static float distanceSq(IPoint2 a, IPoint2 b) {
    return distanceSq(a.x, a.y, b.x, b.y);
  }

  public static IPoint2 difference(IPoint2 b, IPoint2 a, IPoint2 d) {
    if (d == null)
      d = new IPoint2();
    d.set(b.x - a.x, b.y - a.y);
    return d;
  }
  public static IPoint2 difference(IPoint2 b, IPoint2 a) {
    return difference(b, a, null);
  }

  public void set(IPoint2 t) {
    set(t.x, t.y);
  }
}
