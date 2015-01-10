package com.js.editor;

import static com.js.basic.Tools.*;

import java.awt.Component;
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
   * Determine if mouse up/down was right button
   */
  public static boolean right(MouseEvent ev) {
    return SwingUtilities.isRightMouseButton(ev);
  }

  /**
   * Specify view associated with mouse operations
   * 
   * @param view
   *          --- @deprecated use MouseEventGenerator instead
   */
  public static void setView(IEditorView view) {
    ASSERT(MouseOper.sEditorView == null);

    MouseOper.sEditorView = view;
    warning("disabled");
    if (false) {
      OurMouseListener ls = new OurMouseListener();
      Component c = view.getComponent();
      c.addMouseListener(ls);
      c.addMouseMotionListener(ls);
    }
  }

  public static void setDefaultOperation(MouseOper defaultMouseOper) {
    sDefaultMouseOper = defaultMouseOper;
  }

  /**
   * Get current operation
   * 
   * @return current operation, or null if none
   */
  public static MouseOper getOperation() {
    if (editOper == null)
      editOper = sDefaultMouseOper;
    return editOper;
  }

  /**
   * Set current operation
   * 
   * @param oper
   *          operation
   */
  public static void setOperation(MouseOper oper) {
    // ASSERT(oper != null);

    if (editOper != oper) {
      if (editOper != null)
        editOper.stop();
      editOper = oper;
      if (editOper != null)
        editOper.start();
      sEditorView.repaint();
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
    sEnabled = enabled;
    if (!enabled) {
      // Cancel any active operation
      editOper = null;
    }
  }

  /**
   * Construct IPoint from MouseEvent
   * 
   * @param ev
   *          mouse event
   * @return IPoint containing mouse (view) coordinates
   */
  private static IPoint viewLoc(MouseEvent ev) {
    return new IPoint(ev.getX(), ev.getY());
  }

  private static void updateEventGlobals(MouseEvent evt) {
    if (sEditorView == null)
      throw new IllegalStateException();
    ev = evt;
    currentPtView = viewLoc(evt);
    currentPtF = sEditorView.viewToWorld(new Point(currentPtView));
    currentPt = new IPoint(currentPtF);
  }

  /**
   * @deprecated move to MouseEventGenerator
   */
  private static class OurMouseListener implements MouseListener,
      MouseMotionListener {
    @Override
    public void mousePressed(MouseEvent ev) {
      final boolean db = false;
      if (!sEnabled)
        return;

      updateEventGlobals(ev);
      startPtView = currentPtView;
      startPtF = currentPtF;
      startPt = currentPt;

      if (editOper != null) {
        if (!editOper.mouseDown())
          clearOperation();
      }

      if (editOper == null) {
        for (MouseOper sp : opers) {
          if (db)
            pr("testing start " + sp);

          if (sp.mouseDown()) {
            if (db)
              pr(" starting");
            // if editOper has already been set, don't change it.
            // For example, the EditSelectedItemOper operation may have caused
            // another operation to have become active.
            if (editOper == null)
              setOperation(sp);
            break;
          }
        }
      }
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
      if (editOper != null) {
        updateEventGlobals(ev);
        editOper.mouseUp();
        sEditorView.repaint();
      }
    }

    @Override
    public void mouseDragged(MouseEvent ev) {
      if (editOper != null) {
        updateEventGlobals(ev);
        editOper.mouseMove(true);
        sEditorView.repaint();
      }
    }

    @Override
    public void mouseMoved(MouseEvent ev) {
      if (editOper != null) {
        updateEventGlobals(ev);
        editOper.mouseMove(false);
        sEditorView.repaint();
      }
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
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
  // view generating event
  private static IEditorView sEditorView;

  // active operation, or null
  private static MouseOper editOper;
  private static boolean sEnabled = true;

  private static MouseOper sDefaultMouseOper;

  public static interface Listener {
    public void operationChanged(MouseOper oper);
  }

}
