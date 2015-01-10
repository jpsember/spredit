package com.js.scredit;

import java.awt.Color;
import java.util.ArrayList;

import static com.js.basic.Tools.*;

import com.js.editor.Command;
import com.js.editor.MouseOper;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

public class EdPolygonOper extends MouseOper {

  /**
   * Get the polygon currently being edited, if such an operation is being
   * performed
   * 
   * @return polygon, or null
   */
  public static PolygonObject activePoly() {
    if (activeEditor == null)
      return null;
    return activeEditor.poly;
  }

  /**
   * Reverse the current editing orientation (ccw / cw)
   */
  public static void toggleDir() {
    final boolean db = false;
    if (activeEditor != null) {
      if (db)
        pr("toggleDir, reversed=" + reversed + " target=" + activeEditor.target);
      activeEditor.adjustTarget(-1);
    }
    reversed ^= true;
    if (db)
      pr(" reversed now " + reversed + "\n\n");

  }

  /**
   * Advance target by a number of vertices; adjusts for editing orientation
   * 
   * @param amt
   *          amount to advance target by
   */
  public void adjustTarget(int amt) {
    final boolean db = false;

    int prevCursor = target;

    if (reversed)
      amt = -amt;

    int newCursor = prevCursor + amt;
    if (poly.nPoints() == 0)
      target = 0;
    else
      target = poly.fixIndex(newCursor);
    if (db)
      pr("adjustVertex by amt=" + amt + " was " + prevCursor + " now " + target
          + " (#verts=" + poly.nPoints() + ")");
  }

  /**
   * Delete target vertex
   */
  public void deleteTarget() {
    int t = target();

    ArrayList<Point> a = new ArrayList();

    for (int i = 0; i < poly.nPoints(); i++) {
      if (i != t) {
        a.add(poly.getPoint(i));
      }
    }
    PolygonObject np = new PolygonObject(poly, poly.color(), a);
    setActivePolygon(np);

    adjustTarget(reversed ? 1 : 0);
  }

  /**
   * Get the polygon editing operation currently being performed
   * 
   * @return EdPolygonOper, or null
   */
  public static EdPolygonOper activeEditor() {
    return activeEditor;
  }

  /**
   * Construct an editor for a polygon
   * 
   * @param slot
   *          slot containing polygon; if no such slot exists, assumes polygon
   *          is being created and added to end
   * @param target
   *          which vertex is to be the target
   * @param edgeMode
   *          true if user grabbed an edge, false if grabbed a vertex
   */
  public EdPolygonOper(int slot, int target, boolean edgeMode) {

    final boolean db = false;

    this.slot = slot;

    if (slot < ScriptEditor.items().size()) {
      PolygonObject poly = (PolygonObject) ScriptEditor.items().get(slot);
      target = poly.fixIndex(target);
      setActivePolygon(poly);
    }

    if (db)
      pr("EditPolygonOper target=" + target + " edgeMode=" + edgeMode
          + " reversed=" + reversed);

    this.delInitialTarget = poly != null && poly.nPoints() != 0 && !edgeMode;

    this.target = target;
  }

  /**
   * Update this operation in response to a mouse drag / move event
   * 
   * @param drag
   *          true if drag; false if hover
   */
  public void mouseMove(boolean drag) {
    dragPt = Grid.snapToGrid(currentPtF, true);
    if (delInitialTarget) {
      delInitialTarget = false;
      deleteTarget();
    }
  }

  /**
   * Determine which polygon editing operation, if any, is editing a polygon
   * 
   * @param p
   *          polygon
   * @return editing operation, or null
   */
  public static EdPolygonOper getEditorFor(PolygonObject p) {
    EdPolygonOper ret = activeEditor;
    if (ret != null && ret.poly != p)
      ret = null;
    return ret;
  }

  private static EdPolygonOper activeEditor;

  private int target() {
    return target;
  }

  private GLPanel getPanel() {
    return null;
  }

  public void render() {
    GLPanel panel = getPanel();
    if (panel == null) {
      unimp("panel");
      return;
    }

    if (poly.nPoints() > 0) {
      panel.setRenderColor(Color.YELLOW);
      panel.drawCircle(poly.getPoint(target()), 8 / ScriptEditor.zoomFactor());
    }

    if (dragPt != null) {
      panel.setRenderColor(Color.YELLOW);
      panel.drawCircle(dragPt, 3 / ScriptEditor.zoomFactor());
      if (poly.nPoints() > 0) {
        int cursor = target() + (reversed ? 1 : -1);
        panel.drawLine(poly.getPoint(cursor), dragPt);
        panel.drawLine(dragPt, poly.getPoint(target));
      }
    }
  }

  public String toString() {
    return "Polygon Editing";
  }

  @Override
  public boolean mouseDown() {
    final boolean db = false;

    if (db)
      pr("mouseDown, right=" + right(ev));

    if (!right(ev)) {
      if (poly.nPoints() < PolygonObject.MAX_VERTICES)
        insertPt(Grid.snapToGrid(currentPtF, true));
      // we must return true, otherwise the operation will be cleared
      return true;
    } else
      return false;
  }

  @Override
  public void start() {
    final boolean db = false;

    activeEditor = this;

    if (db)
      pr("EdPolygonOper, start; slot=" + slot + " poly=" + poly);

    // create a new Reversible, one that either adds new polygon, or edits
    // existing one,
    // based upon whether an item in the slot exists

    Command rev = null;

    if (poly != null) {
      rev = new ModifyObjectsReversible(slot);
      if (db)
        ((ModifyObjectsReversible) rev).setName("(EditPolygon)");

      if (db)
        pr(" constructed ModObjRev for slot " + slot + ", poly " + poly);

    } else {
      poly = new PolygonObject(null, ScriptEditor.color(),
          new ArrayList<Point>());
      rev = new AddObjectsReversible(poly);
      if (db)
        pr(" constructed AddObjRev for new poly");

    }

    ScriptEditor.editor().registerPush(rev);
    ScriptEditor.perform(rev);

  }

  // private static class RotateReversible extends ModifyObjectsReversible {
  // public RotateReversible(int slot) {
  // super(slot);
  // // EdObject[] orig = getOrigObjects();
  // setName("Edit");
  // }
  // @Override
  // public boolean valid() {
  // return nSlots() > 0;
  // }
  //
  // // @Override
  // // public EdObject perform1(EdObject objOld) {
  // // EdObject ret = objOld;
  // // if (rotation != 0) {
  // // ret = (EdObject) objOld.clone();
  // // ret.rotAndScale(objOld, 1, circ.getOrigin(), rotation);
  // // }
  // // return ret;
  // // }
  // }

  @Override
  public void stop() {

    final boolean db = false;
    if (db)
      pr("EdPolygonOper, stop");

    // Update the last reversible action to reflect the final state of the
    // polygon.

    // The top of stack reversible action is either
    // 1) AddObjectsOper
    // 2) ModifyObjectsReversible
    //

    // If final polygon is not well defined {
    // If stacked reversible was AddObjectsOper, pop stack
    // else
    // replace stack reversible with DeleteObjectsReversible
    // }

    if (!poly.isWellDefined()) {
      if (db)
        pr(" poly not well defined, deleting");

      // if we were adding this polygon, pop the add operation
      // from the undo stack
      Command tos = ScriptEditor.editor().registerPeek();

      if (tos instanceof AddObjectsReversible) {
        ScriptEditor.editor().registerPop();
        ScriptEditor.items().remove(slot);
      } else {
        ASSERT(tos instanceof ModifyObjectsReversible);
        ModifyObjectsReversible mr = (ModifyObjectsReversible) tos;
        ASSERT(mr.nSlots() == 1);

        // ObjArray a = ScriptEditor.items();

        // undo the modify action to restore the original polygon
        mr.getReverse().perform();

        Command del = new DeleteItemReversible(slot);
        ScriptEditor.editor().registerPop();
        ScriptEditor.editor().registerPush(del);
        ScriptEditor.perform(del);
      }
    } else {
      if (db)
        pr(" modified poly is well defined");

      Command tos = ScriptEditor.editor().registerPeek();

      if (tos instanceof AddObjectsReversible) {
        if (false) {
          // replace the object being added with the newer polygon
          AddObjectsReversible op = (AddObjectsReversible) tos;
          if (db)
            pr(" updating " + op);
          warning("this can be simplified as we did before");
          // op.setObject(ScriptEditor.item(slot));
        }
      } else {
        ModifyObjectsReversible mr = (ModifyObjectsReversible) tos;
        if (db)
          pr(" updating " + mr);
        mr.updateModifiedObjects();
        // ASSERT(mr.nSlots() == 1);
        // // replace the object being added with the newer polygon
        // warn("not sure this is required");
        // if (false)
        // mr.updateSelectedObjects(null);
        // mr.updateModifiedObjects();
      }
    }
    activeEditor = null;

  }

  private void insertPt(Point pt) {
    final boolean db = false;
    if (db)
      pr("insertPt target=" + target + " reversed=" + reversed);

    int newTarget = target;

    ArrayList<Point> pts = new ArrayList();

    {
      for (int i = 0; i < poly.nPoints(); i++) {
        if (!reversed && i == target) {
          newTarget = pts.size() + 1;
          pts.add(pt);
        }

        pts.add(poly.getPoint(i));
        if (reversed && i == target) {
          pts.add(pt);
        }
      }
    }
    if (poly.nPoints() == 0) {
      pts.add(pt);
    }
    PolygonObject np = new PolygonObject(poly, poly.color(), pts);

    target = np.fixIndex(newTarget);
    setActivePolygon(np);
  }

  /**
   * Determine current editing orientation (ccw vs cw)
   * 
   * @return true if cw, false if ccw
   */
  public static boolean currentOrientation() {
    return reversed;
  }

  private void setActivePolygon(PolygonObject p) {
    final boolean db = false;

    if (db)
      pr("setActivePolygon to " + p + ", slot=" + slot);

    poly = p;
    if (p != null) {
      p.setSelected(true);
    }
    ScriptEditor.items().set(slot, p);
  }

  // operation used to start this edit operation; null if polygon existed
  // previously
  // private AddObjectsOper addOper;
  private PolygonObject poly;
  private int target;
  private static boolean reversed;
  private Point dragPt;
  private int slot;
  private boolean delInitialTarget;
}
