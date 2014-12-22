package apputil;

import java.awt.event.*;

public abstract class ItemHandler implements ActionListener {

  public void actionPerformed(ActionEvent evt) {
    if (isEnabled())
      go();
  }

  public abstract void go();

  /**
   * Determine if this menu item should be enabled at present time
   * @return true if so
   */
  public boolean isEnabled() {
    return true;
  }
}
