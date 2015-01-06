package com.js.scredit;

import static com.js.basic.Tools.*;
import apputil.*;

public class CutReversible implements Reversible {

  /*
   * 
   * Fwd: create undoOper from current items, clipboard construct clipboard from
   * slots delete slot items
   * 
   * 
   * Bwd: undoOper
   */
  public CutReversible() {
    int[] si = ScriptEditor.items().getSelected();
    if (si.length > 0) {
      this.slots = si;
      origClipboard = ScriptEditor.clipboard();
    }
  }

  @Override
  public String toString() {
    return "Cut " + EdTools.itemsStr(slots.length);
  }

  @Override
  public Reverse getReverse() {
    return new Reverse() {

      @Override
      public void perform() {
        ObjArray clip = ScriptEditor.clipboard();
        ASSERT(clip.size() == slots.length);
        ObjArray items = ScriptEditor.items();

        for (int i = 0; i < slots.length; i++) {
          items.add(slots[i], clip.get(i));
        }
        items.clearAllSelected();
        items.setSelected(slots, true);

        ScriptEditor.setClipboard(origClipboard);
      }

    };
  }

  @Override
  public void perform() {
    Dup.reset();

    ObjArray items = ScriptEditor.items();

    ObjArray newClip = new ObjArray(items, slots);
    ScriptEditor.setClipboard(newClip);
    removeObjects(items, slots);
  }

  private static void removeObjects(ObjArray objects, int[] slots) {
    for (int i = slots.length - 1; i >= 0; i--) {
      objects.remove(slots[i]);
    }
  }

  private int[] slots;
  private ObjArray origClipboard;

  @Override
  public boolean valid() {
    return slots != null;
  }

}
