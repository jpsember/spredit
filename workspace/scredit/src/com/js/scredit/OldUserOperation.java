package com.js.scredit;

import java.awt.event.MouseEvent;
import com.js.editor.UserOperation;
import com.js.geometry.IPoint;
import com.js.geometry.Point;

@Deprecated
public abstract class OldUserOperation extends UserOperation {

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
