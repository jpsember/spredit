package com.js.scredit;

import apputil.*;

public class CopyReversible implements Reversible {
  /*
   Fwd:
       replace clipboard with selected items
       
   Rev:
       replace clipboard with previous clipboard
   */

  public CopyReversible() {
    int[] s = ScriptEditor.items().getSelected();
    if (s.length > 0) {
      this.slots = s;
      oldClipboard = ScriptEditor.clipboard();
    }
  }

  @Override
  public String toString() {
    return "Copy " + EdTools.itemsStr(slots.length);
  }

  @Override
  public Reverse getReverse() {

    return new Reverse() {

      //      @Override
      //      public Reversible getReverse() {
      //        throw new IllegalStateException();
      //     //   return CopyReversible.this;
      //      }

      @Override
      public void perform() {
        ScriptEditor.setClipboard(oldClipboard);
      }

      //      @Override
      //      public boolean valid() {
      //        return CopyReversible.this.valid();
      //      }
    };
  }

  @Override
  public void perform() {
    ScriptEditor.setClipboard(new EdObjectArray(ScriptEditor.items(), slots));
    //    ScriptEditor.items().get(slots));
    Dup.reset();
  }
  @Override
  public boolean valid() {
    return slots != null;
  }

  private int[] slots;
  private EdObjectArray oldClipboard;
}
