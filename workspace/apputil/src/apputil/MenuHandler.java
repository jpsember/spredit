package apputil;

public interface MenuHandler {
  /**
   * Determine if items within this menu should be enabled. If it returns false,
   * the individual item tests are not done.
   * 
   * @return true if so
   */
  public boolean isEnabled();
}
