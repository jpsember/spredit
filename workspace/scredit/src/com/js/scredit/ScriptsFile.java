package com.js.scredit;

import java.io.*;
import java.util.ArrayList;

import static com.js.basic.Tools.*;
import tex.*;

public class ScriptsFile {
  public static final int VERSION = 2093; // added initial atlas tag
  // 2902; // added tints to sprites

  private ScriptsFile() {
  }

  /**
   * Format of script file:
   * 
   *  [2] version number
   *  [2] # strings
   *  [2] # scripts
   *  [2] number of labels 
   *  labels:
   *     [x] id of label (mapped string)
   *     [2] number of scripts for label
   *     scripts:
   *         [2] number of EdObjects
   *         EdObjects: see EdObject
   */
  public static void write(ScriptProject project, ArrayMap labelMap, File file)
      throws IOException {

    // unimp("refer to system font / palette file, if no palette file exists when compiling polygon; but look ahead to find first sprite referring to an atlas that has such a palette?");

    ScriptsFile sf = new ScriptsFile();
    sf.labelMap = labelMap;
    sf.file = file;
    sf.project = project;
    sf.write();
  }
  private void write() throws IOException {
    final boolean db = false;

    // determine first atlas referred to by any sprite objects, and...
    ArrayList<String> keys = labelMap.getKeys();

    Atlas a = null;
    outer: for (String key : keys) {
      LabelSet ls = (LabelSet) labelMap.getValue(key);
      // outer: for (int i = 0; i < keys.size(); i++) {
      // LabelSet ls = (LabelSet) labelMap.getValue(i);
      if (db)
        pr("finding first atlas, examining label set " + ls.label());

      for (int j = 0; j < ls.size(); j++) {
        Script sc = ls.script(j);

        ObjArray itms = sc.items();
        for (int k = 0; k < itms.size(); k++) {
          EdObject obj = itms.get(k);
          if (db)
            pr("  object #" + k + "=" + obj);

          a = obj.getAtlas();
          if (a != null) {
            if (db)
              pr("first atlas " + a + " found");
            break outer;
          }
        }
      }
    }

    dw = new RandomAccessFile(file, "rw");

    dw.writeShort(VERSION);
    // write placeholders for # strings, # scripts
    dw.writeShort(0);
    dw.writeShort(0);
    // -----------------------------

    dw.writeByte(a != null ? 1 : 0);
    if (a != null) {
      writeString(a.atlasTag());
    }

    int nScripts = 0;

    dw.writeShort(labelMap.size());
    for (int i = 0; i < keys.size(); i++) {
      LabelSet ls = (LabelSet) labelMap.getValue(i);
      writeString(ls.label());
      dw.writeShort(ls.size());

      for (int j = 0; j < ls.size(); j++) {
        Script sc = ls.script(j);
        writeScript(sc);
        nScripts++;
      }
    }

    // now fill in the # strings, # scripts
    dw.seek(2);
    dw.writeShort(stringsMap.size());
    dw.writeShort(nScripts);

    dw.close();
    dw = null;
  }
  private void writeScript(Script s) throws IOException {
    int nItems = 0;
    long startPtr = dw.getFilePointer();
    dw.writeShort(-1); // write place holder

    ObjArray items = s.items();

    long filePtr = dw.getFilePointer();
    for (int i = 0; i < items.size(); i++) {
      EdObject obj = items.get(i);
      obj.getFactory().write(this, obj);
      long afterPtr = dw.getFilePointer();
      if (afterPtr != filePtr)
        nItems++;
      filePtr = afterPtr;
    }
    dw.seek(startPtr);
    dw.writeShort(nItems);
    dw.seek(filePtr);
  }

  /**
   * Write string to output.  If string has not appeared before, writes a c-style
   * zero-terminated string, where each character is between 1..0x7f;
   * otherwise, writes   [high | 0x80], [low] where index of string is (high * 0x100) + low.
   * @param s string to write
   * @throws IOException
   */
  public void writeString(String s) throws IOException {
    //    MyTools.unimp("add # strings, # scripts fields to header, so we don't need variable-sized arrays when reading");

    warning("not performing this optimization; script file will probably be JSON anyways");
    int strIndex = -1; // stringsMap.indexOf(s);
    if (strIndex < 0) {
      strIndex = stringsMap.add(s);
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c <= 0 || c > 0x7f)
          throw new IllegalArgumentException("Illegal character: " + (int) c
              + " in string " + s);
        dw.writeByte(c);
      }
      dw.writeByte(0);
    } else {
      int highByte = (strIndex >> 8);
      if (highByte > 0x7f)
        throw new IllegalArgumentException("too many strings: " + s);
      dw.writeByte(highByte | 0x80);
      dw.writeByte(strIndex & 0x7f);
    }
  }
  public ScriptProject project() {
    return project;
  }
  public DataOutput outputStream() {
    return dw;
  }

  private ArrayMap stringsMap = new ArrayMap();
  private RandomAccessFile dw; //DataOutputStream dw;
  private ArrayMap labelMap;
  private ScriptProject project;
  private File file;
}
