package com.js.scredit;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import apputil.*;
import static com.js.scredit.ScriptEditor.*;
import static com.js.basic.Tools.*;

public class RectangleObject extends EdObject {

  public RectangleObject(Color color, Point cornerA, Point cornerB) {

    cornerA = Grid.snapToGrid(cornerA, true);
    cornerB = Grid.snapToGrid(cornerB, true);

    setColorValue(color);

    corner0 = new Point(Math.min(cornerA.x, cornerB.x), Math.min(cornerA.y,
        cornerB.y));
    corner2 = new Point(Math.max(cornerA.x, cornerB.x), Math.max(cornerA.y,
        cornerB.y));
  }

  // public RectangleObject(int color, Point origin, float halfWidth,
  // float halfHeight) {
  // setColorValue(color);
  // this.halfWidth = Math.abs(halfWidth);
  // this.halfHeight = Math.abs(halfHeight);
  // tfm.setLocation(origin);
  // }

  private void setColorValue(Color c) {
    this.color = c;
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

    return new RectangleObject(color, nc0, nc1);
    //
    // Point nc0 = horz ? newLocation :
    //
    // EdObject newObj = (EdObject) this.clone();
    // newObj.setLocation(newLocation);
    // return newObj;
  }

  public EdObject snapToGrid() {
    final boolean db = false;

    if (db)
      pr("snapToGrid " + this);

    Point pt1 = corner0; // getCorner(0, true);
    Point pt2 = corner2; // getCorner(2, true);
    Point spt1 = Grid.snapToGrid(pt1, false);
    Point spt2 = Grid.snapToGrid(pt2, false);
    if (spt1 == pt1 && spt2 == pt2)
      return this;
    if (db)
      pr(" spt1=" + spt1 + ", spt2=" + spt2);

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
    RectangleObject r = new RectangleObject(getColor(), spt1, spt2); // origin,
                                                                     // hWidth,
                                                                     // hHeight);
    //
    // Point origin = Point.midPoint(spt1, spt2);
    // float phi = MyMath.polarAngle(origin, spt2) - this.rotation();
    // float radius = Point.distance(origin, spt2);
    // float hWidth = MyMath.cos(phi) * radius;
    // float hHeight = MyMath.sin(phi) * radius;
    //
    // RectangleObject r = new RectangleObject(color(), origin, hWidth,
    // hHeight);
    // r.setRotation(this.rotation());
    // pt1, pt2.x - pt1.x, pt2.y
    // - pt1.y);
    // //new Rect(spt1, spt2));
    // r.tfm.setRotation(tfm.rotation());
    // r.tfm.setScale(tfm.scale());
    r.setSelected(this.isSelected());
    return r;
  }

  @Override
  public EdObject applyColor(Color color) {
    RectangleObject ret = this;
    if (!color.equals(this.color)) {
      ret = (RectangleObject) this.clone();
      ret.setColorValue(color);
    }
    return ret;
  }

  public Color getColor() {
    return color;
  }

  public static final int CODE = 4; // code for polygon object

  /**
   * Clone the object
   */
  public Object clone() {
    RectangleObject e = (RectangleObject) super.clone();
    // e.tfm = new ObjTransform(tfm);

    // reset some lazy-initialized fields
    e.recalcLazy();
    return e;
  }

  public String toString() {
    return "Rectangle";
    // StringBuilder sb = new StringBuilder();
    // sb.append("Rectangle");
    // sb.append(" origin=" + tfm.location());
    // sb.append(" w/2=" + f(halfWidth) + " h/2=" + f(halfHeight));
    // return sb.toString();
  }

  @Override
  public boolean contains(Point pt) {
    return boundingRect().contains(pt);
    // Point rpt = toRectSpace(pt);
    // return Math.abs(rpt.x) <= halfWidth && Math.abs(rpt.y) <= halfHeight;
  }

  // @Override
  // public boolean isGrabPoint(Point worldPt) {
  // final boolean db = false;
  //
  // Point pt = toRectSpace(worldPt);
  // float dx = Math.abs(pt.x) - halfWidth;
  // float dy = Math.abs(pt.y) - halfHeight;
  // dx = Math.max(0, dx);
  // dy = Math.max(0, dy);
  // float dist = MyMath.sqrt(dx * dx + dy * dy);
  //
  // if (db)
  // pr("isGrabPoint " + worldPt + "(" + this + ") pt=" + pt + " dist="
  // + f(dist));
  //
  // dist *= scale();
  // if (db)
  // pr("  adjust for rectangle scale=" + dist);
  //
  // dist *= zoomFactor();
  // if (db)
  // pr("  adjust for zoom factor=" + dist);
  //
  // return dist < 4;
  //
  // }

  // public Point toRectSpace(Point worldPt) {
  // Point pt = tfm.inverse().apply(worldPt);
  // return pt;
  // }
  // public Point toRectSpace(IPoint worldPt) {
  // return toRectSpace(new Point(worldPt));
  // }

  @Override
  public EdObjectFactory getFactory() {
    return FACTORY;
  }

  @Override
  public void render(GLPanel panel) {
    // DArray a = new DArray();

    // if (halfWidth == 0 || halfHeight == 0) {
    // drawLine(tfm.location(), tfm.location());
    // } else
    {

      // for (int i = 0; i < 4; i++)
      // a.add(getCorner(i, true));
      //
      // PolygonObject p = new PolygonObject(color, a);

      if (color != null) {
        panel.setRenderColor(color);
        panel.fillRect(boundingRect());
        // p.render();
      }

      if (color == null || isSelected()) {
        panel.setRenderColor(isSelected() ? Color.YELLOW : Color.DARK_GRAY);

        panel.lineWidth((isSelected() ? 2 : 1) / zoomFactor());

        for (int i = 0; i < 4; i++) {
          Point p0 = getCorner(i);
          Point p1 = getCorner(i + 1);
          panel.drawLine(p0, p1);
        }
      }

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
    final boolean db = false;

    if (db)
      pr("setElementPosition " + elem + " pos=" + pos);

    Point opp = null;

    switch (elem) {
    case 0:
    case 1:
    case 2:
    case 3:
      opp = getCorner(elem + 2);
      if (db)
        pr(" opp=" + opp);

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
    // {
    // opp = getCorner(0, false);
    // Point curr = getCorner(2, false);
    // pos.x = curr.x;
    // }
    // break;
    // case 7:
    // {
    // opp = getCorner(1, false);
    // Point curr = getCorner(3, false);
    // pos.y = curr.y;
    // }
    // break;
    }
    return constructNew(pos, opp);
    //
    // Point spt1 = tfm.matrix().apply(pos);
    // Point spt2 = tfm.matrix().apply(opp);
    // return constructNew(spt1,spt2);
    //
    // // calc new origin
    // Point localOrigin = Point.midPoint(pos,opp);
    //
    //
    //
    // pos.add(location());
    // opp.add(location());
    // ret = new RectangleObject(color, pos, opp);
    // ret.setSelected(this.isSelected());
    // ret.setScale(this.scale());
    // ret.setRotation(this.rotation());
    // return ret;

  }

  public static EdObjectFactory FACTORY = new RectangleFactory();

  private static class RectangleFactory extends EdObjectFactory {

    @Override
    public MouseOper isEditingSelectedObject(int slot, EdObject obj,
        IPoint mousePt) {

      final boolean db = false;

      MouseOper ret = null;

      if (db)
        pr("isEditingSelectedObject (rectangle) " + obj + "? mousePt="
            + mousePt + " slot=" + slot);
      RectangleObject p = (RectangleObject) obj;

      Point pt = new Point(mousePt); // mousePt; //p.toRectSpace(mousePt);

      float tolerance = 8 / zoomFactor(); // * p.scale() / zoomFactor();

      int edElement = -1;

      for (int c = 0; c < 4; c++) {
        Point corn = p.getCorner(c);
        float dist = MyMath.distanceBetween(corn, pt);
        if (db)
          pr("distance from corner " + c + " is " + dist);

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
          if (db)
            pr("distance from edge " + edge + " is " + dist);
          if (dist < tolerance) {
            edElement = edge + 4;
            break;
          }
        }
      if (edElement >= 0) {
        ret = new EdRectangleOper(mousePt, slot, edElement);
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
      unimp("write color");
      ArrayList<Point> points = new ArrayList();
      points.add(so.corner0);
      points.add(so.corner2);
      map.put("points", Point.toJSON(points));
    }

    @Override
    public EdObject parse(Script script, JSONObject map) throws JSONException {
      unimp("parse color");
      ArrayList<Point> points = Point.parseListFromJSON(map
          .getJSONArray("points"));
      Point ptA = points.get(0);
      Point ptB = points.get(1);
      RectangleObject so = new RectangleObject(Color.blue, ptA, ptB);
      return so;
    }

    /**
     * Write Rectangle to ScriptsFile. Converts it to a polygon, and writes it
     * as that.
     * 
     * @param sf
     *          ScriptsFile to write to
     * @param obj
     *          RectangleObject
     */
    public void write(ScriptsFile sf, EdObject obj) throws IOException {

      RectangleObject so = (RectangleObject) obj;

      ArrayList<Point> a = new ArrayList();
      for (int i = 0; i < 4; i++)
        a.add(so.getCorner(i));

      PolygonObject p = new PolygonObject(so.getColor(), a);

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
    return corner2.x - corner0.x;
  }

  public float height() {
    return corner2.y - corner0.y;
  }

  @Override
  public void setLocation(Point pt) {
    float w = width();
    float h = height();
    corner0 = new Point(pt);
    corner2 = new Point(pt.x + w, pt.y + h);
    // tfm.setLocation(new Point(pt));
    recalcLazy();
  }

  @Override
  public String getInfoMsg() {
    StringBuilder sb = new StringBuilder();

    IPoint c0 = new IPoint(corner0);
    IPoint c2 = new IPoint(corner2);

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

  public Point getCorner(int c) { // , boolean toWorldSpace) {
    c = MyMath.myMod(c, 4);

    float x = corner0.x;
    // halfWidth;
    float y = corner0.y; // halfHeight;
    if (c == 1 || c == 2)
      x = corner2.x;
    if (c >= 2)
      y = corner2.y;
    Point pt = new Point(x, y);
    // if (toWorldSpace)
    // pt = tfm.matrix().apply(pt);
    return pt;
  }

  @Override
  public Point location() {
    return new Point(corner0);
    // return new Point(tfm.location());
  }

  @Override
  public void setRotation(float angle) {
    // tfm.setRotation(angle);
    // recalcLazy();
    //
  }

  private void recalcLazy() {
    tfmBounds = null;
  }

  @Override
  public float rotation() {
    return 0; // tfm.rotation();
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
    if (tfmBounds == null) {
      tfmBounds = new Rect(corner0, corner2);
      // DArray a = new DArray();
      // for (int i = 0; i < 4; i++)
      // a.add(getCorner(i, true));
      // tfmBounds = Rect.boundsForPoints(a);
    }
    return tfmBounds;
  }

  public boolean isWellDefined() {
    return corner2.x > corner0.x && corner2.y > corner0.y; // halfWidth *
                                                           // halfHeight > 0;
  }

  private Point corner0, corner2;

  // // radius of rectangle in each dimension
  // private float halfWidth, halfHeight;
  //
  // private ObjTransform tfm = new ObjTransform();

  // axis-aligned bounding rect of transformed rectangle
  private Rect tfmBounds;

  private Color color; // color and shade
}
