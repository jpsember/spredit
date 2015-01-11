package com.js.scredit;

import java.util.List;

import com.js.editor.Command;
import com.js.editor.MouseOper;
import com.js.editor.UserEvent;
import com.js.geometry.*;

import static com.js.basic.Tools.*;

public class MouseOperMoveItems extends MouseOper {

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
      unimp("distinguish between finishing and aborting operation; if aborting, restore editor state to initial");
      MouseOper.clearOperation();
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

  @Override
  public boolean mouseDown() {
    // This operation is started only by MouseOperSelectItems
    throw new IllegalStateException();
  }

  @Override
  public void mouseMove(boolean drag) {
    unimp("have the operation do the movement; encapsulate movement simply as new editor state");
    // mMoveCommand.update(currentPtF);
  }

  @Override
  public void mouseUp() {
    unimp("do the actual comand, but only if movement occurred");
    clearOperation();
  }

  @Override
  public void stop() {
    warning("what is distinction between mouseUp and stop?  is stop sort of like cancel or abort?");
  }

  private UserEvent mInitialDownEvent;
  private ScriptEditorState mInitialEditorState;
}
