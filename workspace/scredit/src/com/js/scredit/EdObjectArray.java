package com.js.scredit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.js.basic.*;
import com.js.editor.UserOperation;

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
   */
  public EdObjectArray(EdObjectArray source, SlotList slots) {
    for (int slot : slots) {
      mList.add(source.get(slot));
    }
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
    setSelected(new SlotList());
  }

  public void remove(int start, int count) {
    mutate();
    Tools.remove(mList, start, count);
  }

  // Freezable interface

  @Override
  public void freeze() {
    if (isFrozen())
      return;
    for (int i = 0; i < mList.size(); i++)
      set(i, frozen(get(i)));
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

  public int getEditableSlot(UserOperation currentOperation) {
    if (mSelectedSlots.size() == 1 && currentOperation.allowEditableObject()) {
      return mSelectedSlots.get(0);
    }
    return -1;
  }

  private void setSelectedSlotsAux(SlotList slots) {
    mSelectedSlots = slots;
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

  public boolean isSlotSelected(int slot) {
    return mSelectedSlots.contains(slot);
  }

  private List<EdObject> mList = new ArrayList();
  private SlotList mSelectedSlots = new SlotList();
}