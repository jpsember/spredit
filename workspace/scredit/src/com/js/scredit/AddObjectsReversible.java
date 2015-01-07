package com.js.scredit;

import com.js.editor.Command;
import com.js.editor.MouseOper;
import com.js.geometry.IPoint;
import com.js.geometry.Point;

class AddObjectsReversible extends Command.Adapter {

  /**
   * Constructor for adding a single object to end of existing items
   * 
   * @param obj
   */
  public AddObjectsReversible(EdObject obj) {
    addObj = obj;
    slot = ScriptEditor.items().size();
  }

  public void perform() {
    EdObjectArray items = ScriptEditor.items();
    items.clearAllSelected();
    items.add(addObj);
    addObj.setSelected(true);
  }

  // /**
  // * Get slot item is to be added to
  // * @deprecated
  // * @return slot
  // */
  // private int slot() {
  // return slot;
  // }

  private int slot;

  /**
   * Move just-added objects to new location, in response to drag event
   * 
   * @deprecated
   */
  public void updateMove() {

    Point delta = new Point(IPoint.difference(MouseOper.currentPt,
        MouseOper.startPt));

    EdObjectArray items = ScriptEditor.items();

    EdObject obj = items.getCopy(items.size() - 1);
    obj.setLocation(Point.sum(addObj.location(), delta));

  }

  public Command getReverse() {
    return new Command.Adapter() {
      @Override
      public void perform() {
        // update the addObj, in case it changed since we constructed the
        // operation
        EdObjectArray a = ScriptEditor.items();
        // int slot = a.size() - 1;
        addObj = a.get(slot);
        a.remove(slot);
        // ScriptEditor.items().remove(ScriptEditor.items().size() - 1);
      }
    };
  }

  @Override
  public String toString() {
    return "Add " + addObj;
  }

  private EdObject addObj;

  /**
   * @deprecated
   * @param n
   */
  public void setObject(EdObject n) {
    addObj = n;
  }

}
