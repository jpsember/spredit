package com.js.scredit;

import java.util.*;

import com.js.editor.Command;
import com.js.geometry.*;

public class MoveObjectsReversible extends Command.Adapter {

  /**
   * Constructor
   * 
   * @param startLoc
   *          initial mouse position
   */
  public MoveObjectsReversible(Point startLoc) {
    this.startLoc = new Point(startLoc);

    EdObjectArray itm = ScriptEditor.items();
    slots = itm.getSelected();
    origLocs = new Point[slots.length];
    translate = new Point();

    for (int i = 0; i < slots.length; i++)
      origLocs[i] = itm.get(slots[i]).location();
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

  // private int id = baseId++;
  // private static int baseId = 500;
  @Override
  public String toString() {
    return "Move " + slots.length + " item" + (slots.length > 1 ? "s" : "");// +
                                                                            // " "
                                                                            // +
                                                                            // id;
  }

  // Reversible interface
  // --------------------------------------

  @Override
  public void perform() {

    String msg = null;

    for (int i = 0; i < slots.length; i++) {
      int slot = slots[i];
      EdObject objCurr = ScriptEditor.items().getCopy(slot);

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
  public boolean shouldBeEnabled() {
    return slots != null && slots.length > 0;
  }

  @Override
  public Command getReverse() {
    return new Command.Adapter() {

      @Override
      public void perform() {
        EdObjectArray items = ScriptEditor.items();
        for (int i = 0; i < slots.length; i++) {
          EdObject obj = items.getCopy(slots[i]);
          obj.setLocation(origLocs[i]);
        }
      }
    };
  }

  public boolean sameItemsAs(MoveObjectsReversible oper) {
    boolean same = false;
    do {
      if (!Arrays.equals(slots, oper.slots))
        break;
      same = true;
    } while (false);
    return same;
  }

  private int[] slots;
  private Point[] origLocs;
  private Point translate;
  private Point startLoc;

  public void continueWithNewMouseDown(Point currentPtF) {
    this.startLoc = Point.difference(currentPtF, translate);
  }

}
