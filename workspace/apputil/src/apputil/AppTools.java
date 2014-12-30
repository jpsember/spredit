package apputil;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import com.js.basic.Streams;

import static com.js.basic.Tools.*;

public class AppTools {

  public static void startApplication(IApplication app) {
    setApplication(app);
    startGUI();
  }

  private static void setApplication(IApplication app) {
    sApplication = app;
    String lcOSName = System.getProperty("os.name").toLowerCase();
    sIsMac = lcOSName.startsWith("mac os x");
    if (sIsMac) {
      MacUtils.useScreenMenuBar(app.getName());
    }
  }

  private static void startGUI() {
    // Schedule a job for the event-dispatching thread:
    // calling an application object's run() method.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        if (!isMac()) {
          // From
          // http://java.sun.com/products/jfc/tsc/articles/mixing/#issues:
          JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        }

        // disable the close button, since we want to verify close
        final JFrame frame = frame();

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            if (sApplication.exitProgram()) {
              frame.setVisible(false);
              frame.dispose();
            }
          }
        });

        sApplication.createAndShowGUI(frame);
        frame.setVisible(true);
      }
    });

  }

  public static boolean isMac() {
    return sIsMac;
  }

  public static void stringToLabel(String s, StringBuilder sb) {
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\n') {
        sb.append("\\n");
      } else if (c < 0x20 || c > 0x7f || c == '"' || c == '\\') {
        sb.append("\\u");
        sb.append(dh(c, "4"));
      } else
        sb.append(c);
    }
    sb.append('"');
  }

  public static Font getFixedWidthFont() {
    if (sMonotoneFontNormal == null) {
      sMonotoneFontNormal = new Font("Monaco", Font.PLAIN, 16);
    }
    return sMonotoneFontNormal;
  }

  public static Font getSmallFixedWidthFont() {
    if (sMonotoneFontSmall == null)
      sMonotoneFontSmall = new Font("Monaco", Font.PLAIN, 12);
    return sMonotoneFontSmall;
  }

  public static String stringToLabel(String s) {
    StringBuilder sb = new StringBuilder();
    stringToLabel(s, sb);
    return sb.toString();
  }

  public static void showMsg(String msg) {
    unimp("don't use swing to show messages: " + msg);
    JOptionPane.showMessageDialog(frame(), msg, "Hey!",
        JOptionPane.INFORMATION_MESSAGE);
  }

  public static void showError(String msg, Throwable cause) {
    StringBuilder sb = new StringBuilder("*** Error: ");
    if (msg != null)
      sb.append(msg);
    sb.append('\n');
    sb.append(cause);
    sb.append('\n');
    sb.append(stackTrace(cause));
    pr(stackTrace(cause));

    unimp("don't use Swing if in compile mode: " + sb);

    JOptionPane.showMessageDialog(frame(), sb, "Hey!",
        JOptionPane.ERROR_MESSAGE);
  }

  public static JFrame frame() {
    if (sFrame == null) {
      sFrame = sApplication.getFrame();
      sFrame.setTitle(sApplication.getName());
    }
    return sFrame;
  }

  public static File chooseFileToSave(String prompt, MyFileFilter fileFilter,
      File defaultFile) {
    final boolean db = false;
    enableMenuBar(false);

    if (db)
      pr("chooseFileToSave\n " + prompt + "\n filter:" + fileFilter
          + "\n defaultFile:" + defaultFile);

    File f = null;
    do {

      if (AppTools.isMac()) {

        FileDialog d = new FileDialog(sFrame, prompt, FileDialog.SAVE);

        if (defaultFile != null) {
          d.setDirectory(defaultFile.getParent());
          d.setFile(defaultFile.getName());
        }

        d.setFilenameFilter(fileFilter);
        d.setVisible(true);

        String dir = d.getDirectory();
        String s = d.getFile();
        if (dir == null || s == null)
          break;
        f = new File(dir, s);
      } else {
        JFileChooser fc = new JFileChooser(defaultFile);
        fc.setFileFilter(fileFilter);
        fc.setSelectedFile(defaultFile); //

        int result = fc.showSaveDialog(AppTools.frame());
        // File ret = null;
        // do {
        if (result != JFileChooser.APPROVE_OPTION)
          break;

        f = fc.getSelectedFile();
      }
      f = fileFilter.fixExtension(f);

    } while (false);
    enableMenuBar(true);
    return f;
  }

  // private static final boolean USE_SWING_FILECHOOSERS = false;

  private static void enableMenuBar(boolean f) {
    JMenuBar mb = sFrame.getJMenuBar();
    if (mb != null)
      mb.setEnabled(f);
  }

  public static File chooseFileToOpen(String prompt, MyFileFilter fileFilter,
      File defaultFile) {

    enableMenuBar(false);

    File chosenFile = null;
    do {
      if (AppTools.isMac()) {
        FileDialog fileChooser = new FileDialog(sFrame, prompt, FileDialog.LOAD);
        if (defaultFile != null) {
          fileChooser.setDirectory(defaultFile.getParent());
          fileChooser.setFile(defaultFile.getName());
        }
        fileChooser.setFilenameFilter(fileFilter);
        fileChooser.setVisible(true);

        String dir = fileChooser.getDirectory();
        String s = fileChooser.getFile();
        if (dir == null || s == null)
          break;
        chosenFile = new File(dir, s);
      } else {
        JFileChooser fileChooser = new JFileChooser(defaultFile);
        fileChooser.setFileFilter(fileFilter);
        int result = fileChooser.showOpenDialog(frame());
        if (result != JFileChooser.APPROVE_OPTION)
          break;
        chosenFile = fileChooser.getSelectedFile();
      }
      if (!chosenFile.exists() || !fileFilter.accept(chosenFile)) {
        chosenFile = null;
        break;
      }
    } while (false);
    enableMenuBar(true);
    return chosenFile;
  }

  public static File incrementFile(File f) {
    String name = f.getName();
    String ext = Streams.getExtension(name);
    name = Streams.removeExt(name);

    if (name.length() == 0)
      throw new IllegalArgumentException(f.toString());
    int i = name.length();
    while (i > 0) {
      char c = name.charAt(i - 1);
      if (c < '0' || c > '9')
        break;
      i--;
    }
    int number = 0;
    if (i < name.length()) {
      number = 1 + Integer.parseInt(name.substring(i));
      name = name.substring(0, i);
    }
    if (number < 10)
      name = name + "0";
    name = name + number;
    name = name + "." + ext;
    File ret = new File(f.getParent(), name);
    return ret;
  }

  public static void runAsCmdLine() {
    System.setProperty("java.awt.headless", "true");
  }

  public static IApplication app() {
    return sApplication;
  }

  private static boolean sIsMac;
  private static JFrame sFrame;
  private static IApplication sApplication;
  private static Font sMonotoneFontNormal;
  private static Font sMonotoneFontSmall;

}
