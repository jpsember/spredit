package com.js.scredit;

import static com.js.basic.Tools.*;

import com.js.editor.Reverse;
import com.js.editor.Reversible;
import com.js.geometry.*;

public class AdjustSlotsReversible implements Reversible, Reverse {
  private static final boolean db = false;

  
  /**
   * Constructor
   * @param direction   negative to move to back, positive to move to front
   * @param toLimit true to move maximum amount
   */
  public AdjustSlotsReversible(int direction, boolean toLimit) {
    this.dir = direction;
    this.toLimit = toLimit;

    EdObjectArray a = ScriptEditor.items();

    srcSlots = a.getSelected();
    int len = srcSlots.length;

    destSlots = new int[len];
    if (srcSlots.length > 0) {
      int d;
      if (direction < 0)
        d = toLimit ? a.size() - (len - 1) : srcSlots[len - 1] + 1 - (len - 1);
      else
        d = toLimit ? 0 : srcSlots[0] - 1;

      d = MyMath.clamp(d, 0, a.size() - len);
      for (int i = 0; i < len; i++)
        destSlots[i] = d + i; //- (len - i);
    }

    if (db)
      pr("constructed: " + this);

  }

  /**
   * Private constructor, for building reverse operations
   */
  private AdjustSlotsReversible() {
  }

  @Override
  public String toString() {
    // we never need the 'toString' method for the REVERSE of an operation.

    ASSERT(!isRev);
    StringBuilder sb = new StringBuilder();
    sb.append("Move item");
    if (srcSlots.length != 1)
      sb.append('s');
    if (dir < 0) {
      sb.append(toLimit ? " to back" : " backward");
    } else {
      sb.append(toLimit ? " to front" : " forward");
    }
    return sb.toString();
  }

  private int nSlots() {
    return srcSlots.length;
  }

  // Reversible interface
  // --------------------------------------

  @Override
  public void perform() {
    EdObjectArray newItems = new EdObjectArray();
    EdObjectArray oldItems = ScriptEditor.items();

    int unselCursor = 0;
    int newSlotCursor = 0;
    int oldSlotCursor = 0;
    while (newItems.size() < oldItems.size()) {

      if (db)
        pr("unsel=" + unselCursor + " selSlot=" + newSlotCursor + " outSize="
            + newItems.size());

      if (newSlotCursor < nSlots()
          && newItems.size() == destSlots[newSlotCursor]) {
        if (db)
          pr(" moving sel #" + newSlotCursor + "(" + srcSlots[newSlotCursor]
              + ") to new location (" + destSlots[newSlotCursor] + ")");
        newItems.add(oldItems.get(srcSlots[newSlotCursor]));
        newSlotCursor++;
        continue;
      }
      if (oldSlotCursor < nSlots() && unselCursor == srcSlots[oldSlotCursor]) {
        if (db)
          pr(" advancing unselected cursor over old selected item #"
              + oldSlotCursor + "(" + srcSlots[oldSlotCursor] + ")");
        unselCursor++;
        oldSlotCursor++;
        continue;
      }

      if (db)
        pr(" copying unselected item (" + unselCursor + ") to new location ("
            + newItems.size() + ")");

      newItems.add(oldItems.get(unselCursor));
      unselCursor++;
    }
    if (db)
      pr("\nnew array:\n" + newItems);

    if (false) {
      int destCursor = 0;
      int srcCursor = 0;
      int oldCursor = 0;
      int newCursor = 0;

      while (destCursor < oldItems.size()) {

        if (db)
          pr("oldCursor=" + oldCursor + " newCursor=" + newCursor
              + " array size=" + destCursor + " of " + oldItems.size());

        if (newCursor < destSlots.length && destCursor == destSlots[newCursor]) {
          if (db)
            pr(" storing item #" + newCursor + "(" + srcSlots[newCursor]
                + ") in new slot " + destSlots[newCursor]);

          newItems.add(oldItems.get(srcSlots[newCursor]));
          newCursor++;
          destCursor++;
          continue;
        }

        if (oldCursor < srcSlots.length && destCursor == srcSlots[oldCursor]) {
          if (db)
            pr(" skipping over old location of item #" + oldCursor + "("
                + srcSlots[oldCursor] + ")");

          oldCursor++;
          srcCursor++;
          continue;
        }

        if (db)
          pr(" copying unselected item from old location " + srcCursor
              + " to new " + destCursor);

        newItems.add(oldItems.get(srcCursor));
        srcCursor++;
        destCursor++;
      }
    }
    ScriptEditor.setItems(newItems);
  }

  @Override
  public boolean valid() {
    boolean val = false;
    do {
      if (srcSlots.length == 0)
        break;
      for (int i = 0; i < srcSlots.length; i++)
        if (srcSlots[i] != destSlots[i]) {
          val = true;
          break;
        }
    } while (false);
    return val;
  }
  private boolean isRev;

  @Override
  public Reverse getReverse() {
    AdjustSlotsReversible a = new AdjustSlotsReversible();
    a.srcSlots = destSlots;
    a.destSlots = srcSlots;
    a.isRev = true;
    return a;
  }
  private boolean toLimit;
  private int dir;
  private int[] srcSlots;
  private int[] destSlots;
}
