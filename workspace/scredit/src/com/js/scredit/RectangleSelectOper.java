package com.js.scredit;

import static com.js.basic.Tools.*;

import java.awt.Color;
import java.util.ArrayList;

import com.js.editor.UserEvent;
import com.js.editor.UserOperation;
import com.js.geometry.Rect;
import com.js.myopengl.GLPanel;

public class RectangleSelectOper extends UserOperation {

  @Override
  public void processUserEvent(UserEvent event) {
    switch (event.getCode()) {
    case UserEvent.CODE_DRAG:
      mDragEvent = event;
      break;
    case UserEvent.CODE_UP:
      finishSelect();
      event.clearOperation();
      break;
    }
  }

  public static UserOperation build(UserEvent event) {
    return new RectangleSelectOper(event);
  }

  private RectangleSelectOper(UserEvent event) {
    mInitialEvent = event;
  }

  @Override
  public void paint() {
    Rect r = getRect();
    if (r == null)
      return;
    GLPanel mPanel = ScriptEditor.getEditorPanel();
    mPanel.setRenderColor(Color.YELLOW.darker());
    mPanel.drawFrame(r);
  }

  private void finishSelect() {
    Rect r = getRect();
    if (r == null)
      return;
    EdObjectArray items = ScriptEditor.items();

    ArrayList<Integer> slots = new ArrayList();

    for (int i = 0; i < items.size(); i++) {
      EdObject obj = items.get(i);
      if (!r.contains(obj.boundingRect()))
        continue;
      slots.add(i);
    }
    unimp("refactor slot list as in geometry");
  }

  private Rect getRect() {
    if (mDragEvent == null)
      return null;
    Rect r = new Rect(mInitialEvent.getWorldLocation(),
        mDragEvent.getWorldLocation());
    return r;
  }

  private UserEvent mInitialEvent;
  private UserEvent mDragEvent;
}
