package com.js.scredit;

import java.io.*;
import javax.swing.*;
import apputil.*;
import com.js.basic.*;
import static com.js.basic.Tools.*;

/*
 [] class hierarchy
 [] complete save/save as
 */
public class ScrMain implements IApplication {
  public static IApplication app() {
    if (theApp == null)
      theApp = new ScrMain();
    return theApp;
  }

  private static IApplication theApp;
  private static final String APP_NAME = "ScrEdit";

  // private static final String help = "Edit Graphics Scripts\n"
  // + "scredit <options>\n" + "Options: \n" //
  // + " -c <scriptlist[.scf]>  : compile script list\n" //
  // + " -h, --help       : help\n" //
  // , //
  //
  // defaults = " == --help -h" + " !! " + "   ";

  public static void main(String[] args) {
    warning("embedded palette is maybe not right way to go.  just change state when necessary?  maybe make it optional; use palette if available");

    File scriptListPath = null;
    // File symPath = null;
    try {
      CmdLineArgs ca = new CmdLineArgs();
      ca.add("scriptlist").setString().setArray()
          .desc("Compile script list <scriptlist[.scf]>");
      ca.parse(args);

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
        AppTools.setAppName(APP_NAME);

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
    // frame.setTitle(APP_NAME);

    // JFrame f = frame.frame();
    AppTools.setFrame(frame, app());

    ScriptEditor.init((JComponent) frame.getContentPane());

    frame.setVisible(true); // (true);

  }

  public boolean exitProgram() {
    final boolean db = false;
    if (db)
      pr("exitProgram");

    boolean quit = false;
    do {
      if (db)
        pr(" doCloseProject()");

      if (!ScriptEditor.doCloseProject())
        break;

      // write defaults just before exiting
      if (db)
        pr(" writeDefaults");
      writeDefaults();

      quit = true;
    } while (false);
    return quit;
  }

  /**
   * Write defaults to file
   */
  private static void writeDefaults() {
    try {
      config.writeTo(AppTools.getDefaultsPath(DEFAULTS_TAG));

    } catch (IOException e) {
      AppTools.showError("writing defaults file", e);
    }
  }

  private static ConfigSet config;
  private static final String DEFAULTS_TAG = "scredit";
}