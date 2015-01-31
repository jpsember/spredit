package com.js.scredit;

import com.js.editor.Command;

public class CommandForGeneralChanges extends Command.Adapter {

  /**
   * Construct a command in preparation for changes. Saves current editor state.
   * Client should modify the state, and call finish() to mark the completion of
   * the command
   * 
   * @param description
   *          description for this command to appear in undo/redo menu items. If
   *          null, command is considered non-reversible
   */
  public CommandForGeneralChanges(String description) {
    mEditor = ScriptEditor.editor();
    setOriginalState(mEditor.getStateSnapshot());
    setDescription(description);
  }

  /**
   * Set a 'merge' key for this command. A necessary condition for merging
   * adjacent commands is that their merge keys both exist and are equal
   */
  public CommandForGeneralChanges setMergeKey(String mergeKey) {
    mMergeKey = mergeKey;
    return this;
  }

  public void finish() {
    if (finished())
      throw new IllegalStateException();
    mEditor.disposeOfStateSnapshot();
    mNewState = mEditor.getStateSnapshot();
    // Execute the command, and add to the history
    mEditor.recordCommand(this);
  }

  public ScriptEditorState getOriginalState() {
    return mOriginalState;
  }

  private boolean finished() {
    return mNewState != null;
  }

  private CommandForGeneralChanges(ScriptEditor editor,
      ScriptEditorState originalState, ScriptEditorState newState,
      String mergeKey, String description) {
    mEditor = editor;
    setOriginalState(originalState);
    mNewState = newState;
    mMergeKey = mergeKey;
    setDescription(description);
  }

  private boolean reversible() {
    return getDescription() != null;
  }

  @Override
  public Command getReverse() {
    if (mReverse == null && reversible()) {
      mReverse = new CommandForGeneralChanges(mEditor, mNewState,
          mOriginalState, null, null);
    }
    return mReverse;
  }

  @Override
  public void perform() {
    mEditor.setState(mNewState);
  }

  @Override
  public Command attemptMergeWith(Command follower) {
    CommandForGeneralChanges merged = null;
    do {
      if (mMergeKey == null)
        break;

      if (!(follower instanceof CommandForGeneralChanges))
        break;
      CommandForGeneralChanges f = (CommandForGeneralChanges) follower;

      if (!mMergeKey.equals(f.mMergeKey))
        break;

      // The selection after the first command was executed must equal
      // the selection before the second command was.
      if (!mNewState.getSelectedSlots().equals(
          f.mOriginalState.getSelectedSlots()))
        break;

      String mergedDescription = this.getDescription();
      if (mergedDescription == null)
        mergedDescription = follower.getDescription();

      // Merging is possible, so construct merged command
      merged = new CommandForGeneralChanges(mEditor, mOriginalState,
          f.mNewState, mMergeKey, mergedDescription);

    } while (false);
    return merged;
  }

  @Override
  public String toString() {
    String description = getDescription();
    if (description == null)
      description = "Last Command";
    return description;
  }

  private void setOriginalState(ScriptEditorState s) {
    if (s.isMutable())
      throw new IllegalArgumentException();
    mOriginalState = s;
  }

  private ScriptEditor mEditor;
  private ScriptEditorState mOriginalState;
  private ScriptEditorState mNewState;
  private String mMergeKey;
  private Command mReverse;
}
