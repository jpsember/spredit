package com.js.scredit;

import static com.js.basic.Tools.*;
import apputil.Reverse;
import apputil.Reversible;

import com.js.geometry.*;

public class DuplicateReversible implements Reversible {
  @Override
  public String toString() {
    return "Duplicate " + EdTools.itemsStr(slots.length);
  }

  public DuplicateReversible() {
    slots = ScriptEditor.items().getSelected();
  }
  @Override
  public boolean valid() {
    return slots.length > 0;
  }

  @Override
  public void perform() {
    final boolean db = false;

    EdObjectArray items = ScriptEditor.items();
    items.clearAllSelected();

    /*
     *  Determine where to place the duplicated items.
     *  The dup accumulator represents the distance from the 2nd to last instance of 
     *  the selected item from their last instance.
     *  
     *  ...not using clipboard, so we don't use clip adjust...
     */

    Point ds = Dup.getAccum(true); //)getFilteredDupAccum();

    for (int i = 0; i < slots.length; i++) {
      EdObject newInstance = items.getCopy(slots[i]);
      if (db)
        pr("adding dupAmount " + ds + "  to duplicated object #" + i + ": "
            + newInstance);
      newInstance.setLocation(Point.sum(newInstance.location(), ds));
      newInstance.setSelected(true);
      items.add(newInstance);
    }
  }

  @Override
  public Reverse getReverse() {
    return new Reverse() {

      //      @Override
      //      public Reversible getReverse() {
      //        throw new UnsupportedOperationException();
      //      }

      @Override
      public void perform() {
        EdObjectArray items = ScriptEditor.items();
        // remove(items, items.size() - slots.length, slots.length);
        items.remove(items.size() - slots.length, slots.length);
      }

      //      @Override
      //      public boolean valid() {
      //        throw new UnsupportedOperationException();
      //      }
    };
  }
  private int[] slots;

}
