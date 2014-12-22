package base;

/**
 * Utility class for detecting infinite loops
 */
@Deprecated
public class Inf {

  /**
   * Create an infinite loop counter with an empty message and 
   * a maximum of 2000 iterations
   * @return Inf
   */
  public static Inf create() {
    return new Inf("", 2000);
  }

  /**
   * Update an infinite loop counter, if it exists
   * @param inf if not null, counter to update
   */
  public static void update(Inf inf) {
    if (inf != null)
      inf.update();
  }

  /**
   * Construct an infinite loop counter
   * @param message  message to display if loop detected
   * @param limit    max number of iterations before assuming infinite loop
   */
  public Inf(String message, int limit) {
    construct(message, limit);
  }
  private void construct(String message, int limit) {
    if (message == null) {
      message = MyTools.stackTrace(3);
    }
    msg = message;
    this.limit = limit;
  }

  public Inf() {
    construct(null, 300);
  }

  public Inf(int limit) {
    construct(null, limit);
  }

  /**
   * Update infinite loop counter; throw exception if max iterations exceeded
   */
  public void update() {
    if (++counter >= limit) {
      throw new RuntimeException("Infinite loop (" + counter + "): " + msg
          + "\n" + MyTools.stackTrace());
    }
  }
  private String msg;
  private int limit;
  private int counter;
}
