package com.js.editor;

/**
 * Encapsulates an edit operation, to allow for undo/redo functionality
 */
public abstract class Command {

  /**
   * Get a command that will undo this one
   */
  public abstract Command getReverse();

  /**
   * Perform this command
   */
  public abstract void perform();

  /**
   * Merge this command with another that follows it, if possible. For example,
   * if user moves the same selected objects with several consecutive drag
   * operations, it makes sense that they all get merged into a single 'move'
   * command
   * 
   * @param follower
   *          command that follows this one
   * @return merged command, or null if no merge was possible
   */
  public Command attemptMergeWith(Command follower) {
    return null;
  }

}
