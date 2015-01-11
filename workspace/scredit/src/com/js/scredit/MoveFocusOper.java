package com.js.scredit;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.*;

public class MoveFocusOper extends UserOperation {

  public static void start(UserEvent event) {
    MoveFocusOper oper = new MoveFocusOper();
    event.setOperation(oper);
    oper.processUserEvent(event);
  }

  private MoveFocusOper() {
  }

  @Override
  public void processUserEvent(UserEvent event) {
    switch (event.getCode()) {

    case UserEvent.CODE_DRAG:
      if (mOriginalFocus == null) {
        mStartEvent = event;
        mOriginalFocus = new IPoint(ScriptEditor.focus());
      }

      IPoint trans = IPoint.difference(mStartEvent.getViewLocation(),
          event.getViewLocation());
      // compensate for view and world having flipped y axes
      trans.y = -trans.y;
      trans.applyScale(1 / ScriptEditor.zoomFactor());
      trans.add(mOriginalFocus);
      ScriptEditor.setFocus(trans);
      break;

    case UserEvent.CODE_UP:
      event.clearOperation();
      break;
    }
  }

  private IPoint mOriginalFocus;
  private UserEvent mStartEvent;
}
