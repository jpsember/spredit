package com.js.editor;

import com.js.geometry.IPoint;
import com.js.geometry.Point;
import static com.js.basic.Tools.*;

/**
 * <pre>
 * 
 * Represents a user-generated event to be manipulated by an editor.
 * 
 * The user generates events. 
 * 
 * These events are processed by the current operation.
 * 
 * An operation issues commands which modifies the state of the 
 * object being edited.
 * 
 * </pre>
 */
public class UserEvent {

  public static final int CODE_NONE = 0;

  // single touch events
  public static final int CODE_DOWN = 1;
  public static final int CODE_DRAG = 2;
  public static final int CODE_UP = 3;

  // stop existing operation, if one is occurring
  public static final int CODE_STOP = 4;

  public static final UserEvent NONE = new UserEvent(CODE_NONE);
  public static final UserEvent STOP = new UserEvent(CODE_STOP);

  public UserEvent(int code, IEditorView view, IPoint viewLocation) {
    mCode = code;
    mView = view;
    mViewLocation = viewLocation;
  }

  // public UserEvent(int code, Point location) {
  // this(code, location, false);
  // }

  public UserEvent(int code) {
    mCode = code;
  }

  public IPoint getViewLocation() {
    if (!hasLocation())
      throw new IllegalStateException();
    return mViewLocation;
  }

  public int getCode() {
    return mCode;
  }

  public Point getWorldLocation() {
    if (!hasLocation())
      throw new IllegalStateException();
    if (mLocation == null) {
      mLocation = mView.viewToWorld(mViewLocation.toPoint());
    }
    return mLocation;
  }

  public boolean isDownVariant() {
    return mCode == CODE_DOWN;
  }

  public boolean isUpVariant() {
    return mCode == CODE_UP;
  }

  public boolean isDragVariant() {
    return mCode == CODE_DRAG;
  }

  public boolean hasLocation() {
    return mLocation != null;
  }

  public boolean isMultipleTouch() {
    if (!hasLocation())
      throw new IllegalStateException();
    return mMultipleTouchFlag;
  }

  public void printProcessingMessage(String message) {
    if (!DEBUG_ONLY_FEATURES)
      return;
    else {
      if (isDragVariant() && getCode() == sPreviousPrintEvent.getCode()
          && message.equals(sPreviousPrintMessage))
        return;
      pr(message + "; processing:   " + this);
      sPreviousPrintEvent = this;
      sPreviousPrintMessage = message;
    }
  }

  private static String sEditorEventNames[] = { "NONE", "DOWN", "DRAG", "UP  ",
      "STOP", };

  public static String editorEventName(int eventCode) {
    if (!DEBUG_ONLY_FEATURES)
      return null;
    if (eventCode < 0 || eventCode >= sEditorEventNames.length)
      return "??#" + eventCode + "??";
    return sEditorEventNames[eventCode];
  }

  @Override
  public String toString() {
    if (!DEBUG_ONLY_FEATURES)
      return super.toString();
    StringBuilder sb = new StringBuilder();
    if (mCode < 0 || mCode >= sEditorEventNames.length) {
      sb.append("??#" + mCode + "??");
    } else {
      sb.append(sEditorEventNames[mCode]);
    }
    if (hasLocation()) {
      sb.append(" w:");
      sb.append(getWorldLocation());
      sb.append(" v:");
      sb.append(getViewLocation());
    }
    return sb.toString();
  }

  public static interface Listener {
    public void handleUserEvent(UserEvent event);
  }

  private static UserEvent sPreviousPrintEvent = NONE;
  private static String sPreviousPrintMessage;

  private int mCode;
  private IEditorView mView;
  private Point mLocation;
  private IPoint mViewLocation;
  private boolean mMultipleTouchFlag;
}
