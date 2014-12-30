package com.js.basic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONTools {

  /**
   * Get keys from JSONObject as an Iterable<String>
   * 
   * @param object
   * @return
   */
  public static Iterable<String> keys(JSONObject object) {
    return toList((Iterator<String>) object.keys());
  }

  /**
   * Get Iterable from Set<String>
   */
  public static Iterable<String> iterable(Set<String> set) {
    return toList((Iterator<String>) set);
  }

  public static JSONObject parseMap(String source) throws JSONException {
    return (JSONObject) new JSONTokener(source).nextValue();
  }

  /**
   * Construct a list from an iterator
   */
  private static <T> List<T> toList(Iterator<T> iter) {
    List list = new ArrayList();
    while (iter.hasNext())
      list.add(iter.next());
    return list;
  }

  /**
   * Get a Color value from a map; throws exception if none found
   */
  public static Color getColor(JSONObject map, String key) throws JSONException {
    JSONArray array = map.getJSONArray(key);
    int c = 0;
    int red = array.getInt(c++);
    int green = array.getInt(c++);
    int blue = array.getInt(c++);
    int alpha = array.getInt(c++);
    return new Color(red, green, blue, alpha);
  }

  public static Color getColor(JSONObject map) throws JSONException {
    return getColor(map, "color");
  }

  /**
   * Write a Color value to a map
   */
  public static JSONObject put(JSONObject map, String key, Color color)
      throws JSONException {
    JSONArray array = new JSONArray();
    array.put(color.getRed());
    array.put(color.getGreen());
    array.put(color.getBlue());
    array.put(color.getAlpha());
    map.put(key, array);
    return map;
  }

  public static JSONObject put(JSONObject map, Color color)
      throws JSONException {
    return put(map, "color", color);
  }

}
