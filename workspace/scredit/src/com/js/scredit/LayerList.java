package com.js.scredit;

//import static com.js.basic.Tools.*;
//import java.io.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import com.js.geometry.MyMath;

/**
 * Organizes layers of scripts, including a background / foreground partition
 * 
 * @deprecated incorporate into ScriptSet
 */
public class LayerList {
  //
  // static { // Suppress warning of unused basic.Tools import
  // doNothing();
  // }
  //
  // /**
  // * Constructor
  // *
  // * @param callback
  // * method to call when script becomes active
  // */
  // public LayerList() {
  // mLayers = new ArrayList();
  // insert();
  // }
  //
  // /**
  // * Delete current layer, and move to following one (or preceding, if
  // deleting
  // * last layer); first layer cannot be deleted
  // */
  // public void deleteCurrent() {
  // if (mLayers.size() <= 1)
  // throw new IllegalStateException();
  //
  // mLayers.remove(mCursor);
  // if (mCursor < mForegroundStart)
  // mForegroundStart--;
  // select(Math.min(mCursor, mLayers.size() - 1));
  // }
  //
  // /**
  // * Determine if current layer is a background layer
  // */
  // public boolean isBackground() {
  // return mCursor < mForegroundStart;
  // }
  //
  // /**
  // * Insert an orphan (a new script, one without a filename), and make it the
  // * new active one. If the list is not empty, inserts the new one after the
  // * current one
  // */
  // public void insert() {
  // ScriptEditor editor = new ScriptEditor();
  // int newInd = mCursor;
  // if (size() != 0)
  // newInd += 1;
  // mLayers.add(newInd, editor);
  // if (newInd < mForegroundStart)
  // mForegroundStart++;
  // select(newInd);
  // }
  //
  // /**
  // * Move to layer following active one
  // */
  // public void next() {
  // select(MyMath.myMod(mCursor + 1, size()));
  // }
  //
  // /**
  // * Move to layer preceding active one
  // */
  // public void previous() {
  // select(MyMath.myMod(mCursor - 1, size()));
  // }
  //
  // /**
  // * Get index of current layer
  // */
  // public int currentSlot() {
  // return mCursor;
  // }
  //
  // /**
  // * Specify current layer
  // */
  // public void select(int slot) {
  // mCursor = slot;
  // activateCurrentScript();
  // }
  //
  // /**
  // * Replace current layer with a copy of another layer
  // *
  // * @param slot
  // * slot containing other layer
  // * @deprecated
  // */
  // public void useCopyOf(int slot) {
  // mLayers.set(mCursor, mLayers.get(slot));
  // activateCurrentScript();
  // }
  //
  // /**
  // * Get editor
  // */
  // public ScriptEditor layer(int slot) {
  // return (ScriptEditor) mLayers.get(slot);
  // }
  //
  // /**
  // * Determine first slot containing a particular editor
  // *
  // * @param f
  // * file associated with editor
  // * @return first slot containing file, or -1 if none
  // * @deprecated
  // */
  // public int indexOf(File f) {
  // int ret = -1;
  // for (int i = 0; i < size(); i++) {
  // ScriptEditor ed = layer(i);
  // if (f.equals(ed.path())) {
  // ret = i;
  // break;
  // }
  // }
  // return ret;
  // }
  //
  // // /**
  // // * Delete all layers, add a single orphan one in the foreground
  // // */
  // // public void clear() {
  // // while (size() > 1)
  // // deleteCurrentLayer();
  // // mForegroundStart = 0;
  // // resetCurrent();
  // // }
  //
  // /**
  // * Reset the current layer
  // *
  // * @deprecated
  // */
  // public void resetCurrent() {
  // mLayers.set(mCursor, new ScriptEditor());
  // activateCurrentScript();
  // }
  //
  // /**
  // * Get number of layers
  // */
  // public int size() {
  // return mLayers.size();
  // }
  //
  // /**
  // * Move foreground/background partition to include/exclude current layer
  // *
  // * @param backgroundFlag
  // * if true, ensures current layer is background; else, ensures
  // * current layer is foreground
  // */
  // public void setBackground(boolean backgroundFlag) {
  // if (mCursor < mForegroundStart) {
  // if (!backgroundFlag)
  // mForegroundStart = mCursor;
  // } else {
  // if (backgroundFlag)
  // mForegroundStart = mCursor + 1;
  // }
  // }
  //
  // /**
  // * Determine slot of first foreground layer
  // *
  // * @return slot of first foreground editor
  // */
  // public int foregroundStart() {
  // return mForegroundStart;
  // }
  //
  // /**
  // * Set background/foreground partition
  // *
  // * @param fg
  // * slot of first foreground layer
  // */
  // public void setForeground(int fg) {
  // mForegroundStart = MyMath.clamp(fg, 0, size());
  // }
  //
  // @Override
  // public String toString() {
  // StringBuilder sb = new StringBuilder("LayerSet");
  // sb.append(" size:" + mLayers.size());
  // sb.append(" current slot:" + mCursor);
  // sb.append(" foreground:" + mForegroundStart);
  // for (ScriptEditor editor : mLayers) {
  // sb.append("\n  editor: " + editor);
  // }
  // return sb.toString();
  // }
  //
  // private void activateCurrentScript() {
  // layer(mCursor).activate();
  // }
  //
  // // Map of (nonorphan) script filename -> script
  // // private Map<File, ScriptEditor> mFileToScriptMap = new HashMap();
  // private int mForegroundStart;
  // private ArrayList<ScriptEditor> mLayers;
  // // Index of 'active' script, the one currently being edited
  // private int mCursor;
  //
}
