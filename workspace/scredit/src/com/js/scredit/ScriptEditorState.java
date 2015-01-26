package com.js.scredit;

import static com.js.basic.Tools.*;

public class ScriptEditorState {

  public ScriptEditorState() {
    mObjects = frozen(ScriptEditor.items());
    mClipboard = ScriptEditor.clipboard();
    // mDupAccumulator = e.getDupAccumulator();
  }

  public EdObjectArray getObjects() {
    return mObjects;
  }

  public EdObjectArray getClipboard() {
    return mClipboard;
  }

  /**
   * Convenience method to get list of selected items from objects
   */
  public SlotList getSelectedSlots() {
    return mObjects.getSelectedSlots();
  }

  // public Point getDupAccumulator() {
  // return mDupAccumulator;
  // }

  private EdObjectArray mObjects;
  private EdObjectArray mClipboard;
  // private Point mDupAccumulator;
}
