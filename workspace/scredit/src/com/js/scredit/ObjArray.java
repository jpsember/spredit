package com.js.scredit;

import java.util.ArrayList;
import java.util.Iterator;

import com.js.basic.*;

/**
 * <pre>
 * 
 * A sequence of EdObjects
 * 
 * </pre>
 * 
 */
class ObjArray implements Iterable<EdObject> {

  /**
   * Construct empty object array.
   */
  public ObjArray() {
  }

  /**
   * Copy constructor
   */
  public ObjArray(ObjArray source) {
    mItems.addAll(source.mItems);
  }

  /**
   * Construct ObjArray as subset of another
   * 
   * @param slots
   *          strictly increasing sequence of source slot numbers
   */
  public ObjArray(ObjArray source, int[] slots) {
    int prevSlot = -1;
    for (int slot : slots) {
      if (slot <= prevSlot)
        throw new IllegalArgumentException("malformed subset");
      mItems.add(source.get(slot));
    }
  }

  @Override
  public Iterator<EdObject> iterator() {
    return mItems.iterator();
  }

  public int[] getSelected() {
    ArrayList<Integer> a = new ArrayList();
    for (int i = 0; i < size(); i++) {
      EdObject obj = get(i);
      if (obj.isSelected())
        a.add(i);
    }
    return Tools.toArray(a);
  }

  public int size() {
    return mItems.size();
  }

  public EdObject get(int n) {
    return mItems.get(n);
  }

  public EdObject remove(int index) {
    return mItems.remove(index);
  }

  /**
   * Replace an item with a copy, and return the copy
   */
  public EdObject getCopy(int n) {
    EdObject r = (EdObject) get(n).clone();
    mItems.set(n, r);
    return r;
  }

  public void add(int position, EdObject obj) {
    mItems.add(position, obj);
  }

  public void add(EdObject obj) {
    mItems.add(obj);
  }

  public void set(int index, EdObject obj) {
    mItems.set(index, obj);
  }

  public void clearAllSelected() {
    for (int i = 0; i < size(); i++)
      get(i).setSelected(false);
  }

  public void remove(int start, int count) {
    Tools.remove(mItems, start, count);
  }

  public void setSelected(int[] slots, boolean f) {
    for (int i = 0; i < slots.length; i++)
      get(slots[i]).setSelected(f);
  }

  public boolean isEmpty() {
    return mItems.isEmpty();
  }

  private ArrayList<EdObject> mItems = new ArrayList();

}
