package com.js.scredit;

import com.js.editor.Command;

public class CommandForGeneralChanges extends Command.Adapter {

  public CommandForGeneralChanges(ScriptEditorState originalState,
      ScriptEditorState newState, String mergeKey) {
    mOriginalState = originalState;
    mNewState = newState;
    mMergeKey = mergeKey;
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

  private ScriptEditorState mOriginalState;
  private ScriptEditorState mNewState;
  private String mMergeKey;
  private Command mReverse;
}
