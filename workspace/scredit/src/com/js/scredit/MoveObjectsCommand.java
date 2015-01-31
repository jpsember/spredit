package com.js.scredit;

import com.js.editor.Command;
import com.js.geometry.*;

public class MoveObjectsCommand extends Command.Adapter {

  /**
   * Constructor
   * 
   * @param startLoc
   *          initial mouse position
   */
  public MoveObjectsCommand(Point startLoc) {
    this.startLoc = new Point(startLoc);

    EdObjectArray itm = ScriptEditor.items();
    slots = itm.getSelectedSlots();
    origLocs = new Point[slots.size()];
    translate = new Point();

    for (int i = 0; i < slots.size(); i++)
      origLocs[i] = itm.get(slots.get(i)).location();
    origDupAccum = Dup.getAccum(false);
    origDupClipAdjust = Dup.getClipboardAdjust();
  }

  private Point origDupAccum, origDupClipAdjust;

  /**
   * Update the move operation based on new mouse position
   * 
   * @param mouseLoc
   *          position of mouse in world space
   */
  public void update(Point mouseLoc) {

    translate = Point.difference(new Point(mouseLoc), startLoc);

    perform();
  }

  public Point getTranslate() {
    return translate;
  }

  @Override
  public String toString() {
    return "Move " + slots.size() + " item" + (slots.size() > 1 ? "s" : "");
  }

  // Reversible interface
  // --------------------------------------

  @Override
  public void perform() {

    String msg = null;

    for (int i = 0; i < slots.size(); i++) {
      int slot = slots.get(i);
      EdObject objCurr = ScriptEditor.items().get(slot);

      Point newLoc = Point.sum(origLocs[i], translate);
      newLoc = Grid.snapToGrid(newLoc, true);

      objCurr.setLocation(newLoc);

      if (msg == null) {
        msg = objCurr.getInfoMsg();
      }

    }
    ScriptEditor.setInfo(msg);

    Point a = Point.sum(origDupAccum, translate);
    Point b = Point.sum(origDupClipAdjust, translate);
    Dup.setAccum(a);
    Dup.setClipboardAdjust(b);
  }

  @Override
  public boolean valid() {
    return slots != null && slots.size() > 0;
  }

  @Override
  public Command getReverse() {
    return new Command.Adapter() {

      @Override
      public void perform() {
        EdObjectArray items = ScriptEditor.items();
        for (int i = 0; i < slots.size(); i++) {
          EdObject obj = items.get(slots.get(i));
          obj.setLocation(origLocs[i]);
        }
      }
    };
  }

  public boolean sameItemsAs(MoveObjectsCommand oper) {
    return SlotList.equal(slots, oper.slots);
  }

  private SlotList slots;
  private Point[] origLocs;
  private Point translate;
  private Point startLoc;

  public void continueWithNewMouseDown(Point currentPtF) {
    this.startLoc = Point.difference(currentPtF, translate);
  }

}
