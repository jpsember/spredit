package com.js.scredit;

import static com.js.basic.Tools.*;

public class ScriptEditorState {

  public ScriptEditorState() {
    mObjects = frozen(ScriptEditor.items());
    mSelectedSlots = mObjects.getSelectedSlots();
    mClipboard = ScriptEditor.clipboard();
    // mDupAccumulator = e.getDupAccumulator();
  }

  public EdObjectArray getObjects() {
    return mObjects;
  }

  public EdObjectArray getClipboard() {
    return mClipboard;
  }

  public SlotList getSelectedSlots() {
    return mSelectedSlots;
  }

  // public Point getDupAccumulator() {
  // return mDupAccumulator;
  // }

  private EdObjectArray mObjects;
  private SlotList mSelectedSlots;
  private EdObjectArray mClipboard;
  // private Point mDupAccumulator;
}
