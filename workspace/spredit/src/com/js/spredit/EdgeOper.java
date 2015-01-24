package com.js.spredit;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.IPoint;
import com.js.geometry.IRect;
import com.js.geometry.MyMath;
import com.js.geometry.Point;

public class EdgeOper extends UserOperation {

  private EdgeOper(int edgeNum) {
    this.mEdgeNumber = edgeNum;
  }

  @Override
  public void start() {
    SpriteInfo spriteInfo = SpriteEditor.getSpriteInfo();
    SpritePanel spritePanel = SpriteEditor.getSpritePanel();
    mOriginalClip = new IRect(spriteInfo.cropRect());
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
      IPoint bounds = spriteInfo.workImageSize();

      IRect clip = spriteInfo.cropRect();
      int x1 = clip.x;
      int y1 = clip.y;
      int x2 = clip.endX();
      int y2 = clip.endY();

      switch (mEdgeNumber) {
      case 0:
        y1 = snapclamp(mOriginalClip.y + loc.y, 0, y2 - 1);
        break;
      case 1:
        x2 = snapclamp(mOriginalClip.endX() + loc.x, x1 + 1, bounds.x);
        break;
      case 2:
        y2 = snapclamp(mOriginalClip.endY() + loc.y, y1 + 1, bounds.y);
        break;
      case 3:
        x1 = snapclamp(mOriginalClip.x + loc.x, 0, x2 - 1);
        break;
      }
      spriteInfo.setCropRect(new IRect(x1, y1, x2 - x1, y2 - y1));
      break;

    case UserEvent.CODE_UP:
      event.clearOperation();
      break;
    }
  }

  /**
   * Given a mouse down event, attempt to build an appropriate EdgeOper
   */
  public static UserOperation buildFor(UserEvent ev) {

    if (ev.isCtrl() || ev.isShift())
      return null;

    SpriteInfo spriteInfo = SpriteEditor.getSpriteInfo();
    SpritePanel spritePanel = SpriteEditor.getSpritePanel();

    for (int num = 0; num < 4; num++) {
      IPoint pt1, pt2;
      IRect clip = spriteInfo.cropRect();
      switch (num) {
      default:
        pt1 = clip.bottomLeft();
        pt2 = clip.bottomRight();
        break;
      case 1:
        pt1 = clip.bottomRight();
        pt2 = clip.topRight();
        break;
      case 2:
        pt1 = clip.topRight();
        pt2 = clip.topLeft();
        break;
      case 3:
        pt1 = clip.topLeft();
        pt2 = clip.bottomLeft();
        break;
      }

      float dist = MyMath.ptDistanceToSegment(new Point(ev.getWorldLocation()),
          new Point(pt1), new Point(pt2), null) * spritePanel.getZoom();

      if (dist > HOT_DIST)
        continue;
      return new EdgeOper(num);
    }

    return null;
  }

  private static int snapclamp(int v, int min, int max) {
    v = (int) MyMath.snapToGrid(v, 1);
    return MyMath.clamp(v, min, max);
  }

  private static final int HOT_DIST = 15;

  private int mEdgeNumber;
  private IRect mOriginalClip;
  private UserEvent mInitialEvent;
}
