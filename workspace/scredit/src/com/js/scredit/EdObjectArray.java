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

  public int[] getSelected() {
    ArrayList<Integer> a = new ArrayList();
    for (int i = 0; i < size(); i++) {
      EdObject obj = get(i);
      if (obj.isSelected())
        a.add(i);
    }
    return Tools.toArray(a);
  }

  public <T extends EdObject> T last() {
    return (T) Tools.last(mList);
  }

  public EdObject remove(int index) {
    mutate();
    return mList.remove(index);
  }

  public void add(int position, EdObject obj) {
    mutate();
    mList.add(position, obj);
  }

  public void clearAllSelected() {
    for (int i = 0; i < size(); i++)
      get(i).setSelected(false);
  }

  public void remove(int start, int count) {
    mutate();
    Tools.remove(mList, start, count);
  }

  public void setSelected(int[] slots, boolean f) {
    for (int i = 0; i < slots.length; i++)
      get(slots[i]).setSelected(f);
  }

  public void removeSelected() {
    mutate();
    SlotList slots = getSelectedSlots();
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
    mutate();
    SlotList selectedSlots = getSelectedSlots();
    for (int slot : selectedSlots) {
      EdObject obj = get(slot);
      set(slot, copyOf(obj));
    }
  }

  public EdObject updateEditableObjectStatus(boolean allowEditableObject) {
    int currentEditable = -1;
    int newEditable = -1;
    EdObject editableObject = null;
    SlotList list = getSelectedSlots();
    for (int slot : list) {
      EdObject obj = get(slot);
      if (obj.isEditable())
        currentEditable = slot;
    }
    if (list.size() == 1 && allowEditableObject) {
      newEditable = list.get(0);
      editableObject = get(newEditable);
    }
    if (currentEditable != newEditable) {
      if (currentEditable >= 0)
        get(currentEditable).setEditable(false);
      if (newEditable >= 0)
        editableObject.setEditable(true);
    }
    return editableObject;
  }

  // Freezable interface

  @Override
  public void freeze() {
    if (isFrozen())
      return;
    for (int i = 0; i < mList.size(); i++)
      set(i, frozen(get(i)));
    // Perform any lazy initialization, so object is truly immutable once frozen
    mSelectedSlots = getSelectedSlots();
    super.freeze();
  }

  public boolean isEmpty() {
    return mList.isEmpty();
  }

  public void clear() {
    mutate();
    mList.clear();
  }

  public Iterator<EdObject> iterator() {
    return mList.iterator();
  }

  /**
   * Freeze object and add it to the end of the list.
   */
  public int add(EdObject object) {
    mutate();
    object.freeze();
    int index = mList.size();
    mList.add(object);
    return index;
  }

  public <T extends EdObject> T get(int index) {
    return (T) mList.get(index);
  }

  /**
   * Freeze object and place it within the array, replacing existing object in
   * that slot
   */
  public void set(int slot, EdObject object) {
    mutate();
    object.freeze();
    mList.set(slot, object);
  }

  public int size() {
    return mList.size();
  }

  /**
   * Construct array containing only the selected objects from this array
   */
  public EdObjectArray getSelectedObjects() {
    return getSubset(getSelectedSlots());
  }

  /**
   * Construct subset of this array
   */
  public EdObjectArray getSubset(SlotList slots) {
    EdObjectArray subset = new EdObjectArray();
    for (int slot : slots) {
      subset.add(get(slot));
    }
    return subset;
  }

  public EdObjectArray() {
    this(null);
  }

  private EdObjectArray(EdObjectArray source) {
    if (source == null) {
      mList = new ArrayList();
      mSelectedSlots = new SlotList();
      mSelectedSlots.freeze();
      return;
    }
    for (EdObject obj : source.mList)
      add(obj);
    mEditableSlot = source.mEditableSlot;
    mSelectedSlots = source.mSelectedSlots;
  }

  @Override
  public Freezable getMutableCopy() {
    return new EdObjectArray(this);
  }

  /**
   * Get slots of selected items
   */
  public SlotList getSelectedSlots() {
    return mSelectedSlots;
  }

  public int getEditableSlot() {
    return mEditableSlot;
  }

  private void setSelectedSlotsAux(SlotList slots) {
    mSelectedSlots = slots;
    mEditableSlot = -1;
    if (mSelectedSlots.size() == 1)
      mEditableSlot = mSelectedSlots.get(0);
  }

  /**
   * Make specific slots selected, and others unselected
   */
  public void setSelected(SlotList slots) {
    mutate();
    slots = frozen(slots);
    if (!slots.isEmpty() && slots.last() >= size())
      throw new IllegalArgumentException();
    setSelectedSlotsAux(slots);
  }

  public void setEditableSlot(int slot) {
    setSelected(new SlotList(slot));
  }

  public void unselectAll() {
    setSelected(new SlotList());
  }

  public void selectAll() {
    setSelected(SlotList.buildComplete(size()));
  }

  @Override
  public String toString() {
    if (!DEBUG_ONLY_FEATURES)
      return null;
    else {
      StringBuilder sb = new StringBuilder("EdObjectArray");
      sb.append(" [");
      for (EdObject obj : mList) {
        sb.append(" " + obj.getFactory().getTag());
      }
      sb.append("]");
      return sb.toString();
    }
  }

  public boolean isSlotEditable(int slot) {
    return slot == mEditableSlot;
  }

  public boolean isSlotSelected(int slot) {
    return mSelectedSlots.contains(slot);
  }

  private List<EdObject> mList = new ArrayList();
  private SlotList mSelectedSlots = new SlotList();
  private int mEditableSlot = -1;
}