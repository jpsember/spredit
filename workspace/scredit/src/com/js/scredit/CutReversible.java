package com.js.scredit;

import static com.js.basic.Tools.*;

import com.js.editor.Command;

public class CutReversible extends Command.Adapter {

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
  public Command getReverse() {
    return new Command.Adapter() {

      @Override
      public void perform() {
        EdObjectArray clip = ScriptEditor.clipboard();
        ASSERT(clip.size() == slots.length);
        EdObjectArray items = ScriptEditor.items();

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

    EdObjectArray items = ScriptEditor.items();

    EdObjectArray newClip = new EdObjectArray(items, slots);
    ScriptEditor.setClipboard(newClip);
    removeObjects(items, slots);
  }

  private static void removeObjects(EdObjectArray objects, int[] slots) {
    for (int i = slots.length - 1; i >= 0; i--) {
      objects.remove(slots[i]);
    }
  }

  private int[] slots;
  private EdObjectArray origClipboard;

  @Override
  public boolean shouldBeEnabled() {
    return slots != null;
  }

}
