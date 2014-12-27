package apputil;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import scanning.*;
import streams.*;
import static com.js.basic.Tools.*;

public class AppTools {

  private static final boolean SWING_FC = true;

  private static boolean isMac;

  public static void setAppName(String app) {
    String lcOSName = System.getProperty("os.name").toLowerCase();
    isMac = lcOSName.startsWith("mac os x");
    if (isMac) {
      // if (true) {
      // warn("not using screen menubar");
      // } else
      MacUtils.useScreenMenuBar(app);
    }
  }

  public static boolean isMac() {
    return isMac;
  }

  public static void stringToLabel(String s, StringBuilder sb) {
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\n') {
        sb.append("\\n");
      } else if (c < 0x20 || c > 0x7f || c == '"' || c == '\\') {
        sb.append("\\u");
        TextScanner.toHex(sb, c, 4);
      } else
        sb.append(c);
    }
    sb.append('"');
  }

  private static Font monoFont;

  public static Font getFixedWidthFont() {
    if (monoFont == null) {
      monoFont = new Font("Monaco", Font.PLAIN, 16);
      if (false) {
        GraphicsEnvironment ge;
        ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Get the font names from the graphics environment
        String[] fontNames = ge.getAvailableFontFamilyNames();

        for (int index = 0; index < fontNames.length; index++) {
          System.out.println(fontNames[index]);
        }
      }
    }
    return monoFont;

  }

  private static Font monoFontSmall;

  public static Font getSmallFixedWidthFont() {
    if (monoFontSmall == null)
      monoFontSmall = new Font("Monaco", Font.PLAIN, 12);
    return monoFontSmall;
  }

  public static String stringToLabel(String s) {
    StringBuilder sb = new StringBuilder();
    stringToLabel(s, sb);
    return sb.toString();
  }

  @Deprecated
  public static String labelToString(String s) {
    StringBuilder sb = new StringBuilder();
    boolean problem = s.length() < 2 || s.charAt(0) != '"'
        || s.charAt(s.length() - 1) != '"';

    for (int i = 1; i < s.length() - 1; i++) {
      char c = s.charAt(i);
      if (c == '\\') {
        i++;
        if (i == s.length() - 1) {
          problem = true;
          break;
        }
        c = s.charAt(i);
        if (c == 'n') {
          c = '\n';
        } else if (c == 'u') {
          i++;
          if (i + 4 > s.length()) {
            problem = true;
            break;
          }
          c = (char) TextScanner.parseHex(s, i, 4);
          i += 4 - 1;
        } else {
          c = s.charAt(i);
          // problem = true;
          // break;
        }
      }
      sb.append(c);
    }
    if (problem)
      throw new IllegalArgumentException(s);

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

  private static IApplication theApp;

  // public static IApplication app() {return theApp;}
  public static void setFrame(JFrame f, IApplication app) {
    frame = f;
    theApp = app;
    if (!isMac()) {
      // // From
      // http://java.sun.com/products/jfc/tsc/articles/mixing/#issues:
      JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }

    if (frame != null && theApp != null) {
      // disable the close button, since we want to verify close
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          if (theApp.exitProgram()) {
            frame.setVisible(false);
            frame.dispose();
          }
        }
      });

    }

  }

  public static JFrame frame() {
    return frame;
  }

  private static JFrame frame;

  public static File chooseFileToSave(String prompt, MyFileFilter fileFilter,
      File defaultFile) {
    final boolean db = false;
    enableMenuBar(false);

    if (db)
      pr("chooseFileToSave\n " + prompt + "\n filter:" + fileFilter
          + "\n defaultFile:" + defaultFile);

    File f = null;
    do {

      if (!SWING_FC && !AppTools.isMac()) {

        FileDialog d = new FileDialog(frame, prompt, FileDialog.SAVE);

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
    JMenuBar mb = frame.getJMenuBar();
    if (mb != null)
      mb.setEnabled(f);
  }

  public static File chooseFileToOpen(String prompt, MyFileFilter fileFilter,
      File defaultFile) {

    final boolean db = false;

    enableMenuBar(false);

    File f = null;
    do {
      if (!SWING_FC && !AppTools.isMac()) {
        //
        // if (!USE_SWING_FILECHOOSERS) {
        FileDialog d = new FileDialog(frame, prompt, FileDialog.LOAD);
        if (defaultFile != null) {
          d.setDirectory(defaultFile.getParent());
          d.setFile(defaultFile.getName());
        }
        d.setFilenameFilter(fileFilter);
        if (db)
          pr("set filefilter to " + fileFilter);

        d.setVisible(true);

        // do {
        String dir = d.getDirectory();
        String s = d.getFile();
        if (db)
          pr("getFile is " + dir + ":" + s);

        if (dir == null || s == null)
          break;
        f = new File(dir, s);
      } else {

        JFileChooser fc = new JFileChooser(defaultFile);
        fc.setFileFilter(fileFilter);
        int result = fc.showOpenDialog(frame());

        if (result != JFileChooser.APPROVE_OPTION)
          break;
        f = fc.getSelectedFile();
      }

      if (db)
        pr(" file = " + f);

      if (!f.exists() || !fileFilter.accept(f)) {
        f = null;
        break;
      }
      if (db)
        pr("returning " + f);

    } while (false);
    enableMenuBar(true);

    return f;
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

  public static File getDefaultsPath(String appName) {

    File defaultsPath;

    File userHome = Streams.homeDirectory();
    String osName = System.getProperty("os.name");
    boolean isWindows = osName.startsWith("Windows");
    defaultsPath = new File(userHome, isWindows ? appName + "_defaults.txt"
        : "." + appName + "_defaults");

    return defaultsPath;
  }

  public static void runAsCmdLine() {
    System.setProperty("java.awt.headless", "true");
  }

}
