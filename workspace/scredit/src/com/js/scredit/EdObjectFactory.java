package com.js.scredit;

import java.io.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.js.editor.MouseOper;
import com.js.geometry.IPoint;

public abstract class EdObjectFactory {

  /**
   * Get name of this object. This is an identifier that is written to text
   * files to identify this object.
   * 
   * @return String
   */
  public abstract String getTag();

  // /**
  // * Construct an object of this type. Used when user wants to add a new
  // * object in the editor.
  // * @return EditObj
  // */
  // public abstract EdObject construct();

  /**
   * Parse EditObj from JSON map
   * 
   * @param map
   *          JSONObject
   * @return EditObj
   */
  public abstract EdObject parse(Script script, JSONObject map)
      throws JSONException;

  /**
   * Write EditObj in format suitable for parsing
   * 
   * @param script
   *          script containing object
   * @param map
   *          where to write object
   * @param obj
   *          object to write
   * @throws JSONException
   */
  public abstract void write(Script script, JSONObject map, EdObject obj)
      throws JSONException;

  /**
   * Write EditObj to ScriptsFile
   * 
   * @param sf
   *          ScriptsFile writing object
   * @param obj
   *          object to write
   * @throws IOException
   */
  public abstract void write(ScriptsFile sf, EdObject obj) throws IOException;

  /**
   * Get code unique to this object type
   * 
   * @return code
   */
  public abstract int getCode();

  /**
   * Determine if mouse...
   * 
   * @param obj
   * @return
   */
  public MouseOper isEditingSelectedObject(int slot, EdObject obj,
      IPoint mousePt) {
    return null;
  }

}
