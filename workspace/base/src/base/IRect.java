package base;

import java.awt.*;

import com.js.geometry.IPoint;

@Deprecated
public class IRect {
  public int x, y, width, height;

  public IRect(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
  }

  public String toString(boolean digitsOnly) {
    StringBuilder sb = new StringBuilder();
    IPoint loc = new IPoint(x, y);
    IPoint size = new IPoint(width, height);
    if (!digitsOnly)
      sb.append("("); //pos=");
    sb.append(loc);
//    if (!digitsOnly)
//      sb.append(" size=");
    sb.append(size);
    if (!digitsOnly)
      sb.append(")");
    return sb.toString();
  }
  public void translate(int x, int y) {
    this.x += x;
    this.y += y;
  }

  public void translate(IPoint pt) {
    translate(pt.x, pt.y);
  }

  public String toString() {
    return toString(false);
  }
  public IRect() {
  }
  public IRect(Rectangle r) {
    this(r.x, r.y, r.width, r.height);
  }
  public IRect(FlRect r) {
    this((int) r.x, (int) r.y, (int) r.width, (int) r.height);
  }
  public IRect(IRect r) {
    this(r.x, r.y, r.width, r.height);
  }

  public IRect(IPoint p0, IPoint p1) {
    x = Math.min(p0.x, p1.x);
    y = Math.min(p0.y, p1.y);
    width = Math.max(p0.x, p1.x) - x;
    height = Math.max(p0.y, p1.y) - y;
  }
  public int endX() {
    return x + width;
  }
  public int endY() {
    return y + height;
  }
  public boolean intersects(IRect s) {
    return x < s.endX() && s.x < endX() && y < s.endY() && s.y < endY();
  }

  public IPoint bottomLeft() {
    return new IPoint(x, y);
  }

  public IPoint topRight() {
    return new IPoint(endX(), endY());
  }

  public IPoint bottomRight() {
    return new IPoint(endX(), y);
  }

  public IPoint topLeft() {
    return new IPoint(x, endY());
  }

  public boolean contains(IRect r) {
    return x <= r.x && y <= r.y && endX() >= r.endX() && endY() >= r.endY();
  }

  public boolean contains(IPoint pt) {
    return pt.x <= endX() && pt.y <= endY() && pt.x >= x && pt.y >= y;
  }

  public int midX() {
    return x + width / 2;
  }
  public int midY() {
    return y + height / 2;
  }

  public static IRect smallestContainingRect(FlRect sr) {
    int ix = (int) Math.floor(sr.x);
    int iy = (int) Math.floor(sr.y);
    int iex = (int) Math.ceil(sr.endX());
    int iey = (int) Math.ceil(sr.endY());

    return new IRect(ix, iy, iex - ix, iey - iy);

  }
  public void scale(float f) {
    x = Math.round(f * x);
    y = Math.round(f * y);
    width = Math.round(f * width);
    height = Math.round(f * height);
  }

  public float distanceFrom(IPoint pt) {
    int dx = 0;
    int dy = 0;
    dx = Math.max(0, Math.max(x - pt.x, pt.x - endX()));
    dy = Math.max(0, Math.max(y - pt.y, pt.y - endY()));
    return (float)Math.sqrt(dx * dx + dy * dy);
  }
  public void set(IRect r) {
    x = r.x;y=r.y;width = r.width; height = r.height;
  }
}
