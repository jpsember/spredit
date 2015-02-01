package com.js.scredit;

import java.awt.Color;
//
//import com.js.myopengl.GLPanel;
//
//import com.js.editor.Command;
//import com.js.editor.UserEvent;
//import com.js.geometry.*;
//
//import static com.js.basic.Tools.*;
//
//public class ScaleOper extends OldUserOperation {
//  private static final boolean db = false;
//
//  private ScaleObjects oper;
//  private boolean reg;
//
//  @Override
//  public void processUserEvent(UserEvent event) {
//    die("not implemented yet");
//  }
//
//  public ScaleOper() {
//    oper = new ScaleObjects();
//    initScale = 1;
//  }
//
//  public boolean shouldBeEnabled() {
//    return oper.valid();
//  }
//
//  @Override
//  public boolean mouseDown() {
//    boolean f = false;
//    do {
//      if ( // ev.isRight() ||
//      ev.isControlDown() || ev.isShiftDown())
//        break;
//
//      if (!oper.withinCircle(currentPtF))
//        break;
//
//      f = true;
//
//      if (db)
//        pr("ScaleOper.mouseDown");
//
//      initScale = oper.scale;
//
//      if (!reg) {
//        reg = true;
//        ScriptEditor.editor().recordCommand(oper);
//      }
//
//    } while (false);
//
//    // // if not a valid rotate mouse press, cancel the rotate operation.
//    // if (!f)
//    // clearOperation();
//
//    return f;
//  }
//
//  @Override
//  public void paint() {
//    if (true) {
//      unimp("panel arg");
//      return;
//    }
//    oper.paint(null);
//  }
//
//  @Override
//  public void mouseMove(boolean drag) {
//    if (!drag)
//      return;
//
//    Circle circ = oper.circ;
//    float distOrig = MyMath.distanceBetween(startPtF, circ.getOrigin());
//    float dist = MyMath.distanceBetween(currentPtF, circ.getOrigin());
//    float sc = (dist / distOrig) * initScale;
//
//    oper.scale = sc;
//    oper.perform();
//  }
//
//  public static Command getResetOper() {
//    return new ResetScaleOper();
//  }
//
//  private static class ResetScaleOper extends ModifyObjectsReversible {
//    public ResetScaleOper() {
//      setName("Reset Scale for");
//    }
//
//    // @Override
//    // public void perform(EdObject orig, EdObject obj) {
//    // obj.setScale(1);
//    // }
//    @Override
//    public void perform() {
//      // UserOperation.clearOperation();
//      super.perform();
//    }
//
//    @Override
//    public EdObject perform(EdObject orig) {
//      EdObject ret = orig;
//      if (orig.scale() != 1) {
//        ret = mutableCopyOf(orig);
//        ret.setScale(1);
//      }
//      return ret;
//    }
//
//    // @Override
//    // public String toString() {
//    // return "Reset Scale for " + EdTools.itemsStr(nSlots());
//    // }
//  }
//
//  private float initScale;
//
//  private static class ScaleObjects extends ModifyObjectsReversible {
//    private Circle circ;
//    // scale to apply to objects
//    private float scale;
//
//    public ScaleObjects() {
//      EdObject[] orig = getOrigObjects();
//      if (orig.length != 0) {
//        circ = EdTools.smallestBoundingDisc(orig);
//      }
//      scale = 1;
//      setName("Scale");
//    }
//
//    @Override
//    public boolean valid() {
//      return nSlots() > 0;
//    }
//
//    public void paint(GLPanel panel) {
//      panel.setRenderColor(Color.YELLOW);
//      panel.drawCircle(circ.getOrigin(), circ.getRadius() * scale);
//      final float W = 4;
//      panel.drawFrame(circ.getOrigin().x - W / 2, circ.getOrigin().y - W / 2,
//          W, W);
//    }
//
//    // @Override
//    // public void perform(EdObject objOld, EdObject objCurr) {
//    // objCurr.rotAndScale(objOld, scale, circ.getOrigin(), 0);
//    // }
//
//    @Override
//    public EdObject perform(EdObject orig) {
//      EdObject ret = orig;
//      if (scale != 1) {
//        ret = mutableCopyOf(orig);
//        ret.rotAndScale(orig, scale, circ.getOrigin(), 0);
//      }
//      return ret;
//    }
//
//    public boolean withinCircle(Point p) {
//      float dist = MyMath.distanceBetween(p, circ.getOrigin());
//      return (dist > 1 && dist < circ.getRadius() * scale);
//    }
//  }
//
//}

import java.util.ArrayList;
import java.util.List;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

public class ScaleOper extends UserOperation {

  private static final int NUM_HANDLES = 8;

  public ScaleOper() {
    prepareScaleOperation();
  }

  @Override
  public void processUserEvent(UserEvent event) {

    switch (event.getCode()) {

    case UserEvent.CODE_DOWN: {
      prepareScaleOperation();
      // Find handle
      float minDist = 0;
      int minHandle = -1;
      Point minHandleLocation = null;
      for (int i = 0; i < NUM_HANDLES; i++) {
        Point handleLoc = handleBaseLocation(i, true);
        float dist = MyMath
            .distanceBetween(event.getWorldLocation(), handleLoc);
        if (minHandle < 0 || dist < minDist) {
          minDist = dist;
          minHandle = i;
          minHandleLocation = handleLoc;
        }
      }
      if (minDist > pickRadius()) {
        event.clearOperation();
        break;
      }
      mActiveHandle = minHandle;
      mInitialHandleOffset = MyMath.subtract(minHandleLocation,
          event.getWorldLocation());
    }
      break;

    case UserEvent.CODE_DRAG:
      if (!mPerformingDragSequence)
        break;
      performScale(mActiveHandle, event.getWorldLocation());
      break;

    case UserEvent.CODE_UP: {
      if (!mPerformingDragSequence)
        break;
      mCommand.finish();
      setUnprepared();
    }
      break;
    }

  }

  @Override
  public void paint() {
    GLPanel panel = ScriptEditor.getEditorPanel();

    // Render with emphasis if scaled rect = original rect
    boolean equalsOriginal = mRect.equals(mScaledRect);
    if (equalsOriginal)
      panel.setRenderColor(new Color(255, 64, 64, 255));
    else
      panel.setRenderColor(new Color(128, 64, 64, 255));

    // Calculate handles corresponding to scaled rect
    List<Point> handles = new ArrayList();
    calculateHandleBaseLocations(mScaledRect, handles);
    for (int i = 0; i < sLinesBetweenHandles.length; i += 2)
      panel.drawLine(handles.get(sLinesBetweenHandles[i]),
          handles.get(sLinesBetweenHandles[i + 1]));
    float p = pickRadius() * .3f;
    for (int i = 0; i < NUM_HANDLES; i++) {
      Point center = applyHandleExternalPadding(handles.get(i), i, true);
      panel.drawFrame(new Rect(center.x - p, center.y - p, 2 * p, 2 * p));
    }
  }

  @Override
  public boolean allowEditableObject() {
    return false;
  }

  private static final int[] sLinesBetweenHandles = { 0, 2, 2, 4, 4, 6, 6, 0, };

  /**
   * Calculate the bounding rectangle for a set of objects
   * 
   * @param objects
   * @return bounding rectangle, or null if there are no objects
   */
  private Rect boundsForObjects(EdObjectArray objects) {
    Rect bounds = null;
    for (EdObject obj : objects) {
      Rect objBounds = obj.boundingRect();
      if (bounds == null)
        bounds = objBounds;
      else
        bounds.include(objBounds);
    }
    return bounds;
  }

  private void prepareScaleOperation() {
    if (!mPerformingDragSequence) {
      mCommand = new CommandForGeneralChanges("Scale").setMergeKey("scale");
      mHandles = new ArrayList();
      // Don't replace an existing bounding rectangle, since it may have
      // been derived from a previous scale procedure involving these
      // objects, and recalculating it may produce a different rectangle
      // which can be disorienting to the user.
      if (mRect == null) {
        mRect = boundsForObjects(ScriptEditor.items().getSelectedObjects());
        // ensure rectangle isn't degenerate
        final float MIN_DIM = 1;
        Point mid = mRect.midPoint();
        if (mRect.width <= MIN_DIM) {
          mRect.x = mid.x - MIN_DIM;
          mRect.width = MIN_DIM;
        }
        if (mRect.height <= MIN_DIM) {
          mRect.y = mid.y - MIN_DIM;
          mRect.height = MIN_DIM;
        }
      }
      mScaledRect = mRect;
      calculateHandleBaseLocations(mRect, mHandles);
      mPerformingDragSequence = true;
    }
  }

  private void setUnprepared() {
    if (mPerformingDragSequence) {
      // Set the original (unscaled) rect equal to the previous
      // operation's scaled rect, so it doesn't get recalculated and
      // change its appearance disconcertingly.
      mRect = mScaledRect;
      mPerformingDragSequence = false;
    }
  }

  /**
   * Calculate handle base locations for a particular bounding rectangle.
   * 
   * A 'handle' is represented by an icon that the user can grab to perform a
   * scale operation. Each handle has a 'base location' which corresponds to its
   * location before scaling begins.
   */
  private void calculateHandleBaseLocations(Rect boundingRect,
      List<Point> handleLocations) {
    Point p0 = new Point(boundingRect.x, boundingRect.y);
    Point p1 = new Point(boundingRect.midX(), p0.y);
    Point p2 = new Point(boundingRect.endX(), p0.y);
    Point p3 = new Point(p2.x, boundingRect.midY());
    Point p4 = new Point(p2.x, boundingRect.endY());
    Point p5 = new Point(p1.x, p4.y);
    Point p6 = new Point(p0.x, p4.y);
    Point p7 = new Point(p0.x, p3.y);
    handleLocations.clear();
    handleLocations.add(p0);
    handleLocations.add(p1);
    handleLocations.add(p2);
    handleLocations.add(p3);
    handleLocations.add(p4);
    handleLocations.add(p5);
    handleLocations.add(p6);
    handleLocations.add(p7);
  }

  /**
   * Adjust the location of a handle to move it between its actual location (on
   * the bounding rect boundary) and its displayed location (some small distance
   * outside the rect). This adjustment allows the handles to appear
   * well-separated even if bounding rect is small (or degenerate).
   * 
   * @param location
   *          location of handle (actual vs displayed)
   * @param handleIndex
   * @param addFlag
   *          true to adjust actual->displayed; false for displayed->actual
   */
  private Point applyHandleExternalPadding(Point location, int handleIndex,
      boolean addFlag) {
    Point adjustedLocation = new Point(location);
    float sign = addFlag ? 1 : -1;
    float p = pickRadius() * .5f * sign;
    if (handleIndex <= 2)
      adjustedLocation.y -= p;
    else if (handleIndex >= 4 && handleIndex <= 6)
      adjustedLocation.y += p;
    if (handleIndex == 0 || handleIndex >= 6)
      adjustedLocation.x -= p;
    else if (handleIndex >= 2 && handleIndex <= 4)
      adjustedLocation.x += p;
    return adjustedLocation;
  }

  private float pickRadius() {
    return ScriptEditor.pickRadius();
  }

  /**
   * Filter user's touch location to be a valid new location for a particular
   * handle
   * 
   * @param handle
   *          index of handle being adjusted
   * @param touchLocation
   *          user's touch location
   */
  private Point filteredHandle(int handle, Point touchLocation) {
    touchLocation = MyMath.add(touchLocation, mInitialHandleOffset);
    touchLocation = applyHandleExternalPadding(touchLocation, handle, false);

    float x0 = touchLocation.x;
    float x1 = touchLocation.x;
    float y0 = touchLocation.y;
    float y1 = touchLocation.y;

    // Add some padding to stop the scaled rectangle from becoming
    // degenerate
    float padding = Math.min(mRect.minDim() / 2, pickRadius() * .3f);
    if (handle == 0 || handle == 1 || handle == 2)
      y1 = mRect.midY() - padding;
    if (handle == 2 || handle == 3 || handle == 4)
      x0 = mRect.midX() + padding;
    if (handle == 4 || handle == 5 || handle == 6)
      y0 = mRect.midY() + padding;
    if (handle == 6 || handle == 7 || handle == 0)
      x1 = mRect.midX() - padding;
    touchLocation.setTo(MyMath.clamp(touchLocation.x, x0, x1),
        MyMath.clamp(touchLocation.y, y0, y1));

    // Replace new handle location with its projection to the line between
    // the handle and the midpoint
    Point origin = mRect.midPoint();
    Point filtered = new Point();
    Point handleBase = handleBaseLocation(handle, false);
    MyMath.ptDistanceToLine(touchLocation, handleBase, origin, filtered);
    return filtered;
  }

  private Point handleBaseLocation(int handleIndex, boolean applyExternalPadding) {
    Point loc = mHandles.get(handleIndex);
    if (applyExternalPadding)
      loc = applyHandleExternalPadding(loc, handleIndex, true);
    return loc;
  }

  /**
   * Calculate the scaled rectangle corresponding to a particular handle
   * location
   */
  private Rect calculateScaledRect(int handle, Point handleLocation) {
    Point origin = mRect.midPoint();
    float w = mRect.width / 2;
    float h = mRect.height / 2;
    if (handle != 1 && handle != 5)
      w = Math.abs(origin.x - handleLocation.x);
    if (handle != 3 && handle != 7)
      h = Math.abs(origin.y - handleLocation.y);
    return new Rect(origin.x - w, origin.y - h, w * 2, h * 2);
  }

  private void performScale(int handle, Point touchLocation) {
    touchLocation = filteredHandle(handle, touchLocation);
    mScaledRect = calculateScaledRect(handle, touchLocation);

    // Act as if there's a 'groove' at the original (unscaled) rectangle
    // location: If scaled rectangle is very close to original, set it
    // exactly equal to it. We don't want the groove to be too large,
    // because this prevents small changes and is frustrating
    float diff = Math.max(Math.abs(mScaledRect.width - mRect.width),
        Math.abs(mScaledRect.height - mRect.height)) / 2;
    if (diff < pickRadius() * .05f) {
      mScaledRect = mRect;
    }
    scaleObjects();
  }

  /**
   * Calculate transform to scale objects relative to their bounding rect's
   * center
   */
  private Matrix calcScaleTransform() {
    Point origin = mRect.midPoint();

    Matrix matrix = Matrix.getTranslate(-origin.x, -origin.y);
    Matrix matrix2 = Matrix.getScale(mScaledRect.width / mRect.width,
        mScaledRect.height / mRect.height);
    Matrix.multiply(matrix2, matrix, matrix);
    matrix2 = Matrix.getTranslate(origin);
    Matrix.multiply(matrix2, matrix, matrix);
    return matrix;
  }

  /**
   * Replace selected objects with scaled counterparts
   */
  private void scaleObjects() {
    Matrix matrix = calcScaleTransform();
    ScriptEditorState state = mCommand.getOriginalState();
    for (int slot : state.getSelectedSlots()) {
      EdObject object = state.getObjects().get(slot);
      EdObject scaled = mutableCopyOf(object);
      scaled.applyTransform(matrix);
      ScriptEditor.items().set(slot, scaled);
    }
  }

  // True if processing a down/drag/up scaling operation
  private boolean mPerformingDragSequence;
  private CommandForGeneralChanges mCommand;
  // Bounding rect of unscaled objects
  private Rect mRect;
  // Handle base locations for unscaled objects
  private List<Point> mHandles;
  // Scaled bounding rect
  private Rect mScaledRect;
  // Which handle the user is adjusting
  private int mActiveHandle;
  // amount to add to user touch location to place exactly at handle
  private Point mInitialHandleOffset;
}
