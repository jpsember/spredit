package com.js.scredit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.js.basic.*;
import static com.js.basic.Tools.*;

/**
 * <pre>
 * 
 * A sequence of EdObjects
 * 
 * </pre>
 * 
 */
class EdObjectArray extends Freezable.Mutable implements Iterable<EdObject> {

  /**
   * Construct empty object array.
   */
  public EdObjectArray() {
  }

  /**
   * Copy constructor
   */
  public EdObjectArray(EdObjectArray source) {
    mList.addAll(source.mList);
  }

  /**
   * Construct ObjArray as subset of another
   * 
   * @param slots
   *          strictly increasing sequence of source slot numbers
   */
  public EdObjectArray(EdObjectArray source, int[] slots) {
    int prevSlot = -1;
    for (int slot : slots) {
      if (slot <= prevSlot)
        throw new IllegalArgumentException("malformed subset");
      mList.add(source.get(slot));
    }
  }

  @Override
  public Iterator<EdObject> iterator() {
    return mList.iterator();
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
    return mList.size();
  }

  public <T extends EdObject> T get(int index) {
    return (T) mList.get(index);
  }

  public <T extends EdObject> T last() {
    return (T) Tools.last(mList);
  }

  public EdObject remove(int index) {
    return mList.remove(index);
  }

  /**
   * Replace an item with a copy, and return the copy
   */
  public EdObject getCopy(int n) {
    EdObject r = (EdObject) get(n).getCopy();
    mList.set(n, r);
    return r;
  }

  public void add(int position, EdObject obj) {
    mList.add(position, obj);
  }

  /**
   * Add an object to the end of the list
   * 
   * @param object
   * @return the index of the object
   */
  public int add(EdObject object) {
    mutate();
    int index = mList.size();
    mList.add(object);
    return index;
  }

  public void set(int index, EdObject object) {
    mutate();
    mList.set(index, object);
  }

  public void clearAllSelected() {
    for (int i = 0; i < size(); i++)
      get(i).setSelected(false);
  }

  public void remove(int start, int count) {
    Tools.remove(mList, start, count);
  }

  public void setSelected(int[] slots, boolean f) {
    for (int i = 0; i < slots.length; i++)
      get(slots[i]).setSelected(f);
  }

  public boolean isEmpty() {
    return mList.isEmpty();
  }

  /**
   * <pre>
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * </pre>
   */

  public void clear() {
    mutate();
    mList.clear();
  }

  /**
   * Construct array containing only the selected objects from this array
   */
  public EdObjectArray getSelectedObjects() {
    EdObjectArray subset = new EdObjectArray();
    for (int slot : getSelectedSlots()) {
      subset.add(get(slot));
    }
    return subset;
  }

  /**
   * Get slots of selected items
   */
  public List<Integer> getSelectedSlots() {
    if (mSelectedSlots == null) {
      List<Integer> slots = SlotList.build();
      for (int i = 0; i < mList.size(); i++) {
        if (mList.get(i).isSelected()) {
          slots.add(i);
        }
      }
      mSelectedSlots = slots;
    }
    return mSelectedSlots;
  }

  /**
   * Make specific slots selected, and others unselected
   */
  public void setSelected(List<Integer> slots) {
    mutate();
    int j = 0;
    for (int i = 0; i < mList.size(); i++) {
      boolean sel = j < slots.size() && slots.get(j) == i;
      mList.get(i).setSelected(sel);
      if (sel)
        j++;
    }
    mSelectedSlots = slots;
  }

  public void removeSelected() {
    List<Integer> slots = getSelectedSlots();
    mutate();
    List<EdObject> newList = new ArrayList();
    int j = 0;
    for (int i = 0; i < mList.size(); i++) {
      if (j < slots.size() && i == slots.get(j)) {
        j++;
        continue;
      }
      newList.add(mList.get(i));
    }
    mList = newList;
  }

  /**
   * Replace selected objects with copies
   */
  public void replaceSelectedObjectsWithCopies() {
    List<Integer> selectedSlots = getSelectedSlots();
    mutate();
    for (int slot : selectedSlots) {
      EdObject obj = get(slot);
      set(slot, (EdObject) obj.getCopy());
    }
  }

  public void unselectAll() {
    mutate();
    setSelected(SlotList.build());
  }

  public void selectAll() {
    mutate();
    for (EdObject obj : mList)
      obj.setSelected(true);
  }

  // Freezable interface

  @Override
  public void mutate() {
    super.mutate();
    mSelectedSlots = null;
  }

  @Override
  public EdObjectArray getMutableCopy() {
    EdObjectArray copy = new EdObjectArray();
    for (EdObject obj : mList)
      copy.add((EdObject) obj.getCopy());
    return copy;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ObjArray");
    sb.append(" [");
    for (EdObject obj : mList) {
      sb.append(" " + obj.getFactory().getTag());
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public void freeze() {
    if (isFrozen())
      return;
    warning("figure out this frozen business...");
    for (int i = 0; i < mList.size(); i++)
      set(i, (EdObject) get(i).getFrozenCopy());
    // Perform any lazy initialization, so object is truly immutable once frozen
    getSelectedSlots();
    super.freeze();
  }

  private List<EdObject> mList = new ArrayList();
  private List<Integer> mSelectedSlots;
}
