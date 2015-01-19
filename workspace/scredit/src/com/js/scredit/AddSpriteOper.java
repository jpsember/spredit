package com.js.scredit;

import com.js.editor.Command;
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

    case UserEvent.CODE_DOWN: {
      SpriteObject sp = new SpriteObject(null);
      sp.setLocation(event.getWorldLocation());
      ScriptEditor.items().clearAllSelected();

      // get id of sprite from panel
      SpriteObject sr = ScriptEditor.lastSprite();
      sp.setSprite(sr);

      Command oper = new AddObjectsReversible(sp);
      ScriptEditor.editor().registerPush(oper);
      ScriptEditor.perform(oper);

      // change to move oper
      UserOperation m = MouseOperMoveItems.build(event);
      event.setOperation(m);
    }
      break;

    case UserEvent.CODE_DRAG: {
    }
      break;

    case UserEvent.CODE_UP: {
      event.clearOperation();
    }
      break;
    }
  }

}
