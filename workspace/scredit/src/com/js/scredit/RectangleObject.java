package com.js.scredit;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Freezable;
import com.js.basic.JSONTools;
import com.js.editor.UserOperation;
import com.js.editor.UserEvent;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.scredit.ScriptEditor.*;
import static com.js.basic.Tools.*;

public class RectangleObject extends EdObject {

  public RectangleObject(EdObject source, Color color, Point cornerA,
      Point cornerB) {
    super(source);
    cornerA = Grid.snapToGrid(cornerA, true);
    cornerB = Grid.snapToGrid(cornerB, true);

    setColor(color);

    mBottomLeftCorner = new Point(Math.min(cornerA.x, cornerB.x), Math.min(
        cornerA.y, cornerB.y));
    mTopRightCorner = new Point(Math.max(cornerA.x, cornerB.x), Math.max(
        cornerA.y, cornerB.y));
  }

  private void setColor(Color color) {
    ASSERT(color != null);
    this.mColor = color;
  }

  @Override
  public EdObject flip(boolean horz, Point newLocation) {
    Point nc0, nc1;
    if (horz) {
      nc0 = new Point(newLocation.x - width(), newLocation.y);
    } else {
      nc0 = new Point(newLocation.x, newLocation.y - height());
    }
    nc1 = new Point(nc0.x + width(), nc0.y + height());

    return new RectangleObject(this, mColor, nc0, nc1);
  }

  public EdObject snapToGrid() {
    Point pt1 = mBottomLeftCorner;
    Point pt2 = mTopRightCorner;
    Point spt1 = Grid.snapToGrid(pt1, false);
    Point spt2 = Grid.snapToGrid(pt2, false);
    if (spt1 == pt1 && spt2 == pt2)
      return this;
    return constructNew(spt1, spt2);
  }

  /**
   * Construct a new rectangle based on current rectangle, but with new opposite
   * corners; we try to preserve the rotation (but not the scale)
   * 
   * @param spt1
   *          one corner, in world space
   * @param spt2
   *          opposite corner, in world space
   * @return
   */
  private RectangleObject constructNew(Point spt1, Point spt2) {
    RectangleObject r = new RectangleObject(this, getColor(), spt1, spt2);
    return r;
  }

  @Override
  public EdObject applyColor(Color color) {
    RectangleObject ret = this;
    if (!color.equals(this.mColor)) {
      ret = copyOf(this);
      ret.setColor(color);
    }
    return ret;
  }

  public Color getColor() {
    return mColor;
  }

  public static final int CODE = 4; // code for rectangle object

  @Override
  public Freezable getMutableCopy() {
    RectangleObject r = new RectangleObject(this, mColor, mBottomLeftCorner,
        mTopRightCorner);
    return r;
  }

  public String toString() {
    return "Rectangle";
  }

  @Override
  public boolean contains(Point pt) {
    return boundingRect().contains(pt);
  }

  @Override
  public EdObjectFactory getFactory() {
    return FACTORY;
  }

  @Override
  public void render(GLPanel panel, boolean isSelected, boolean isEditable) {
    ASSERT(mColor != null);

    Rect r = boundingRect();
    panel.setRenderColor(mColor);
    panel.fillRect(r);

    if (isSelected) {
      panel.setRenderColor(Color.YELLOW);
      panel.lineWidth(2 / zoomFactor());
      panel.drawFrame(r);
    }
  }

  public Point getElementPosition(int elem) {
    return getCorner(elem);
  }

  /**
   * Construct modified rectangle for an element's new position
   * 
   * @param elem
   *          element (0..3: corner, 4..7 edge)
   * @param pos
   *          position of element, in rectangle space
   * @return new rectangle
   */
  public RectangleObject setElementPosition(int elem, Point pos) {
    Point opp = null;

    switch (elem) {
    case 0:
    case 1:
    case 2:
    case 3:
      opp = getCorner(elem + 2);
      break;

    case 4:
    case 6: {
      opp = getCorner(elem + 2);
      Point curr = getCorner(elem);
      pos.x = curr.x;
    }
      break;
    case 5:
    case 7: {
      opp = getCorner(elem + 2);
      Point curr = getCorner(elem);
      pos.y = curr.y;
    }
      break;
    }
    return constructNew(pos, opp);
  }

  public static EdObjectFactory FACTORY = new RectangleFactory();

  private static class RectangleFactory extends EdObjectFactory {

    @Override
    public UserOperation isEditingSelectedObject(int slot, EdObject obj,
        UserEvent event) {
      UserOperation ret = null;
      RectangleObject p = (RectangleObject) obj;
      Point pt = event.getWorldLocation();
      float tolerance = 8 / zoomFactor();
      int edElement = -1;
      for (int c = 0; c < 4; c++) {
        Point corn = p.getCorner(c);
        float dist = MyMath.distanceBetween(corn, pt);
        if (dist < tolerance) {
          edElement = c;
          break;
        }
      }
      if (edElement < 0)
        for (int edge = 0; edge < 4; edge++) {
          Point ep0 = p.getCorner(edge);
          Point ep1 = p.getCorner((edge + 1) & 3);
          float dist = MyMath.ptDistanceToSegment(pt, ep0, ep1, null);
          if (dist < tolerance) {
            edElement = edge + 4;
            break;
          }
        }
      if (edElement >= 0) {
        ret = EditOper.buildEditExistingOper(event, slot, edElement);
      }
      return ret;
    }

    @Override
    public String getTag() {
      return "R";
    }

    @Override
    public void write(Script script, JSONObject map, EdObject obj)
        throws JSONException {

      RectangleObject so = (RectangleObject) obj;
      JSONTools.put(map, "color", so.getColor());
      ArrayList<Point> points = new ArrayList();
      points.add(so.mBottomLeftCorner);
      points.add(so.mTopRightCorner);
      Point.put(points, map, "points");
    }

    @Override
    public EdObject parse(Script script, JSONObject map) throws JSONException {
      List<Point> points = Point.getList(map, "points");
      Point ptA = points.get(0);
      Point ptB = points.get(1);
      RectangleObject so = new RectangleObject(null, JSONTools.getColor(map,
          "color"), ptA, ptB);
      return so;
    }

    /**
     * Write Rectangle to ScriptsFile. Converts it to a polygon, and writes it
     * as that.
     */
    public void write(ScriptsFile sf, EdObject obj) throws IOException {

      RectangleObject so = (RectangleObject) obj;

      ArrayList<Point> a = new ArrayList();
      for (int i = 0; i < 4; i++)
        a.add(so.getCorner(i));

      PolygonObject p = new PolygonObject(obj, so.getColor(), a);

      p.getFactory().write(sf, p);
      // DataOutput dw = sf.outputStream();
      //
      // dw.writeByte(getCode());
      // dw.writeShort(so.color);
      //
      // for (int i = 0; i < 2; i++) {
      // Point pt = so.getCorner(i * 2);
      // dw.writeFloat(pt.x);
      // dw.writeFloat(pt.y);
      // }

    }

    @Override
    public int getCode() {
      return CODE;
    }
  }

  public float width() {
    return mTopRightCorner.x - mBottomLeftCorner.x;
  }

  public float height() {
    return mTopRightCorner.y - mBottomLeftCorner.y;
  }

  @Override
  public void setLocation(Point pt) {
    mutate();
    float w = width();
    float h = height();
    mBottomLeftCorner = new Point(pt);
    mTopRightCorner = new Point(pt.x + w, pt.y + h);
  }

  @Override
  public String getInfoMsg() {
    StringBuilder sb = new StringBuilder();

    IPoint c0 = new IPoint(mBottomLeftCorner);
    IPoint c2 = new IPoint(mTopRightCorner);

    sb.append("(");
    sb.append(c0.x);
    sb.append(",");
    sb.append(c0.y);
    sb.append(")...");
    sb.append("(");
    sb.append(c2.x);
    sb.append(",");
    sb.append(c2.y);
    sb.append(")  ");
    sb.append("W: " + (c2.x - c0.x));

    sb.append(" H:" + (c2.y - c0.y));
    return sb.toString();
  }

  public Point getCorner(int c) {
    c = MyMath.myMod(c, 4);

    float x = mBottomLeftCorner.x;
    float y = mBottomLeftCorner.y;
    if (c == 1 || c == 2)
      x = mTopRightCorner.x;
    if (c >= 2)
      y = mTopRightCorner.y;
    Point pt = new Point(x, y);
    return pt;
  }

  @Override
  public Point location() {
    return new Point(mBottomLeftCorner);
  }

  @Override
  public void setRotation(float angle) {
  }

  @Override
  public float rotation() {
    return 0;
  }

  @Override
  public void setScale(float scale) {
  }

  @Override
  public float scale() {
    return 1;
  }

  @Override
  public Rect boundingRect() {
    return new Rect(mBottomLeftCorner, mTopRightCorner);
  }

  public boolean isWellDefined() {
    return mTopRightCorner.x > mBottomLeftCorner.x
        && mTopRightCorner.y > mBottomLeftCorner.y;
  }

  public static UserOperation buildNewObjectOperation() {
    return EditOper.buildAddOper();
  }

  private Point mBottomLeftCorner, mTopRightCorner;
  private Color mColor;

  private static class EditOper extends UserOperation {

    public static EditOper buildAddOper() {
      EditOper oper = new EditOper();
      oper.mAddingNew = true;
      return oper;
    }

    public static UserOperation buildEditExistingOper(UserEvent event,
        int slot, int handle) {
      EditOper oper = new EditOper();
      oper.init(event, slot, handle);
      return oper;
    }

    private void init(UserEvent event, int slot, int handle) {
      mEditHandle = handle;
      mStartEvent = event;
      if (mAddingNew) {
        slot = items().size();
        // create a new rectangle at mouse position
        RectangleObject obj = new RectangleObject(null, ScriptEditor.color(),
            event.getWorldLocation(), event.getWorldLocation());
        ScriptEditor.items().add(obj);
        ScriptEditor.items().setSelected(new SlotList(slot));
      } else {

      }
      mEditSlot = slot;
      mOriginalRect = ScriptEditor.items().get(mEditSlot);
      ScriptEditor.setInfo(mOriginalRect);
    }

    @Override
    public void processUserEvent(UserEvent event) {

      switch (event.getCode()) {

      case UserEvent.CODE_DOWN:
        if (!mAddingNew)
          throw new IllegalStateException();
        init(event, -1, 0);
        break;

      case UserEvent.CODE_DRAG: {

        Point adjMousePt = Point.sum(event.getWorldLocation(),
            grabOffset(event.getWorldLocation()));
        Point rectPt = adjMousePt;
        Point pt = Grid.snapToGrid(rectPt, true);

        RectangleObject r2 = mOriginalRect.setElementPosition(mEditHandle, pt);
        ScriptEditor.items().set(mEditSlot, r2);

        ScriptEditor.setInfo(r2);
      }

        break;

      case UserEvent.CODE_UP: {
        RectangleObject r = ScriptEditor.items().get(mEditSlot);
        if (r.isWellDefined()) {
          CommandForGeneralChanges command = new CommandForGeneralChanges(
              "add rect", (mAddingNew ? "Add" : "Edit") + " Rectangle");
          command.finish();
          ScriptEditor.editor().recordCommand(command);
        }
        event.clearOperation();
      }
        break;
      }
    }

    private Point grabOffset(Point location) {
      if (mGrabOffset == null)
        mGrabOffset = Point
            .difference(location, mStartEvent.getWorldLocation());
      return mGrabOffset;
    }

    private UserEvent mStartEvent;
    // true if this is for adding a new rectangle, vs editing existing
    private boolean mAddingNew;
    // slot containing edit rectangle
    private int mEditSlot;
    // Which part of the rectangle is being edited (e.g. side, corner)
    private int mEditHandle;
    // Rectangle before editing occurred (or, initial 'new' rectangle)
    private RectangleObject mOriginalRect;
    // offset of handle position to initial mouse press
    private Point mGrabOffset;
  }

}
