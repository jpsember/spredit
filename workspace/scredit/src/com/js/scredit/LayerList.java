package com.js.scredit;

import static com.js.basic.Tools.*;
import java.io.*;
import java.util.ArrayList;

import com.js.geometry.MyMath;

/**
 * Organizes layers of scripts, including a background / foreground partition
 */
public class LayerList {

  static { // Suppress warning of unused basic.Tools import
    doNothing();
  }

  /**
   * Constructor
   * 
   * @param callback
   *          method to call when script becomes active
   */
  public LayerList() {
    mLayers = new ArrayList();
    insert(false);
  }

  /**
   * Delete current layer, and move to following one (or preceding, if deleting
   * last layer); first layer cannot be deleted
   */
  public void delete() {
    if (mLayers.size() <= 1)
      throw new IllegalStateException();

    mLayers.remove(mCursor);
    if (mCursor < mForegroundStart)
      mForegroundStart--;
    select(Math.min(mCursor, mLayers.size() - 1));
  }

  /**
   * Determine if current layer is a background layer
   */
  public boolean isBackground() {
    return mCursor < mForegroundStart;
  }

  /**
   * Insert an orphan (a new script, one without a filename)
   * 
   * @param afterCurrentPos
   *          if true, inserts it after current layer; otherwise, before
   */
  public void insert(boolean afterCurrentPos) {
    ScriptEditor editor = new ScriptEditor();
    int newInd = mCursor + (afterCurrentPos ? 1 : 0);
    mLayers.add(newInd, editor);
    if (newInd < mForegroundStart)
      mForegroundStart++;
    select(newInd);
  }

  /**
   * Move to layer following active one
   */
  public void next() {
    select(MyMath.myMod(mCursor + 1, size()));
  }

  /**
   * Move to layer preceding active one
   */
  public void prev() {
    select(MyMath.myMod(mCursor - 1, size()));
  }

  /**
   * Get index of current layer
   * 
   * @return
   */
  public int currentSlot() {
    return mCursor;
  }

  /**
   * Specify current layer
   * 
   * @param slot
   *          slot of layer to make current
   */
  public void select(int slot) {
    mCursor = slot;
    activateCursorScript();
  }

  /**
   * Replace current layer with a copy of another layer
   * 
   * @param slot
   *          slot containing other layer
   * @deprecated
   */
  public void useCopyOf(int slot) {
    mLayers.set(mCursor, mLayers.get(slot));
    activateCursorScript();
  }

  private void activateCursorScript() {
    layer(mCursor).activate();
  }

  /**
   * Get editor
   */
  public ScriptEditor layer(int slot) {
    return (ScriptEditor) mLayers.get(slot);
  }

  /**
   * Determine first slot containing a particular editor
   * 
   * @param f
   *          file associated with editor
   * @return first slot containing file, or -1 if none
   * @deprecated
   */
  public int indexOf(File f) {
    int ret = -1;
    for (int i = 0; i < size(); i++) {
      ScriptEditor ed = layer(i);
      if (f.equals(ed.path())) {
        ret = i;
        break;
      }
    }
    return ret;
  }

  /**
   * Delete all layers, add a single orphan one in the foreground
   */
  public void reset() {
    while (size() > 1)
      delete();
    mForegroundStart = 0;
    resetCurrent();
  }

  /**
   * Reset the current layer
   * 
   * @deprecated
   */
  public void resetCurrent() {
    mLayers.set(mCursor, new ScriptEditor());
    activateCursorScript();
  }

  /**
   * Get number of layers
   */
  public int size() {
    return mLayers.size();
  }

  /**
   * Move foreground/background partition to include/exclude current layer
   * 
   * @param backgroundFlag
   *          if true, ensures current layer is background; else, ensures
   *          current layer is foreground
   */
  public void setBackground(boolean backgroundFlag) {
    if (mCursor < mForegroundStart) {
      if (!backgroundFlag)
        mForegroundStart = mCursor;
    } else {
      if (backgroundFlag)
        mForegroundStart = mCursor + 1;
    }
  }

  /**
   * Determine slot of first foreground layer
   * 
   * @return slot of first foreground editor
   */
  public int foregroundStart() {
    return mForegroundStart;
  }

  /**
   * Set background/foreground partition
   * 
   * @param fg
   *          slot of first foreground layer
   */
  public void setForeground(int fg) {
    mForegroundStart = MyMath.clamp(fg, 0, size());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("LayerSet");
    sb.append(" size:" + mLayers.size());
    sb.append(" current slot:" + mCursor);
    sb.append(" foreground:" + mForegroundStart);
    for (ScriptEditor editor : mLayers) {
      sb.append("\n  editor: " + editor);
    }
    return sb.toString();
  }

  private int mForegroundStart;
  private ArrayList<ScriptEditor> mLayers;
  // Index of 'active' script, the one currently being edited
  private int mCursor;

}
