package com.js.scredit;

import apputil.*;

/**
 * Operation to determine if editing a particular element of a selected item is
 * to start
 */
public class EditSelectedItemOper extends MouseOper {

  @Override
  public boolean mouseDown() {
    boolean f = false;
    do {

      if (right() || ev.isControlDown() || ev.isMetaDown() || ev.isShiftDown())
        break;

      // look through selected elements, front to rear,
      // looking for element at mouse point that can be edited

      EdObjectArray a = ScriptEditor.items();
      for (int i = a.size() - 1; i >= 0; i--) {
        EdObject obj = a.get(i);
        if (!obj.isSelected())
          continue;

        EdObjectFactory fa = obj.getFactory();

        // call factory to see if mouse pressed at editable element of this
        // object.
        // If so, it should return the new MouseOper.

        MouseOper newOper = fa.isEditingSelectedObject(i, obj, currentPt);
        if (newOper != null) {
          MouseOper.setOperation(newOper);
          f = true;
          break;
        }
      }
    } while (false);
    return f;
  }

  @Override
  public void mouseUp() {
    throw new IllegalStateException();
  }

  @Override
  public void mouseMove(boolean drag) {
    throw new IllegalStateException();
  }

  @Override
  public void paint() {
    throw new IllegalStateException();
  }
}
