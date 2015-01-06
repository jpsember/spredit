package com.js.scredit;

import java.io.*;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Files;

import static com.js.basic.Tools.*;

/**
 * <pre>
 * 
 * Represents a layered sequence of scripts.  Each script has
 * 
 * [] a filename
 * [] an editor, which includes the objects comprising the script
 * [] a 'modified' flag
 * 
 * If a script's filename is null, it's never been saved, and is an 'orphan'.
 * A sequence of scripts can contain multiple copies of a particular (non-orphan)
 * script.
 * 
 * A ScriptSet always contains at least one script.
 * 
 * </pre>
 * 
 */
public class ScriptSet {

  /**
   * Construct empty ScriptSet
   * 
   * @param projectBase
   *          optional file indicating directory that will contain all scripts
   *          in the set
   */
  public ScriptSet(File projectBase) {
    mProjectBase = projectBase;
    // Add a single orphan script to the set
    insert(0, null);
  }

  /**
   * Construct ScriptSet from JSON map
   * 
   * @param projectBase
   *          optional file indicating directory that will contain all scripts
   *          in the set
   * @param map
   * @throws JSONException
   */
  public ScriptSet(File projectBase, JSONObject map) throws JSONException {
    this(projectBase);
    if (map == null)
      return;
    mCursor = map.getInt("current");
    mForegroundStart = map.getInt("fg");
    JSONArray array = map.getJSONArray("paths");
    for (int i = 0; i < array.length(); i++) {
      String relPathString = array.getString(i);
      File f = null;
      if (!relPathString.isEmpty()) {
        f = new File(mProjectBase, relPathString);
      }
      setFile(i, f);
    }
  }

  /**
   * Construct JSON map from this set
   */
  public JSONObject encode() throws JSONException {
    JSONObject map = new JSONObject();
    map.put("current", mCursor);
    map.put("fg", mForegroundStart);
    JSONArray array = new JSONArray();
    for (int i = 0; i < size(); i++) {
      ScriptEditor editor = get(i);
      File f = editor.getScript().getFile();
      if (f == null)
        array.put("");
      else
        array.put(Files.fileWithinDirectory(f, mProjectBase).toString());
    }
    map.put("paths", array);
    return map;
  }

  public int size() {
    return mEditors.size();
  }

  public ScriptEditor get(int slot) {
    return mEditors.get(slot);
  }

  /**
   * Get cursor position
   */
  public int getCursor() {
    return mCursor;
  }

  /**
   * Get editor at cursor position
   */
  public ScriptEditor get() {
    return get(getCursor());
  }

  /**
   * Set cursor position
   */
  public void setCursor(int slot) {
    mCursor = slot;
  }

  /**
   * Get the slot marking the beginning of foreground layers
   */
  public int getForegroundLayer() {
    return mForegroundStart;
  }

  /**
   * Determine if current layer is a background layer
   */
  public boolean isBackground() {
    return mCursor < mForegroundStart;
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
   * Replace editor for current slot
   * 
   * @param file
   *          file for new editor, or null
   */
  public void setCursorFile(File file) {
    ScriptEditor editor = editorForFile(file);
    mEditors.set(getCursor(), editor);
  }

  /**
   * Insert editor
   * 
   * @param slot
   * @param file
   *          file for editor, or null if anonymous
   */
  public void insert(int slot, File file) {
    ScriptEditor editor = editorForFile(file);
    mEditors.add(slot, editor);
    if (slot < mForegroundStart)
      mForegroundStart++;
    if (slot < mCursor)
      mCursor++;
  }

  public void remove(int slot) {
    if (mEditors.size() == 1)
      throw new IllegalStateException();
    mEditors.remove(slot);
    if (slot < mForegroundStart)
      mForegroundStart--;
    if (slot < mCursor)
      mCursor--;
  }

  /**
   * Assign name to an editor; rename it if it is already named. If different
   * than current name, the new name cannot belong to any other slot's editor
   * 
   * @param slot
   * @param name
   *          name to assign (non-null)
   */
  public void setName(int slot, File name) {
    if (name == null)
      throw new IllegalArgumentException();
    Script script = get(slot).getScript();

    // If name matches current, do nothing
    if (equal(name, script.getFile()))
      return;

    // Make sure this name doesn't already appear in another slot
    if (findEditorForNamedFile(name) >= 0)
      throw new IllegalStateException("Cannot assign name " + name
          + " to slot " + slot + ", already used");
    script.setFile(name);
  }

  /**
   * Find which slot, if any, contains a named editor
   * 
   * @param file
   *          file to look for (non-null)
   * @return slot, if found, or -1
   */
  public int findEditorForNamedFile(File file) {
    ASSERT(file != null);
    for (int slot = 0; slot < mEditors.size(); slot++) {
      ScriptEditor editor = mEditors.get(slot);
      if (file.equals(editor.getScript().getFile()))
        return slot;
    }
    return -1;
  }

  /**
   * Get editor for a script. If script is an orphan, returns a new editor;
   * else, looks for the script within the set. If found, returns that editor;
   * else, constructs a new one
   */
  private ScriptEditor editorForFile(File file) {
    if (file == null)
      return new ScriptEditor();
    int slot = findEditorForNamedFile(file);
    if (slot >= 0)
      return mEditors.get(slot);
    ScriptEditor editor = new ScriptEditor();
    editor.getScript().setFile(file);
    return editor;
  }

  private void setFile(int slot, File f) {
    ScriptEditor editor = editorForFile(f);
    if (mEditors.size() == slot) {
      mEditors.add(editor);
    } else {
      mEditors.set(slot, editor);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(nameOf(this));
    sb.append("\n projectBase " + mProjectBase);
    sb.append("\n cursor " + mCursor + "\n");
    sb.append("----- Editors -----\n");
    for (int i = 0; i < mEditors.size(); i++) {
      sb.append("  #" + i + ": " + mEditors.get(i) + "\n");
    }
    sb.append("-------------------\n");
    return sb.toString();
  }

  private File mProjectBase;
  private int mForegroundStart;
  private ArrayList<ScriptEditor> mEditors = new ArrayList();
  private int mCursor;

}
