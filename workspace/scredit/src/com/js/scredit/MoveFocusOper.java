package com.js.scredit;

import com.js.editor.MouseOper;
import com.js.geometry.*;

public class MoveFocusOper extends MouseOper {

  @Override
  public boolean mouseDown() {
    boolean f = false;
    do {
      if (!right(ev) || ev.isControlDown() || ev.isShiftDown())
        break;
      startFocus = new IPoint(ScriptEditor.focus());
      f = true;
    } while (false);
    return f;
  }

  @Override
  public void mouseMove(boolean drag) {
    if (drag) {
      IPoint trans = IPoint.difference(startPtView, currentPtView);
      // compensate for view and world having flipped y axes
      trans.y = -trans.y;

      trans.applyScale(1 / ScriptEditor.zoomFactor());
      trans.add(startFocus);
      ScriptEditor.setFocus(trans);
      // focus = trans;
      // warn("is repaint necessary?");
      // ScriptEditor.repaint();
    }
  }

  private IPoint startFocus;
}
