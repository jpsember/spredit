package com.js.geometry;

import static com.js.basic.Tools.*;

import org.json.JSONArray;
import org.json.JSONException;

public class Point {

  public static final Point ZERO = new Point();

  public Point() {
  }

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

  /**
   * Encode point as JSON array
   */
  public JSONArray toJSON() throws JSONException {
    JSONArray a = new JSONArray();
    unimp("figure out way to truncate float values to something reasonable");
    a.put(x);
    a.put(y);
    return a;
  }

  /**
   * Parse point from JSONArray
   */
  public static Point parseJSON(JSONArray array) throws JSONException {
    int c = 0;
    float x = (float) array.getDouble(c++);
    float y = (float) array.getDouble(c++);
    return new Point(x, y);
  }

  public float x;
  public float y;


}
