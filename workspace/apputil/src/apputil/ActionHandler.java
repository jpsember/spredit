package apputil;

import java.awt.event.*;

import com.js.editor.Enableable;

/**
 * Abstract class that implements ActionListener and Enableable, and only
 * performs the action after determining if it ought to be enabled
 */
public abstract class ActionHandler implements Enableable, ActionListener {

  @Override
  public void actionPerformed(ActionEvent evt) {
    if (shouldBeEnabled())
      go();
  }

  /**
   * Default implementation; always returns true
   */
  @Override
  public boolean shouldBeEnabled() {
    return true;
  }

  /**
   * Process the action
   */
  public abstract void go();

}
