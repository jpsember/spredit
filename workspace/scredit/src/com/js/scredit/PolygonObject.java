package com.js.scredit;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import apputil.*;
import com.js.basic.*;
import com.js.editor.MouseOper;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

public class PolygonObject extends EdObject {

  private PolygonObject() {
  }

  public PolygonObject(Color color, List<Point> pts) {
    this(color, pts.toArray(new Point[0]), null);
  }

  public PolygonObject(Color color, Point[] pts, int[] vertIndices) {
    setColorValue(color);
    this.pts2 = pts;
    this.ptInds = vertIndices;
  }

  private void setColorValue(Color c) {
    this.mColor = c;
  }

  @Override
  public EdObject snapToGrid() {
    boolean changed = false;
    Point[] pts = new Point[nPoints()];
    for (int i = 0; i < pts.length; i++) {
      Point p = getPoint(i);
      pts[i] = Grid.snapToGrid(p, false);
      if (p != pts[i])
        changed = true;
    }
    if (!changed)
      return this;

    PolygonObject p = new PolygonObject(color(), pts, null);
    return p;

  }

  @Override
  public EdObject applyColor(Color color) {
    PolygonObject ret = this;
    if (!color.equals(this.mColor)) {
      ret = this.getCopy();
      ret.setColorValue(color);
    }
    return ret;
  }

  /**
   * Determine which part of the polygon's boundary is very near a point
   * 
   * @param mptf
   *          point to test
   * @return 0 if not on boundary; 1 + v if at vertex #v; -(1 + e) if at edge
   *         #e.
   */
  public int findGrabbedElement(Point mptf) {
    final boolean db = false;
    PolygonObject p = this;

    int ret = 0;

    for (int pass = 0; pass < 2; pass++) {
      final float EPS = 6.0f;

      int bestIndex = -1;
      float bestDist = 0;

      for (int i = 0; i < p.nPoints(); i++) {

        if (pass == 0) {
          Point pt = p.getPoint(i);

          float dist = MyMath.distanceBetween(mptf, pt);
          if (bestIndex >= 0 && dist > bestDist)
            continue;

          if (db)
            pr(" dist from vert " + i + ": " + pt + " is " + dist);

          bestIndex = i;
          bestDist = dist;

        } else {
          if (p.isWellDefined()) {
            Point pt = p.getPoint(i);
            Point pt2 = p.getPoint(i + 1);
            float dist = MyMath.ptDistanceToSegment(mptf, pt, pt2, null);
            if (bestIndex >= 0 && dist > bestDist)
              continue;

            if (db)
              pr(" dist from edge " + i + ": " + pt + pt2 + " is " + dist);
            bestIndex = i;
            bestDist = dist;

          }
        }

      }

      if (bestIndex < 0)
        continue;

      // warn("tune epsilon later");

      if (bestDist > EPS / ScriptEditor.zoomFactor())
        continue;

      if (pass == 0) {
        ret = 1 + bestIndex;
      } else {
        ret = -(1 + bestIndex);
      }
      break;
    }
    return ret;
  }

  @Override
  public Color getColor() {
    return mColor;
  }

  public Point[] getPoints() {
    return pts2;
  }

  public int[] getVertIndices() {
    return ptInds;
  }

  public static ActionHandler DELETE_VERTEX = new ActionHandler() {
    @Override
    public boolean shouldBeEnabled() {
      return EdPolygonOper.activePoly() != null
          && EdPolygonOper.activePoly().nPoints() > 0;
    }

    @Override
    public void go() {
      EdPolygonOper.activeEditor().deleteTarget();
      ScriptEditor.repaint();
    }
  };
  public static ActionHandler PREV_VERTEX = new ActionHandler() {
    @Override
    public boolean shouldBeEnabled() {
      return EdPolygonOper.activePoly() != null
          && EdPolygonOper.activePoly().nPoints() > 1;
    }

    @Override
    public void go() {
      EdPolygonOper.activeEditor().adjustTarget(-1);
      ScriptEditor.repaint();

    }
  };
  public static ActionHandler NEXT_VERTEX = new ActionHandler() {
    @Override
    public boolean shouldBeEnabled() {
      return EdPolygonOper.activePoly() != null
          && EdPolygonOper.activePoly().nPoints() > 0;
    }

    @Override
    public void go() {
      EdPolygonOper.activeEditor().adjustTarget(1);
      ScriptEditor.repaint();
    }
  };
  public static ActionHandler TOGGLE_VERTEX_DIR = new ActionHandler() {
    @Override
    public boolean shouldBeEnabled() {
      return EdPolygonOper.activePoly() != null;
    }

    @Override
    public void go() {
      EdPolygonOper.toggleDir();
      ScriptEditor.repaint();
    }
  };

  public static final int CODE = 2; // code for polygon object

  @Override
  public boolean contains(Point pt) {
    boolean ret = boundingRect().contains(pt);
    if (ret && isSimple())
      ret = isPointInside(pt) != 0;
    return ret;
  }

  @Override
  public EdObject flip(boolean horz, Point newLocation) {
    ArrayList<Point> a = new ArrayList();
    Point origin = location();

    for (int i = 0; i < nPoints(); i++) {
      Point pt = getPoint(i);
      pt = Point.difference(pt, origin);
      if (horz)
        pt.x = -pt.x;
      else
        pt.y = -pt.y;
      pt.add(newLocation);
      a.add(pt);
    }
    return new PolygonObject(getColor(), a);
  }

  @Override
  public boolean isGrabPoint(Point pt) {
    if (true) {
      return super.isGrabPoint(pt);
    } else {

      // this turned out to be a little irritating.

      if (!isWellDefined())
        return super.isGrabPoint(pt);

      int elem = findGrabbedElement(pt);
      return elem != 0;
    }
  }

  public Point getPoint(int index) {
    index = fixIndex(index);

    Point pt;
    if (ptInds != null)
      pt = pts2[ptInds[index]];
    else
      pt = pts2[index];
    return mTransform.matrix().apply(pt);
  }

  @Override
  public EdObjectFactory getFactory() {
    return FACTORY;
  }

  @Override
  public void render(GLPanel panel) {
    final boolean DETAILS = false;

    int[] tri = null;

    if (isSimple()) {
      tri = triangulation();
      if (tri == null)
        mFlags &= ~F_SIMPLE;
    }
    if (tri != null) {
      Point[] uPts = getPoints();
      Point[] tPts = new Point[uPts.length];
      Matrix m = mTransform.matrix();
      for (int i = 0; i < uPts.length; i++)
        tPts[i] = m.apply(uPts[i]);

      // final boolean SHOWTRI = false;

      if (mColor != null) {

        panel.setRenderColor(mColor);

        int k = 0;

        Point[] tvv = new Point[tri.length];
        int td = 0;

        while (k < tri.length) {

          Point v0 = tPts[tri[k + 0]];
          Point v1 = tPts[tri[k + 1]];
          Point v2 = tPts[tri[k + 2]];

          // if (pass == 1) {
          // lineWidth(.5f);
          // drawLine(v0, v1);
          // drawLine(v1, v2);
          // drawLine(v2, v0);
          // lineWidth(1);
          // } else {
          tvv[td++] = v0;
          tvv[td++] = v1;
          tvv[td++] = v2;
          // }

          k += 3;
        }
        panel.fillTriangles(tvv);
      }

      if (showVertices) {
        panel.setRenderColor(Color.GREEN);
        int k = 0;
        // Point[] tvv = new Point[tri.length];
        // int td = 0;

        while (k < tri.length) {
          Point v0 = tPts[tri[k + 0]];
          Point v1 = tPts[tri[k + 1]];
          Point v2 = tPts[tri[k + 2]];

          panel.drawLine(v0, v1);
          panel.drawLine(v1, v2);
          panel.drawLine(v2, v0);

          k += 3;
        }

        panel.setRenderColor(Color.GREEN);
        for (int i = 0; i < tPts.length; i++)
          panel.drawCircle(tPts[i], 3 / ScriptEditor.zoomFactor());

      }

    }

    // if (dbline.size() > 0) {
    // setRenderColor(GREEN);
    // for (int i = 0; i < dbline.size(); i++)
    // ((DbLine) dbline.get(i)).render();
    //
    // }

    if (mColor == null || isSelected()) {
      panel.setRenderColor(isSelected() ? Color.YELLOW : Color.DARK_GRAY);

      panel.lineWidth((isSelected() ? 2 : 1) / panel.getZoom());
      if (nPoints() > 1) {
        for (int i = 0; i < nPoints(); i++) {
          Point p0 = getPoint(i - 1);
          Point p1 = getPoint(i);
          panel.drawLine(p0, p1);
          if (DETAILS) {
            panel.plotString("" + i, p1);
          }
        }
      }
    }

    if (DETAILS) {
      if (isWellDefined()) {
        // Point loc = this.boundingRect().midPoint();
        float y = boundingRect().endY();
        float x = boundingRect().endX();

        final int SEP = 15;
        panel.plotString("well defined", x, y);
        y += SEP;
        if (isSimple()) {
          panel.plotString("simple", x, y);
          y += SEP;
        }
        if (isCW()) {
          panel.plotString("cw", x, y);
          y += SEP;
        }
        if (isConvex()) {
          panel.plotString("convex", x, y);
          y += SEP;
        }
      }
    }

    EdPolygonOper ed = EdPolygonOper.getEditorFor(this);
    if (ed != null)
      ed.render();
  }

  public static EdObjectFactory FACTORY = new PolygonFactory();

  private static class PolygonFactory extends EdObjectFactory {

    @Override
    public MouseOper isEditingSelectedObject(int slot, EdObject obj,
        IPoint mousePt) {
      final boolean db = false;

      EdPolygonOper ret = null;

      if (db)
        pr("isEditingSelectedObject (polygon) " + obj + "? mousePt=" + mousePt
            + " slot=" + slot);

      // check if vertex of polygon is at mouse point
      PolygonObject p = (PolygonObject) obj;
      Point mptf = new Point(mousePt);

      int elem = p.findGrabbedElement(mptf);
      if (elem > 0) {
        int bestIndex = elem - 1;
        if (db)
          pr("editing vertex " + bestIndex + " of polygon " + p);
        EdPolygonOper ep = new EdPolygonOper(slot, bestIndex, false);
        ret = ep;
      } else if (elem < 0) {
        int bestIndex = -elem - 1;

        if (!EdPolygonOper.currentOrientation()) {
          ret = new EdPolygonOper(slot, bestIndex + 1, true);
        } else {
          ret = new EdPolygonOper(slot, bestIndex, true);
        }
      }
      // for (int pass = 0; pass < 2; pass++) {
      // final float EPS = 20.0f;
      //
      // int bestIndex = -1;
      // float bestDist = 0;
      //
      // for (int i = 0; i < p.nPoints(); i++) {
      //
      // if (pass == 0) {
      // Point pt = p.getPoint(i);
      //
      // float dist = Point.distance(mptf, pt);
      // if (bestIndex >= 0 && dist > bestDist)
      // continue;
      //
      // if (db)
      // pr(" dist from vert " + i + ": " + pt + " is " + dist);
      //
      // bestIndex = i;
      // bestDist = dist;
      //
      // } else {
      // if (p.isWellDefined()) {
      // Point pt = p.getPoint(i);
      // Point pt2 = p.getPoint(i + 1);
      // float dist = MyMath.ptDistanceToSegment(mptf, pt, pt2, null);
      // if (bestIndex >= 0 && dist > bestDist)
      // continue;
      //
      // if (db)
      // pr(" dist from edge " + i + ": " + pt + pt2 + " is " + dist);
      // bestIndex = i;
      // bestDist = dist;
      //
      // }
      // }
      //
      // }
      //
      // if (bestIndex < 0)
      // continue;
      //
      // warn("tune epsilon later");
      //
      // if (bestDist > EPS / ScriptEditor.zoomFactor())
      // continue;
      // if (pass == 0) {
      // if (db)
      // pr("editing vertex " + bestIndex + " of polygon " + p);
      // EdPolygonOper ep = new EdPolygonOper(slot, bestIndex, false);
      // ret = ep;
      // } else {
      // if (!EdPolygonOper.currentOrientation()) {
      // ret = new EdPolygonOper(slot, bestIndex + 1, true);
      // } else {
      // ret = new EdPolygonOper(slot, bestIndex, true);
      // }
      // }
      // break;
      return ret;
    }

    @Override
    public String getTag() {
      return "P";
    }

    @Override
    public EdObject parse(Script script, JSONObject map) throws JSONException {
      List<Point> a = Point.getList(map, "vertices");
      PolygonObject so = new PolygonObject(JSONTools.getColor(map, "color"), a);
      return so;
    }

    @Override
    public void write(Script script, JSONObject map, EdObject obj)
        throws JSONException {

      PolygonObject so = (PolygonObject) obj;
      JSONTools.put(map, "color", obj.getColor());
      ArrayList<Point> pts = new ArrayList();
      for (int i = 0; i < so.nPoints(); i++)
        pts.add(so.getPoint(i));
      Point.put(pts, map, "vertices");
    }

    /**
     * Write PolygonObject to ScriptsFile. Doesn't write it if it is not simple.
     * Format: [1] type (PolygonObj.CODE) [2] color [1] # vertices [8n] vertices
     * (Point's) [1] # triangles [3n] vertex indices of triangles
     * 
     * @param sf
     *          ScriptsFile to write to
     * @param obj
     *          PolygonObject
     */
    public void write(ScriptsFile sf, EdObject obj) throws IOException {
      PolygonObject so = (PolygonObject) obj;
      DataOutput dw = sf.outputStream();

      if (!so.isSimple() || so.color() == null) {
        // dw.writeByte(NULL_OBJECT_CODE);
        return;
      }

      dw.writeByte(getCode());
      unimp("write color");
      // dw.writeShort(so.color());
      dw.writeByte(so.nPoints());
      for (int i = 0; i < so.nPoints(); i++) {
        Point pt = so.getPoint(i);
        dw.writeFloat(pt.x);
        dw.writeFloat(pt.y);
      }

      int[] tri = so.triangulation();
      int nFace = tri.length / 3;
      dw.writeByte(nFace);
      for (int i = 0; i < tri.length; i++) {
        dw.writeByte(tri[i]);
      }
    }

    @Override
    public int getCode() {
      return CODE;
    }

  }

  @Override
  public void setLocation(Point pt) {
    mutate();
    mTransform.setLocation(new Point(pt));
  }

  public int fixIndex(int ind) {
    if (nPoints() == 0)
      return 0;
    return MyMath.myMod(ind, nPoints());
  }

  @Override
  public Point location() {
    return new Point(mTransform.location());
  }

  @Override
  public void setRotation(float angle) {
    mutate();
    mTransform.setRotation(angle);
  }

  @Override
  public void mutate() {
    super.mutate();
    triangulation = null;
    bounds = null;
  }

  @Override
  public float rotation() {
    return mTransform.rotation();
  }

  @Override
  public void setScale(float scale) {
    mutate();
    mTransform.setScale(scale);
  }

  @Override
  public float scale() {
    return mTransform.scale();
  }

  public int nPoints() {
    if (ptInds != null)
      return ptInds.length;
    else
      return pts2.length;
  }

  private boolean isFlag(int f) {
    return (mFlags & f) != 0;
  }

  // private static class DbLine {
  // public DbLine(Point p1, Point p2) {
  // this.p1 = new Point(p1);
  // this.p2 = new Point(p2);
  // }
  // public void render() {
  // drawLine(p1, p2);
  // }
  // private Point p1, p2;
  // }

  private boolean calcSimple() {
    // final boolean db = false;

    Point p0 = null;
    // getPoint(0);
    for (int i = 0; i < nPoints() - 1; i++) {
      if (p0 == null)
        p0 = getPoint(i);
      Point p1 = getPoint(i + 1);

      Point q0 = null;
      for (int j = i + 2; j < nPoints() - 1; j++) {
        if (q0 == null)
          q0 = getPoint(j);
        Point q1 = getPoint(j + 1);

        Point iPt = MyMath.segSegIntersection(p0, p1, q0, q1, null);
        if (iPt != null) {
          return false;
        }

        q0 = q1;
      }
      p0 = p1;
    }
    return true;
  }

  private boolean calcConvex() {
    Point pA = getPoint(-2);
    Point pB = getPoint(-1);
    boolean prevSide = false;

    for (int i = 0; i < nPoints(); i++) {

      Point pC = getPoint(i);
      boolean isLeft = MyMath.sideOfLine(pA, pB, pC) >= 0;
      if (i == 0) {
        prevSide = isLeft;
      }
      if (prevSide != isLeft)
        return false;
      pA = pB;
      pB = pC;
    }
    return true;
  }

  private void calcFlags() {
    if (!isFlag(F_FLAGSVALID)) {
      mFlags = F_FLAGSVALID;
      do {
        if (nPoints() < 3)
          break;
        mFlags |= F_WELLDEFINED;

        if (!calcSimple())
          break;
        mFlags |= F_SIMPLE;

        int w = calcWinding();
        if (w == 0)
          throw new IllegalStateException("unable to calculate winding for:\n"
              + this);

        if (w < 0)
          mFlags |= F_CWORIENTED;

        if (calcConvex())
          mFlags |= F_CONVEX;
      } while (false);
    }
  }

  @Override
  public boolean isWellDefined() {
    calcFlags();
    return isFlag(F_WELLDEFINED);
  }

  public boolean isSimple() {
    calcFlags();
    return isFlag(F_SIMPLE);
  }

  public boolean isConvex() {
    calcFlags();
    return isFlag(F_CONVEX);
  }

  public boolean isCW() {
    calcFlags();
    return isFlag(F_CWORIENTED);
  }

  public Rect boundingRect() {

    if (bounds == null) {
      ArrayList<Point> a = new ArrayList();
      for (int i = 0; i < nPoints(); i++)
        a.add(getPoint(i));
      bounds = Rect.rectContainingPoints(a);
    }
    return bounds;
  }

  /**
   * Determine if point is inside polygon
   * 
   * @param pt
   *          point to test
   * @return -1 if inside and polygon has cw winding, 1 if inside and polygon
   *         has ccw winding, 0 if outside
   */
  private int isPointInside(Point pt) {

    final float EPS = .001f;
    // final boolean db = false;

    int ret = 0;

    // if the sum of the angles swept by seg from pt to each vertex is
    // +/- 360 degrees, we're inside.

    float sum = 0;
    float prevTheta = 0;
    boolean prevThetaDefined = false;
    for (int i = 0; i <= nPoints(); i++) {
      Point v = getPoint(i);
      if (MyMath.squaredDistanceBetween(v, pt) < EPS) {
        continue;
      }
      float theta = MyMath.polarAngleOfSegment(pt, v);
      if (prevThetaDefined) {
        float add = MyMath.normalizeAngle(theta - prevTheta);
        sum += add;
      }
      prevThetaDefined = true;
      prevTheta = theta;
    }
    if (Math.abs(sum) > 1e-2) {
      ret = (sum > 0) ? 1 : -1;
    }
    return ret;
  }

  /**
   * Determine winding of polygon
   * 
   * @return 1 if ccw, -1 if cw, 0 if unknown
   */
  private int calcWinding() {
    int ret = 0;

    Point c = findInteriorPoint();
    if (c != null)
      ret = isPointInside(c);
    return ret;
  }

  /**
   * Find a point that is strictly interior to a polygon. Runs in O(n^2) time.
   * 
   * @param p
   * @return interior point, or null if failed to find one
   */
  private Point findInteriorPoint() {
    Point ret = null;
    outer: do {

      // starting with a point near an arbitrary vertex, perform a binary search
      // moving point closer to vertex, testing if it's interior
      Point cv, cw;
      {
        int cVert = 0;
        Point p0 = getPoint(cVert - 1);
        cv = getPoint(cVert);
        Point p2 = getPoint(cVert + 1);
        float th0 = MyMath.polarAngleOfSegment(cv, p0);
        float th1 = MyMath.polarAngleOfSegment(cv, p2);
        float thMid = MyMath.normalizeAngle(th0 - th1) / 2 + th1;
        cw = MyMath.pointOnCircle(cv, thMid, 10.0f);
      }
      for (int attempt = 0; attempt < this.nPoints(); attempt++) {
        if (isPointInside(cw) == 1) {
          ret = cw;
          break outer;
        }
        cw = MyMath.midPoint(cw, cv);
      }

      // binary search failed; examine centroid of every possible ear
      for (int i = 0; i < nPoints(); i++) {
        // find interior point by examining centroids of every possible ear
        Point p0 = getPoint(i);
        Point p1 = getPoint(i + 1);
        Point p2 = getPoint(i + 2);
        Point c = new Point((p0.x + p1.x + p2.x) / 3, (p0.y + p1.y + p2.y) / 3);
        if (isPointInside(c) != 0) {
          ret = c;
          break;
        }
      }
    } while (false);
    return ret;
  }

  public PolygonObject getIndexedVersion() {
    PolygonObject ret = this;

    if (ptInds == null) {
      int[] vi = new int[nPoints()];
      for (int i = 0; i < vi.length; i++)
        vi[i] = i;
      ret = new PolygonObject(mColor, getPoints(), vi);
      ret.mFlags = mFlags;
    }
    return ret;
  }

  /**
   * Find the shortest interior diagonal connecting two vertices
   * 
   * @param ret
   */
  private int[] shortestInteriorDiagonal() {
    final boolean db = false;
    if (db)
      pr("shortestInteriorDiagonal for " + this);

    if (!isSimple())
      throw new IllegalArgumentException("Must be simple");
    if (nPoints() <= 3)
      throw new IllegalArgumentException("must have > 3 vertices");

    // find shortest segment that is interior to the polygon

    float shortestLen = 0;
    int viBest = -1, vjBest = -1;

    for (int vi = 0; vi < nPoints(); vi++) {
      Point pi = getPoint(vi);
      jLoop: for (int vj = vi + 2; vj < nPoints(); vj++) {
        if (vi == 0 && vj == nPoints() - 1)
          continue;

        Point pj = getPoint(vj);
        float currLen = MyMath.distanceBetween(pi, pj);
        if (db)
          pr(" vi:" + ptInds[vi] + " " + pi + "  vj:" + ptInds[vj] + " " + pj
              + " len=" + d(currLen));

        if (viBest >= 0 && shortestLen < currLen)
          continue;

        // is this seg outside the polygon?
        if (isPointInside(MyMath.midPoint(pi, pj)) == 0) {
          if (db)
            pr(" not an interior seg");

          continue;
        }

        // does this seg cross any edges?

        Point s1 = getPoint(-1);
        int pk = nPoints() - 1;

        for (int k = 0; k < nPoints(); pk = k, k++) {
          Point s0 = s1;
          s1 = getPoint(k);
          if (k == vi || k == vj || pk == vi || pk == vj)
            continue;

          Point iPt = MyMath.segSegIntersection(pi, pj, s0, s1, null);
          if (iPt != null) {
            if (db)
              pr("  crosses edge " + k + ":" + s0 + " " + s1);

            continue jLoop;
          }
        }

        if (viBest < 0 || currLen < shortestLen) {
          viBest = vi;
          vjBest = vj;
          shortestLen = currLen;
        }
      }
    }
    if (viBest < 0) {
      return null;
    }
    int[] ret = new int[2];
    ret[0] = viBest;
    ret[1] = vjBest;
    return ret;
  }

  public int[] triangulation() {
    if (triangulation == null) {
      triangulation = triangulate(this);
    }
    return triangulation;
  }

  /**
   * Attempt to trianglulate polygon. Returns null if triangulation fails.
   * 
   * @param orig
   * @return
   */
  private static int[] triangulate(PolygonObject orig) {
    final boolean db = false;

    int[] ret = null;

    if (!orig.isSimple())
      return ret;
    // throw new
    // IllegalArgumentException("Can't triangulate nonsimple polygon");

    // if original polygon is not indexed, construct indexed version
    orig = orig.getIndexedVersion();
    if (db)
      pr("triangulate: " + orig);

    ArrayList<PolygonObject> input = new ArrayList(), output = new ArrayList();
    input.add(orig);

    while (!input.isEmpty()) {
      PolygonObject p = input.remove(input.size() - 1);
      if (p.nPoints() == 3) {
        output.add(p);
      } else {
        // find shortest segment that is interior to the polygon

        int[] vi = p.getVertIndices();

        int[] seg = p.shortestInteriorDiagonal();

        if (seg == null) {
          // if (false) {
          warning("can't find interior diagonal for:\n" + p);
          return null;
          // continue;
          // }
          // throw new IllegalStateException();
        }

        int viBest = seg[0], vjBest = seg[1];
        int indi = vi[viBest], indj = vi[vjBest];
        // = seg[0], vjBest = seg[1];

        // split polygon into two
        {

          if (db) {
            pr("\n shortest interior diagonal for:\n" + p);
            pr(" split along interior edge " + indi + " ... " + indj);
          }
          ArrayList<Integer> a1 = new ArrayList(), a2 = new ArrayList();
          for (int i = 0; i < p.nPoints(); i++) {
            if (i <= viBest || i >= vjBest)
              a1.add(vi[i]);
            if (i >= viBest && i <= vjBest)
              a2.add(vi[i]);
          }

          PolygonObject s1 = new PolygonObject(p.color(), p.getPoints(),
              Tools.toArray(a1));
          PolygonObject s2 = new PolygonObject(p.color(), p.getPoints(),
              Tools.toArray(a2));
          if (db)
            pr(" s1=" + s1 + "\n s2=" + s2);
          ASSERT(a1.size() >= 3 && a2.size() >= 3);

          input.add(s1);
          input.add(s2);
        }
      }
    }

    int[] trv = new int[output.size() * 3];
    int j = 0;
    for (int i = 0; i < output.size(); i++) {
      PolygonObject p = (PolygonObject) output.get(i);
      for (int k = 0; k < 3; k++)
        trv[j++] = p.ptInds[k];
    }
    return trv;
  }

  public Color color() {
    return mColor;
  }

  @Override
  public <T extends Freezable> T getMutableCopy() {
    warning("refactor PolygonObject");
    PolygonObject e = new PolygonObject();
    e.pts2 = new Point[pts2.length];
    for (int i = 0; i < pts2.length; i++)
      e.pts2[i] = pts2[i];
    e.mTransform = new ObjTransform(mTransform);
    return (T) e;
  }

  public static boolean showVertices;

  private static final int F_FLAGSVALID = (1 << 0), //
      F_WELLDEFINED = (1 << 1), //
      F_CWORIENTED = (1 << 2), //
      F_SIMPLE = (1 << 3), F_CONVEX = (1 << 4);
  public static final int MAX_VERTICES = 100;

  private ObjTransform mTransform = new ObjTransform();
  private Point[] pts2; //
  private Color mColor; // color and shade (to be clarified later)

  // lazy-initialized fields
  private int[] ptInds;
  private int mFlags;
  private Rect bounds;
  // indices of vertices forming triangulation (only if simple)
  private int[] triangulation;

}
