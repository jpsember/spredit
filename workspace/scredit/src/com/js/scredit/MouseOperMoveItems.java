package com.js.scredit;

import com.js.editor.UserOperation;
import com.js.editor.UserEvent;
import com.js.geometry.*;
import static com.js.basic.Tools.*;

public class MouseOperMoveItems extends UserOperation {

  public static MouseOperMoveItems build(UserEvent initialDownEvent) {
    return new MouseOperMoveItems(initialDownEvent);
  }

  @Override
  public void processUserEvent(UserEvent event) {

    switch (event.getCode()) {

    case UserEvent.CODE_DRAG:
      updateMove(event);
      break;

    case UserEvent.CODE_UP:
      mCommand.finish();
      ScriptEditor.editor().recordCommand(mCommand);
      event.clearOperation();
      break;

    }
  }

  private void updateMove(UserEvent event) {
    Point translate = Point.difference(event.getWorldLocation(),
        mInitialDownEvent.getWorldLocation());

    EdObjectArray items = mutableCopyOf(ScriptEditor.items());
    for (int slot : items.getSelectedSlots()) {
      EdObject orig = mCommand.getOriginalState().getObjects().get(slot);
      EdObject object = mutableCopyOf(orig);

      Point newLoc = Point.sum(object.location(), translate);
      newLoc = Grid.snapToGrid(newLoc, true);

      object.setLocation(newLoc);
      items.set(slot, object);
    }
    ScriptEditor.setItems(items);

    String msg = null;

    SlotList slots = mInitialEditorState.getSelectedSlots();
    for (int slot : slots) {
      EdObject object = mInitialEditorState.getObjects().get(slot);

      Point newLoc = Point.sum(object.location(), translate);
      newLoc = Grid.snapToGrid(newLoc, true);

      EdObject modObject = ScriptEditor.items().get(slot);
      modObject.setLocation(newLoc);

      if (msg == null) {
        msg = object.getInfoMsg();
      }
    }
    ScriptEditor.setInfo(msg);
  }

  private MouseOperMoveItems(UserEvent initialDownEvent) {
    mInitialDownEvent = initialDownEvent;
    mInitialEditorState = new ScriptEditorState();
    mCommand = new CommandForGeneralChanges("move", "Move");
  }

  private UserEvent mInitialDownEvent;
  private ScriptEditorState mInitialEditorState;
  private CommandForGeneralChanges mCommand;
}
