package com.js.editor;

import com.js.geometry.Point;

public interface UserEventSource {

  /**
   * Transform view-space point to world point
   * 
   * @param viewPt
   *          point in view space
   * @return point in world
   */
  public Point viewToWorld(Point viewPt);

  /**
   * Repaint view
   */
  public void repaint();

  /**
   * Determine scaling factor to convert world to view
   */
  public float getZoom();

  public MouseOper getDefaultOperation();

}
