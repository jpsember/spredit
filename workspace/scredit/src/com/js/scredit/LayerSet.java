package com.js.scredit;

import static com.js.basic.Tools.*;
import java.io.*;
import java.util.ArrayList;

import com.js.geometry.MyMath;

/**
 * Organizes layers of editors, background / foreground partitions
 */
public class LayerSet {
  private static final boolean db = false;

  /**
   * Callback for LayerSet
   */
  public static interface ICallback {
    /**
     * Called by useLayer()
     * @param editor active editor
     */
    public void useEditor(ScriptEditor editor);
  }

  /**
   * Delete current layer, and move to following one 
   *  (or preceding, if deleting last layer);
   * first layer cannot be deleted
   */
  public void delete() {
    if (db)
      pr("deleteLayer " + currentSlot);
    if (layers.size() <= 1)
      throw new IllegalStateException();

    layers.remove(currentSlot);
    if (currentSlot < foregroundStart)
      foregroundStart--;
    select(Math.min(currentSlot, layers.size() - 1));
  }

  /**
   * Determine if current layer is a background layer
   * @return true if so
   */
  public boolean isBackground() {
    return currentSlot < foregroundStart;
  }

  /**
   * Insert an orphan
   * @param afterCurrentPos if true, inserts it after current layer; otherwise,
   *   before
   */
  public void insert(boolean afterCurrentPos) {
    if (db)
      pr("insertLayer, index= " + currentSlot);
    ScriptEditor editor = new ScriptEditor();
    int newInd = currentSlot + (afterCurrentPos ? 1 : 0);
    layers.add(newInd, editor);
    if (newInd < foregroundStart)
      foregroundStart++;

    select(newInd);
  }

  /**
   * Move to layer following active one
   */
  public void next() {
    select(MyMath.myMod(currentSlot + 1, size()));

  }
  /**
   * Move to layer preceding active one
   */
  public void prev() {
    select(MyMath.myMod(currentSlot - 1, size()));
  }

  /**
   * Get index of current layer
   * @return
   */
  public int currentSlot() {
    return currentSlot;
  }
  
  /**
   * Specify current layer
   * @param slot slot of layer to make current
   */
  public void select(int slot) {
    currentSlot = slot;
    doCallback();
  }

  /**
   * Replace current layer with a copy of another layer
   * @param slot slot containing other layer
   */
  public void useCopyOf(int slot) {
    layers.set(currentSlot, layers.get(slot));
    doCallback();
  }

  private void doCallback() {
    if (db)
      pr("using editor " + layer(currentSlot));

    cb.useEditor(layer(currentSlot));
  }

  /**
   * Get editor
   * @param slot slot 
   * @return editor editor within slot
   */
  public ScriptEditor layer(int slot) {
    return (ScriptEditor) layers.get(slot);
  }

  /**
   * Determine first slot containing a particular editor
   * @param f file associated with editor
   * @return first slot containing file, or -1 if none
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
    foregroundStart = 0;
    resetCurrent();
  }

  /**
   * Reset the current layer
   */
  public void resetCurrent() {
    layers.set(currentSlot, new ScriptEditor());
    doCallback();
  }

 

  /**
  * Get number of layers
  * @return number of layers
  */
  public int size() {
    return layers.size();
  }

  public LayerSet(ICallback callback) {
    cb = callback;
    layers = new ArrayList();
    insert(false);
  }

  /**
   * Move foreground/background partition to
   * include/exclude current layer
   * @param f if true, ensures current layer is background;
   *   else, ensures current layer is foreground
   */
  public void setBackground(boolean f) {
    if (currentSlot < foregroundStart) {
      if (!f)
        foregroundStart = currentSlot;
    } else {
      if (f)
        foregroundStart = currentSlot + 1;
    }
  }

  /**
   * Determine slot of first foreground layer
   * @return slot of first foreground editor
   */
  public int foregroundStart() {
    return foregroundStart;
  }
  
  /**
   * Set background/foreground partition
   * @param fg slot of first foreground layer
   */
  public void setForeground(int fg) {
    foregroundStart = MyMath.clamp(fg, 0, size());
  }

  private int foregroundStart;
  private ICallback cb;
  private ArrayList<ScriptEditor> layers;
  private int currentSlot;

}
