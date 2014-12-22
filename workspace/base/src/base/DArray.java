package base;

import java.util.*;
import java.lang.reflect.*;
import static com.js.geometry.MyMath.*;
import static com.js.basic.Tools.*;

public class DArray extends ArrayList implements Cloneable {

  /**
   * Build a DArray that contains one object
   *
   * @param o1  Object
   * @return DArray
   */
  public static DArray build(Object o1) {
    DArray out = new DArray();
    out.add(o1);
    return out;
  }

  /**
   * Build a DArray that contains two objects
   *
   * @return DArray
   */
  public static DArray build(Object o1, Object o2) {
    DArray out = new DArray();
    out.add(o1);
    out.add(o2);
    return out;
  }

  public void removeRange(int fromIndex, int toIndex) {
    super.removeRange(fromIndex, toIndex);
  }

  //  /**
  //   * Get an iterator to examine items in forward or reverse order
  //   * @param reverseOrder : true to iterate from last to first
  //   * @return Iterator
  //   */
  //  public Iterator iterator(boolean reverseOrder) {
  //    if (!reverseOrder)
  //      return iterator();
  //    return new Iterator() {
  //      private int index = size() - 1;
  //      public boolean hasNext() {
  //        return index >= 0;
  //      }
  //
  //      public Object next() {
  //        return get(index--);
  //      }
  //
  //      public void remove() {
  //        throw new UnsupportedOperationException();
  //      }
  //    };
  //
  //  }

  /**
   * Build a DArray that contains three objects
   *
   * @return DArray
   */
  public static DArray build(Object o1, Object o2, Object o3) {
    DArray out = new DArray();
    out.add(o1);
    out.add(o2);
    out.add(o3);
    return out;
  }

  /**
   * Swap two elements of the array
   *
   * @param i :
   *          first element
   * @param j :
   *          second element
   */
  public void swap(int i, int j) {

    Object iObj = get(i);
    set(i, get(j));
    set(j, iObj);
  }

  /**
   * Push a boolean onto the stack
   *
   * @param b
   *          boolean value to push
   */
  public void pushBoolean(boolean b) {
    add(new Boolean(b));
  }

  /**
   * Pop a boolean
   *
   * @return boolean value
   */
  public boolean popBoolean() {
    return ((Boolean) pop()).booleanValue();
  }

  public String getString(int i) {
    return (String) get(i);
  }

  public void addFloat(float f) {
    add(new Float(f));
  }

  public int popInt() {
    return ((Integer) pop()).intValue();
  }

  public double popFloat() {
    return ((Float) pop()).floatValue();
  }

  public void pushInt(int i) {
    push(new Integer(i));
  }

  public void push(Object o) {
    add(o);
  }

  public Object peek(int distFromLast) {
    int ind = size() - 1 - distFromLast;
    Object obj = get(ind);
    return obj;
  }

  public Object pop() {
    int ind = size() - 1;
    Object obj = get(ind);
    remove(ind);
    return obj;
  }

  /**
   * Find position of a particular integer value
   *
   * @param val :
   *          integer to find
   * @return location, or -1 if not found
   */
  public int indexOf(int val) {
    return indexOf(new Integer(val));
  }

  public int lastInt() {
    return ((Integer) last()).intValue();
  }

  public boolean lastBoolean() {
    return ((Boolean) last()).booleanValue();
  }

  public Object last() {
    return getMod(-1);
  }

  /**
   * Get element from array, using modulus of index
   * @param index location of element; this value is taken mod size()
   * @return element 
   */
  public Object getMod(int index) {
    return get(myMod(index, size()));
  }
  /**
   * Replace array element, using modulus of index
   * @param index location of element; this value is taken mod size()
   * @param element element to store
   */
  public void setMod(int index, Object element) {
    set(myMod(index, size()), element);
  }

  /**
   * Determine if an item exists at a particular location
   *
   * @param index :
   *          index of item
   * @return true if it's a valid index and there's a non-null item stored there
   */
  public boolean exists(int index) {
    return index >= 0 && index < size() && get(index) != null;
  }

  //  public void setDouble(int position, double val) {
  //    set(position, new Double(val));
  //  }

  public int addInt(int val) {
    int ret = size();
    add(new Integer(val));
    return ret;
  }

  public void setInt(int position, int val) {
    set(position, new Integer(val));
  }

  public int getInt(int item) {
    return ((Integer) (get(item))).intValue();
  }

  public boolean getBoolean(int item) {
    return ((Boolean) get(item)).booleanValue();
  }

  public void sort(Comparator c) {
    Collections.sort(this, c);
  }

  //  public static final Comparator COMPARE_DOUBLES = new Comparator() {
  //    public int compare(Object arg0, Object arg1) {
  //      return MyMath.sign(((Double) arg0).doubleValue()
  //          - ((Double) arg1).doubleValue());
  //    }
  //  };

  public Object[] toArray(Class itemType) {
    DArray src = this;
    Object[] result = (Object[]) Array.newInstance(itemType, src.size());
    for (int i = 0; i < src.size(); i++)
      result[i] = src.get(i);
    return result;
  }

  /**
   * Construct an array from the DArray, assuming each item is a String.
   *
   * @return an array of Strings
   */
  public String[] toStringArray() {
    return (String[]) toArray(String.class);
  }

  //  /**
  //   * Get descriptive string
  //   *
  //   * @return String
  //   */
  //  public String toString() {
  //    return toString(false);
  //  }

  public static int[] copy(int[] s, int start, int length) {
    int[] a = new int[length];
    System.arraycopy(s, start, a, 0, length);
    return a;
  }

  public static float[] copy(float[] s, int start, int length) {
    float[] a = new float[length];
    System.arraycopy(s, start, a, 0, length);
    return a;
  }

  public static int[] copy(int[] s) {
    return copy(s, 0, s.length);
  }

  public static float[] copy(float[] s) {
    return copy(s, 0, s.length);
  }

  public static void clearTo(int[] a, int val) {
    for (int i = 0; i < a.length; i++) {
      a[i] = val;
    }
  }

  public static void clearTo(float[] a, float val) {
    for (int i = 0; i < a.length; i++) {
      a[i] = val;
    }
  }

  //  public static String toString(int[] a) {
  //    return toString(a, false);
  //  }

  public static String toString(Object[] a, boolean withLF) {
    StringBuilder sb = new StringBuilder();
    if (!withLF)
      sb.append(" [");
    for (int i = 0; i < a.length; i++) {
      if (i != 0 && !withLF) {
        sb.append(' ');
      }
      Object obj = a[i];
      sb.append(obj);
      if (withLF)
        sb.append('\n');
    }
    if (!withLF)
      sb.append("] ");
    return sb.toString();
  }

  public static String toString(Object[] a) {
    return toString(a, false);
  }
  //    StringBuilder sb = new StringBuilder(" [");
  //    for (int i = 0; i < a.length; i++) {
  //      if (i != 0) {
  //        sb.append(' ');
  //      }
  //      Object obj = a[i];
  //      sb.append(Tools.tv(obj));
  //    }
  //    sb.append("] ");
  //    return sb.toString();
  //  }

  //  public static String toString(int[] a, boolean hexMode) {
  //    StringBuilder sb = new StringBuilder(" [");
  //    for (int i = 0; i < a.length; i++) {
  //       addSp(sb);
  //      //      if (i != 0) {
  //      //        sb.append(' ');
  //      //      }
  //      if (hexMode)
  //        TextScanner.toHex(sb, a[i], 8);
  //      else
  //        sb.append(a[i]);
  //    }
  //    sb.append("] ");
  //    return sb.toString();
  //  }

  //  public static String toString(byte[] a) {
  //    return toString(a, false);
  //  }

  //  public static String toString(byte[] a, boolean hexMode) {
  //    StringBuilder sb = new StringBuilder(" [");
  //    for (int i = 0; i < a.length; i++) {
  //      Tools.addSp(sb);
  //      if (hexMode)
  //        TextScanner.toHex(sb, a[i], 2);
  //      else
  //        sb.append(a[i]);
  //    }
  //    sb.append("] ");
  //    return sb.toString();
  //  }
  //
  //  public static String toString(String[] str) {
  //    StringBuilder sb = new StringBuilder();
  //    for (int i = 0; i < str.length; i++) {
  //      String s = str[i];
  //      sb.append(s);
  //      if (!s.endsWith("\n"))
  //        sb.append('\n');
  //    }
  //    return sb.toString();
  //  }
  //
  //  public static String toString(double[] a) {
  //    StringBuilder sb = new StringBuilder(" [");
  //    // sb.append(" #="+a.length+" [");
  //    for (int i = 0; i < a.length; i++) {
  //      Tools.addSp(sb);
  //      sb.append(Tools.f(a[i]));
  //    }
  //    sb.append("] ");
  //    return sb.toString();
  //  }
  //  public static String toString(double[] a, int start, int len) {
  //    StringBuilder sb = new StringBuilder(" [");
  //    // sb.append(" #="+a.length+" [");
  //    for (int i = start; i < start + len; i++) {
  //      Tools.addSp(sb);
  //      sb.append(Tools.f(a[i]));
  //    }
  //    sb.append("] ");
  //    return sb.toString();
  //  }

//  private static int baseId = 900;
//  private int id = baseId++;
//  @Override
//  public String toString() {
//    return "#" + id + " " + super.toString();
//  }
  //  public String toString(boolean withLineFeeds) {
  //    return toString(this, withLineFeeds);
  //
  //    //    StringBuilder sb = new StringBuilder();
  //    //    if (withLineFeeds) {
  //    //      //sb.append("DArray length=" + size() + "\n");
  //    //    } else {
  //    //      sb.append("[");
  //    //    }
  //    //    // int max = withLineFeeds ? length() : 5;
  //    //
  //    //    for (int i = 0; i < size(); i++) {
  //    //      if (size() == i) {
  //    //        break;
  //    //      }
  //    //      if (!withLineFeeds && sb.length() >= 60) {
  //    //        sb.append(" ...");
  //    //        sb.append("(" + (size() - 1 - i) + " more)");
  //    //        break;
  //    //      }
  //    //      if (!withLineFeeds) {
  //    //        sb.append(" ");
  //    //        sb.append(get(i));
  //    //      } else {
  //    //        sb.append(get(i));
  //    //        sb.append("\n");
  //    //      }
  //    //    }
  //    //    if (!withLineFeeds) {
  //    //      sb.append("] ");
  //    //    }
  //    //    return sb.toString();
  //  }

  //  public static String toString(Collection c, boolean withLineFeeds) {
  //    StringBuilder sb = new StringBuilder();
  //    if (withLineFeeds) {
  //      //sb.append("DArray length=" + size() + "\n");
  //    } else {
  //      sb.append("[");
  //    }
  //    Iterator it = c.iterator();
  //    for (int i = 0; it.hasNext(); i++) {
  //      Object obj = it.next();
  //      if (!withLineFeeds && sb.length() >= 60) {
  //        sb.append(" ...");
  //        sb.append("(" + (c.size() - i) + " more)");
  //        break;
  //      }
  //      if (!withLineFeeds) {
  //        if (i != 0)
  //          sb.append(" ");
  //        sb.append(obj);
  //      } else {
  //        sb.append(obj);
  //        sb.append("\n");
  //      }
  //    }
  //    if (!withLineFeeds) {
  //      sb.append("] ");
  //    }
  //    return sb.toString();
  //  }

  /**
   * Constructor
   */
  public DArray() {
  }

  /**
   * Store item in array; grow array if necessary so it contains at least 
   * that many items
   * @param position
   * @param item
   * @return item
   */
  public Object growSet(int position, Object item) {
    final boolean db = false;
    if (db)
      pr("growSet pos=" + position + " size=" + size());

    while (super.size() < 1 + position)
      add(null);
    return set(position, item);
  }

  public DArray(int initialCapacity) {
    super(initialCapacity);
  }

  public DArray(Object[] oa) {
    for (int i = 0; i < oa.length; i++) {
      Object obj = oa[i];
      if (obj != null)
        growSet(i, obj);
    }
  }

  public DArray(int[] ia) {
    for (int i = 0; i < ia.length; i++) {
      addInt(ia[i]);
    }
  }

  //  public void permute(Random r) {
  //    if (r == null)
  //      r = new Random();
  //    for (int i = 0; i < size(); i++) {
  //      int j = r.nextInt(size());
  //      swap(i, j);
  //    }
  //  }

  public int[] toIntArray() {
    int[] a = new int[size()];
    for (int i = 0; i < size(); i++) {
      a[i] = getInt(i);
    }
    return a;
  }
  //  public double[] toDoubleArray() {
  //    double[] a = new double[size()];
  //    for (int i = 0; i < size(); i++) {
  //      a[i] = getDouble(i);
  //    }
  //    return a;
  //  }
  public byte[] toByteArray() {
    byte[] a = new byte[size()];
    for (int i = 0; i < size(); i++) {
      a[i] = ((Byte) get(i)).byteValue();
    }
    return a;
  }

  public DArray subset(int start, int length) {
    DArray out = new DArray(length);
    for (int i = start; i < start + length; i++)
      out.add(get(i));
    return out;
  }

  public int getIntMod(int i) {
    return ((Integer) getMod(i)).intValue();
  }

}
