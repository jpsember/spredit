package com.js.scredit;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.*;

import static com.js.basic.Tools.*;

public class ScriptCompiler {
  public static final String SCRIPTFILE_EXT = "scf";
  public static final String SCRIPTFILE_COMPILED_EXT = "scb";

  public static void build(File sourceFile) throws IOException {
    final boolean db = false;

    if (db)
      pr("ScriptCompiler.build " + sourceFile);

    ScriptCompiler sc = new ScriptCompiler(sourceFile);
    sc.build();
  }

  private ScriptCompiler(File sourceFile) {
    this.sourceFile = sourceFile.getAbsoluteFile();
    this.outputFile = Files.setExtension(sourceFile, SCRIPTFILE_COMPILED_EXT);
  }

  private void build() throws IOException {
    unimp("ScriptCompiler.build");
    /*
     * final boolean db = false;
     * 
     * if (db) pr("ScriptCompiler.build, buildFile=" + sourceFile);
     * 
     * if (!sourceFile.exists()) throw new
     * FileNotFoundException("No such script file: " + sourceFile);
     * 
     * defineProject();
     * 
     * DefScanner sc = new DefScanner(sourceFile); while (!sc.done()) {
     * 
     * // is next line a directive (.XXX)? if (sc.peek().id('.')) { sc.read();
     * // read period String label = sc.sId().toUpperCase();
     * 
     * if (labelMap.indexOf(label) >= 0) { sc.exception("duplicate label: " +
     * label); }
     * 
     * LabelSet lset = new LabelSet(label); labelMap.add(label, lset); labelSet
     * = lset; if (db) pr(" starting label set: " + label); continue; }
     * 
     * if (labelSet == null) sc.peek().exception("no label defined");
     * 
     * String str = null; if (sc.peek().id(LBL)) { str = sc.sLabel(); } else str
     * = sc.sId();
     * 
     * if (db) pr(" read str=" + str);
     * 
     * File f = new File(sourceFile.getParent(), str); do { if (f.exists())
     * break;
     * 
     * // see if set exists; if not, then individual script if
     * (!Streams.hasExtension(f)) { String s2 =
     * Streams.addExtension(f.getAbsolutePath(), Script.SET_EXT); File f2 = new
     * File(s2); if (db) pr("testing if extended file exists: " + f2);
     * 
     * if (f2.exists()) { f = f2; break; }
     * 
     * s2 = Streams.addExtension(f.getAbsolutePath(), Script.SRC_EXT); f2 = new
     * File(s2); if (f2.exists()) { f = f2; break; } } } while (false);
     * 
     * if (!f.exists()) sc.exception("script file or set not found: " + str);
     * 
     * if (db) pr(" processing script or set file: " + f);
     * 
     * String ext = Streams.getExtension(f); if (ext.equals(Script.SET_EXT)) {
     * if (db) pr(" has set extension"); processScriptSet(f); } else if
     * (ext.equals(Script.SRC_EXT)) { processScript(f); } else
     * sc.exception("unknown file type: " + f); }
     * 
     * ScriptsFile.write(project, labelMap, outputFile); writeSymFile();
     */
  }

  // private
  void writeSymFile() throws IOException {
    final boolean db = false;

    if (db)
      pr("writeSymFile corresponding to " + sourceFile);

    File symPath = sourceFile.getParentFile();
    String scriptSetName = Files.removeExtension(sourceFile).getName();
    scriptSetName = scriptSetName + "_scripts";
    symPath = Files.setExtension(new File(symPath, scriptSetName), "h");

    StringBuilder sb = new StringBuilder();
    String equName = scriptSetName.toUpperCase() + "_EQU";
    sb.append("#ifndef " + equName + "\n");
    sb.append("#define " + equName + "\n\n");

    sb.append("\n// This file has been generated by ScrEdit; do not modify!\n\n");

    int scriptIndex = 0;

    for (int i = 0; i < labelMap.size(); i++) {
      LabelSet ls = (LabelSet) labelMap.getValue(i);
      String lblName = ls.label().toUpperCase();

      appendSymbol(sb, "#define S_", lblName, scriptIndex);
      appendSymbol(sb, "#define L_", lblName, ls.size());
      sb.append('\n');

      scriptIndex += ls.size();
    }
    sb.append("#endif\n");
    if (db)
      pr(" generated file:[\n" + sb + "]");
    if (db)
      pr("writing to path:[" + symPath + "] if changed");

    Files.writeStringToFileIfChanged(symPath, sb.toString());
  }

  private static void appendSymbol(StringBuilder sb, String prefix,
      String lblName, int value) {
    int j = sb.length();
    sb.append(prefix);
    sb.append(lblName);
    sb.append(' ');
    while (sb.length() - j < 25)
      sb.append(' ');
    sb.append(value);
    sb.append('\n');
  }

  // private
  void processScriptSet(File f) throws IOException, JSONException {
    final boolean db = false;
    if (db)
      pr("processScriptSet: " + f);

    ScriptSet ss = new ScriptSet(project.directory(), new JSONObject(
        FileUtils.readFileToString(f)));
    for (int i = 0; i < ss.size(); i++) {
      File f2 = ss.get(i).file();
      if (f2 != null)
        processScript(f2);
    }

  }

  private void processScript(File f) throws IOException {
    final boolean db = false;
    if (db)
      pr("processScript: " + f);

    Script scr = new Script(project, f);
    labelSet.add(scr);
  }

  /**
   * If project is not yet defined, attempt to find project file in directories
   * at or above script file.
   * 
   * @throws FileNotFoundException
   */
  // private
  void defineProject() throws IOException {
    final boolean db = false;
    if (project == null) {
      if (db)
        pr("defineProject, buildFile=" + sourceFile + ", exists="
            + sourceFile.exists());

      File base = sourceFile.getParentFile();
      while (true) {
        if (base == null || !base.isDirectory())
          throw new FileNotFoundException("cannot find project file, base="
              + base);

        File[] fl = base.listFiles((FileFilter) ScriptProject.FILES);
        if (db)
          pr(" files at base:" + base + " = " + fl.length);

        if (fl.length > 1)
          throw new FileNotFoundException("multiple project files at: " + base);

        if (fl.length == 1) {
          project = new ScriptProject(fl[0]);
          // unimp("set global project here...");

          // projectPath = fl[0].getParentFile();
          break;
        }
        if (db)
          pr(" retreating to parent file");

        base = base.getParentFile();
      }
      if (db)
        pr(" projectPath defined to be " + project.directory());

    }
  }

  private File sourceFile;
  // private
  File outputFile;
  private ScriptProject project;
  private ArrayMap labelMap = new ArrayMap();
  // current label set
  private LabelSet labelSet;
}
