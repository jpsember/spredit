package com.js.scredit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    mList.addAll(source.mList);
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

  public EdObject remove(int index) {
    return mList.remove(index);
  }

  /**
   * Replace an item with a copy, and return the copy
   */
  public EdObject getCopy(int n) {
    EdObject r = (EdObject) get(n).clone();
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
    prepareForChanges();
    int index = mList.size();
    mList.add(object);
    return index;
  }

  public void set(int index, EdObject object) {
    prepareForChanges();
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

  /**
   * Invalidate any cached information in preparation for changes being made to
   * this object
   */
  private void prepareForChanges() {
    if (mFrozen)
      throw new IllegalStateException();
    mFrozenVersion = null;
    mSelectedSlots = null;
  }

  /**
   * Make this array immutable, if it's not already
   */
  public ObjArray freeze() {
    if (!mFrozen) {
      mFrozen = true;
      mFrozenVersion = this;
    }
    return this;
  }

  public void clear() {
    prepareForChanges();
    mList.clear();
  }

  /**
   * Construct array containing only the selected objects from this array
   */
  public ObjArray getSelectedObjects() {
    ObjArray subset = new ObjArray();
    for (int slot : getSelectedSlots()) {
      subset.add(get(slot));
    }
    return subset;
  }

  /**
   * Get a mutable copy of this array
   */
  public ObjArray getMutableCopy() {
    ObjArray copy = new ObjArray();
    for (EdObject obj : mList)
      copy.add(obj);
    // Have the copy share this array's frozen version, if it has one
    copy.mFrozenVersion = this.mFrozenVersion;
    return copy;
  }

  /**
   * Construct an immutable (i.e. frozen) version of this array
   * 
   * @return frozen version, which may be this
   */
  public ObjArray getFrozen() {
    return getCopy().freeze();
  }

  /**
   * Get a copy of this array; if we're frozen, returns this
   */
  private ObjArray getCopy() {
    if (isFrozen())
      return this;
    return getMutableCopy();
  }

  private boolean isFrozen() {
    return mFrozen;
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
    prepareForChanges();
    int j = 0;
    for (int i = 0; i < mList.size(); i++) {
      boolean sel = j < slots.size() && slots.get(j) == i;
      mList.get(i).setSelected(sel);
      if (sel)
        j++;
    }
    mSelectedSlots = slots;
  }

  // public void setEditableSlot(int slot) {
  // ASSERT(slot >= 0);
  // setSelected(SlotList.build(slot));
  // get(slot).setEditable(true);
  // prepareForChanges();
  // }

  public void removeSelected() {
    List<Integer> slots = getSelectedSlots();
    prepareForChanges();
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
    prepareForChanges();
    for (int slot : selectedSlots) {
      EdObject obj = get(slot);
      set(slot, obj.getCopy());
    }
  }

  public void unselectAll() {
    prepareForChanges();
    setSelected(SlotList.build());
  }

  public void selectAll() {
    prepareForChanges();
    for (EdObject obj : mList)
      obj.setSelected(true);
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

  // public EdObject updateEditableObjectStatus(boolean allowEditableObject) {
  // int currentEditable = -1;
  // int newEditable = -1;
  // EdObject editableObject = null;
  // List<Integer> list = getSelectedSlots();
  // for (int slot : list) {
  // EdObject obj = get(slot);
  // if (obj.isEditable())
  // currentEditable = slot;
  // }
  // if (list.size() == 1 && allowEditableObject) {
  // newEditable = list.get(0);
  // editableObject = get(newEditable);
  // }
  // if (currentEditable != newEditable) {
  // prepareForChanges();
  // if (currentEditable >= 0)
  // get(currentEditable).setEditable(false);
  // if (newEditable >= 0)
  // editableObject.setEditable(true);
  // }
  // return editableObject;
  // }

  private List<EdObject> mList = new ArrayList();
  private List<Integer> mSelectedSlots;
  private boolean mFrozen;
  private ObjArray mFrozenVersion;

}
