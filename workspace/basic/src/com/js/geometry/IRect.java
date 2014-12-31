package com.js.geometry;

import java.awt.Rectangle;
import java.util.List;
import org.json.*;

/**
 * Rectangle with integer coordinates; modelled after Rect
 */
public class IRect {

  public IRect() {
  }

  // Copy constructors

  public IRect(Rectangle r) {
    setTo((int) r.x, (int) r.y, (int) r.width, (int) r.height);
  }

  public IRect(Rect r) {
    setTo((int) r.x, (int) r.y, (int) r.width, (int) r.height);
  }

  public IRect(IRect r) {
    setTo(r.x, r.y, r.width, r.height);
  }

  public Rectangle toRectangle() {
    return new Rectangle(x, y, width, height);
  }

  public Rect toRect() {
    return new Rect(this);
  }

  public int midX() {
    return (x + width / 2);
  }

  public int midY() {
    return (y + height / 2);
  }

  public int maxDim() {
    return Math.max(width, height);
  }

  public int minDim() {
    return Math.min(width, height);
  }

  public boolean equals(IRect r) {
    return r != null && r.x == x && r.y == y && r.width == width
        && r.height == height;
  }

  public IRect(int x, int y, int w, int h) {
    setTo(x, y, w, h);
  }

  public void setTo(IRect r) {
    setTo(r.x, r.y, r.width, r.height);
  }

  public void setTo(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    IPoint loc = new IPoint(x, y);
    IPoint size = new IPoint(width, height);
    sb.append(loc);
    sb.append(size);
    return sb.toString();
  }

  /**
   * Construct smallest rectangle containing two points
   * 
   * @param pt1
   * @param pt2
   */
  public IRect(IPoint pt1, IPoint pt2) {
    x = Math.min(pt1.x, pt2.x);
    y = Math.min(pt1.y, pt2.y);
    width = Math.max(pt1.x, pt2.x) - x;
    height = Math.max(pt1.y, pt2.y) - y;
  }

  public IRect(IPoint size) {
    setTo(0, 0, size.x, size.y);
  }

  public void inset(int dx, int dy) {
    x += dx;
    y += dy;
    width -= 2 * dx;
    height -= 2 * dy;
  }

  public IPoint bottomLeft() {
    return new IPoint(x, y);
  }

  public IPoint bottomRight() {
    return new IPoint(endX(), y);
  }

  public IPoint topRight() {
    return new IPoint(endX(), endY());
  }

  public IPoint topLeft() {
    return new IPoint(x, endY());
  }

  public int endX() {
    return x + width;
  }

  public int endY() {
    return y + height;
  }

  public boolean contains(IRect r) {
    return x <= r.x && y <= r.y && endX() >= r.endX() && endY() >= r.endY();
  }

  public void include(IRect r) {
    include(r.topLeft());
    include(r.bottomRight());
  }

  public void include(IPoint pt) {
    int ex = endX(), ey = endY();
    x = Math.min(x, pt.x);
    y = Math.min(y, pt.y);
    ex = Math.max(ex, pt.x);
    ey = Math.max(ey, pt.y);
    width = ex - x;
    height = ey - y;
  }

  public float distanceFrom(IPoint pt) {
    return MyMath.distanceBetween(new Point(pt), new Point(nearestPointTo(pt)));
  }

  /**
   * Find the nearest point within the rectangle to a query point
   * 
   * @param queryPoint
   */
  public IPoint nearestPointTo(IPoint queryPoint) {
    return new IPoint(MyMath.clamp(queryPoint.x, x, endX()), MyMath.clamp(
        queryPoint.y, y, endY()));
  }

  public void translate(int dx, int dy) {
    x += dx;
    y += dy;
  }

  public IPoint midPoint() {
    return new IPoint(midX(), midY());
  }

  public boolean contains(IPoint pt) {
    return x <= pt.x && y <= pt.y && endX() >= pt.x && endY() >= pt.y;
  }

  public void translate(IPoint tr) {
    translate(tr.x, tr.y);
  }

  /**
   * Scale x,y,width,height by factor
   * 
   * @param f
   */
  public void scale(float f) {
    x *= f;
    y *= f;
    width *= f;
    height *= f;
  }

  public void snapToGrid(int gridSize) {
    int x2 = endX();
    int y2 = endY();
    x = (int) MyMath.snapToGrid(x, gridSize);
    y = (int) MyMath.snapToGrid(y, gridSize);
    width = (int) MyMath.snapToGrid(x2, gridSize) - x;
    height = (int) MyMath.snapToGrid(y2, gridSize) - y;
  }

  /**
   * Get point for corner of rectangle
   * 
   * @param i
   *          corner number (0..3), bottomleft ccw to topleft
   * @return corner
   */
  public IPoint corner(int i) {
    switch (i) {
    default:
      throw new IllegalArgumentException();
    case 0:
      return bottomLeft();
    case 1:
      return bottomRight();
    case 2:
      return topRight();
    case 3:
      return topLeft();
    }
  }

  public static IRect rectContainingPoints(List<IPoint> a) {
    if (a.isEmpty())
      throw new IllegalArgumentException();
    IRect r = null;
    for (IPoint pt : a) {
      if (r == null)
        r = new IRect(pt, pt);
      else
        r.include(pt);
    }
    return r;
  }

  public static IRect rectContainingPoints(IPoint s1, IPoint s2) {
    IPoint m1 = new IPoint(Math.min(s1.x, s2.x), Math.min(s1.y, s2.y));
    IPoint m2 = new IPoint(Math.max(s1.x, s2.x), Math.max(s1.y, s2.y));
    return new IRect(m1.x, m1.y, m2.x - m1.x, m2.y - m1.y);
  }

  public boolean intersects(IRect t) {
    return (x < t.endX() && endX() > t.x && y < t.endY() && endY() > t.y);
  }

  public IPoint size() {
    return new IPoint(width, height);
  }

  /**
   * Encode IRect as JSON array
   */
  public JSONArray toJSON() throws JSONException {
    JSONArray a = new JSONArray();
    a.put(x);
    a.put(y);
    a.put(width);
    a.put(height);
    return a;
  }

  /**
   * Parse IRect from JSON map
   */
  public static IRect parseJSON(JSONObject map, String key)
      throws JSONException {
    JSONArray array = map.optJSONArray(key);
    if (array == null)
      return null;
    return parseJSON(array);
  }

  /**
   * Parse IRect from JSONArray
   */
  public static IRect parseJSON(JSONArray array) throws JSONException {
    int c = 0;
    int x = array.getInt(c++);
    int y = array.getInt(c++);
    int w = array.getInt(c++);
    int h = array.getInt(c++);
    return new IRect(x, y, w, h);
  }

  public int x, y, width, height;
}