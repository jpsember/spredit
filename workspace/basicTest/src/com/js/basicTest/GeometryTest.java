package com.js.basicTest;

import com.js.geometry.*;
import com.js.testUtils.*;

public class GeometryTest extends MyTestCase {

  public void testRotate() {
    Matrix m = Matrix.getRotate(MyMath.M_DEG * 30);
    Point p = new Point(100, 0);
    Point p2 = m.apply(p);
    assertEqualsFloat(86.6025404f, p2.x);
    assertEqualsFloat(50, p2.y);
  }

}
