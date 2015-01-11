package com.js.editor;

import static com.js.basic.Tools.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * Class representing a user operation, usually involving the mouse (or touch
 * device)
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

}
