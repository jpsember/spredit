package com.js.scredit;

import java.util.*;
import static com.js.basic.Tools.*;

/**
 * Collection for a map whose items are also stored in an array for quick
 * access.
 */
public class ArrayMap {

  /**
   * Add a key to the map, storing its position as its value. If it already
   * exists, doesn't replace it.
   * 
   * @param key
   *          item to add
   * @return position position in array
   */
  public int add(String key) {
    Integer pos = (Integer) keyPositionMap.get(key);
    if (pos == null) {
      pos = new Integer(size());
      add(key, pos);
    }
    return pos.intValue();
  }

  /**
   * Add an item to the map; replace existing if found
   * 
   * @param key
   * @param value
   */
  public Object add(String key, Object value) {
    Object prevValue = null;
    Integer pos = (Integer) keyPositionMap.get(key);
    if (pos == null) {
      int index = valArray.size();
      valArray.add(value);
      keyArray.add(key);
      keyPositionMap.put(key, new Integer(index));
    } else {
      prevValue = valArray.get(pos.intValue());
      valArray.set(pos.intValue(), value);
    }
    return prevValue;
  }

  public ArrayList<String> getKeys() {
    return keyArray;
  }

  /**
   * Determine if map is empty
   * 
   * @return
   */
  public boolean isEmpty() {
    return keyArray.isEmpty();
  }

  /**
   * Get # items in map
   * 
   * @return
   */
  public int size() {
    return keyArray.size();
  }

  /**
   * Get key from map
   * 
   * @param index
   *          : index within the array (0...size()-1)
   * @return
   */
  public String getKey(int index) {
    return keyArray.get(index);
  }

  /**
   * Get value associated with key
   * 
   * @param key
   * @return
   */
  public Object getValue(String key) {
    Object ret = null;
    Integer pos = (Integer) keyPositionMap.get(key);
    if (pos != null) {
      ret = valArray.get(pos.intValue());
    }

    return ret;
  }

  /**
   * Get value associated with index within array
   * 
   * @param index
   * @return
   */
  public Object getValue(int index) {
    return valArray.get(index);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ArrayMap[\n");
    for (int i = 0; i < keyArray.size(); i++) {
      sb.append(' ');
      Object key = getKey(i);
      sb.append(key.toString());
      sb.append(" --> ");
      sb.append(valArray.get(i));
      sb.append('\n');
    }
    sb.append("]\n");

    return sb.toString();
  }

  /**
   * Construct a map from [int]->[Object]
   * 
   * @param iVals
   *          : int array
   * @param oVals
   *          : array of Objects
   * @return Map
   */
  public static Map intKeyMap(int[] iVals, Object[] oVals) {
    Map map = new HashMap(iVals.length);
    for (int i = iVals.length - 1; i >= 0; i--) {
      Object prev = map.put(new Integer(iVals[i]), oVals[i]);
      if (prev != null)
        warning("intKeyMap, duplicate entry for key: " + iVals[i]);
    }
    return map;
  }

  public static Map intKeyMap(int[] iVals, String labels) {
    StringTokenizer tk = new StringTokenizer(labels);
    ArrayList<String> lbl = new ArrayList();
    while (tk.hasMoreTokens()) {
      String l = tk.nextToken();
      l = d(l, 16);
      lbl.add(l);
    }
    if (lbl.size() != iVals.length)
      throw new IllegalArgumentException("unexpected # of labels");
    return intKeyMap(iVals, lbl.toArray(new String[0]));
  }

  private Map keyPositionMap = new HashMap();

  private ArrayList valArray = new ArrayList();
  private ArrayList<String> keyArray = new ArrayList();

  public static String readString(Map<Integer, String> map, int type) {
    String s = map.get(type);
    if (s == null)
      s = "<unknown: " + type + ">";
    return s;
  }
}
