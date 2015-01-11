package com.js.scredit;

import static com.js.basic.Tools.*;

import com.js.editor.Command;
import com.js.editor.UserEvent;
import com.js.editor.UserOperation;

public class AddSpriteOper extends UserOperation {
  private static final boolean db = false;

  @Override
  public void processUserEvent(UserEvent event) {
    die("not implemented yet");
  }

  @Override
  public boolean mouseDown() {
    if (db)
      pr("AddSpriteOper.mouseDown");

    boolean f = false;
    do {
      SpriteObject sp = new SpriteObject(null);
      sp.setLocation(currentPtF);
      ScriptEditor.items().clearAllSelected();

      // get id of sprite from panel
      SpriteObject sr = ScriptEditor.lastSprite();
      sp.setSprite(sr);

      Command oper = new AddObjectsReversible(sp);
      ScriptEditor.editor().registerPush(oper);
      ScriptEditor.perform(oper);

      // change to move oper

      UserOperation m = MouseOperSelectItems.startMovingSelectedItems();
      
      if (db)
        pr(" changing MouseOper to " + m);

      UserOperation.setOperation(m);
      f = true;
    } while (false);
    return f;
  }

  @Override
  public void mouseMove(boolean drag) {
    if (drag)
      throw new IllegalStateException();
  }

  @Override
  public void mouseUp() {
    throw new IllegalStateException();
    //    if (db)
    //      pr("AddSpriteOper, mouseUp");
    //
    //    clearOperation();
  }

}
