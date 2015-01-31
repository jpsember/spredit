package com.js.scredit;

import com.js.editor.Command;

public class CopyReversible extends Command.Adapter {
  /*
   * Fwd: replace clipboard with selected items
   * 
   * Rev: replace clipboard with previous clipboard
   */

  public CopyReversible() {
    mSlots = ScriptEditor.items().getSelectedSlots();
    if (!mSlots.isEmpty()) {
      oldClipboard = ScriptEditor.clipboard();
    }
  }

  @Override
  public String toString() {
    return "Copy " + EdTools.itemsStr(mSlots.size());
  }

  @Override
  public Command getReverse() {

    return new Command.Adapter() {

      @Override
      public void perform() {
        ScriptEditor.setClipboard(oldClipboard);
      }

      @Override
      public Command getReverse() {
        return CopyReversible.this;
      }

    };
  }

  @Override
  public void perform() {
    ScriptEditor.setClipboard(new EdObjectArray(ScriptEditor.items(), mSlots));
    // ScriptEditor.items().get(slots));
    Dup.reset();
  }

  @Override
  public boolean valid() {
    return !mSlots.isEmpty();
  }

  private SlotList mSlots;
  private EdObjectArray oldClipboard;

}
