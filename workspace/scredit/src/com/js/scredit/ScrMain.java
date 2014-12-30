package com.js.scredit;

import java.io.*;
import javax.swing.*;
import apputil.*;
import com.js.basic.*;

public class ScrMain implements IApplication {

  public static IApplication app() {
    if (theApp == null)
      theApp = new ScrMain();
    return theApp;
  }

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
        // Schedule a job for the event-dispatching thread:
        // calling an application object's run() method.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            createAndShowGUI();
          }
        });
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void createAndShowGUI() {

    config = new ConfigSet(null);
    config.add(apputil.MyFrame.CONFIG);
    config.add(ScriptEditor.CONFIG);
    config.add(Grid.CONFIG);

    try {
      config.readFrom(AppTools.getDefaultsPath(DEFAULTS_TAG));
    } catch (IOException e) {
      AppTools.showError("reading defaults", e);
    }

    MyFrame frame = new MyFrame("APP");

    AppTools.setApplication(app(), frame);

    ScriptEditor.init((JComponent) frame.getContentPane());
    frame.setVisible(true);
  }

  @Override
  public String getName() {
    return APP_NAME;
  }

  @Override
  public boolean exitProgram() {
    if (!ScriptEditor.doCloseProject())
      return false;
    writeDefaults();
    return true;
  }

  private static void writeDefaults() {
    try {
      config.writeTo(AppTools.getDefaultsPath(DEFAULTS_TAG));
    } catch (IOException e) {
      AppTools.showError("writing defaults file", e);
    }
  }

  private static ConfigSet config;
  private static final String DEFAULTS_TAG = "scredit";
  private static IApplication theApp;
  private static final String APP_NAME = "ScrEdit";

}