package com.js.scredit;

import com.js.editor.Command;
import com.js.editor.MouseOper;
import com.js.geometry.*;

import static com.js.basic.Tools.*;

public class MouseOperMoveItems extends MouseOper {

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
    // if (mMoveCommand.getTranslate().magnitude() == 0) {
    // ScriptEditor.editor().registerPop();
    // }
  }

}