package com.js.scredit;

import java.awt.Color;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

public class RotateOper extends UserOperation {

  private int[] mSelectedItems;
  private UserEvent mInitialEvent;
  // rotation factor at mouse down
  private float mouseDownRotation;
  // polar angle at mousedown
  private float mouseDownAngle;
  private Circle circ;
  private float rotation;
  private float displayRotationOffset;
  private GLPanel mPanel;

  public RotateOper() {
    mSelectedItems = ScriptEditor.items().getSelected();
    if (!shouldBeEnabled())
      return;

    unimp("create circle from actual objects");
    circ = new Circle(new Point(500, 500), 200);
    // circ = EdTools.smallestBoundingDisc(mSelectedItems);
    // displayRotationOffset = orig[0].rotation();
  }

  @Override
  public boolean shouldBeEnabled() {
    return mSelectedItems.length != 0;
  }

  @Override
  public void processUserEvent(UserEvent event) {
    switch (event.getCode()) {
    case UserEvent.CODE_DOWN:
      doMouseDown(event);
      break;
    case UserEvent.CODE_DRAG:
      if (!doingRotate())
        break;
      doDrag(event);
      break;
    }
  }

  @Override
  public void paint() {
    if (mPanel == null) {
      warning("no GLPanel");
      return;
    }

    mPanel.setRenderColor(Color.YELLOW);

    mPanel.drawCircle(circ.getOrigin(), circ.getRadius());
    final float W = 4;
    mPanel.drawFrame(circ.getOrigin().x - W / 2, circ.getOrigin().y - W / 2, W,
        W);

    mPanel.drawLine(circ.getOrigin(), MyMath.pointOnCircle(circ.getOrigin(),
        rotation + displayRotationOffset, circ.getRadius()));
  }

  private void doMouseDown(UserEvent event) {
    if (event.isCtrl() || event.isShift())
      return;
    mInitialEvent = event;
  }

  private void doDrag(UserEvent event) {
    float rot = MyMath.normalizeAngle(MyMath.polarAngleOfSegment(
        circ.getOrigin(), event.getWorldLocation()));
    pr("rotation " + da(rot));
  }

  private boolean doingRotate() {
    return mInitialEvent != null;
  }
}
