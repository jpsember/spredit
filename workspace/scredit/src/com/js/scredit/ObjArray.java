package com.js.scredit;

import java.util.ArrayList;

import com.js.basic.*;

class ObjArray {

  public int[] getSelected() {
    ArrayList<Integer> a = new ArrayList();
    for (int i = 0; i < size(); i++) {
      EdObject obj = get(i);
      if (obj.isSelected())
        a.add(i);
    }
    return Tools.toArray(a);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("< ");
    for (int i = 0; i < size(); i++) {
      sb.append(get(i));
      sb.append(' ');
    }
    sb.append(">");
    return sb.toString();
  }

  /**
   * @deprecated
   * @param slots
   * @return
   */
  public ObjArray get(int[] slots) {
    ObjArray r = new ObjArray();
    for (int i = 0; i < slots.length; i++) {
      r.add(get(slots[i]));
    }
    return r;
  }

  /**
   * Replace specific items
   * 
   * @param slots
   *          slots of items to be replaced
   * @param newItems
   *          set of items to replace with; must be same number as there are
   *          slots; the items are replaced with COPIES of these items
   * @deprecated
   */
  public void replace(int[] slots, ObjArray newItems) {
    for (int i = 0; i < slots.length; i++)
      set(slots[i], (EdObject) newItems.get(i).clone()); // duplicate());
  }

  public int size() {
    return items.size();
  }

  public ObjArray remove(int[] slots) {
    ObjArray r = new ObjArray();
    for (int i = 0; i < slots.length; i++)
      r.add(get(slots[i]));

    for (int i = slots.length - 1; i >= 0; i--) {
      items.remove(slots[i]);
    }
    return r;
  }

  public EdObject remove(int index) {
    return (EdObject) items.remove(index);
  }

  public EdObject get(int n) {
    EdObject r = (EdObject) items.get(n);
    return r;
  }

  /**
   * Replace an item with a copy, and return the copy
   * 
   * @param n
   * @return
   */
  public EdObject getCopy(int n) {
    EdObject r = (EdObject) get(n).clone();
    items.set(n, r);
    return r;
  }

  public EdObject[] getArray(int[] slots) {
    EdObject[] a = new EdObject[slots.length];
    for (int i = 0; i < slots.length; i++)
      a[i] = get(slots[i]);
    return a;
  }

  public void add(int position, EdObject obj) {
    items.add(position, obj);
  }

  public void add(EdObject obj) {
    items.add(obj);
  }

  public void set(int index, EdObject obj) {
    items.set(index, obj);
  }

  public void set(int[] slots, EdObject[] items) {
    for (int i = 0; i < slots.length; i++)
      set(slots[i], items[i]);
  }

  /**
   * Construct empty object array.
   */
  public ObjArray() {
  }

  public ObjArray(ObjArray source, int[] slots) {
    for (int i = 0; i < slots.length; i++)
      items.add(source.get(slots[i]));
  }

  // /**
  // * @deprecated should be unnecessary; now replacing copies whenever modified
  // * @return
  // */
  // public ObjArray duplicate() {
  // ObjArray r = new ObjArray();
  // for (int i = 0; i < size(); i++)
  // r.add(get(i).duplicate());
  // return r;
  // }

  // /**
  // * Insert new items
  // * @param slots array of destination indices, in ascending order
  // * @param a new items
  // * @deprecated
  // */
  // public void add(int[] slots, ObjArray a) {
  // for (int i = 0; i < slots.length; i++)
  // items.add(slots[i], a.get(i));
  // }
  public void clearAllSelected() {
    for (int i = 0; i < size(); i++)
      get(i).setSelected(false);
  }

  public void clear() {
    items.clear();
  }

  public void addAll(ObjArray a) {
    items.addAll(a.items);
  }

  public void remove(int start, int count) {
    Tools.remove(items, start, count);
  }

  // public void clearAllSelected() {
  // for (int i = 0; i < slots.length; i++)
  // get(i).setSelected(false);
  // }

  public void setSelected(int[] slots, boolean f) {
    for (int i = 0; i < slots.length; i++)
      get(slots[i]).setSelected(f);
  }

  public boolean hasSelected() {
    return getSelected().length > 0;
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  private ArrayList<EdObject> items = new ArrayList();

}
