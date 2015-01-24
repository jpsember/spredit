package com.js.spredit;

import com.js.editor.UserOperation;
import com.js.editor.UserEvent;
import static com.js.basic.Tools.*;

public class DefaultMouseOper extends UserOperation {

  @Override
  public void processUserEvent(UserEvent event) {
    final boolean db = true;
    if (db)
      pr("DefaultMouseOper.processUserEvent " + event);

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
  }

  /* private */UserEvent mInitialDownEvent;
  private boolean mIsDrag;
}
