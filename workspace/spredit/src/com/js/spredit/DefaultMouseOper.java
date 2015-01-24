package com.js.spredit;

import com.js.editor.UserOperation;
import com.js.editor.UserEvent;

public class DefaultMouseOper extends UserOperation {

  @Override
  public void processUserEvent(UserEvent event) {

    switch (event.getCode()) {
    case UserEvent.CODE_DOWN:
      mInitialDownEvent = event;
      mIsDrag = false;
      break;

    case UserEvent.CODE_DRAG:
      if (!mIsDrag) {
        mIsDrag = true;
        doStartDrag(event);
      }
      doContinueDrag(event);
      break;
    case UserEvent.CODE_UP:
      if (!mIsDrag)
        doClick(event);
      else
        doFinishDrag();
      break;
    }
  }

  private void doFinishDrag() {
  }

  private void doClick(UserEvent event) {
  }

  private void doContinueDrag(UserEvent event) {
  }

  private void doStartDrag(UserEvent event) {
    // Determine which operation to start
    UserOperation oper = null;
    do {
      if (!SpriteEditor.defined())
        break;
      oper = MoveClipOper.buildFor(mInitialDownEvent);
      if (oper != null)
        break;
      oper = CornerOper.buildFor(mInitialDownEvent);
      if (oper != null)
        break;
      oper = EdgeOper.buildFor(mInitialDownEvent);
      if (oper != null)
        break;
    } while (false);
    if (oper != null) {
      event.setOperation(oper);
      oper.processUserEvent(mInitialDownEvent);
      oper.processUserEvent(event);
    }
  }

  private UserEvent mInitialDownEvent;
  private boolean mIsDrag;
}
