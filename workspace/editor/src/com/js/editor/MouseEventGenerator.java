package com.js.editor;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

import com.js.geometry.*;
import static com.js.basic.Tools.*;

/**
 * Listens to mouse events within a view, and generates corresponding UserEvents
 */
public class MouseEventGenerator {

  /**
   * Specify view to listen to (Java) mouse events
   */
  public void setView(IEditorView view) {
    mView = view;
    OurMouseListener ls = new OurMouseListener();
    Component c = view.getComponent();
    c.addMouseListener(ls);
    c.addMouseMotionListener(ls);
  }

  /**
   * Specify the (single) listener for UserEvents
   */
  public void setListener(UserEvent.Listener listener) {
    mListener = listener;
  }

  private void generateMouseEvent(MouseEvent evt, int type) {
    IPoint viewPoint = new IPoint(evt.getX(), evt.getY());

    int modifierFlags = 0;
    if (SwingUtilities.isRightMouseButton(evt))
      modifierFlags |= UserEvent.FLAG_RIGHT;
    if (evt.isAltDown())
      modifierFlags |= UserEvent.FLAG_ALT;
    if (evt.isControlDown())
      modifierFlags |= UserEvent.FLAG_CTRL;
    if (evt.isMetaDown())
      modifierFlags |= UserEvent.FLAG_META;
    if (evt.isShiftDown())
      modifierFlags |= UserEvent.FLAG_SHIFT;

    UserEvent event = new UserEvent(type, mView, viewPoint, modifierFlags);
    if (mListener != null)
      mListener.handleUserEvent(event);
  }

  private class OurMouseListener implements MouseListener, MouseMotionListener {

    @Override
    public void mousePressed(MouseEvent ev) {
      generateMouseEvent(ev, UserEvent.CODE_DOWN);
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
      generateMouseEvent(ev, UserEvent.CODE_UP);
    }

    @Override
    public void mouseDragged(MouseEvent ev) {
      generateMouseEvent(ev, UserEvent.CODE_DRAG);
    }

    @Override
    public void mouseMoved(MouseEvent ev) {
      unimp("mouseMoved");
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
      unimp("mouseClicked");
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
      unimp("mouseEntered");
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
      unimp("mouseExited");
    }
  }

  private IEditorView mView;
  private UserEvent.Listener mListener;
}
