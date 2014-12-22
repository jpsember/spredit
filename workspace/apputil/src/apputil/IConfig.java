package apputil;

public interface IConfig {

  /**
   * Give object an opportunity to handle a configuration argument
   * @param scanner
   * @param item argument
   * @return true if this object processed the argument
   */
  public boolean process(DefScanner scanner, String item);
  
  /**
   * Give object an opportunity to write its configuration arguments
   * @param sb destination
   */
  public void writeTo(DefBuilder sb);
}
