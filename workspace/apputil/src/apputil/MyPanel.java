package apputil;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import static com.js.basic.Tools.*;

/**
 * Panel that supports my horizontal / vertical stacking, stretching components pattern
 */
public class MyPanel extends JPanel implements IComponentHints {

  public static JComponent insetBy(JComponent c, int nPix) {
    c.setBorder(new EmptyBorder(nPix, nPix, nPix, nPix));
    return c;
  }

  private static IComponentHints hintsFor(Component c) {
    if (c instanceof IComponentHints) {
      return (IComponentHints) c;
    }
    return DEFAULT_HINTS;
  }

  private static final IComponentHints DEFAULT_HINTS = new IComponentHints() {
    @Override
    public boolean stretch(int axis) {
      return false;
    }
  };

  private static class StretchablePanel extends JPanel implements
      IComponentHints {
    public StretchablePanel(int width, int height) {
      this.hs = width == 0;
      this.vs = height == 0;
      Dimension d = new Dimension(Math.max(1, width), Math.max(1, height));
      setMinimumSize(d);
      setPreferredSize(d);
      // setBackground(Color.RED);
    }

    private boolean hs, vs;
    @Override
    public boolean stretch(int axis) {
      return axis == 0 ? hs : vs;
    }
  }
  /**
   * Construct a spacer component that can stretch in either dimension, but 
   * otherwise has a single pixel size
   * @return
   */
  public static Component stretch() {
    return new StretchablePanel(0, 0);
  }

  /**
   * Construct a spacer component that has a fixed height, and can stretch horizontally
   * @param pix
   * @return
   */
  public static Component vSpace(int pix) {
    return new StretchablePanel(0, pix);
  }

  /**
   * Construct a spacer component that has a fixed width, and can stretch vertically
   * @param pix
   * @return
   */
  public static Component hSpace(int pix) {
    return new StretchablePanel(pix, 0);
  }

  public static JPanel vertPanel() {
    return new MyPanel(false);
  }
  public static JPanel horzPanel() {
    return new MyPanel(true);
  }
  public MyPanel(boolean horzLayout) {
    this.horzLayout = horzLayout;
    this.setLayout(new GridBagLayout());
  }
  private int nChildren;

  public Component add(Component comp) {
    final boolean db = false;
    IComponentHints h = hintsFor(comp);

    GridBagConstraints c = new GridBagConstraints();
    if (horzLayout) {
      c.gridx = nChildren;
    } else {
      c.gridy = nChildren;
    }
    nChildren++;
    c.gridwidth = 1;
    c.gridheight = 1;

    // determine fill value
    {
      c.fill = GridBagConstraints.NONE;
      if (h.stretch(0)) {
        c.fill = (h.stretch(1) ? GridBagConstraints.BOTH
            : GridBagConstraints.HORIZONTAL);
      } else if (h.stretch(1))
        c.fill = GridBagConstraints.VERTICAL;
    }

    if (h.stretch(0))
      c.weightx = 1.0;
    if (h.stretch(1))
      c.weighty = 1.0;

    if (db)
      pr("MyComp.add, " + h + ", fill=" + c.fill);

    {
      int ourAxis = horzLayout ? 0 : 1;

      // Hints hUs = hintsFor(this);

      // if any of the components added to this panel are stretchable along our axis,
      // make us stretchable along that axis as well.

      boolean stretchable = h.stretch(ourAxis);
      if (stretchable) {
        setStretch(ourAxis, true);
      }

      // if ALL of the components added to this panel are stretchable along the axis perpendicular to ours,
      // make our perpendicular axis stretchable
      if (!h.stretch(1 - ourAxis))
        perpNonStretchFound = true;
      if (perpNonStretchFound)
        setStretch(1 - ourAxis, false);
      else
        setStretch(1 - ourAxis, true);
    }

    super.add(comp, c);
    return comp;
  }
  private void setStretch(int axis, boolean f) {
    stretchFlags[axis] = f;
  }
  @Override
  public boolean stretch(int axis) {
    return stretchFlags[axis];
  }

  private boolean[] stretchFlags = new boolean[2];
  private boolean horzLayout;
  private boolean perpNonStretchFound;
}
