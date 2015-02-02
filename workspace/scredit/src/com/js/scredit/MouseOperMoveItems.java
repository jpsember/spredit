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
      event.clearOperation();
      break;

    }
  }

  private void updateMove(UserEvent event) {
    Point translate = Point.difference(event.getWorldLocation(),
        mInitialDownEvent.getWorldLocation());

    // Construct a transformation matrix to represent the movement
    Matrix matrix = Matrix.getTranslate(translate);

    String msg = null;
    EdObjectArray items = mutableCopyOf(ScriptEditor.items());
    for (int slot : items.getSelectedSlots()) {
      EdObject orig = mCommand.getOriginalState().getObjects().get(slot);
      EdObject object = mutableCopyOf(orig);

      object.applyTransform(matrix);

      items.set(slot, object);
      if (msg == null) {
        msg = object.getInfoMsg();
      }
    }
    ScriptEditor.setItems(items);
    ScriptEditor.setInfo(msg);
  }

  private MouseOperMoveItems(UserEvent initialDownEvent) {
    mInitialDownEvent = initialDownEvent;
    mCommand = new CommandForGeneralChanges("Move").setMergeKey("move");
  }

  private UserEvent mInitialDownEvent;
  private CommandForGeneralChanges mCommand;
}
