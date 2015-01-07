package com.js.editor;

/**
 * Reversable procedure 
 */
public interface Reversible {

  /**
   * Get a procedure that will undo this procedure
   * @return Reversible procedure
   */
  public Reverse getReverse();

  /**
   * Perform this operation
   */
  public void perform();

  /**
   * Determine if operation is valid in current context;
   * essentially whether menu item should be enabled or not
   * @return true if valid
   */
  public boolean valid();
}
