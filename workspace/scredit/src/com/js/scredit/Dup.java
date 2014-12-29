package com.js.scredit;

import com.js.geometry.*;

/**
 * Logic for offsetting multiple duplicated, pasted objects based on user's
 * adjustments.
 * 
 * Let A be the position of the new instance of an item, the result of a paste
 * or duplicate operation. Let B be the position of the last instance of this
 * item. Let C be the position of the second-to-last instance of this item.
 * 
 * Let P be the position of the clipboard's instance of the item.
 * 
 * We wish to determine the location of A given those of B and C.
 * 
 * There are two values manipulated:
 * 
 * 1) The accumulator represents the distance from C to B.
 * 
 * 2) The clipboard adjustment represents the distance from P to B.
 * 
 */

public class Dup {

  /**
   * Reset the values to defaults; basically, 'forget' any user adjustments.
   */
  public static void reset() {
    dupAccum.setTo(1, 1);
    dupClipAdjust.clear();
    userDirDefined = false;
  }

  /**
   * Get the accumulator.
   * 
   * @param filter
   *          if true, and accumulator has changed direction radically, reset
   *          the accumulator value so user has to re-specify this offset
   * @return accumulator
   */
  public static Point getAccum(boolean filter) {
    if (filter)
      getFilteredAccum();
    return new Point(dupAccum);
  }

  /**
   * Get the clipboard adjust value
   * 
   * @return clipboard adjust value
   */
  public static Point getClipboardAdjust() {
    return new Point(dupClipAdjust);
  }

  public static void setAccum(Point a) {
    dupAccum.setTo(a);
  }

  /**
   * Set the clipboard adjust value
   */
  public static void setClipboardAdjust(Point b) {
    dupClipAdjust.setTo(b);
  }

  /**
   * Update the clipboard adjust value by adding the accumulator to it
   */
  public static void updateClipboardAdjust() {
    dupClipAdjust.add(dupAccum);
  }

  /**
   * Get dup accumulator amount. If user has changed direction abruptly, resets
   * it so things don't get too wild.
   * 
   * @return filtered dup accumulator
   * 
   */
  private static Point getFilteredAccum() {
    float daLen = dupAccum.magnitude();
    float daMin = 20 / ScriptEditor.zoomFactor();

    if (daLen > daMin) {

      float dir = MyMath.polarAngle(dupAccum);

      if (!userDirDefined) {
        userDirDefined = true;
        prevUserDir = dir;

      } else {
        float angDiff = MyMath.normalizeAngle(prevUserDir - dir);

        if (Math.abs(angDiff) > MyMath.M_DEG * 30) {

          userDirDefined = false;
          Point newAccum = new Point(1 - dupAccum.x, 1 - dupAccum.y);

          dupAccum.add(newAccum);
        }
      }
    }

    return dupAccum;
  }

  private static boolean userDirDefined;
  private static float prevUserDir;

  // dupAccum is the sum of the user's little adjustments
  private static Point dupAccum = new Point(1, 1);

  // dupClipAdjust is the translation that must be applied to the clipboard;
  // it reflects amounts added to dupAccum after clipboard last modified
  private static Point dupClipAdjust = new Point();
}
