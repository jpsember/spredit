package apputil;

import java.awt.Component;

import com.js.geometry.IPoint;
import com.js.geometry.Point;

public interface IEditorView {
  /**
   * Transform view-space point to world point
   * @param viewPt point in view space
   * @return point in world
   */
  public Point viewToWorld(IPoint viewPt);
  
  /**
   * Repaint view 
   */
  public void repaint();
  
  /**
   * Determine scaling factor to convert world to view
   * @return
   */
  public float zoomFactor();

  public Component getComponent();
}