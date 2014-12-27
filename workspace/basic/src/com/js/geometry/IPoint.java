package com.js.geometry;

import static com.js.basic.Tools.*;

import org.json.JSONArray;
import org.json.JSONException;

public final class IPoint {

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(d(x));
		sb.append(' ');
		sb.append(d(y));
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
    a.put(x);
    a.put(y);
    return a;
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

	public int x;
	public int y;
}
