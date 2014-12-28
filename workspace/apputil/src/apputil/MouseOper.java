package apputil;

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

  /**
   * Determine if this operation should start in response to a mouse down event
   * @return true if event has started
   */
  public abstract boolean mouseDown();

  /**
   * Update this operation in response to a mouse drag / move event
   * @param drag true if drag; false if hover
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
   * @return
   */
  public boolean right() {
    return SwingUtilities.isRightMouseButton(ev);
  }

  /**
   * Specify view associated with mouse operations
   * @param view
   */
  public static void setView(IEditorView view) {
    ASSERT(MouseOper.view == null);

    MouseOper.view = view;
    OurMouseListener ls = new OurMouseListener();
    Component c = view.getComponent();
    c.addMouseListener(ls);
    c.addMouseMotionListener(ls);
  }

  /**
   * Construct a string describing the mouse button, modifier key states
   * @return
   */
  public String mouseStateString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Mouse[");

    sb.append("rt:" + d(right()));
    sb.append(" ctrl:" + d(ev.isControlDown()));
    sb.append(" shift:" + d(ev.isShiftDown()));
    sb.append(" alt:" + d(ev.isAltDown()));
    sb.append(" meta:" + d(ev.isMetaDown()));
    return sb.toString();
  }
  /**
   * Get current operation
   * @return current operation, or null if none
   */
  public static MouseOper getOperation() {
    return editOper;
  }

  /**
   * Set current operation
   * @param oper operation 
   */
  public static void setOperation(MouseOper oper) {
    // ASSERT(oper != null);

    if (editOper != oper) {
      if (editOper != null)
        editOper.stop();
      editOper = oper;
      if (editOper != null)
        editOper.start();
      notifyListeners();
      view.repaint();
    }
  }

  private static void notifyListeners() {
    Iterator it = listeners.iterator();
    while (it.hasNext()) {
      Listener ls = (Listener) it.next();
      ls.operationChanged(editOper);
    }
  }

  public static void clearOperation() {
    setOperation(null);
    //    if (editOper != null) {
    //      editOper = null;
    //      notifyListeners();
    //    }
  }

  public static void addListener(Listener listener) {
    listeners.add(listener);
  }
  public static void removeListener(Listener listener) {
    listeners.remove(listener);
  }
  private static Set listeners = new HashSet();

  /**
   * Add an operation to the sequence
   * @param oper
   */
  public static void add(MouseOper oper) {
    opers.add(oper);
  }

  /**
   * Construct IPoint from MouseEvent
   * @param ev mouse event
   * @return IPoint containing mouse (view) coordinates
   */
  private static IPoint viewLoc(MouseEvent ev) {
    return new IPoint(ev.getX(), ev.getY());
  }

  private static void updateEventGlobals(MouseEvent evt) {
    if (view == null)
      throw new IllegalStateException();
    ev = evt;
    currentPtView = viewLoc(evt);
    currentPtF = view.viewToWorld(new Point(currentPtView));
    currentPt = new IPoint(currentPtF);
  }

  private static class OurMouseListener implements MouseListener,
      MouseMotionListener {
    @Override
    public void mousePressed(MouseEvent ev) {
      final boolean db = false;

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
        view.repaint();
      }
    }

    @Override
    public void mouseDragged(MouseEvent ev) {
      if (editOper != null) {
        updateEventGlobals(ev);
        editOper.mouseMove(true);
        view.repaint();
      }
    }

    @Override
    public void mouseMoved(MouseEvent ev) {
      if (editOper != null) {
        updateEventGlobals(ev);
        editOper.mouseMove(false);
        view.repaint();
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
  private static IEditorView view;

  // active operation, or null
  private static MouseOper editOper;

  public static interface Listener {
    public void operationChanged(MouseOper oper);
  }
}
