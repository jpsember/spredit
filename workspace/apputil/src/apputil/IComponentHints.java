package apputil;

public interface IComponentHints {
  
  /**
   * Determine if component can stretch along an axis
   * @param axis 0: horizontal 1: vertical
   * @return true if component can stretch in that direction
   */
  public boolean stretch(int axis);

}
