package com.js.scredit;

import com.js.editor.Reverse;
import com.js.editor.Reversible;

import static com.js.basic.Tools.*;

public class DeleteItemReversible implements Reversible, Reverse {
  private static final boolean db = false;

  /**
   * Constructor
   * @param slots slots of items about to be changed
   */
  public DeleteItemReversible(int slot, EdObject obj, boolean deleteMode) {
    this.slot = slot;
    this.delete = deleteMode;
    this.origObject = obj;
  }

  /**
   * Constructor
   * @param slots slots of items about to be changed
   */
  public DeleteItemReversible(int slot) {
    this(slot, ScriptEditor.items().get(slot), true);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(delete ? "Delete " : "Insert ");
    sb.append(origObject);
    return sb.toString();
  }

  @Override
  public Reverse getReverse() {
    Reverse r = new DeleteItemReversible(slot, origObject, !delete);
    return r;
  }

  @Override
  public void perform() {
    if (db)
      pr("Perform " + this);

    EdObjectArray a = ScriptEditor.items();

    if (delete)
      a.remove(slot);
    else
      a.add(slot, origObject);
  }

  @Override
  public boolean valid() {
    return true;
  }

  private int slot;
  private boolean delete;
  private EdObject origObject;
}
