package com.js.editor;

public class UserEventManager {

  /**
   * Constructor
   * 
   * @param defaultOper
   *          the default MouseOper, which will be active if no other is
   *          specified
   */
  public UserEventManager(MouseOper defaultOper) {
    sDefaultOperation = defaultOper;
  }

  /**
   * Get current operation
   */
  public MouseOper getOperation() {
    if (sCurrentOperation == null)
      sCurrentOperation = sDefaultOperation;
    return sCurrentOperation;
  }

  /**
   * Set current operation
   */
  public void setOperation(MouseOper oper) {
    if (oper == null) {
      oper = sDefaultOperation;
    }
    if (sCurrentOperation != oper) {
      if (sCurrentOperation != null)
        sCurrentOperation.stop();
      sCurrentOperation = oper;
      if (sCurrentOperation != null)
        sCurrentOperation.start();
    }
  }

  public void clearOperation() {
    setOperation(null);
  }

  private MouseOper sDefaultOperation;
  private MouseOper sCurrentOperation;
}
