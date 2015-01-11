package com.js.scredit;

import com.js.editor.Command;

public class CommandForGeneralChanges extends Command.Adapter {

  /**
   * Constructor
   * 
   * @param originalState
   * @param newState
   *          editor state after change; if null, constructs from current
   *          editor's state
   * @param mergeKey
   *          optional merge key
   */
  public CommandForGeneralChanges(ScriptEditorState originalState,
      ScriptEditorState newState, String mergeKey) {
    if (newState == null)
      newState = new ScriptEditorState();
    mOriginalState = originalState;
    mNewState = newState;
    mMergeKey = mergeKey;
  }

  /**
   * Set description of command; this shows up in the 'undo' and 'redo' menu
   * items. If none is specified, uses generic 'last command' description
   * 
   * @param description
   * @return this, for chaining
   */
  public CommandForGeneralChanges setDescription(String description) {
    mCommandDescription = description;
    return this;
  }

  @Override
  public Command getReverse() {
    if (mReverse == null) {
      mReverse = new CommandForGeneralChanges(mNewState, mOriginalState, null);
    }
    return mReverse;
  }

  @Override
  public void perform() {
    ScriptEditor.editor().setState(mNewState);
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

      // Merging is possible, so construct merged command
      merged = new CommandForGeneralChanges(mOriginalState, f.mNewState,
          mMergeKey);

    } while (false);
    return merged;
  }

  @Override
  public String toString() {
    if (mCommandDescription != null)
      return mCommandDescription;
    return "Last Command";
  }

  private String mCommandDescription;
  private ScriptEditorState mOriginalState;
  private ScriptEditorState mNewState;
  private String mMergeKey;
  private Command mReverse;
}
