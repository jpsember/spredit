package com.js.editor;

import static com.js.basic.Tools.*;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.js.geometry.IPoint;
import com.js.geometry.Point;

/**
 * Class representing a mouse editing operation
 */
public abstract class MouseOper {

  public void processUserEvent(UserEvent event) {
    unimp("processUserEvent, " + nameOf(this));
  }

  /**
   * Determine if this operation should start in response to a mouse down event
   * 
   * @return true if event has started
   */
  public abstract boolean mouseDown();

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
   * End this operation in response to a mouse up event
   */
  public void mouseUp() {
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
   * Determine if an object can be editable during this operation. Default is
   * true; for some operations, e.g. rotation and scaling, this will be false
   */
  public boolean allowEditableObject() {
    return true;
  }

  /**
   * Determine if mouse up/down was right button
   */
  public static boolean right(MouseEvent ev) {
    return SwingUtilities.isRightMouseButton(ev);
  }

  // At some point, we may want to refactor to make the 'current' and 'default'
  // operations belong to the UserEventSource, so that there can exist multiple
  // views, each with their own independent operations

  public static void setDefaultOperation(MouseOper defaultMouseOper) {
    sDefaultMouseOper = defaultMouseOper;
  }

  /**
   * Get current operation
   */
  public static MouseOper getOperation() {
    if (editOper == null)
      editOper = sDefaultMouseOper;
    return editOper;
  }

  /**
   * Set current operation
   */
  public static void setOperation(MouseOper oper) {
    if (oper == null) {
      if (sDefaultMouseOper == null)
        throw new IllegalStateException("no default mouse oper defined");
      oper = sDefaultMouseOper;
    }
    if (editOper != oper) {
      if (editOper != null)
        editOper.stop();
      editOper = oper;
      if (editOper != null)
        editOper.start();
    }
  }

  public static void clearOperation() {
    setOperation(null);
  }

  /**
   * Add an operation to the sequence
   * 
   * @param oper
   *          --- make this deprecated
   */
  public static void add(MouseOper oper) {
    opers.add(oper);
  }

  public static void setEnabled(boolean enabled) {
    if (!enabled) {
      // Cancel any active operation
      editOper = null;
    }
  }

  private static ArrayList<MouseOper> opers = new ArrayList();

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

  // active operation, or null
  private static MouseOper editOper;

  private static MouseOper sDefaultMouseOper;

  public static interface Listener {
    public void operationChanged(MouseOper oper);
  }

}
