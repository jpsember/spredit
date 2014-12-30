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
        String s = scriptList[0];
        Streams.addExtension(s, ScriptCompiler.SCRIPTFILE_EXT);
        scriptListPath = new File(s);
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

    config = new ConfigSet(null);
    config.add(apputil.MyFrame.CONFIG);
    config.add(ScriptEditor.CONFIG);
    config.add(Grid.CONFIG);

    try {
      config.readFrom(AppTools.getDefaultsPath(getName()));
    } catch (IOException e) {
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
      config.writeTo(AppTools.getDefaultsPath(getName()));
    } catch (IOException e) {
      AppTools.showError("writing defaults file", e);
    }
  }

  private static ConfigSet config;

}