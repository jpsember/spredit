package com.js.scredit;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import com.js.geometry.*;

import apputil.*;
import tex.*;
import static com.js.basic.Tools.*;

/*
   [] weird scroll problem; images are smudged
 */
public class AtlasPanel extends JPanel implements ListSelectionListener {

  /**
  * Constructor
  */
  public AtlasPanel() {
    super(new BorderLayout());

    add(initCtrlPanel(), BorderLayout.NORTH);

    listModel = new DefaultListModel();

    //Create the list and put it in a scroll pane.
    list = new JList(listModel);
    list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    MyCellRenderer r = new MyCellRenderer();
    list.setCellRenderer(r);
    list.setVisibleRowCount(-1);
    list.setFocusable(false);

    list.setSelectedIndex(0);
    list.addListSelectionListener(this);
    JScrollPane listScrollPane = new JScrollPane(list);

    add(listScrollPane, BorderLayout.CENTER);
  }

  public void setAtlas(ScriptProject project, File f) {
    final boolean db = false;

    if (db)
      pr("AtlasPanel, setAtlas to " + f);

    Atlas a = null;
    try {
      if (f != null)
        a = project.getAtlas(f);

      if (a != atlas) {
        this.atlas = a;
        repopulateList(null);
      }
    } catch (IOException e) {
      AppTools.showError("Problem setting atlas: " + f, e);
    }
  }
  public Atlas atlas() {
    return atlas;
  }
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(420, 40);
  }
  private void repopulateList(File selectedFile) {
    listModel.clear();

    if (atlas != null) {
      int selIndex = -1;

      for (int i = 0; i < atlas.size(); i++) {
        Sprite s = atlas.sprite(i);
        if (s.id().startsWith("_"))
          continue;

        listModel.addElement(new Integer(i));
      }

      if (selIndex < 0 && listModel.size() > 0)
        selIndex = 0;

    }
  }

  //This method is required by ListSelectionListener.
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting() == false) {
      if (list.getSelectedIndex() >= 0) {
        Integer sprInd = ((Integer) list.getSelectedValue()).intValue();
        SpriteObject s = new SpriteObject(atlas, sprInd);
        ScriptEditor.doSetSpritesTo(s);
      }
    }
  }

  private JPanel initCtrlPanel() {
    ctrls = new JPanel();

    ctrls.setLayout(new BoxLayout(ctrls, BoxLayout.LINE_AXIS));
    ctrls.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    return ctrls;

  }

  //Display an icon and a string for each object in the list.
  private class MyCellRenderer extends JPanel implements ListCellRenderer {

    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.

    public Component getListCellRendererComponent(JList list, // the list
        Object value, // value to display
        int index, // cell index
        boolean isSelected, // is the cell selected
        boolean cellHasFocus) // does the cell have focus
    {
      selected = isSelected;
      spriteIndex = ((Integer) value).intValue();
      sprite = atlas.sprite(spriteIndex);
      label.setText(sprite.id());

      imgPanel.setBackground(selected ? Color.blue.brighter() : Color.white);

      return this;
    }

    private JLabel label;
    private ImagePanel imgPanel;
    private Sprite sprite;

    private class ImagePanel extends JPanel {
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle r = g.getClipBounds();

        BufferedImage img = atlas.image();

        Sprite sp = atlas.sprite(spriteIndex);

        IRect cr = new IRect(sp.bounds());
        cr.translate(sp.translate());

        img = SprTools.subImage(img, cr);
        //        // compensate for flipped y axis
        //        cr.y = GLPanel.flipYAxis(img.getHeight(), cr);
        //        img = img.getSubimage(cr.x, cr.y, cr.width, cr.height);

        final int PAD = 2;
        int destW = (r.width - PAD * 2);
        int destH = (r.width - PAD * 2);

        // determine scale factor; we scale it down to fit in our panel
        float scl = Math.min(destW / (float) img.getWidth(), destH
            / (float) img.getHeight());

        float destWidth = img.getWidth() * scl;
        float destHeight = img.getHeight() * scl;
        float destcx = r.x + r.width * .5f;
        float destcy = r.y + r.height * .5f;

        IRect destRect = new IRect(new Rect(destcx - destWidth / 2, destcy
            - destHeight / 2, destWidth, destHeight));

        g.drawImage(img, destRect.x, destRect.y, destRect.endX(),
            destRect.endY(), 0, 0, img.getWidth(), img.getHeight(), null);

      }

    }

    public MyCellRenderer() {
      this.setBorder(BorderFactory.createRaisedBevelBorder());
      label = new JLabel();
      label.setHorizontalAlignment(JLabel.CENTER);
      imgPanel = new ImagePanel();
      this.setLayout(new BorderLayout());
      this.add(imgPanel, BorderLayout.CENTER);
      this.add(label, BorderLayout.SOUTH);
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(100, 120);
    }
    private boolean selected;
    private int spriteIndex;
  }
  public File file() {
    return (atlas == null) ? null : atlas.dataFile();
  }
  private JList list;
  private DefaultListModel listModel;
  private JPanel ctrls;
  private Atlas atlas;
}
