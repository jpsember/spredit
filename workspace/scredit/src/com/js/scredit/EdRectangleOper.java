package com.js.scredit;

import apputil.*;
import com.js.geometry.*;
import static com.js.basic.Tools.*;

public class EdRectangleOper extends MouseOper {
  private static final boolean db = false;

  /**
   * Construct an editor for a rectangle
   * @param slot slot containing rectangle, or -1 to add new rectangle to end
   * @param element which element of rectangle is being edited (corner 0..3, edge 4..7)
   */
  public EdRectangleOper(IPoint initialMousePt, int slot, int element) {

    this.slot = slot;
    this.editElement = element;
    if (db)
      pr("constructed EdRectangleOper for slot " + slot + ", element" + element);

    if (slot >= 0) {
      origRect = (RectangleObject) ScriptEditor.items().get(slot);
      Point actualLoc = origRect.getElementPosition(element);
      grabOffset = new Point(actualLoc.x - initialMousePt.x, actualLoc.y
          - initialMousePt.y);
      if (db)
        pr("grabOffset=" + grabOffset);

    }

  }

  /**
   * Update this operation in response to a mouse drag / move event
   * @param drag true if drag; false if hover
   */
  public void mouseMove(boolean drag) {
    if (!drag)
      return;

    Point adjMousePt = Point.sum(currentPtF, grabOffset);
    Point rectPt = adjMousePt;
    Point pt = Grid.snapToGrid(rectPt, true);

    RectangleObject r2 = origRect.setElementPosition(editElement, pt);
    ScriptEditor.items().set(slot, r2);

    ScriptEditor.setInfo(r2);
  }
  public String toString() {
    return "Rectangle Editing";
  }

  @Override
  public boolean mouseDown() {

    if (db)
      pr("mouseDown, right=" + right());

    if (!right()) {
      if (slot < 0) {
        // create a new rectangle at mouse position
        origRect = new RectangleObject(ScriptEditor.color(), currentPtF,
            currentPtF);
        grabOffset = new Point();
        editElement = 0;
        slot = ScriptEditor.items().size();

        Reversible rev = new AddObjectsReversible(origRect);
        ScriptEditor.editor().registerPush(rev);
        ScriptEditor.perform(rev);
      }

      // we must return true, otherwise the operation will be cleared
      return true;
    } else
      return false;
  }
  @Override
  public void mouseUp() {
    clearOperation();
  }

  @Override
  public void start() {
    warning("redo doesn't seem to work right away");

    if (db)
      pr("EdRectangleOper, start; slot=" + slot + " rect=" + origRect);

    // create a new Reversible, one that either adds new polygon, or edits existing one,
    // based upon whether an item in the slot exists

    Reversible rev = null;

    if (slot >= 0) {
      rev = new ModifyObjectsReversible(slot);
      if (db)
        ((ModifyObjectsReversible) rev).setName("(EditRectangle)");

      if (db)
        pr(" constructed ModObjRev for slot " + slot + ", rect " + origRect);

      ScriptEditor.editor().registerPush(rev);
      ScriptEditor.perform(rev);

    }

  }

  @Override
  public void stop() {

    final boolean db = false;
    if (db)
      pr("EdRectangleOper, stop");

    // Update the last reversible action to reflect the final state of the polygon.

    // The top of stack reversible action is either
    //  1) AddObjectsOper
    //  2) ModifyObjectsReversible
    //

    // If final object is not well defined {
    //     If stacked reversible was AddObjectsOper, pop stack
    //   else
    //     replace stack reversible with DeleteObjectsReversible
    // }

    if (!ScriptEditor.items().get(slot).isWellDefined()) {
      if (db)
        pr(" object not well defined, deleting");

      // if we were adding this polygon, pop the add operation
      // from the undo stack
      Reversible tos = ScriptEditor.editor().registerPeek();

      if (tos instanceof AddObjectsReversible) {
        ScriptEditor.editor().registerPop();
        ScriptEditor.items().remove(slot);
      } else {
        ASSERT(tos instanceof ModifyObjectsReversible);
        ModifyObjectsReversible mr = (ModifyObjectsReversible) tos;
        ASSERT(mr.nSlots() == 1);

        // undo the modify action to restore the original object
        mr.getReverse().perform();

        Reversible del = new DeleteItemReversible(slot);
        ScriptEditor.editor().registerPop();
        ScriptEditor.editor().registerPush(del);
        ScriptEditor.perform(del);
      }
    } else {

      if (db)
        pr(" modified object is well defined");

      Reversible tos = ScriptEditor.editor().registerPeek();

      if (tos instanceof ModifyObjectsReversible) {
        ModifyObjectsReversible mr = (ModifyObjectsReversible) tos;
        if (db)
          pr(" updating " + mr);
        mr.updateModifiedObjects();
      }
    }
  }

  // copy of original rectangle before editing
  private RectangleObject origRect;

  private int slot;
  private int editElement;
  private Point grabOffset;
}
