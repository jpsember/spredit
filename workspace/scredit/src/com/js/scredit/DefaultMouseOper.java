package com.js.scredit;

import java.util.List;

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
    mPickSet = SlotList.build();
    mPickSetSelected = SlotList.build();
    for (int i = 0; i < items.size(); i++) {
      EdObject obj = items.get(i);
      if (!obj.contains(event.getWorldLocation()))
        continue;
      mPickSet.add(i);
      if (obj.isSelected())
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
    if (!event.isShift()) {
      if (!mPickSet.isEmpty()) {
        walkThroughPickSet();
      } else {
        ScriptEditor.items().unselectAll();
      }
    } else {
      if (!mPickSet.isEmpty()) {
        EdObject frontmostItem = ScriptEditor.items().get(last(mPickSet));
        frontmostItem.setSelected(!frontmostItem.isSelected());
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
      EdObject obj = items.get(slot);
      if (obj.isSelected()) {
        outputSlot = -1;
      } else {
        if (outputSlot < 0)
          outputSlot = slot;
      }
    }
    if (outputSlot < 0)
      outputSlot = last(mPickSet);
    items.setSelected(SlotList.build(outputSlot));
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
        event.setOperation(MouseOperMoveItems.build(mInitialDownEvent));
      } else if (!mPickSet.isEmpty()) {
        unimp("consider doing selection changes as commands");
        ScriptEditor.items().setSelected(SlotList.build(last(mPickSet)));
        event.setOperation(MouseOperMoveItems.build(mInitialDownEvent));
      } else {
        ScriptEditor.items().unselectAll();
        event.setOperation(RectangleSelectOper.build(mInitialDownEvent));
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
    int editableSlot = getEditableSlot();
    if (editableSlot < 0)
      return null;
    EdObject obj = ScriptEditor.items().get(editableSlot);
    UserOperation operation = obj.getFactory().isEditingSelectedObject(
        editableSlot, obj, mInitialDownEvent);
    return operation;
  }

  /**
   * Determine which slot, if any, holds the (at most one) editable object
   * 
   * @return slot if found, or -1
   */
  private int getEditableSlot() {
    EdObjectArray items = ScriptEditor.items();
    List<Integer> selected = items.getSelectedSlots();
    if (selected.size() != 1)
      return -1;
    int slot = selected.get(0);
    EdObject src = items.get(slot);
    if (!src.isEditable())
      return -1;
    return slot;
  }

  private UserEvent mInitialDownEvent;
  private boolean mIsDrag;
  private List<Integer> mPickSet;
  private List<Integer> mPickSetSelected;
}
