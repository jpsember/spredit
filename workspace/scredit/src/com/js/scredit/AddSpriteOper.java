package com.js.scredit;

import static com.js.basic.Tools.*;
import apputil.*;

public class AddSpriteOper extends MouseOper {
  private static final boolean db = false;

  @Override
  public boolean mouseDown() {
    if (db)
      pr("AddSpriteOper.mouseDown");

    boolean f = false;
    do {
      SpriteObject sp = new SpriteObject();
      sp.setLocation(currentPtF);
      ScriptEditor.items().clearAllSelected();

      // get id of sprite from panel
      SpriteObject sr = ScriptEditor.lastSprite();
      sp.setSprite(sr);

      Reversible oper = new AddObjectsReversible(sp);
      ScriptEditor.editor().registerPush(oper);
      ScriptEditor.perform(oper);

      // change to move oper

      MouseOper m = MouseOperSelectItems.startMovingSelectedItems();
      
      if (db)
        pr(" changing MouseOper to " + m);

      MouseOper.setOperation(m);
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
  //private AddObjectsReversible oper;
}
