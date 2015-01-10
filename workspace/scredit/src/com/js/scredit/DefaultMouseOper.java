package com.js.scredit;

import java.util.List;

import com.js.editor.MouseOper;
import com.js.editor.UserEvent;
import static com.js.basic.Tools.*;

// TODO: look at calls to ScriptEditor and see if we can put some in an interface

public class DefaultMouseOper extends MouseOper {

  @Override
  public void processUserEvent(UserEvent event) {
    final boolean db = false;
    if (db)
      pr("DefaultMouseOper.processUserEvent " + event);

    if (processPendingAddOperation(event))
      return;

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
     * [] If pick set contains any selected objects, start a move operation with
     * the selection;
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
     */
    if (!event.isShift()) {
      if (!mPickSetSelected.isEmpty()) {
        MouseOper.setOperation(MouseOperMoveItems.build(mInitialDownEvent));
      } else if (!mPickSet.isEmpty()) {
        unimp("consider doing selection changes as commands");
        ScriptEditor.items().setSelected(SlotList.build(last(mPickSet)));
        MouseOper.setOperation(MouseOperMoveItems.build(mInitialDownEvent));
      } else {
        unimp("unselect all, start drag rectangle");
      }
    } else {
      unimp("start drag, add selected items to existing");
    }
  }

  @Override
  public boolean mouseDown() {
    // TODO Auto-generated method stub
    return false;
  }

  // If user wants to add a new object, do so
  private boolean processPendingAddOperation(UserEvent event) {

    if (!(event.isDownVariant() && !event.isRight() && !event.isMeta()))
      return false;

    MouseOper addObjectOper = ScriptEditor.sPendingAddObjectOperation;
    if (addObjectOper == null)
      return false;
    ScriptEditor.sPendingAddObjectOperation = null;

    MouseOper.setOperation(addObjectOper);
    unimp("have the 'add object' operation handle the down event to add object at mouse loc");
    MouseOper.getOperation().processUserEvent(event);

    return true;
  }

  private UserEvent mInitialDownEvent;
  private boolean mIsDrag;
  private List<Integer> mPickSet;
  private List<Integer> mPickSetSelected;
}
