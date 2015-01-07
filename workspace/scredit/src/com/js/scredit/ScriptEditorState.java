package com.js.scredit;

import java.util.List;

import com.js.editor.EditorState;

public class ScriptEditorState extends EditorState {

  public ScriptEditorState(ScriptEditor e) {
    mObjects = e.getScript().items().getFrozenCopy();
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

  public List<Integer> getSelectedSlots() {
    return mSelectedSlots;
  }

  // public Point getDupAccumulator() {
  // return mDupAccumulator;
  // }

  private EdObjectArray mObjects;
  private List<Integer> mSelectedSlots;
  private EdObjectArray mClipboard;
  // private Point mDupAccumulator;
}
