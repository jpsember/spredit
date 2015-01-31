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
    slots = ScriptEditor.items().getSelectedSlots();
    if (!slots.isEmpty()) {
      origClipboard = ScriptEditor.clipboard();
      setDescription("Cut");
    }
  }

  @Override
  public String toString() {
    return "Cut " + EdTools.itemsStr(slots.size());
  }

  @Override
  public Command getReverse() {
    return new Command.Adapter() {

      @Override
      public void perform() {
        EdObjectArray clip = ScriptEditor.clipboard();
        ASSERT(clip.size() == slots.size());
        EdObjectArray items = ScriptEditor.items();

        for (int i = 0; i < slots.size(); i++) {
          items.add(slots.get(i), clip.get(i));
        }
        items.setSelected(slots);

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

  private static void removeObjects(EdObjectArray objects, SlotList slots) {
    for (int i = slots.size() - 1; i >= 0; i--) {
      objects.remove(slots.get(i));
    }
  }

  private SlotList slots;
  private EdObjectArray origClipboard;

  @Override
  public boolean valid() {
    return slots != null;
  }

}
