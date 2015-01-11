package com.js.scredit;

import java.util.List;

import com.js.editor.Command;
import com.js.editor.UserOperation;
import com.js.editor.UserEvent;
import com.js.geometry.*;

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
      Command command = new CommandForGeneralChanges(mInitialEditorState, null,
          "move", "Move");
      ScriptEditor.editor().registerPush(command);
      event.clearOperation();
      break;

    }
  }

  private void updateMove(UserEvent event) {
    Point translate = Point.difference(event.getWorldLocation(),
        mInitialDownEvent.getWorldLocation());

    String msg = null;

    List<Integer> slots = mInitialEditorState.getSelectedSlots();
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
  }

  private UserEvent mInitialDownEvent;
  private ScriptEditorState mInitialEditorState;
}
