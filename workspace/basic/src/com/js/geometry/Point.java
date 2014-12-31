package com.js.geometry;

import static com.js.basic.Tools.*;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

  public void snapToGrid(float gridSize) {
    x = MyMath.snapToGrid(x, gridSize);
    y = MyMath.snapToGrid(y, gridSize);
  }

  // JSON encoding

  private JSONArray toJSON() throws JSONException {
    JSONArray a = new JSONArray();
    unimp("figure out way to truncate float values to something reasonable");
    a.put(x);
    a.put(y);
    return a;
  }

  private static JSONArray toJSON(List<Point> points) throws JSONException {
    JSONArray a = new JSONArray();
    for (Point pt : points) {
      a.put(pt.x);
      a.put(pt.y);
    }
    return a;
  }

  private static Point get(JSONArray array) throws JSONException {
    int c = 0;
    float x = (float) array.getDouble(c++);
    float y = (float) array.getDouble(c++);
    return new Point(x, y);
  }

  private static List<Point> getList(JSONArray array) throws JSONException {
    ArrayList<Point> list = new ArrayList();
    if (array.length() % 2 != 0)
      throw new JSONException("malformed array");
    int cursor = 0;
    while (cursor < array.length()) {
      float x = (float) array.getDouble(cursor++);
      float y = (float) array.getDouble(cursor++);
      list.add(new Point(x, y));
    }
    return list;
  }

  public void put(JSONObject map, String key) throws JSONException {
    map.put(key, toJSON());
  }

  /**
   * Parse point from JSON map; return null if none found
   */
  public static Point opt(JSONObject map, String key) throws JSONException {
    JSONArray a = map.optJSONArray(key);
    if (a == null)
      return null;
    return get(a);
  }

  /**
   * Parse point from JSON map
   */
  public static Point get(JSONObject map, String key) throws JSONException {
    return get(map.getJSONArray(key));
  }

  /**
   * Encode a list of points to a JSON map
   */
  public static void put(List<Point> points, JSONObject map, String key)
      throws JSONException {
    map.put(key, toJSON(points));
  }

  public static List<Point> getList(JSONObject map, String key)
      throws JSONException {
    return getList(map.getJSONArray(key));
  }

  public float x;
  public float y;

}
