package com.js.scredit;

import java.awt.Color;
import java.util.ArrayList;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

public class ScaleOper extends UserOperation {

  private static final int STATE_INACTIVE = 0;
  private static final int STATE_STARTED = 1;
  private static final int STATE_DRAGGING = 2;

  private static final int NUM_HANDLES = 8;

  public ScaleOper() {
  }

  private void setState(int state) {
    if (state == mState)
      throw new IllegalArgumentException();
    mState = state;
  }

  @Override
  public boolean shouldBeEnabled() {
    return calcBounds() != null;
  }

  @Override
  public void stop() {
    setState(STATE_INACTIVE);
  }

  @Override
  public void start() {
    if (mState != STATE_INACTIVE)
      throw new IllegalStateException();

    mStateSnapshot = ScriptEditor.editor().getStateSnapshot();
    Rect r = calcBounds();
    if (r == null)
      throw new IllegalStateException();

    // ensure rectangle isn't degenerate
    float MIN_DIM = 1;
    Point mid = r.midPoint();
    if (r.width <= MIN_DIM) {
      r.x = mid.x - MIN_DIM;
      r.width = MIN_DIM;
    }
    if (r.height <= MIN_DIM) {
      r.y = mid.y - MIN_DIM;
      r.height = MIN_DIM;
    }
    mOriginalRect = r;
    mStartDragRect = r;
    setScaledRect(r);

    setState(STATE_STARTED);
  }

  @Override
  public void processUserEvent(UserEvent event) {

    switch (event.getCode()) {

    case UserEvent.CODE_DOWN: {
      // Find handle
      float minDist = 0;
      int minHandle = -1;
      Point minHandleLocation = null;
      for (int i = 0; i < NUM_HANDLES; i++) {
        Point handleLoc = paddedHandleLoc(mStartDragRect, i);
        float dist = MyMath
            .distanceBetween(event.getWorldLocation(), handleLoc);
        if (minHandle < 0 || dist < minDist) {
          minDist = dist;
          minHandle = i;
          minHandleLocation = handleLoc;
        }
      }
      if (minDist > ScriptEditor.pickRadius()) {
        break;
      }
      mActiveHandle = minHandle;
      mInitialHandleOffset = MyMath.subtract(minHandleLocation,
          event.getWorldLocation());
      setState(STATE_DRAGGING);
    }
      break;

    case UserEvent.CODE_DRAG:
      if (mState != STATE_DRAGGING)
        break;
      performScale(mActiveHandle, event.getWorldLocation());
      break;

    case UserEvent.CODE_UP: {
      if (mState != STATE_DRAGGING)
        break;
      mStartDragRect = mScaledRect;
      CommandForGeneralChanges c = new CommandForGeneralChanges("Scale",
          mStateSnapshot);
      c.finish();
      setState(STATE_STARTED);
    }
      break;
    }

  }

  @Override
  public void paint() {
    GLPanel panel = ScriptEditor.getEditorPanel();

    // Render with emphasis if scaled rect = original rect
    boolean equalsOriginal = mOriginalRect.equals(mScaledRect);
    if (equalsOriginal)
      panel.setRenderColor(new Color(255, 64, 64, 255));
    else
      panel.setRenderColor(new Color(128, 64, 64, 255));

    // Calculate handles corresponding to scaled rect
    ArrayList<Point> handles = new ArrayList();
    for (int i = 0; i < NUM_HANDLES; i++)
      handles.add(paddedHandleLoc(mScaledRect, i));
    for (int i = 0; i < sLinesBetweenHandles.length; i += 2)
      panel.drawLine(handles.get(sLinesBetweenHandles[i]),
          handles.get(sLinesBetweenHandles[i + 1]));
    float p = ScriptEditor.pickRadius() * .3f;
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

  private Point paddedHandleLoc(Rect boundingRect, int handleIndex) {
    Point pt = handleLoc(boundingRect, handleIndex);
    return applyHandleExternalPadding(pt, handleIndex, true);
  }

  /**
   * Calculate handle location
   * 
   * A 'handle' is represented by an icon that the user can grab to perform a
   * scale operation
   */
  private Point handleLoc(Rect boundingRect, int ind) {
    switch (ind) {
    default:
      throw new IllegalArgumentException();
    case 0:
      return new Point(boundingRect.x, boundingRect.y);
    case 1:
      return new Point(boundingRect.midX(), boundingRect.y);
    case 2:
      return new Point(boundingRect.endX(), boundingRect.y);
    case 3:
      return new Point(boundingRect.endX(), boundingRect.midY());
    case 4:
      return new Point(boundingRect.endX(), boundingRect.endY());
    case 5:
      return new Point(boundingRect.midX(), boundingRect.endY());
    case 6:
      return new Point(boundingRect.x, boundingRect.endY());
    case 7:
      return new Point(boundingRect.x, boundingRect.midY());
    }
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
    float p = ScriptEditor.pickRadius() * .5f * sign;
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
    float x1 = x0;
    float y0 = touchLocation.y;
    float y1 = y0;

    // Add some padding to stop the scaled rectangle from becoming
    // degenerate
    float padding = Math.min(mStartDragRect.minDim() / 2,
        ScriptEditor.pickRadius() * .3f);
    if (handle == 0 || handle == 1 || handle == 2)
      y1 = mStartDragRect.midY() - padding;
    if (handle == 2 || handle == 3 || handle == 4)
      x0 = mStartDragRect.midX() + padding;
    if (handle == 4 || handle == 5 || handle == 6)
      y0 = mStartDragRect.midY() + padding;
    if (handle == 6 || handle == 7 || handle == 0)
      x1 = mStartDragRect.midX() - padding;
    touchLocation.setTo(MyMath.clamp(touchLocation.x, x0, x1),
        MyMath.clamp(touchLocation.y, y0, y1));

    // Replace new handle location with its projection to the line between
    // the handle and the midpoint
    Point origin = mStartDragRect.midPoint();
    Point filtered = new Point();
    Point handleBase = handleLoc(mStartDragRect, handle);
    MyMath.ptDistanceToLine(touchLocation, handleBase, origin, filtered);
    return filtered;
  }

  /**
   * Calculate the scaled rectangle corresponding to a particular handle
   * location
   */
  private Rect calculateScaledRect(int handle, Point handleLocation) {
    Point origin = mStartDragRect.midPoint();
    float w = mStartDragRect.width / 2;
    float h = mStartDragRect.height / 2;
    if (handle != 1 && handle != 5)
      w = Math.abs(origin.x - handleLocation.x);
    if (handle != 3 && handle != 7)
      h = Math.abs(origin.y - handleLocation.y);
    return new Rect(origin.x - w, origin.y - h, w * 2, h * 2);
  }

  private void performScale(int handle, Point touchLocation) {
    touchLocation = filteredHandle(handle, touchLocation);
    setScaledRect(calculateScaledRect(handle, touchLocation));

    // Act as if there's a 'groove' at the original (unscaled) rectangle
    // location: If scaled rectangle is very close to original, set it
    // exactly equal to it. We don't want the groove to be too large,
    // because this prevents small changes and is frustrating
    float diff = Math.max(Math.abs(mScaledRect.width - mOriginalRect.width),
        Math.abs(mScaledRect.height - mOriginalRect.height)) / 2;
    if (diff < ScriptEditor.pickRadius() * .05f) {
      setScaledRect(mOriginalRect);
    }
    scaleObjects();
  }

  private void setScaledRect(Rect r) {
    mScaledRect = r;
  }

  /**
   * Calculate transform to scale objects relative to their bounding rect's
   * center
   */
  private Matrix calcScaleTransform() {
    Point origin = mOriginalRect.midPoint();

    Matrix matrix = Matrix.getTranslate(-origin.x, -origin.y);
    Matrix matrix2 = Matrix.getScale(mScaledRect.width / mOriginalRect.width,
        mScaledRect.height / mOriginalRect.height);
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
    ScriptEditorState state = mStateSnapshot;
    for (int slot : state.getSelectedSlots()) {
      EdObject object = state.getObjects().get(slot);
      EdObject scaled = mutableCopyOf(object);
      scaled.applyTransform(matrix);
      ScriptEditor.items().set(slot, scaled);
    }
  }

  private Rect calcBounds() {
    EdObjectArray sel = ScriptEditor.items().getSelectedObjects();
    Rect bounds = null;
    for (EdObject obj : sel) {
      Rect objBounds = obj.boundingRect();
      if (bounds == null)
        bounds = objBounds;
      else
        bounds.include(objBounds);
    }
    return bounds;
  }

  private int mState;

  // Editor state at time operation was started
  private ScriptEditorState mStateSnapshot;

  // Bounding rect of original objects
  private Rect mOriginalRect;

  // Bounding rect when most recent drag operation started
  private Rect mStartDragRect;

  // Bounding rect of scaled objects
  private Rect mScaledRect;

  // Which handle the user is currently dragging
  private int mActiveHandle;

  // amount to add to user touch location to place exactly at handle
  private Point mInitialHandleOffset;
}
