package com.js.editor;

public class UserEventManager implements UserEvent.Listener {

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

  @Override
  public void processUserEvent(UserEvent event) {
    mLastEventHandled = event;

    // Pass this event to the current operation
    getOperation().processUserEvent(event);

    // If an additional listener has been specified,
    // pass it along
    if (mListener != null) {
      mListener.processUserEvent(event);
    }
  }

  /**
   * Specify an optional listener, which will be passed the event after it's
   * been handled by the current operation
   */
  public void setListener(UserEvent.Listener listener) {
    mListener = listener;
  }

  public UserEvent getLastEventHandled() {
    return mLastEventHandled;
  }

  private UserEvent.Listener mListener;
  private MouseOper sDefaultOperation;
  private MouseOper sCurrentOperation;
  private UserEvent mLastEventHandled;
}
