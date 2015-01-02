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

  private static Enableable ALWAYS_ENABLED_HANDLER = new Enableable() {
    @Override
    public boolean shouldBeEnabled() {
      return true;
    }
  };

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
      this.mFile = file;
    }

    public File file() {
      return mFile;
    }

    private File mFile;
  }

  private static class RecentFilesMenu extends JMenu implements MenuListener,
      ActionListener, Enableable, RecentFiles.Listener {

    public RecentFilesMenu(String title, RecentFiles rf,
        ActionHandler evtHandler) {
      super(title);
      mRecentFiles = rf;
      mRecentFiles.addListener(this);
      mItemHandler = evtHandler;
      addMenuListener(this);
    }

    @Override
    public boolean shouldBeEnabled() {
      if (mRecentFiles == null)
        return false;
      return !mRecentFiles.getList(true).isEmpty();
    }

    @Override
    public void actionPerformed(ActionEvent arg) {
      RecentFilesMenuItem item = (RecentFilesMenuItem) arg.getSource();
      mRecentFiles.setCurrentFile(item.file());
      mItemHandler.go();
    }

    @Override
    public void menuCanceled(MenuEvent arg0) {
    }

    @Override
    public void menuDeselected(MenuEvent arg0) {
    }

    @Override
    public void menuSelected(MenuEvent arg0) {
      rebuild();
    }

    @Override
    public void mostRecentFileChanged(RecentFiles recentFiles) {
      pr("most recent files changing to " + recentFiles.getMostRecentFile());
      rebuild();
    }

    private void rebuild() {
      RecentFiles r = mRecentFiles.getAlias();
      if (db)
        pr("Rebuilding recent files " + nameOf(r) + " (unaliased "
            + nameOf(mRecentFiles) + ")");
      removeAll();
      for (File f : r.getList(true)) {
        String s = f.getPath();
        if (db)
          pr(" file=" + f + "\n  withinDirectory " + r.getRootDirectory()
              + " = " + s);
        JMenuItem item = new RecentFilesMenuItem(f, s);
        this.add(item);
        item.addActionListener(this);
      }
    }

    private ActionHandler mItemHandler;
    private RecentFiles mRecentFiles;
  }

  public MyMenuBar(JFrame frame) {
    mbar = new JMenuBar();
    frame.setJMenuBar(mbar);
  }

  public void addMenu(String name) {
    addMenu(name, null);
  }

  public void addMenu(String name, Enableable handler) {

    menu = new Menu(name, handler);
    sepPending = false;
    itemsAdded = 0;
    mbar.add(menu);

    menu.addMenuListener(new MenuListener() {
      @Override
      public void menuCanceled(MenuEvent ev) {
        JMenu m = (JMenu) ev.getSource();
        enableItems(m, false);
      }

      @Override
      public void menuDeselected(MenuEvent ev) {
        JMenu m = (JMenu) ev.getSource();
        enableItems(m, false);
      }

      @Override
      public void menuSelected(MenuEvent ev) {
        JMenu m = (JMenu) ev.getSource();
        enableItems(m, true);
      }
    });
  }

  private static void enableItems(JMenu m, boolean showingMenu) {
    for (int i = 0; i < m.getItemCount(); i++) {
      JMenuItem item = m.getItem(i);
      if (item == null)
        continue;

      if (item instanceof MenuItem) {
        // If the menu isn't showing, ALWAYS enable the items.
        // If user selects them via shortcut key, we'll perform an additional
        // call to shouldBeEnabled() before acting on them.
        MenuItem ourMenuItem = (MenuItem) item;
        ourMenuItem.setEnabled(!showingMenu || ourMenuItem.shouldBeEnabled());
      } else if (item instanceof RecentFilesMenu) {
        RecentFilesMenu rm = (RecentFilesMenu) item;
        boolean enable = true;
        if (showingMenu) {
          enable = rm.shouldBeEnabled();
        }
        rm.setEnabled(enable);
      }
    }
  }

  public JMenuItem addRecentFilesList(String name, RecentFiles rf,
      ActionHandler evtHandler) {

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
      ActionHandler evtHandler) {

    if (sepPending) {
      menu.addSeparator();
      sepPending = false;
    }
    itemsAdded++;

    MenuItem m = new MenuItem(name, evtHandler, menu);

    warning("why this special case?");
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

  private Menu menu;
  private JMenuBar mbar;
  private boolean sepPending;
  private int itemsAdded;

  private static class MenuItem extends JMenuItem implements Enableable {

    public MenuItem(String name, ActionHandler itemHandler, Menu containingMenu) {
      super(name);
      ASSERT(containingMenu != null);
      mContainingMenu = containingMenu;
      mItemHandler = itemHandler;

      // In case user selects menu item using its shortcut key,
      // we need to have the ActionListener verify that the
      // item (and its containing menu) are both enabled before
      // acting upon it.

      addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
          if (shouldBeEnabled())
            handler().actionPerformed(event);
        }
      });
    }

    public ActionHandler handler() {
      return mItemHandler;
    }

    @Override
    public boolean shouldBeEnabled() {
      // examine both the containing menu's enabled state and the item handler
      // interface
      ASSERT(mContainingMenu != null); // verify not partially constructed
      boolean enabled = mContainingMenu.handler().shouldBeEnabled()
          && mItemHandler.shouldBeEnabled();
      return enabled;
    }

    private ActionHandler mItemHandler;
    private Menu mContainingMenu;
  }

  private static class Menu extends JMenu {

    public void paint(Graphics g) {
      super.paint(g);
      if (redrawOpenGLComponent != null)
        redrawOpenGLComponent.repaint(50);
    }

    public Menu(String name, Enableable handler) {
      super(name);
      if (handler == null)
        handler = ALWAYS_ENABLED_HANDLER;
      this.mHandler = handler;
    }

    public Enableable handler() {
      return mHandler;
    }

    private Enableable mHandler;
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
