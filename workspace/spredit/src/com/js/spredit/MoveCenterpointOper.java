package com.js.spredit;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.MyMath;
import com.js.geometry.Point;

public class MoveCenterpointOper extends UserOperation {

  private MoveCenterpointOper() {
  }

  @Override
  public void start() {
    SpriteInfo spriteInfo = SpriteEditor.getSpriteInfo();
    SpritePanel spritePanel = SpriteEditor.getSpritePanel();
    mOriginalCenterpointLocation = new Point(spriteInfo.centerpoint());
    spritePanel.setHighlightCenterpoint(true);
  }

  @Override
  public void stop() {
    SpritePanel spritePanel = SpriteEditor.getSpritePanel();
    spritePanel.setHighlightClip(false);
  }

  @Override
  public void processUserEvent(UserEvent event) {
    SpriteInfo spriteInfo = SpriteEditor.getSpriteInfo();
    switch (event.getCode()) {
    case UserEvent.CODE_DOWN:
      mInitialEvent = event;
      break;

    case UserEvent.CODE_DRAG:
      Point loc = Point.difference(event.getWorldLocation(),
          mInitialEvent.getWorldLocation());
      spriteInfo.setCenterpoint(Point.sum(mOriginalCenterpointLocation, loc));
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

      SpriteInfo spriteInfo = SpriteEditor.getSpriteInfo();
      // IRect clip = spriteInfo.cropRect();
      SpritePanel spritePanel = SpriteEditor.getSpritePanel();

      if (ev.isCtrl())
        break;

      if (ev.isShift())
        spriteInfo.setCenterpoint(ev.getWorldLocation());

      Point origLoc = spriteInfo.centerpoint();
      float dist = MyMath.distanceBetween(origLoc, ev.getWorldLocation())
          * spritePanel.getZoom();
      if (dist > HOT_DIST * 3)
        break;

      return new MoveCenterpointOper();
    } while (false);
    return null;
  }

  private static final int HOT_DIST = 15;

  private UserEvent mInitialEvent;
  private Point mOriginalCenterpointLocation;
}
