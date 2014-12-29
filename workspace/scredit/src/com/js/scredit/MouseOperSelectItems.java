package com.js.scredit;

import java.awt.Color;
import java.util.*;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;
import apputil.*;

public class MouseOperSelectItems extends MouseOper {

  private static final int STATE_NONE = 0;
  private static final int STATE_WAITFORDRAG1 = 1;
  private static final int STATE_MOVINGITEMS = 2;
  private static final int STATE_WAITFORDRAG2 = 3;
  private static final int STATE_ADJUSTINGBOX = 4;

  public MouseOperSelectItems() {
    ASSERT(singleton == null);
    singleton = this;
  }

  private static MouseOperSelectItems singleton;

  /**
   * Modify the singleton instance to start a move operation. Used to append a
   * 'move' operation to an 'add new item' operation.
   * 
   * @return
   */
  public static MouseOperSelectItems startMovingSelectedItems() {
    ASSERT(singleton != null);
    singleton.oper = new MoveObjectsReversible(currentPtF);

    singleton.setState(STATE_MOVINGITEMS);
    ScriptEditor.editor().registerPush(singleton.oper);

    return singleton;
  }

  @Override
  public boolean mouseDown() {

    final boolean db = false;
    if (db)
      pr("MouseOperSelectItems.mouseDown() " + mouseStateString());

    boolean f = false;

    boolean resetDup = false;

    do {
      if (right())
        break;

      boolean ctrlMode = ev.isControlDown();

      if (!ctrlMode) {
        int mouseItem = -1;
        do {
          if (db)
            pr("testing if press in same location");

          if (lastFoundLoc == null) {
            if (db)
              pr(" no last defined");
            break;
          }
          if (MyMath.distanceBetween(currentPt, lastFoundLoc) > 2) {
            if (db)
              pr(" not pressed at same location");
            break;
          }
          if (itemsUnderMouse.length < 2) {
            if (db)
              pr(" less than two items in list");

            break;
          }

          lastFoundCursor = (1 + lastFoundCursor) % itemsUnderMouse.length;

          mouseItem = itemsUnderMouse[lastFoundCursor];

        } while (false);

        if (db)
          pr(" mouseItem (A) = " + mouseItem);

        if (mouseItem < 0) {

          lastFoundLoc = new IPoint(currentPt);
          itemsUnderMouse = toArray(findItemsAtMouse(lastFoundLoc));
          lastFoundCursor = 0;

          if (itemsUnderMouse.length > 0)
            mouseItem = itemsUnderMouse[0];
          if (db)
            pr(" mouseItem (B) = " + mouseItem);

        }

        if (mouseItem < 0) {
          resetDup = true;
          ScriptEditor.unselectAll();
          ScriptEditor.repaint();
          setState(STATE_WAITFORDRAG1);
          f = true;
        } else {
          if (db)
            pr(" starting MoveObjectsReversible");

          EdObject obj = ScriptEditor.item(mouseItem);
          if (!obj.isSelected()) {
            ScriptEditor.unselectAll();
            obj.setSelected(true);
            ScriptEditor.repaint();
          }

          {
            oper = new MoveObjectsReversible(currentPtF);
            // if there is an existing operation, and it has the same
            // highlighted items,
            // continue it

            boolean continuing = false;

            {
              Reversible tos = ScriptEditor.editor().registerPeek();
              if (tos != null && tos instanceof MoveObjectsReversible) {
                MoveObjectsReversible r = (MoveObjectsReversible) tos;
                if (r.sameItemsAs(oper)) {
                  oper = r;
                  oper.continueWithNewMouseDown(currentPtF);
                  continuing = true;
                }
              }
            }

            if (!continuing) {
              ScriptEditor.editor().registerPush(oper);
            }
          }
          setState(STATE_MOVINGITEMS);
          f = true;
        }
      } else {
        // ctrl is pressed

        int mouseItem = -1;
        lastFoundLoc = new IPoint(currentPt);
        itemsUnderMouse = toArray(findItemsAtMouse(lastFoundLoc));
        lastFoundCursor = 0;
        if (itemsUnderMouse.length > 0)
          mouseItem = itemsUnderMouse[lastFoundCursor];

        if (mouseItem < 0) {
          resetDup = true;
          setState(STATE_WAITFORDRAG1);
          f = true;
        } else {
          pressedAtItem = mouseItem;
          setState(STATE_WAITFORDRAG2);
          f = true;
        }
      }
      if (resetDup)
        Dup.reset();
      f = true;
    } while (false);
    return f;
  }

  @Override
  public void mouseMove(boolean drag) {
    switch (state) {
    case STATE_WAITFORDRAG1:
    case STATE_WAITFORDRAG2:
      boxStart = startPt;
      state = STATE_ADJUSTINGBOX;
      boxEnd = currentPt;
      ScriptEditor.repaint();
      break;
    case STATE_ADJUSTINGBOX:
      boxEnd = currentPt;
      ScriptEditor.repaint();
      break;
    case STATE_MOVINGITEMS:
      oper.update(currentPtF);
      break;
    }
  }

  @Override
  public void mouseUp() {
    boolean clr = true;

    switch (state) {
    case STATE_WAITFORDRAG1:
      setState(STATE_NONE);
      break;
    case STATE_WAITFORDRAG2: {
      ScriptEditor.item(pressedAtItem).toggleSelected();
      setState(STATE_NONE);
    }
      break;
    case STATE_ADJUSTINGBOX: {
      boxEnd = currentPt;
      toggleBoxedObjects(boxStart, boxEnd);
      setState(STATE_NONE);
    }
      break;
    }
    if (clr)
      clearOperation();
    ScriptEditor.repaint();
  }

  /**
   * Determine frontmost item under mouse, giving priority to selected items
   * 
   * @param mouseLoc
   * 
   */
  private static ArrayList<Integer> findItemsAtMouse(IPoint iMouseLoc) {
    final boolean db = false;
    if (db)
      pr("findItemsAtMouse " + iMouseLoc);

    ArrayList<Integer> mouseItems = new ArrayList();
    EdObject bestFound = null;
    int bestIndex = -1;

    Point mouseLoc = new Point(iMouseLoc);

    ObjArray items = ScriptEditor.items();

    // first item in array is first plotted, therefore hidden behind others
    for (int i = items.size() - 1; i >= 0; i--) {
      EdObject obj = items.get(i);

      if (obj.isGrabPoint(mouseLoc)) {
        if (bestFound == null || (obj.isSelected() && !bestFound.isSelected())) {
          bestFound = obj;
          bestIndex = mouseItems.size();
          if (db)
            pr("  item " + i
                + " is under mouse, setting bestFound to its position "
                + bestIndex);
        }
        mouseItems.add(i);
      }
    }
    if (db)
      pr(" found items: " + mouseItems + ", best=" + bestIndex);

    ArrayList<Integer> a = new ArrayList();
    for (int i = 0; i < mouseItems.size(); i++)
      a.add(mouseItems.get(MyMath.myMod(i + bestIndex, mouseItems.size())));
    if (db)
      pr(" after adjusting for best index, returning:\n" + a);

    return a;
  }

  private void toggleBoxedObjects(IPoint p1, IPoint p2) {
    ObjArray items = ScriptEditor.items();
    Rect r = new IRect(p1, p2).toRect();
    for (int i = 0; i < items.size(); i++) {
      EdObject obj = items.get(i);
      if (!obj.isContainedBy(r))
        continue;
      obj.setSelected(!obj.isSelected());
    }
  }

  private void setState(int s) {
    final boolean db = false;
    if (state != s) {
      if (db)
        pr("chg state from " + stateNames[state] + " to " + stateNames[s]);
      state = s;
    }
  }

  @Override
  public void stop() {
    if (state == STATE_MOVINGITEMS) {
      // if we didn't move anywhere, pop the operation
      if (oper.getTranslate().magnitude() == 0) {
        ScriptEditor.editor().registerPop();
      }

      oper = null;
    }
    setState(STATE_NONE);
  }

  private IRect getBox() {
    IRect box = null;
    if (state == STATE_ADJUSTINGBOX) {
      box = new IRect(boxStart, boxEnd);
    }
    return box;
  }

  private GLPanel getPanel() {
    return null;
  }

  public void render() {
    GLPanel panel = getPanel();
    if (panel == null) {
      unimp("panel");
      return;
    }
    IRect box = getBox();
    if (box != null) {
      panel.setRenderColor(Color.darkGray);
      panel.drawFrame(box.x, box.y, box.width, box.height);
    }
  }

  // STATE_x
  private int state;

  private static int lastFoundCursor;
  private static int[] itemsUnderMouse;
  private static IPoint lastFoundLoc;
  private static String[] stateNames = { "NONE", "WAITFORDRAG1", "MOVINGITEMS",
      "WAITFORDRAG2", "ADUSTINGBOX", };

  // item user pressed on with ctrl pressed as well, prior to STATE_WAITFORDRAG2
  private int pressedAtItem;

  // If MOVINGITEMS, the operation performing the move
  private MoveObjectsReversible oper;
  private static IPoint boxStart, boxEnd;

}
