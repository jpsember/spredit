package com.js.scredit;

import static com.js.basic.Tools.*;

import com.js.editor.Command;
import com.js.geometry.*;

public class DuplicateReversible extends Command.Adapter {
  @Override
  public String toString() {
    return "Duplicate " + EdTools.itemsStr(slots.size());
  }

  public DuplicateReversible() {
    slots = ScriptEditor.items().getSelectedSlots();
  }

  @Override
  public boolean valid() {
    return slots.size() > 0;
  }

  @Override
  public void perform() {
    final boolean db = false;

    EdObjectArray items = ScriptEditor.items();
    items.clearAllSelected();

    /*
     * Determine where to place the duplicated items. The dup accumulator
     * represents the distance from the 2nd to last instance of the selected
     * item from their last instance.
     * 
     * ...not using clipboard, so we don't use clip adjust...
     */

    Point ds = Dup.getAccum(true); // )getFilteredDupAccum();

    for (int i = 0; i < slots.size(); i++) {
      EdObject newInstance = items.get(slots.get(i));
      if (db)
        pr("adding dupAmount " + ds + "  to duplicated object #" + i + ": "
            + newInstance);
      newInstance.setLocation(Point.sum(newInstance.location(), ds));
      newInstance.setSelected(true);
      items.add(newInstance);
    }
  }

  @Override
  public Command getReverse() {
    return new Command.Adapter() {

      @Override
      public void perform() {
        EdObjectArray items = ScriptEditor.items();
        // remove(items, items.size() - slots.length, slots.length);
        items.remove(items.size() - slots.size(), slots.size());
      }

      @Override
      public Command getReverse() {
        return DuplicateReversible.this;
      }
    };
  }

  private SlotList slots;

}
