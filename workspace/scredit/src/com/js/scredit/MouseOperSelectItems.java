package com.js.scredit;

import java.awt.Color;
import java.util.*;

import com.js.editor.Command;
import com.js.editor.UserOperation;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

public class MouseOperSelectItems extends UserOperation {

  private static final int STATE_NONE = 0;
  private static final int STATE_WAITFORDRAG1 = 1;
  private static final int STATE_MOVINGITEMS = 2;
  private static final int STATE_WAITFORDRAG2 = 3;
  private static final int STATE_ADJUSTINGBOX = 4;

  /**
   * Modify the singleton instance to start a move operation. Used to append a
   * 'move' operation to an 'add new item' operation.
   */
  public static MouseOperSelectItems startMovingSelectedItems() {
    // ASSERT(sSingleton != null);
    // sSingleton.mMoveCommand = new MoveObjectsCommand(currentPtF);
    //
    // sSingleton.setState(STATE_MOVINGITEMS);
    // ScriptEditor.editor().registerPush(sSingleton.mMoveCommand);
    //
    // return sSingleton;
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean mouseDown() {

    boolean f = false;

    boolean resetDup = false;

    do {
      if (right(ev))
        break;

      boolean ctrlMode = ev.isControlDown();

      if (!ctrlMode) {
        int mouseItem = -1;
        do {
          if (db)
            pr("testing if press in same location");

          if (sLastFoundLoc == null) {
            if (db)
              pr(" no last defined");
            break;
          }
          if (MyMath.distanceBetween(currentPt, sLastFoundLoc) > 2) {
            if (db)
              pr(" not pressed at same location");
            break;
          }
          if (sItemsUnderMouse.length < 2) {
            if (db)
              pr(" less than two items in list");

            break;
          }

          sLastFoundCursor = (1 + sLastFoundCursor) % sItemsUnderMouse.length;

          mouseItem = sItemsUnderMouse[sLastFoundCursor];

        } while (false);

        if (db)
          pr(" mouseItem (A) = " + mouseItem);

        if (mouseItem < 0) {

          sLastFoundLoc = new IPoint(currentPt);
          sItemsUnderMouse = toArray(findItemsAtMouse(sLastFoundLoc));
          sLastFoundCursor = 0;

          if (sItemsUnderMouse.length > 0)
            mouseItem = sItemsUnderMouse[0];
          if (db)
            pr(" mouseItem (B) = " + mouseItem);

        }

        if (mouseItem < 0) {
          resetDup = true;
          ScriptEditor.items().clearAllSelected();
          ScriptEditor.repaint();
          setState(STATE_WAITFORDRAG1);
          f = true;
        } else {
          if (db)
            pr(" starting MoveObjectsReversible");

          EdObject obj = ScriptEditor.items().get(mouseItem);
          if (!obj.isSelected()) {
            ScriptEditor.items().clearAllSelected();
            obj.setSelected(true);
            ScriptEditor.repaint();
          }

          {
            mMoveCommand = new MoveObjectsCommand(currentPtF);
            // if there is an existing operation, and it has the same
            // highlighted items,
            // continue it

            boolean continuing = false;

            {
              Command tos = ScriptEditor.editor().registerPeek();
              if (tos != null && tos instanceof MoveObjectsCommand) {
                MoveObjectsCommand r = (MoveObjectsCommand) tos;
                if (r.sameItemsAs(mMoveCommand)) {
                  mMoveCommand = r;
                  mMoveCommand.continueWithNewMouseDown(currentPtF);
                  continuing = true;
                }
              }
            }

            if (!continuing) {
              ScriptEditor.editor().registerPush(mMoveCommand);
            }
          }
          setState(STATE_MOVINGITEMS);
          f = true;
        }
      } else {
        // ctrl is pressed

        int mouseItem = -1;
        sLastFoundLoc = new IPoint(currentPt);
        sItemsUnderMouse = toArray(findItemsAtMouse(sLastFoundLoc));
        sLastFoundCursor = 0;
        if (sItemsUnderMouse.length > 0)
          mouseItem = sItemsUnderMouse[sLastFoundCursor];

        if (mouseItem < 0) {
          resetDup = true;
          setState(STATE_WAITFORDRAG1);
          f = true;
        } else {
          mPressedAtItem = mouseItem;
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
    switch (mState) {
    case STATE_WAITFORDRAG1:
    case STATE_WAITFORDRAG2:
      mBoxStart = startPt;
      mState = STATE_ADJUSTINGBOX;
      mBoxEnd = currentPt;
      ScriptEditor.repaint();
      break;
    case STATE_ADJUSTINGBOX:
      mBoxEnd = currentPt;
      ScriptEditor.repaint();
      break;
    case STATE_MOVINGITEMS:
      mMoveCommand.update(currentPtF);
      break;
    }
  }

  @Override
  public void mouseUp() {
    boolean clr = true;

    switch (mState) {
    case STATE_WAITFORDRAG1:
      setState(STATE_NONE);
      break;
    case STATE_WAITFORDRAG2: {
      EdObject obj = ScriptEditor.items().get(mPressedAtItem);
      obj.setSelected(!obj.isSelected());
      setState(STATE_NONE);
    }
      break;
    case STATE_ADJUSTINGBOX: {
      mBoxEnd = currentPt;
      toggleBoxedObjects(mBoxStart, mBoxEnd);
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

    EdObjectArray items = ScriptEditor.items();

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
    EdObjectArray items = ScriptEditor.items();
    Rect r = new IRect(p1, p2).toRect();
    for (EdObject obj : items) {
      if (!obj.isContainedBy(r))
        continue;
      obj.setSelected(!obj.isSelected());
    }
  }

  private void setState(int s) {
    final boolean db = false;
    if (mState != s) {
      if (db)
        pr("chg state from " + sStateNames[mState] + " to " + sStateNames[s]);
      mState = s;
    }
  }

  @Override
  public void stop() {
    if (mState == STATE_MOVINGITEMS) {
      // if we didn't move anywhere, pop the operation
      if (mMoveCommand.getTranslate().magnitude() == 0) {
        ScriptEditor.editor().registerPop();
      }

      mMoveCommand = null;
    }
    setState(STATE_NONE);
  }

  private IRect getBox() {
    IRect box = null;
    if (mState == STATE_ADJUSTINGBOX) {
      box = new IRect(mBoxStart, mBoxEnd);
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

  private static int sLastFoundCursor;
  private static int[] sItemsUnderMouse;
  private static IPoint sLastFoundLoc;
  private static String[] sStateNames = {//
  "NONE", "WAITFORDRAG1", "MOVINGITEMS", "WAITFORDRAG2", "ADUSTINGBOX", };

  // item user pressed on with ctrl pressed as well, prior to STATE_WAITFORDRAG2
  private int mPressedAtItem;

  // STATE_x
  private int mState;

  // If MOVINGITEMS, the operation performing the move
  private MoveObjectsCommand mMoveCommand;
  private static IPoint mBoxStart, mBoxEnd;

}
