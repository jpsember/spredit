package com.js.scredit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.js.geometry.*;
import static com.js.basic.Tools.*;

public class EdTools {
  public static String itemsStr(int count) {
    return (count == 1) ? (count + " item") : (count + " items");
  }

  /**
   * @deprecated
   * @param slots
   * @return
   */
  public static String itemsStr(int[] slots) {
    StringBuilder sb = new StringBuilder();
    if (slots.length == 1) {
      sb.append(ScriptEditor.items().get(slots[0]).toString());
    } else {
      sb.append(slots.length);
      sb.append(" items");
    }
    return sb.toString();
  }

  /**
   * Find the 1-center of a set of points
   * 
   * @param C
   *          : array of FPoint2's
   * @param r
   *          : Random number generator, or null to choose a new one
   * @return Circle
   */
  public static Circle findCenter(ArrayList<Point> pts) {
    Random r = new Random();
    for (int i = 0; i < pts.size(); i++) {
      int j = r.nextInt(pts.size());
      Collections.swap(pts, i, j);
    }
    return extend(pts, pts.size(), new ArrayList(), null);
  }

  public static Circle smallestBoundingDisc(EdObject obj) {
    EdObject[] items = new EdObject[1];
    items[0] = obj;
    return smallestBoundingDisc(items);
  }

  public static Circle smallestBoundingDisc(EdObject[] items) {
    Circle ret = null;
    Rect bounds = null;
    for (int i = 0; i < items.length; i++) {
      EdObject obj = items[i];
      Rect objBnd = obj.boundingRect();
      if (objBnd == null)
        continue;
      if (bounds == null)
        bounds = objBnd;
      else
        bounds.include(objBnd);
    }
    if (bounds != null) {
      ret = new Circle(bounds.midPoint(), MyMath.distanceBetween(
          bounds.topLeft(), bounds.midPoint()));
    }
    return ret;
  }

  private static Circle extend(ArrayList<Point> C, int cSize,
      ArrayList<Point> G, Circle s) {

    for (int i = 0; i < cSize; i++) {
      Point pt = C.get(i);
      if (s == null || !s.contains(pt)) {
        G.add(pt);
        s = solve(G);
        s = extend(C, i, G, s);
        pop(G);
      }
    }
    return s;
  }

  private static Circle solve(ArrayList<Point> C) {
    Circle r = null;
    switch (C.size()) {
    case 1:
      r = new Circle(C.get(0), 0);
      break;
    case 2: {
      Point p0 = C.get(0), p1 = C.get(1);
      Point midPt = MyMath.midPoint(p0, p1);
      float rad = MyMath.distanceBetween(midPt, p0);
      r = new Circle(midPt, rad);
    }
      break;
    case 3: {
      Point p0 = C.get(0), p1 = C.get(1), p2 = C.get(2);
      r = Circle.calcCircumCenter(p0, p1, p2);
    }
      break;
    default:
      throw new IllegalStateException("can't solve for:\n" + C);
    }
    return r;
  }

}
