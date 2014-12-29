package com.js.scredit;

import com.js.geometry.*;
import static com.js.basic.Tools.*;

public class FlipReversible extends ModifyObjectsReversible {
  private static final boolean db = false;

  private boolean horz;
  public FlipReversible(boolean horz) {
    if (db)
      pr("constructed FlipReversible");

    this.horz = horz;
    setName("Flip " + (horz ? "Horizontally" : "Vertically"));
  }

  private Rect getBounds() {
    if (bounds == null) {
      if (db)
        pr("constructing bounds");

      for (int i = 0; i < nSlots(); i++) {
        EdObject origObj = getOrigObjects()[i];
        Rect r = origObj.boundingRect();
        if (bounds == null)
          bounds = r;
        else
          bounds.include(r);
        if (db)
          pr(" item bounds=" + r + ", bounds now " + bounds);

      }
    }
    return bounds;
  }

  @Override
  public EdObject perform(EdObject orig) {
    if (db)
      pr("perform with " + orig);

    Rect bounds = getBounds();

    Point loc = orig.location();
    Point newLoc = new Point(loc);
    if (horz) {
      newLoc.x = bounds.endX() - (loc.x - bounds.x);
    } else {
      newLoc.y = bounds.endY() - (loc.y - bounds.y);
    }
    EdObject newObj = orig.flip(horz, newLoc);
    return newObj;
  }
  private Rect bounds;

}
