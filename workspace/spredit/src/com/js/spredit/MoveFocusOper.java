package com.js.spredit;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.IPoint;
import com.js.geometry.Point;

public class MoveFocusOper extends UserOperation {

  private MoveFocusOper() {
  }

  @Override
  public void start() {
    SpritePanel spritePanel = SpriteEditor.getSpritePanel();
    startOrigin = new Point(spritePanel.getOrigin());
  }

  @Override
  public void stop() {
    SpritePanel spritePanel = SpriteEditor.getSpritePanel();
    spritePanel.setHighlightClip(false);
  }

  @Override
  public void processUserEvent(UserEvent event) {
    switch (event.getCode()) {
    case UserEvent.CODE_DOWN:
      mInitialEvent = event;
      break;

    case UserEvent.CODE_DRAG:
      SpritePanel spritePanel = SpriteEditor.getSpritePanel();

      Point trans = new Point(IPoint.difference(
          mInitialEvent.getViewLocation(), event.getViewLocation()));
      // compensate for view and world having flipped y axes
      trans.y = -trans.y;

      trans.applyScale(1 / spritePanel.getZoom());
      trans.add(startOrigin);
      spritePanel.setOrigin(trans);
      break;

    case UserEvent.CODE_UP:
      event.clearOperation();
      break;
    }
  }

  /**
   * Given a mouse down event, attempt to build an appropriate operation
   */
  public static UserOperation buildFor(UserEvent ev) {
    do {
      if (ev.isCtrl() || ev.isShift())
        break;
      return new MoveFocusOper();
    } while (false);
    return null;
  }

  private UserEvent mInitialEvent;
  private Point startOrigin;
}
