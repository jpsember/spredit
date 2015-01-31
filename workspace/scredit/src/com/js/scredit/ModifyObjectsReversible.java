package com.js.scredit;

import static com.js.basic.Tools.*;

import com.js.editor.Command;

/**
 * General purpose reversible function that saves the original items in an
 * internal buffer for restoring by the reverse method
 * 
 * deprecated try to put this in the editor project
 */
public class ModifyObjectsReversible extends Command.Adapter {
  private static final boolean db = false;

  /**
   * Constructor; uses selected items as originals
   */
  public ModifyObjectsReversible() {
    construct(ScriptEditor.items().getSelectedSlots());
  }

  /**
   * Constructor
   * 
   * @param slot
   *          slot of the single item about to be changed
   */
  public ModifyObjectsReversible(int slot) {
    construct(new SlotList(slot));
  }

  private EdObject[] currentSlotContents() {
    EdObject[] a = new EdObject[slots.size()];
    for (int i = 0; i < slots.size(); i++)
      a[i] = ScriptEditor.items().get(slots.get(i));
    return a;
  }

  public void updateModifiedObjects() {
    if (db)
      pr("updateModifiedObjects for " + this);
    EdObjectArray a = ScriptEditor.items();
    modObjects = getArray(a, slots);
  }

  private static EdObject[] getArray(EdObjectArray objects, SlotList slots) {
    EdObject[] a = new EdObject[slots.size()];
    for (int i = 0; i < slots.size(); i++)
      a[i] = objects.get(slots.get(i));
    return a;
  }

  private void construct(SlotList slots) {
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
    if (slots.size() == 1)
      sb.append(origObjects[0]);
    else
      sb.append(slots.size() + " items");
    if (db) {
      sb.append("\n");
      for (int i = 0; i < slots.size(); i++) {
        sb.append("slot #" + slots.get(i) + " " + origObjects[i]);
      }
    }
    return sb.toString();
  }

  @Override
  public Command getReverse() {
    ASSERT(origObjects != null);
    return new Command.Adapter() {
      @Override
      public void perform() {
        constructModifiedVersions();

        EdObjectArray a = ScriptEditor.items();
        set(a, slots, origObjects);
        a.setSelected(slots);
      }

      @Override
      public Command getReverse() {
        return ModifyObjectsReversible.this;
      }
    };
  }

  private static void set(EdObjectArray objects, SlotList slots,
      EdObject[] items) {
    for (int i = 0; i < slots.size(); i++)
      objects.set(slots.get(i), items[i]);
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
      EdObjectArray a = ScriptEditor.items();
      set(a, slots, modObjects);
      a.setSelected(slots);
    } else {
      // otherwise, call user method with fresh copy of item
      EdObjectArray items = ScriptEditor.items();
      for (int i = 0; i < nSlots(); i++) {
        EdObject origObj = origObjects[i];
        EdObject modObj = perform(origObj);
        if (modObj != origObj) {
          items.set(slots.get(i), modObj);
        }
      }
    }
  }

  /**
   * Apply changes to a single object; return original object if changes will
   * have no effecct
   * 
   * @param origObj
   *          original object
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
    return slots.size();
  }

  /**
   * Get an array containing the original objects
   */
  public EdObject[] getOrigObjects() {
    return origObjects;
  }

  private SlotList slots;
  private EdObject[] origObjects;
  // saved copy of modified objects, stored just prior to performing reverse
  private EdObject[] modObjects;
  private String operName = "Modify";
}
