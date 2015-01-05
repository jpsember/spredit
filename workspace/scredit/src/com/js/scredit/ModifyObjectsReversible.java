package com.js.scredit;

import apputil.*;
import static com.js.basic.Tools.*;

/**
 * General purpose reversible function that saves the original
 * items in an internal buffer for restoring by the reverse method
 */
public class ModifyObjectsReversible implements Reversible {
  private static final boolean db = false;

  /**
   * Constructor; uses selected items as originals
   */
  public ModifyObjectsReversible() {
    construct(ScriptEditor.items().getSelected());
  }

  /**
   * Constructor
   * @param slot slot of the single item about to be changed
   */
  public ModifyObjectsReversible(int slot) {
    int[] s = new int[1];
    s[0] = slot;
    construct(s);
  }

  private EdObject[] currentSlotContents() {
    EdObject[] a = new EdObject[slots.length];
    for (int i = 0; i < slots.length; i++)
      a[i] = ScriptEditor.items().get(slots[i]);
    return a;
  }
  public void updateModifiedObjects() {
    if (db)
      pr("updateModifiedObjects for " + this);
    ObjArray a = ScriptEditor.items();
    modObjects = a.getArray(slots);
  }

  private void construct(int[] slots) {
    this.slots = slots;
    this.origObjects = currentSlotContents();
    if (db) {
      pr("constructed origObjects:");
      for (int i = 0; i < origObjects.length; i++)
        pr(" " + origObjects[i]);
    }
  }

  public void setName(String operName) {
    this.operName = operName;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(operName);
    sb.append(' ');
    if (slots.length == 1)
      sb.append(origObjects[0]);
    else
      sb.append(slots.length + " items");
    if (db) {
      sb.append("\n");
      for (int i = 0; i < slots.length; i++) {
        sb.append("slot #" + slots[i] + " " + origObjects[i]);
      }
    }
    return sb.toString();
  }

  @Override
  public Reverse getReverse() {
    ASSERT(origObjects != null);

    return new Reverse() {
      @Override
      public void perform() {
        constructModifiedVersions();

        ObjArray a = ScriptEditor.items();
        a.set(slots, origObjects);
        a.clearAllSelected();
        a.setSelected(slots, true);
      }
    };
  }

  private void constructModifiedVersions() {
    final boolean db = false;
    if (db)
      pr("constructModifiedVersions for oper:" + operName);

    if (modObjects == null) {

      modObjects = new EdObject[nSlots()];
      // ObjArray items = ScriptEditor.items();
      for (int i = 0; i < nSlots(); i++) {
        EdObject origObj = origObjects[i];
        EdObject modObj = perform(origObj);
        if (modObj != origObj) {
          changesMade = true;
        }
        modObjects[i] = modObj;
      }
    } else {
      if (db)
        pr(" (already existed)");

    }

  }
  private boolean changesMade;

  @Override
  public void perform() {

    // if we already have modified versions of the items, 
    // use them

    if (modObjects != null) {
      ObjArray a = ScriptEditor.items();
      a.set(slots, modObjects);
      a.clearAllSelected();
      a.setSelected(slots, true);
    } else {
      // otherwise, call user method with fresh copy of item
      ObjArray items = ScriptEditor.items();
      for (int i = 0; i < nSlots(); i++) {
        EdObject origObj = origObjects[i];
        EdObject modObj = perform(origObj);
        if (modObj != origObj) {
          items.set(slots[i], modObj);
        }
      }
    }
  }

  /**
   * Apply changes to a single object; return original object if
   * changes will have no effecct
   * @param origObj original object
   * @return original object, or modified copy
   */
  public EdObject perform(EdObject origObj) {
    return origObj;
  }

  @Override
  public boolean valid() {
    constructModifiedVersions();
    return changesMade;
  }

  public int nSlots() {
    return slots.length;
  }

  /**
   * Get an array containing the original objects
   */
  public EdObject[] getOrigObjects() {
    return origObjects;
  }

  private int[] slots;
  private EdObject[] origObjects;
  // saved copy of modified objects, stored just prior to performing reverse
  private EdObject[] modObjects;
  private String operName = "Modify";
}
