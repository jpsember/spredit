package com.js.scredit;

import java.util.ArrayList;
import java.util.List;

import com.js.editor.Command;
import static com.js.basic.Tools.*;

/**
 * Class to encapsulate the state and behaviour of the undo/redo functionality.
 * Conceptually, maintains two stacks of commands: an undo stack, and a redo
 * stack.
 * 
 * (The stacks are actually implemented internally using a single array.)
 */
public class UndoManager {

  private static final boolean DBUNDO = false;
  private static final int MAX_COMMAND_HISTORY_SIZE = 50;

  /**
   * Clear the command history; essentially place object into just-constructed
   * state
   */
  public void reset() {
    mCommandHistoryCursor = 0;
    mCommandHistory.clear();
  }

  public boolean undoPossible() {
    return mCommandHistoryCursor != 0;
  }

  public boolean redoPossible() {
    return mCommandHistoryCursor < mCommandHistory.size();
  }

  public Command peekUndo() {
    if (!undoPossible())
      throw new IllegalStateException();
    return mCommandHistory.get(mCommandHistoryCursor - 1);
  }

  public Command peekRedo() {
    if (!redoPossible())
      throw new IllegalStateException();
    return mCommandHistory.get(mCommandHistoryCursor);
  }

  /**
   * Get command from undo stack, and place onto redo stack
   */
  public Command undo() {
    final boolean db = DBUNDO;
    if (db)
      pr("\nDoing pop();\n" + this);
    Command command = peekUndo();
    mCommandHistoryCursor--;
    if (db)
      pr(" command " + command + ";\n afterward:\n" + this + "\n");

    return command;
  }

  /**
   * Get command from redo stack, and place onto undo stack
   */
  public Command redo() {
    Command c = peekRedo();
    mCommandHistoryCursor++;
    return c;
  }

  private void clearRedoStack() {
    while (mCommandHistory.size() > mCommandHistoryCursor) {
      pop(mCommandHistory);
    }
  }

  private Command mergeWithPredecessors(Command command) {
    final boolean db = DBUNDO;
    // Merge this command with its predecessor if possible
    while (true) {
      if (mCommandHistoryCursor == 0)
        break;
      Command prev = mCommandHistory.get(mCommandHistoryCursor - 1);
      Command merged = prev.attemptMergeWith(command);
      if (merged == null)
        break;
      pop(mCommandHistory);
      mCommandHistoryCursor--;
      command = merged;
      if (db)
        pr(" merged with previous, now " + command);
    }
    return command;
  }

  /**
   * Perform command
   * 
   * Throws out any existing redo commands, since their validity depended upon
   * the preceding commands, which are now changing.
   * 
   * Merges the command, if possible, with one or more preceding commands so
   * that undo 'makes sense'; e.g., combine multiple little 'move' adjustments
   * into a single move command.
   * 
   * Trim the undo stack to a reasonable size.
   * 
   * Note: the command is not actually executed; this should be done by the
   * client (either before or after this call)
   * 
   */
  public void perform(Command command) {
    final boolean db = DBUNDO;
    if (db)
      pr("recordCommand " + command + ";\n" + this);

    // Something funny is going on if the same command object is being performed
    // again
    if (mCommandHistoryCursor > 0 && peekUndo() == command)
      throw new IllegalArgumentException(
          "attempt to perform identical command: " + command);

    // Throw out any older 'redoable' commands that will now be stale
    clearRedoStack();
    if (db)
      pr(" after popping older 'redoables':\n" + this);

    command = mergeWithPredecessors(command);

    mCommandHistory.add(command);
    mCommandHistoryCursor++;

    // If this command is not reversible, throw out all commands, including
    // this one
    if (command.getReverse() == null) {
      if (db)
        pr(" command is not reversible, clearing history");
      reset();
    }

    if (mCommandHistoryCursor > MAX_COMMAND_HISTORY_SIZE) {
      int del = mCommandHistoryCursor - MAX_COMMAND_HISTORY_SIZE;
      if (db)
        pr(" trimming history by deleting " + del + " from head of queue");
      mCommandHistoryCursor -= del;
      mCommandHistory.subList(0, del).clear();
    }
    if (db)
      pr(" after pushing:\n" + this + "\n");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("UndoManager");
    sb.append(" cursor=" + mCommandHistoryCursor);
    sb.append(" [");
    for (int i = 0; i < mCommandHistory.size(); i++) {
      sb.append(" ");
      if (i == mCommandHistoryCursor)
        sb.append("*");
      Command c = mCommandHistory.get(i);
      sb.append(c.getDescription());
    }
    if (mCommandHistoryCursor == mCommandHistory.size())
      sb.append(" *");
    sb.append(" ]");
    return sb.toString();
  }

  private List<Command> mCommandHistory = new ArrayList();
  private int mCommandHistoryCursor;

}
