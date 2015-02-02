package com.js.scredit;

import com.js.editor.UserOperation;
import com.js.editor.UserEvent;
import static com.js.basic.Tools.*;

// TODO: look at calls to ScriptEditor and see if we can put some in an interface

public class DefaultMouseOper extends UserOperation {

  @Override
  public void processUserEvent(UserEvent event) {
    final boolean db = false;
    if (db)
      pr("DefaultMouseOper.processUserEvent " + event);

    switch (event.getCode()) {
    case UserEvent.CODE_DOWN:
      mInitialDownEvent = event;
      mIsDrag = false;
      constructPickSet(event);
      break;

    case UserEvent.CODE_DRAG:
      if (!mIsDrag) {
        mIsDrag = true;
        doStartDrag(event);
      }
      doContinueDrag(event);
      break;
    case UserEvent.CODE_UP:
      if (!mIsDrag)
        doClick(event);
      else
        doFinishDrag();
      break;
    }
  }

  private void constructPickSet(UserEvent event) {
    EdObjectArray items = ScriptEditor.items();
    mPickSet = new SlotList();
    mPickSetSelected = new SlotList();
    for (int i = 0; i < items.size(); i++) {
      EdObject obj = items.get(i);
      if (!obj.contains(event.getWorldLocation()))
        continue;
      mPickSet.add(i);
      if (items.isSlotSelected(i))
        mPickSetSelected.add(i);
    }
  }

  private void doFinishDrag() {
    // TODO Auto-generated method stub

  }

  /**
   * 
   * If click, no shift key:
   * 
   * [] If pick set is empty, unselect all objects (i.e., any selected objects
   * not intersecting the click location); otherwise, cycle through the pick
   * set, so exactly one element is selected (and thus editable).
   * 
   * If click, with shift key:
   * 
   * [] if pick set is nonempty, toggle the selected state of its frontmost item
   * 
   */
  private void doClick(UserEvent event) {
    EdObjectArray items = ScriptEditor.items();
    if (!event.isShift()) {
      if (!mPickSet.isEmpty()) {
        walkThroughPickSet();
      } else {
        items.unselectAll();
      }
    } else {
      if (!mPickSet.isEmpty()) {
        int index = mPickSet.last();
        SlotList single = new SlotList(index);
        SlotList current = items.getSelectedSlots();
        if (current.contains(index))
          current = current.minus(single);
        else
          current = SlotList.union(current, single);
        items.setSelected(current);
        unimp("should this be done as a command?");
      }
    }

  }

  /**
   * Find item immediately following the last currently selected item in the
   * pick set, and select that item. If no following item exists, select the
   * first item.
   */
  private void walkThroughPickSet() {

    if (mPickSet.isEmpty())
      throw new IllegalArgumentException();
    // Look through pick set to find item following last selected item
    int outputSlot = -1;
    EdObjectArray items = ScriptEditor.items();

    // Walk from highest to lowest, since frontmost are highest
    for (int cursor = mPickSet.size() - 1; cursor >= 0; cursor--) {
      int slot = mPickSet.get(cursor);
      if (items.getSelectedSlots().contains(slot)) {
        outputSlot = -1;
      } else {
        if (outputSlot < 0)
          outputSlot = slot;
      }
    }
    if (outputSlot < 0)
      outputSlot = mPickSet.last();
    items.setSelected(new SlotList(outputSlot));
  }

  private void doContinueDrag(UserEvent event) {
  }

  private void doStartDrag(UserEvent event) {
    /**
     * 
     * If drag, no shift key:
     * 
     * [] If an object is editable, see if press starts an editing operation
     * with it;
     * 
     * [] else, if pick set contains any selected objects, start a move
     * operation with the selection;
     * 
     * [] else, if pick set contains any objects, unselect and move just the
     * topmost;
     * 
     * [] else, unselect all items, start a drag rectangle operation, and select
     * the items contained within the rectangle.
     * 
     * If drag, with shift key:
     * 
     * [] Leave any existing selected set intact, and start a drag rectangle
     * operation, and add the enclosed items to the selected set.
     * 
     * If drag, right mouse button:
     * 
     * [] start move focus operation
     */

    if (event.isRight()) {
      MoveFocusOper.start(event);
      return;
    }

    if (!event.isShift()) {
      UserOperation oper = findOperationForEditableObject();
      if (oper != null) {
        event.setOperation(oper);
        return;
      }
      if (!mPickSetSelected.isEmpty()) {
        oper = MouseOperMoveItems.build(mInitialDownEvent);
        event.setOperation(oper);
      } else if (!mPickSet.isEmpty()) {
        unimp("consider doing selection changes as commands");
        ScriptEditor.items().setSelected(new SlotList(mPickSet.last()));
        oper = MouseOperMoveItems.build(mInitialDownEvent);
        event.setOperation(oper);
      } else {
        ScriptEditor.items().unselectAll();
        oper = RectangleSelectOper.build(mInitialDownEvent);
        event.setOperation(oper);
      }
    } else {
      event.setOperation(RectangleSelectOper.build(mInitialDownEvent));
    }
  }

  /**
   * Determine if there's an editable object which can construct an edit
   * operation for a particular location. If so, return that operation
   */
  private UserOperation findOperationForEditableObject() {
    int editableSlot = ScriptEditor.items().getEditableSlot(this);
    if (editableSlot < 0)
      return null;
    EdObject obj = ScriptEditor.items().get(editableSlot);
    UserOperation operation = obj.getFactory().isEditingSelectedObject(
        editableSlot, obj, mInitialDownEvent);
    return operation;
  }

  private UserEvent mInitialDownEvent;
  private boolean mIsDrag;
  private SlotList mPickSet;
  private SlotList mPickSetSelected;
}
