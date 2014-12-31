package com.js.geometry;

import static com.js.basic.Tools.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class IPoint {

  public static final IPoint ZERO = new IPoint();

  public IPoint() {
  }

  public IPoint(IPoint src) {
    this(src.x, src.y);
  }

  public IPoint(Point src) {
    this(src.x, src.y);
  }

  public IPoint(float x, float y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  public IPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public static IPoint sum(IPoint a, IPoint b) {
    return new IPoint(a.x + b.x, a.y + b.y);
  }

  public static IPoint difference(IPoint a, IPoint b) {
    return new IPoint(a.x - b.x, a.y - b.y);
  }

  public void setTo(IPoint src) {
    x = src.x;
    y = src.y;
  }

  public void add(IPoint offset) {
    x += offset.x;
    y += offset.y;
  }

  public void applyScale(float scaleFactor) {
    x *= scaleFactor;
    y *= scaleFactor;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(d(x));
    sb.append(' ');
    sb.append(d(y));
    return sb.toString();
  }

  /**
   * Encode point as JSON array
   */
  public JSONArray toJSON() throws JSONException {
    JSONArray a = new JSONArray();
    a.put(x);
    a.put(y);
    return a;
  }

  /**
   * Parse point from JSON map; returns null if no point found
   */
  public static IPoint parseJSON(JSONObject map, String key)
      throws JSONException {
    JSONArray a = map.optJSONArray(key);
    if (a == null)
      return null;
    return parseJSON(a);
  }

  /**
   * Parse point from JSONArray
   */
  public static IPoint parseJSON(JSONArray array) throws JSONException {
    int c = 0;
    int x = array.getInt(c++);
    int y = array.getInt(c++);
    return new IPoint(x, y);
  }

  public Point toPoint() {
    return new Point(x, y);
  }

  public int x;
  public int y;

}
