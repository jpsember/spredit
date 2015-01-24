package com.js.spredit;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.IPoint;
import com.js.geometry.IRect;
import com.js.geometry.MyMath;
import com.js.geometry.Point;

public class MoveClipOper extends UserOperation {

  private MoveClipOper() {
  }

  @Override
  public void start() {
    SpriteInfo spriteInfo = SpriteEditor.getSpriteInfo();
    SpritePanel spritePanel = SpriteEditor.getSpritePanel();

    origLoc = spriteInfo.cropRect().bottomLeft();
    spritePanel.setHighlightClip(true);

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
      IPoint loc = new IPoint(Point.difference(event.getWorldLocation(),
          mInitialEvent.getWorldLocation()));

      IRect clip = spriteInfo.cropRect();
      // Note: we are actually modifying the original cropRect returned;
      // refactor this later

      clip.x = origLoc.x + loc.x;
      clip.y = origLoc.y + loc.y;

      // don't let the clip region move outside of the original bounds
      clip.x = snapclamp(clip.x, 0, spriteInfo.workImageSize().x - clip.width);
      clip.y = snapclamp(clip.y, 0, spriteInfo.workImageSize().y - clip.height);

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
      if (ev.isCtrl() || !ev.isShift())
        break;

      SpriteInfo spriteInfo = SpriteEditor.getSpriteInfo();
      IRect clip = spriteInfo.cropRect();
      SpritePanel spritePanel = SpriteEditor.getSpritePanel();
      float dist = clip.distanceFrom(new IPoint(ev.getWorldLocation()))
          * spritePanel.getZoom();

      if (dist > HOT_DIST)
        break;

      return new MoveClipOper();
    } while (false);
    return null;
  }

  private static int snapclamp(int v, int min, int max) {
    v = (int) MyMath.snapToGrid(v, 1);
    return MyMath.clamp(v, min, max);
  }

  private static final int HOT_DIST = 15;

  private UserEvent mInitialEvent;
  // bottom left of clip rectangle at start of operation
  private IPoint origLoc;
}
