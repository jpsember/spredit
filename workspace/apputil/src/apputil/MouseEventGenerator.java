package apputil;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

import com.js.editor.UserEvent;
import com.js.editor.UserEventSource;
import com.js.geometry.*;

/**
 * Listens to mouse events within a AWT view, and generates corresponding
 * UserEvents
 */
public class MouseEventGenerator {

  /**
   * Specify view to listen to (Java) mouse events
   */
  public void setView(UserEventSource view, Component c) {
    mView = view;
    OurMouseListener ls = new OurMouseListener();
    c.addMouseListener(ls);
    c.addMouseMotionListener(ls);
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
    event.getManager().processUserEvent(event);
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

  private UserEventSource mView;
}
