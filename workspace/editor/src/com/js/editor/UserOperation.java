package com.js.editor;

import static com.js.basic.Tools.*;

import java.awt.event.*;
import javax.swing.*;

import com.js.geometry.IPoint;
import com.js.geometry.Point;

/**
 * Class representing a user operation, often involving the mouse (or
 */
public abstract class UserOperation implements UserEvent.Listener {

  @Override
  public abstract void processUserEvent(UserEvent event);

  /**
   * Determine if an object can be editable during this operation. Default is
   * true; for some operations, e.g. rotation and scaling, this will be false
   */
  public boolean allowEditableObject() {
    return true;
  }

  /**
   * Determine if this operation should start in response to a mouse down event
   * 
   * @return true if event has started
   */
  public boolean mouseDown() {
    throw new UnsupportedOperationException();
  }

  /**
   * Update this operation in response to a mouse drag / move event
   * 
   * @param drag
   *          true if drag; false if hover
   */
  public void mouseMove(boolean drag) {
  }

  /**
   * Display any highlighting associated with this operation
   */
  public void paint() {
  }

  /**
   * Called when operation is starting
   */
  public void start() {
  }

  /**
   * Called when operation is stopping
   */
  public void stop() {
  }

  /**
   * Determine if mouse up/down was right button
   */
  public static boolean right(MouseEvent ev) {
    return SwingUtilities.isRightMouseButton(ev);
  }

  public static void setEnabled(boolean enabled) {
    warning("setEnabled is deprecated; " + stackTrace(1, 1));
    if (!enabled) {
      // Cancel any active operation
      // editOper = null;
    }
  }

  // event when operation started
  public static MouseEvent startEv;
  // current mouse event
  public static MouseEvent ev;
  // location of mouse when operation started (in world, and in view)
  public static Point startPtF;
  public static IPoint startPt;
  public static IPoint startPtView;
  // current location of mouse (in world, and in view)
  public static Point currentPtF;
  public static IPoint currentPt;
  public static IPoint currentPtView;

}
