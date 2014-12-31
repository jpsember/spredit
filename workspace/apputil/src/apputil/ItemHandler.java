package apputil;

import java.awt.event.*;

public abstract class ItemHandler implements ItemEnabled, ActionListener {

  public void actionPerformed(ActionEvent evt) {
    if (shouldBeEnabled())
      go();
  }

  public abstract void go();

  @Override
  public boolean shouldBeEnabled() {
    return true;
  }
}
