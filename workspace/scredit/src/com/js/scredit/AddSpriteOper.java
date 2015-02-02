package com.js.scredit;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;

public class AddSpriteOper extends UserOperation {

  @Override
  public boolean shouldBeEnabled() {
    return ScriptEditor.lastSprite() != null;
  }

  @Override
  public void processUserEvent(UserEvent event) {

    switch (event.getCode()) {

    case UserEvent.CODE_DOWN:
      throw new UnsupportedOperationException();

    case UserEvent.CODE_DRAG:
      break;

    case UserEvent.CODE_UP:
      event.clearOperation();
      break;
    }
  }

}
