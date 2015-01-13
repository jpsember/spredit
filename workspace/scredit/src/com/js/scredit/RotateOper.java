package com.js.scredit;

import java.awt.Color;

import com.js.editor.Command;
import com.js.editor.UserEvent;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

public class RotateOper extends OldUserOperation {

  @Override
  public void processUserEvent(UserEvent event) {
    die("not implemented yet");
  }

  public RotateOper() {
    oper = new RotateReversible();
  }

  public boolean shouldBeEnabled() {
    return oper.valid();
  }

  private boolean reg;

  @Override
  public boolean mouseDown() {

    boolean f = false;
    do {
      if (// right(ev) ||
      ev.isControlDown() || ev.isShiftDown())
        break;

      if (!oper.withinCircle(currentPtF))
        break;

      f = true;

      mouseDownAngle = MyMath.polarAngleOfSegment(oper.circ.getOrigin(),
          startPtF);
      mouseDownRotation = oper.rotation;

      if (!reg) {
        reg = true;
        ScriptEditor.editor().registerPush(oper);
      }

    } while (false);

    // // if not a valid rotate mouse press, cancel the rotate operation.
    // if (!f)
    // clearOperation();

    return f;
  }

  @Override
  public void paint() {
    if (true) {
      unimp("panel arg");
      return;
    }
    oper.paint(null);
  }

  @Override
  public void mouseMove(boolean drag) {
    if (!drag)
      return;

    oper.rotation = MyMath.normalizeAngle(MyMath.polarAngleOfSegment(
        oper.circ.getOrigin(), currentPtF)
        - mouseDownAngle + mouseDownRotation);
    oper.perform();
  }

  // rotation factor at mouse down
  private float mouseDownRotation;

  // polar angle at mousedown
  private float mouseDownAngle;

  private static class RotateReversible extends ModifyObjectsReversible {
    public RotateReversible() {
      EdObject[] orig = getOrigObjects();
      if (orig.length > 0) {
        circ = EdTools.smallestBoundingDisc(orig);
        displayRotationOffset = orig[0].rotation();
      }
      setName("Rotate");
    }

    @Override
    public boolean valid() {
      return nSlots() > 0;
    }

    public void paint(GLPanel panel) {
      panel.setRenderColor(Color.YELLOW);

      panel.drawCircle(circ.getOrigin(), circ.getRadius());
      final float W = 4;
      panel.drawFrame(circ.getOrigin().x - W / 2, circ.getOrigin().y - W / 2,
          W, W);

      panel.drawLine(
          circ.getOrigin(),
          MyMath.pointOnCircle(circ.getOrigin(), rotation
              + displayRotationOffset, circ.getRadius()));
    }

    @Override
    public EdObject perform(EdObject objOld) {
      EdObject ret = objOld;
      if (rotation != 0) {
        ret = copyOf(objOld);
        ret.rotAndScale(objOld, 1, circ.getOrigin(), rotation);
      }
      return ret;
    }

    public boolean withinCircle(Point p) {
      float dist = MyMath.distanceBetween(p, circ.getOrigin());
      return (dist < circ.getRadius());
    }

    private Circle circ;
    private float rotation;
    private float displayRotationOffset;
  }

  private RotateReversible oper;

  // ------------- Reset Rotation operation ------------------

  public static Command getResetOper() {
    return new ResetRotOper();
  }

  private static class ResetRotOper extends ModifyObjectsReversible {
    public ResetRotOper() {
      setName("Reset Rotation for");
    }

    @Override
    public void perform() {
      // UserOperation.clearOperation();
      super.perform();
    }

    @Override
    public EdObject perform(EdObject orig) {
      final boolean db = false;

      if (db)
        pr("perform ResetRotation for " + orig + ", rot=" + orig.rotation());

      EdObject ret = orig;
      float rot = orig.rotation();
      if (rot != 0) {
        ret = copyOf(orig);

        Circle circ = EdTools.smallestBoundingDisc(orig);

        ret.rotAndScale(orig, 1, circ.getOrigin(), -rot);

        ret.setRotation(0);
        if (db)
          pr("  after reset, rot=" + ret.rotation());
      }
      return ret;
    }
  }

}
