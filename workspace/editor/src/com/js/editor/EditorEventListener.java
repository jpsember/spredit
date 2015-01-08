package com.js.editor;

public interface EditorEventListener {

  /**
   * Process an event, if possible
   * 
   * @param event
   * @return event after filtering
   * 
   */
  public UserEvent processEvent(UserEvent event);

  /**
   * Perform any rendering specific to this operation
   */
  public void render();

  /**
   * Determine if an object can be editable during this operation
   */
  public boolean allowEditableObject();

  public abstract class Adapter implements EditorEventListener {

    public void render() {
    }

    public boolean allowEditableObject() {
      return true;
    }
  }

}
