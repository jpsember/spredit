package apputil;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import static com.js.basic.Tools.*;

/*
 [] trouble disabling recent files submenus
 [] support dynamic menu item labels for undo/redo, sensitive to operation to be performed 

 */
public class MyMenuBar {
  public static final int CTRL = (1 << 0);
  public static final int SHIFT = (1 << 3);
  public static final int META = (1 << 2);
  public static final int ALT = (1 << 1);

  /**
   * Kludge to deal with OpenGL window / menu repaint conflict; generate repaint
   * every time a menu is redrawn
   * 
   * @param c
   */
  public static void addRepaintComponent(Component c) {
    redrawOpenGLComponent = c;
  }

  private static Component redrawOpenGLComponent;

  private static class RecentFilesMenuItem extends JMenuItem {
    public RecentFilesMenuItem(File file, String label) {
      super(label);
      this.file = file;
    }

    private File file;

    public File file() {
      return file;
    }
  }

  public static void updateRecentFilesFor(JMenuItem recentFilesList,
      RecentFiles recentFiles) {
    if (recentFilesList != null) {
      RecentFilesMenu rm = (RecentFilesMenu) recentFilesList;
      rm.rf = recentFiles;
    }
  }

  private static class RecentFilesMenu extends JMenu implements MenuListener,
      ActionListener {
    private static final boolean db = false;

    public RecentFilesMenu(String title, RecentFiles rf, ItemHandler evtHandler) {
      super(title);
      this.rf = rf;
      this.evtHandler = evtHandler;
      this.addMenuListener(this);
    }

    public boolean isEnabled() {
      if (rf == null)
        return false;
      int size = rf.size();
      if (rf.getCurrentFile() != null)
        size--;
      return size > 0;
    }

    @Override
    public void actionPerformed(ActionEvent arg) {
      RecentFilesMenuItem item = (RecentFilesMenuItem) arg.getSource();
      rf.setCurrentFile(item.file());
      evtHandler.go();
    }

    private ItemHandler evtHandler;
    private RecentFiles rf;

    @Override
    public void menuCanceled(MenuEvent arg0) {
      if (db)
        pr("recent files, cancelled");
    }

    @Override
    public void menuDeselected(MenuEvent arg0) {
      if (db)
        pr("recent files, deselected");

    }

    @Override
    public void menuSelected(MenuEvent arg0) {
      if (db)
        pr("recent files, selected; rf=\n" + rf);
      this.removeAll();

      if (rf != null) {

        for (int i = 0; i < rf.size(); i++) {
          File f = rf.get(i);
          if (rf.getCurrentFile() == f) {
            continue;
          }
          String s = new RelPath(rf.getProjectBase(), f).display(); // RelPath.toString(rf.getProjectBase(),
                                                                    // f);
          JMenuItem item = new RecentFilesMenuItem(f, s);
          this.add(item);
          item.addActionListener(this);
          if (db)
            pr(" adding file " + f);

        }
      }
    }
  }

  public MyMenuBar(JFrame frame) {
    mbar = new JMenuBar();
    frame.setJMenuBar(mbar);

  }

  public void addMenu(String name) {
    addMenu(name, null);
  }

  public void addMenu(String name, MenuHandler handler) {

    menu = new MyMenu(name, handler);
    sepPending = false;
    itemsAdded = 0;
    mbar.add(menu);

    menu.addMenuListener(

    new MenuListener() {
      final boolean db = false;

      @Override
      public void menuCanceled(MenuEvent ev) {
        JMenu m = (JMenu) ev.getSource();
        if (db)
          pr("cancelled: " + m.getText());
        enableItems(m, false);
      }

      @Override
      public void menuDeselected(MenuEvent ev) {
        JMenu m = (JMenu) ev.getSource();
        if (db)
          pr("deselected: " + m.getText());
        enableItems(m, false);
      }

      @Override
      public void menuSelected(MenuEvent ev) {
        JMenu m = (JMenu) ev.getSource();
        if (db)
          pr("selected: " + m.getText());
        enableItems(m, true);

      }
    });
  }

  private static void enableItems(JMenu m, boolean showingMenu) {

    final boolean db = false;

    if (db)
      pr("MyMenuBar.enableItems, showing=" + showingMenu);

    for (int i = 0; i < m.getItemCount(); i++) {
      JMenuItem item = m.getItem(i);
      if (db)
        pr(" item #" + i);

      if (item instanceof MyMenuItem) {
        MyMenuItem it2 = (MyMenuItem) item;
        boolean enable = true;
        if (showingMenu) {
          enable = it2.menu.handler.isEnabled() && it2.handler.isEnabled();
        }
        it2.setEnabled(enable);
      } else if (item instanceof RecentFilesMenu) {
        RecentFilesMenu rm = (RecentFilesMenu) item;
        boolean enable = true;
        if (showingMenu) {
          enable = rm.isEnabled();
        }
        rm.setEnabled(enable);
        if (db)
          pr(" rm enabled=" + rm.isEnabled());

      }
    }
  }

  public JMenuItem addRecentFilesList(String name, RecentFiles rf,
      ItemHandler evtHandler) {

    if (sepPending) {
      menu.addSeparator();
      sepPending = false;
    }
    itemsAdded++;

    JMenuItem item = new RecentFilesMenu(name, rf, evtHandler);
    menu.add(item);
    return item;
  }

  public JMenuItem addItem(String name, int accelKey, int accelFlags,
      ItemHandler evtHandler) {

    if (sepPending) {
      menu.addSeparator();
      sepPending = false;
    }
    itemsAdded++;

    MyMenuItem m = new MyMenuItem(name, evtHandler, menu);

    if (name.equals("Open"))
      m.setEnabled(false);

    if (accelKey != 0) {
      int k = 0;
      for (int i = 0; i < 4; i++) {
        if (0 != (accelFlags & (1 << i)))
          k |= masks[i];
      }
      m.setAccelerator(KeyStroke.getKeyStroke(accelKey, k));
    }
    menu.add(m);
    return m;
  }

  public void addSeparator() {
    if (itemsAdded > 0)
      sepPending = true;
  }

  private MyMenu menu;
  private JMenuBar mbar;
  private boolean sepPending;
  private int itemsAdded;

  private static class MyMenuItem extends JMenuItem {
    public MyMenuItem(String name, ItemHandler h, MyMenu menu) {
      super(name);
      this.menu = menu;
      this.handler = h;
      addActionListener(handler);
    }

    private ItemHandler handler;
    private MyMenu menu;
  }

  private static class MyMenu extends JMenu {

    public void paint(Graphics g) {
      super.paint(g);
      if (redrawOpenGLComponent != null)
        redrawOpenGLComponent.repaint(50);
    }

    public MyMenu(String name, MenuHandler handler) {
      super(name);
      if (handler == null)
        handler = new MenuHandler();
      this.handler = handler;

    }

    private MenuHandler handler;
  }

  // masks for modifier keys; these are lazy-initialized according
  // to operating system
  private static int[] masks;
  static {
    {
      masks = new int[4];

      masks[0] = KeyEvent.CTRL_MASK;
      masks[1] = KeyEvent.ALT_MASK;
      masks[2] = KeyEvent.META_MASK;
      masks[3] = KeyEvent.SHIFT_MASK;

      int j = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
      for (int k = 1; k < masks.length; k++) {
        if (masks[k] == j) {
          int tmp = masks[0];
          masks[0] = j;
          masks[k] = tmp;
          break;
        }
      }
    }
  }

}
