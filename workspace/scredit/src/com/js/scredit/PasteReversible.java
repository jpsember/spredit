package com.js.scredit;

import com.js.geometry.*;
import apputil.*;

public class PasteReversible implements Reversible {
  @Override
  public String toString() {
    return "Paste " + EdTools.itemsStr(ScriptEditor.clipboard().size()); // .length);ScriptEditor.clipboard().size()+" items";
  }

  public PasteReversible() {
    EdObjectArray clip = ScriptEditor.clipboard();
    if (!clip.isEmpty())
      this.clip = clip;
  }

  private EdObjectArray clip;

  @Override
  public void perform() {
    EdObjectArray items = ScriptEditor.items();
    items.clearAllSelected();

    /*
     * Determine where to place the pasted items. The dup accumulator represents
     * the distance from the 2nd to last instance of the selected item from
     * their last instance.
     * 
     * The clip adjustment represents the offset between the clipboard instance
     * and the last instance.
     * 
     * Adding these two together gives us the offset to add to the clipboard
     * instance to get the new instance.
     */

    Point ds = Point.sum(Dup.getAccum(true), Dup.getClipboardAdjust());

    for (EdObject obj : clip) {
      EdObject newInstance = obj.getCopy();
      newInstance.setLocation(Point.sum(newInstance.location(), ds));
      newInstance.setSelected(true);
      items.add(newInstance);
    }

    // add the dup accumulator to the clip adjust, to make clipboard represent
    // newest instance.
    Dup.updateClipboardAdjust();

  }

  @Override
  public Reverse getReverse() {
    return new Reverse() {

      // @Override
      // public Reversible getReverse() {
      // throw new UnsupportedOperationException();
      // }

      @Override
      public void perform() {
        // ObjArray clip = ScriptEditor.clipboard();
        EdObjectArray items = ScriptEditor.items();
        items.remove(items.size() - clip.size(), clip.size());
      }

      // @Override
      // public boolean valid() {
      // throw new UnsupportedOperationException();
      // }
    };
  }

  @Override
  public boolean valid() {
    return clip != null;
  }

}
