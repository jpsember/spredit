package apputil;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.js.editor.Enableable;
import com.js.editor.UserEventManager;
import com.js.editor.UserOperation;

import static com.js.basic.Tools.*;

public class MyMenuBar {

  public static final int CTRL = (1 << 0);
  public static final int ALT = (1 << 1);
  public static final int META = (1 << 2);
  public static final int SHIFT = (1 << 3);

  private static final int TOTAL_MODIFIER_KEY_FLAGS = 4;

  /**
   * Constructor
   * 
   * @param frame
   *          frame associated with menu bar
   * @deprecated
   */
  public MyMenuBar(JFrame frame) {
    mMenuBar = new JMenuBar();
    frame.setJMenuBar(mMenuBar);
  }

  /**
   * Constructor
   * 
   * @param frame
   *          frame associated with menu bar
   */
  public MyMenuBar(JFrame frame, UserEventManager eventManager) {
    mMenuBar = new JMenuBar();
    mEventManager = eventManager;
    frame.setJMenuBar(mMenuBar);
  }

  /**
   * Start a new 'current' menu, and add it to the menu bar
   * 
   * @param title
   *          title of menu
   * @param handler
   *          optional Enableable handler
   */
  public void addMenu(String title, Enableable handler) {

    mMenu = new Menu(title, mEventManager);
    mMenu.setEnableableDelegate(handler);
    mSeparatorPending = false;
    mMenuBar.add(mMenu);

    mMenu.addMenuListener(new MenuListener() {
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

  public void addAppMenu() {
    if (AppTools.isMac()) {
      MacUtils.setQuitHandler();
    } else {
      addMenu(AppTools.app().getName());
      addItem("Quit", KeyEvent.VK_Q, CTRL, new ActionHandler() {
        public void go() {
          if (AppTools.app().exitProgram())
            System.exit(0);
        }
      });
    }
  }

  /**
   * Start a new 'current' menu
   * 
   * @param title
   *          title of menu
   */
  public void addMenu(String name) {
    addMenu(name, null);
  }

  /**
   * Add an item to the current menu
   */
  public void addItem(JMenuItem item) {
    if (mSeparatorPending) {
      mMenu.addSeparator();
      mSeparatorPending = false;
    }
    mMenu.add(item);
  }

  /**
   * Add an item to the current menu, optionally with a keyboard accelerator
   * 
   * @deprecated refactor to use UserOperation instead
   */
  public JMenuItem addItem(String name, int accelKey, int accelFlags,
      ActionHandler evtHandler) {
    MenuItem m = new MenuItem(name, evtHandler, mMenu);
    if (accelKey != 0) {
      int k = 0;
      for (int i = 0; i < TOTAL_MODIFIER_KEY_FLAGS; i++) {
        if (0 != (accelFlags & (1 << i)))
          k |= modifierKeyMasks()[i];
      }
      m.setAccelerator(KeyStroke.getKeyStroke(accelKey, k));
    }
    addItem(m);
    return m;
  }

  /**
   * Add an item to the current menu, optionally with a keyboard accelerator
   */
  public JMenuItem addItem(String name, int accelKey, int accelFlags,
      UserOperation.InstantOperation operation) {
    ASSERT(mEventManager != null, "no event manager defined");
    MenuItem m = new MenuItem(name, operation, mMenu);
    if (accelKey != 0) {
      int k = 0;
      for (int i = 0; i < TOTAL_MODIFIER_KEY_FLAGS; i++) {
        if (0 != (accelFlags & (1 << i)))
          k |= modifierKeyMasks()[i];
      }
      m.setAccelerator(KeyStroke.getKeyStroke(accelKey, k));
    }
    addItem(m);
    return m;
  }

  /**
   * Insert a separator line before adding next item to the current menu
   */
  public void addSeparator() {
    if (mMenu.getItemCount() > 0)
      mSeparatorPending = true;
  }

  private static void enableItems(JMenu m, boolean showingMenu) {
    for (int i = 0; i < m.getItemCount(); i++) {
      JMenuItem item = m.getItem(i);
      if (item == null)
        continue;

      if (item instanceof Enableable) {
        Enableable enableable = (Enableable) item;
        // If the menu isn't showing, ALWAYS enable the items.
        // If user selects them via shortcut key, we'll perform an additional
        // call to shouldBeEnabled() before acting on them.
        item.setEnabled(!showingMenu
            || (enableable.shouldBeEnabled() && !menusAreDisabled()));
      }
    }
  }

  /**
   * Get masks for modifier keys. These are OS-specific
   */
  private static int[] modifierKeyMasks() {
    if (sModifierKeyMasks == null) {
      int[] m = new int[TOTAL_MODIFIER_KEY_FLAGS];
      sModifierKeyMasks = m;

      m[0] = KeyEvent.CTRL_MASK;
      m[1] = KeyEvent.ALT_MASK;
      m[2] = KeyEvent.META_MASK;
      m[3] = KeyEvent.SHIFT_MASK;

      int j = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
      for (int k = 1; k < m.length; k++) {
        if (m[k] == j) {
          int tmp = m[0];
          m[0] = j;
          m[k] = tmp;
          break;
        }
      }
    }
    return sModifierKeyMasks;
  }

  private static int[] sModifierKeyMasks;

  private Menu mMenu;
  private JMenuBar mMenuBar;
  private UserEventManager mEventManager;
  private boolean mSeparatorPending;

  /**
   * Subclass of JMenu designed for use with MyMenuBar; supports optional
   * Enableable delegate
   */
  public static class Menu extends JMenu {

    public Menu(String name, UserEventManager eventManager) {
      super(name);
      mEventManager = eventManager;
    }

    /**
     * Set (optional) Enableable delegate; if null, assumes menu is always
     * enableable
     */
    public void setEnableableDelegate(Enableable e) {
      mEnableableDelegate = e;
    }

    public Enableable enableableDelegate() {
      if (mEnableableDelegate == null)
        return ALWAYS_ENABLED_HANDLER;
      return mEnableableDelegate;
    }

    public UserEventManager getEventManager() {
      if (mEventManager == null)
        throw new UnsupportedOperationException();
      return mEventManager;
    }

    private static Enableable ALWAYS_ENABLED_HANDLER = new Enableable() {
      @Override
      public boolean shouldBeEnabled() {
        return true;
      }
    };

    private Enableable mEnableableDelegate;
    private UserEventManager mEventManager;
  }

  /**
   * Subclass of JMenuItem designed for use with MyMenuBar
   */
  private static class MenuItem extends JMenuItem implements Enableable {

    public MenuItem(String name, UserOperation.InstantOperation operation,
        Menu containingMenu) {
      super(name);
      ASSERT(containingMenu != null);
      mContainingMenu = containingMenu;
      mOperation = operation;

      // In case user selects menu item using its shortcut key,
      // we need to have the ActionListener verify that the
      // item (and its containing menu) are both enabled before
      // acting upon it.

      addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
          pr("actionPerformed for item " + getText() + "; shouldBeEnabled "
              + shouldBeEnabled());
          if (shouldBeEnabled() && !menusAreDisabled()) {
            pr(" performing " + nameOf(mOperation));
            mContainingMenu.getEventManager().perform(mOperation);
          }
        }
      });
    }

    /**
     * @deprecated refactor to use UserHandler instead
     */
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
          if (shouldBeEnabled() && !menusAreDisabled())
            handler().actionPerformed(event);
        }
      });
    }

    private ActionHandler handler() {
      return mItemHandler;
    }

    @Override
    public boolean shouldBeEnabled() {
      // examine both the containing menu's enabled state and the item handler
      // interface
      boolean enabled = mContainingMenu.enableableDelegate().shouldBeEnabled();
      if (!enabled)
        return false;
      if (mItemHandler != null)
        return mItemHandler.shouldBeEnabled();
      return mOperation.shouldBeEnabled();
    }

    private ActionHandler mItemHandler;
    private Menu mContainingMenu;
    private UserOperation.InstantOperation mOperation;
  }

  private static boolean menusAreDisabled() {
    return sAllMenusDisabled;
  }

  /**
   * Set enable state for our menubars. We can temporarily disable them, e.g.,
   * when displaying modal dialogs such as file choosers
   */
  static void enableMenuBar(boolean f) {
    sAllMenusDisabled = !f;
  }

  private static boolean sAllMenusDisabled;

}
