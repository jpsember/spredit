package com.js.spredit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.js.geometry.IPoint;

import apputil.*;
import static com.js.basic.Tools.*;

public class BuildParms {

  public static void showDialog() {
    if (false) {
      OLDshowDialog();
      return;
    }

    MyPanel c1 = new MyPanel(false);
    MyPanel.insetBy(c1, 8);

    MyPanel c;

    c = new MyPanel(true);
    c.add(MyPanel.stretch());
    c.add(new JLabel("fixed size:"));
    c.add(fixedSize);
    c1.add(c);

    c = new MyPanel(true);
    c.add(MyPanel.stretch());
    c.add(new JLabel("width:"));
    c.add(texWidth);
    c1.add(c);

    c = new MyPanel(true);
    c.add(MyPanel.stretch());
    c.add(new JLabel("height:"));
    c.add(texHeight);
    c1.add(c);

    //MyFrame f = 
      new MyFrame("BUILDPARMS", "Atlas Parameters", c1);

  }

  private static void OLDshowDialog() {

    // ------------------------------------------------- OLD
    final JDialog d = new JDialog((Frame) null, false);

    d.setTitle("Atlas Parameters");

    if (true) {
      MyPanel c1 = new MyPanel(false);
      MyPanel.insetBy(c1, 8);

      MyPanel c;
      // ------------------------------------------------- OLD

      c = new MyPanel(true);
      c.add(MyPanel.stretch());
      c.add(new JLabel("fixed size:"));
      c.add(fixedSize);
      c1.add(c);
      // ------------------------------------------------- OLD

      c = new MyPanel(true);
      c.add(MyPanel.stretch());
      c.add(new JLabel("width:"));
      c.add(texWidth);
      c1.add(c);

      c = new MyPanel(true);
      c.add(MyPanel.stretch());
      c.add(new JLabel("height:"));
      c.add(texHeight);
      c1.add(c);
      // ------------------------------------------------- OLD

      c = new MyPanel(true);
      c.add(MyPanel.stretch());
      c.add(ok);
      c1.add(c);

      // only add component to parent when complete
      d.getContentPane().add(c1);

    }
    //    else { // old way
    //      Container c = d.getContentPane();
    //      c.setLayout(new FlowLayout());
    // ------------------------------------------------- OLD
    //
    //      c.add(new JLabel("fixed size:"));
    //      c.add(fixedSize);
    //      c.add(new JLabel("width:"));
    //      c.add(texWidth);
    //      c.add(new JLabel("height:"));
    //      c.add(texHeight);
    //
    //      c.add(ok);
    //
    //      if (false) {
    //        warn("testing new comp stuff");
    //
    //        MyComp p2 = new MyComp(true);
    //
    //        {
    //          MyComp panel = new MyComp(false);
    //
    //          panel.add(MyComp.testComp(40, 220, true, true, Color.ORANGE));
    //          p2.add(panel);
    //        }
    //
    //        p2.add(MyComp.hSpace(22));
    //        {
    //          MyComp panel = new MyComp(false);
    //
    //          panel.add(MyComp.testComp(100, 40, false, false, Color.red));
    //          // panel.add(MyComp.testComp(1, 1, true, true, Color.green));
    //          panel.add(MyComp.testComp(150, 40, true, false, Color.blue));
    //          panel.add(MyComp.stretch());
    //          //  panel.add(MyComp.testComp(1, 1, true, true, Color.green));
    //          panel.add(MyComp.testComp(50, 40, true, false, Color.DARK_GRAY));
    //          //panel.validate();
    //          p2.add(panel);
    //        }
    //
    //        // panel.setPreferredSize(new Dimension(200, 200));
    //        c.add(p2);
    //
    //      }
    //    }
    ok.addActionListener(new ActionListener() {
      // ------------------------------------------------- OLD

      @Override
      public void actionPerformed(ActionEvent arg0) {
        d.setVisible(false);
      }
    });

    // ------------------------------------------------- OLD
    //    if (false)
    //      d.setPreferredSize(new Dimension(400, 400));

    // set min width to accomodate title
    d.setMinimumSize(new Dimension(300, 1));
    d.setResizable(false);

    d.pack();
    // ------------------------------------------------- OLD

    d.setVisible(true);
  }

  private static JButton ok = new JButton("Ok");

  private static JSpinner texWidth, texHeight;
  private static JCheckBox fixedSize;

  static {
    texWidth = new JSpinner();
    texWidth.setValue(new Integer(1024));
    texHeight = new JSpinner();
    texHeight.setValue(new Integer(1024));
    fixedSize = new JCheckBox();
  }

  private static int iv(JSpinner v) {
    Object obj = v.getValue();
    return ((Integer) obj).intValue();
  }

  /**
   * Get texture page size
   * @return page size, or null if not a fixed size
   */
  public static IPoint texSize() {
    return fixedSize.isSelected() ? new IPoint(iv(texWidth), iv(texHeight))
        : null;
  }

  /**
   * @param s
   */
  public static void setTexSize(IPoint s) {
    texWidth.setValue(new Integer(s.x));
    texHeight.setValue(new Integer(s.y));
  }

  public static void parseFrom(JSONObject map) throws JSONException {
    if (map == null)
      map = new JSONObject();
    JSONArray array = map.optJSONArray("SIZE");
    unimp("have default value if array is null?");
    if (array != null)
      setTexSize(IPoint.parseJSON(array));
    fixedSize.setSelected(map.optBoolean("FIXEDSIZE", false));
  }

  public static void encodeTo(JSONObject map) throws JSONException {
    map.put("SIZE", new IPoint(iv(texWidth), iv(texHeight)).toJSON());
    map.put("FIXEDSIZE", fixedSize.isSelected());
  }

}
