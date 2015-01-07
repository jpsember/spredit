package com.js.scredit;

import java.awt.Color;

import com.js.myopengl.GLPanel;

import com.js.editor.Command;
import com.js.editor.MouseOper;
import com.js.geometry.*;

import static com.js.basic.Tools.*;

public class ScaleOper extends MouseOper {
  private static final boolean db = false;

  private ScaleObjects oper;
  private boolean reg;

  public ScaleOper() {
    oper = new ScaleObjects();
    initScale = 1;
  }

  public boolean shouldBeEnabled() {
    return oper.shouldBeEnabled();
  }

  @Override
  public boolean mouseDown() {
    boolean f = false;
    do {
      if (right() || ev.isControlDown() || ev.isShiftDown())
        break;

      if (!oper.withinCircle(currentPtF))
        break;

      f = true;

      if (db)
        pr("ScaleOper.mouseDown");

      initScale = oper.scale;

      if (!reg) {
        reg = true;
        ScriptEditor.editor().registerPush(oper);
      }

    } while (false);

    // if not a valid rotate mouse press, cancel the rotate operation.
    if (!f)
      clearOperation();

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

    Circle circ = oper.circ;
    float distOrig = MyMath.distanceBetween(startPtF, circ.getOrigin());
    float dist = MyMath.distanceBetween(currentPtF, circ.getOrigin());
    float sc = (dist / distOrig) * initScale;

    oper.scale = sc;
    oper.perform();
  }

  public static Command getResetOper() {
    return new ResetScaleOper();
  }

  private static class ResetScaleOper extends ModifyObjectsReversible {
    public ResetScaleOper() {
      setName("Reset Scale for");
    }

    // @Override
    // public void perform(EdObject orig, EdObject obj) {
    // obj.setScale(1);
    // }
    @Override
    public void perform() {
      MouseOper.clearOperation();
      super.perform();
    }

    @Override
    public EdObject perform(EdObject orig) {
      EdObject ret = orig;
      if (orig.scale() != 1) {
        ret = (EdObject) orig.clone();
        ret.setScale(1);
      }
      return ret;
    }

    // @Override
    // public String toString() {
    // return "Reset Scale for " + EdTools.itemsStr(nSlots());
    // }
  }

  private float initScale;

  private static class ScaleObjects extends ModifyObjectsReversible {
    private Circle circ;
    // scale to apply to objects
    private float scale;

    public ScaleObjects() {
      EdObject[] orig = getOrigObjects();
      if (orig.length != 0) {
        circ = EdTools.smallestBoundingDisc(orig);
      }
      scale = 1;
      setName("Scale");
    }

    @Override
    public boolean shouldBeEnabled() {
      return nSlots() > 0;
    }

    public void paint(GLPanel panel) {
      panel.setRenderColor(Color.YELLOW);
      panel.drawCircle(circ.getOrigin(), circ.getRadius() * scale);
      final float W = 4;
      panel.drawFrame(circ.getOrigin().x - W / 2, circ.getOrigin().y - W / 2,
          W, W);
    }

    // @Override
    // public void perform(EdObject objOld, EdObject objCurr) {
    // objCurr.rotAndScale(objOld, scale, circ.getOrigin(), 0);
    // }

    @Override
    public EdObject perform(EdObject orig) {
      EdObject ret = orig;
      if (scale != 1) {
        ret = (EdObject) orig.clone();
        ret.rotAndScale(orig, scale, circ.getOrigin(), 0);
      }
      return ret;
    }

    public boolean withinCircle(Point p) {
      float dist = MyMath.distanceBetween(p, circ.getOrigin());
      return (dist > 1 && dist < circ.getRadius() * scale);
    }
  }

}
