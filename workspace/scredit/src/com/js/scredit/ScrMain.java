package com.js.scredit;

import java.io.*;
import javax.swing.*;

import apputil.*;
import com.js.basic.*;

public class ScrMain implements IApplication {

  public static void main(String[] args) {
    try {
      CmdLineArgs ca = new CmdLineArgs();
      ca.add("scriptlist").setString().setArray()
          .desc("Compile script list <scriptlist[.scf]>");
      ca.parse(args);

      File scriptListPath = null;
      String[] scriptList = ca.getStrings("scriptlist");
      if (scriptList.length > 0) {
        if (scriptList.length > 1)
          ca.fail("only one scriptlist file allowed");
        File s = new File(scriptList[0]);
        if (!Files.hasExtension(s))
          s = Files.setExtension(s, ScriptCompiler.SCRIPTFILE_EXT);
        scriptListPath = s;
      }

      // add handlers for various object types
      {
        Script.addObjectFactory(SpriteObject.FACTORY);
        Script.addObjectFactory(PolygonObject.FACTORY);
        Script.addObjectFactory(RectangleObject.FACTORY);
        Script.addObjectFactory(GroupObject.FACTORY);
      }

      if (scriptListPath != null) {
        AppTools.runAsCmdLine();
        ScriptCompiler.build(scriptListPath);
      } else {
        AppTools.startApplication(new ScrMain());
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void createAndShowGUI(JFrame frame) {

    try {
      config = new ConfigSet(null) //
          .add(apputil.MyFrame.CONFIG) //
          .add(ScriptEditor.CONFIG) //
          .add(Grid.CONFIG) //
          .restore();
    } catch (Throwable e) {
      AppTools.showError("reading defaults", e);
    }
    ScriptEditor.init((JComponent) frame.getContentPane());
  }

  @Override
  public String getName() {
    return "ScrEdit";
  }

  @Override
  public boolean exitProgram() {
    if (!ScriptEditor.doCloseProject())
      return false;
    writeDefaults();
    return true;
  }

  @Override
  public JFrame getFrame() {
    return new MyFrame(getName());
  }

  private void writeDefaults() {
    try {
      config.save();
    } catch (IOException e) {
      AppTools.showError("writing defaults file", e);
    }
  }

  private static ConfigSet config;

}