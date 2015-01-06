package com.js.scredit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import apputil.AppTools;
import apputil.MyPanel;

import com.js.basic.Files;

class InfoPanel extends MyPanel implements ActionListener {

  public InfoPanel() {
    super(true);

    setBorder(BorderFactory.createRaisedBevelBorder());

    {
      JPanel c1 = vertPanel();

      {
        JPanel c = horzPanel();

        c.add(new JLabel("Script:"));
        slotNumber = text(5, false);
        slotNumber.setHorizontalAlignment(SwingConstants.CENTER);
        c.add(slotNumber);
        mFilePath = text(24, false);
        c.add(mFilePath);
        c.add(stretch());

        c1.add(c);
      }
      {
        JPanel c = horzPanel();
        bgnd = new JCheckBox("Bgnd");
        bgnd.addActionListener(this);
        bgnd.setToolTipText("Plot this, and preceding scripts, as background");
        c.add(bgnd);

        c.add(origin);
        origin.addActionListener(this);
        origin.setToolTipText("Plot origin crosshairs");

        c.add(faded);
        faded.addActionListener(this);
        faded.setToolTipText("Plot faded previous foreground script");

        c.add(Grid.intGridActiveCtrl());
        c.add(new JLabel("Size:"));
        c.add(Grid.gridSizeCtrl());

        c.add(stretch());

        c1.add(c);
      }
      {
        JPanel c = horzPanel();

        msg = text(50, true);
        c.add(msg);

        // infoLabel = new JLabel();
        // infoLabel.setFont(AppTools.getSmallFixedWidthFont());
        // infoLabel.setPreferredSize(new Dimension(30, infoLabel.getFont()
        // .getSize()));
        // size(infoLabel, 40);
        // c.add(infoLabel);
        // infoLabel.setText("har");
        c.add(stretch());
        c1.add(c);
      }

      c1.add(stretch());

      this.add(c1);
    }
    this.add(hSpace(8));

    this.add(stretch());
    // refresh();
  }

  private static JTextField text(int chars, boolean small) {
    JTextField tf = new JTextField();
    tf.setEditable(false);
    tf.setFont(small ? AppTools.getSmallFixedWidthFont() : AppTools
        .getFixedWidthFont());
    size(tf, chars);
    return tf;
  }

  private static void size(Component c, int chars) {
    Dimension d = new Dimension(chars * 13, Short.MAX_VALUE);
    Dimension cs = c.getPreferredSize();
    c.setPreferredSize(new Dimension(d.width, cs.height));
  }

  public void refresh(ScriptEditor editor, ScriptProject project,
      ScriptSet scriptSet) {

    layers = scriptSet;
    if (project != null) {

      {
        if (layers.size() > 1) {
          upd(slotNumber, (layers.getCursor() + 1) + "/" + (layers.size()));
        } else
          upd(slotNumber, null);
      }

      StringBuilder sb = new StringBuilder();
      sb.append(editor.modified() ? "*" : " ");

      Script script = editor.getScript();
      if (script.getFile() != null)
        sb.append(Files.fileWithinDirectory(script.getFile(),
            project.directory()));
      upd(mFilePath, sb);
      displayProjectPath(project.file().getName());
      upd(bgnd, true, layers.isBackground());

    } else {
      upd(slotNumber, null);
      upd(mFilePath, null);
      displayProjectPath(null);
      upd(bgnd, false, false);
    }
  }

  private void displayProjectPath(String s) {
    StringBuilder t = new StringBuilder("ScrEdit");
    if (s != null) {
      t.append(" (Project:");
      t.append(s);
      t.append(")");
    }
    String nt = t.toString();
    if (!nt.equals(prevTitle)) {
      prevTitle = nt;
      AppTools.frame().setTitle(prevTitle);
    }
  }

  private static String prevTitle;

  private void upd(JCheckBox cb, boolean enabled, boolean value) {
    cb.setSelected(enabled && value);
    cb.setEnabled(enabled);
  }

  private void upd(JTextField tf, Object content) {
    if (content == null)
      content = "";
    String cs = content.toString();
    String prev = tf.getText();
    if (!prev.equals(cs)) {
      tf.setText(cs);
    }
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (!ScriptEditor.isProjectOpen())
      return;
    layers.setBackground(bgnd.isSelected());
    ScriptEditor.repaint();
  }

  public boolean isFaded() {
    return faded.isSelected();
  }

  public void setFaded(boolean f) {
    faded.setSelected(f);
  }

  public boolean isOriginShowing() {
    return origin.isSelected();
  }

  public void setOriginShowing(boolean f) {
    origin.setSelected(f);
  }

  public void setMessage(String text) {
    if (text == null)
      text = "";
    msg.setText(text);
  }

  private JToggleButton origin = new JCheckBox("Origin");
  private JToggleButton faded = new JCheckBox("Onion skin");

  private JCheckBox bgnd;
  private JTextField slotNumber;
  private JTextField mFilePath;
  private JTextField msg;
  private ScriptSet layers;
}
