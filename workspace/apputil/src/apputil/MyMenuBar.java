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

  private static ItemEnabled ALWAYS_ENABLED_HANDLER = new ItemEnabled() {
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

  public static void updateRecentFilesFor(JMenuItem recentFilesList,
      RecentFiles recentFiles) {
    if (recentFilesList != null) {
      RecentFilesMenu rm = (RecentFilesMenu) recentFilesList;
      rm.mRecentFiles = recentFiles;
    }
  }

  private static class RecentFilesMenu extends JMenu implements MenuListener,
      ActionListener, ItemEnabled {

    public RecentFilesMenu(String title, RecentFiles rf, ItemHandler evtHandler) {
      super(title);
      this.mRecentFiles = rf;
      this.mItemHandler = evtHandler;
      this.addMenuListener(this);
    }

    @Override
    public boolean shouldBeEnabled() {
      if (mRecentFiles == null)
        return false;
      int size = mRecentFiles.size();
      if (mRecentFiles.getCurrentFile() != null)
        size--;
      return size > 0;
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
      removeAll();
      if (mRecentFiles != null) {
        for (int i = 0; i < mRecentFiles.size(); i++) {
          File f = mRecentFiles.get(i);
          if (mRecentFiles.getCurrentFile() == f)
            continue;
          String s = new RelPath(mRecentFiles.getProjectBase(), f).display();
          JMenuItem item = new RecentFilesMenuItem(f, s);
          this.add(item);
          item.addActionListener(this);
        }
      }
    }

    private ItemHandler mItemHandler;
    private RecentFiles mRecentFiles;

  }

  public MyMenuBar(JFrame frame) {
    mbar = new JMenuBar();
    frame.setJMenuBar(mbar);
  }

  public void addMenu(String name) {
    addMenu(name, null);
  }

  public void addMenu(String name, ItemEnabled handler) {

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

  private static class MenuItem extends JMenuItem implements ItemEnabled {

    public MenuItem(String name, ItemHandler itemHandler, Menu containingMenu) {
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

    public ItemHandler handler() {
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

    private ItemHandler mItemHandler;
    private Menu mContainingMenu;
  }

  private static class Menu extends JMenu {

    public void paint(Graphics g) {
      super.paint(g);
      if (redrawOpenGLComponent != null)
        redrawOpenGLComponent.repaint(50);
    }

    public Menu(String name, ItemEnabled handler) {
      super(name);
      if (handler == null)
        handler = ALWAYS_ENABLED_HANDLER;
      this.mHandler = handler;
    }

    public ItemEnabled handler() {
      return mHandler;
    }

    private ItemEnabled mHandler;
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
