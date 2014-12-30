package apputil;

public interface IApplication {
  /**
   * Get the application's name. This will appear as the app's menu title, and
   * perhaps will be incorporated into the application frame's title
   */
  public String getName();

  public boolean exitProgram();
}
