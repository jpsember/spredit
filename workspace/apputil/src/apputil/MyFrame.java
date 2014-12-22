package apputil;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import static com.js.basic.Tools.*;

import com.js.geometry.Rect;

public class MyFrame extends JFrame {
  private static final boolean db = false;

  //  public void makeNonResizable() {
  //    f.setResizable(false);
  //  }

  //  public void setTitle(String title) {
  //    f.setTitle(title);
  //  }
  //private boolean listenable;

  private String persistId2;
  private boolean boundsDefined;

  //  public void packIfNecessary() {
  //    if (!boundsDefined) {
  //      pack();
  //    }
  //    boundsDefined = true;
  //  }

  @Override
  public void setVisible(boolean f) {
    final boolean db = false;
    if (db)
      pr("setVisible " + this + ", f=" + f + " isVis=" + isVisible());

    if (f) {
      if (!isVisible()) {
        if (db)
          pr("  not already visible");

        if (!restoreBounds())
          pack();
//        
//        if (!boundsDefined) {
//          if (db)
//            pr("  attempting to restore bounds");
//
//          restoreBounds();
//        }
//        if (!boundsDefined) {
//          if (db)
//            pr(" bounds weren't defined, packing");
//          pack();
//        }
        if (db)
          pr("calling super.setVisible");

        super.setVisible(f);
      }
    } else
      super.setVisible(f);
  }

  //  @Override
  //  public void pack() {
  //    
  //    if (!boundsDefined)
  //  }

  private boolean restoreBounds() {
    final boolean db = false;
    if (db)
      pr("restoreBounds id=" + persistId2+" defined="+boundsDefined);
if (!boundsDefined) {
      Rect r = (Rect) frameInfo.get(persistId2);

    if (db)
      pr(" read " + persistId2 + ":" + r);
    if (r != null) {
        setBounds(new Rectangle((int) r.x, (int) r.y, (int) r.width,
            (int) r.height));
      boundsDefined = true;
    }
}
return boundsDefined;
  }

  public MyFrame(String persistId) {
    // f = new JFrame();
    this.persistId2 = persistId;
    // set min width to accomodate title
    setMinimumSize(new Dimension(300, 1));

    if (db)
      pr("MyFrame construct, id=" + persistId2);

    //    IRect r = (IRect) frameInfo.get(persistId2);
    //
    //    //    if (true) {
    //    //      warn("setting r null");
    //    //      r = null;
    //    //    }
    //
    //    if (db)
    //      pr(" read " + persistId2 + ":" + r);
    //    if (r != null) {
    //      setBounds(new Rectangle(r.x, r.y, r.width, r.height));
    //      boundsDefined = true;
    //    }

    if (db)
      pr(" adding component listener");

    if (persistId != null)
      addComponentListener(new ComponentListener() {
       // private static final boolean db = false;

        @Override
        public void componentHidden(ComponentEvent arg0) {
          if (db)
            pr(" component hidden: " + MyFrame.this);

        }

        @Override
        public void componentMoved(ComponentEvent ev) {
          if (db)
            pr(" component moved: " + MyFrame.this + "\n"
 + stackTrace(15));
          if (db)
            pr("  visible=" + isVisible() + " valid=" + isValid()
            // + " listenable=" + listenable
            );

          //          if (!listenable) {
          //            if (db)
          //              pr(" ignoring, not listenable");
          //            return;
          //          }

          Rect r = new Rect(getBounds());
          if (db)
            pr(" storing " + persistId2 + ":" + r);

          frameInfo.put(persistId2, r);
        }

        @Override
        public void componentResized(ComponentEvent arg0) {
          if (db)
            pr(" resized: " + this);
        }

        @Override
        public void componentShown(ComponentEvent arg0) {
          if (db)
            pr(" component shown: " + MyFrame.this);
        }
      });
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MyFrame");
    if (persistId2 != null)
      sb.append(" id=" + persistId2);

    return sb.toString();
  }
  // private String persistId;

  public MyFrame(String persistId, String title, Component contents) {
    this(persistId);

    if (title != null)
      setTitle(title);

    if (contents != null) {
      setContents(contents);
    }
  }
  public void setContents(Component c) {
    getContentPane().add(c);
    warning("not sure if pack required here");
    //pack();
  }
  //  public void show() {
  //    packIfNecessary();
  //    setVisible(true);
  //    listenable = true;
  //  }
  //  public void hide() {
  //    listenable = false;
  //    setVisible(false);
  //  }

  private static Map frameInfo = new HashMap();

  private static void readFrameInfo(String fiString) {
    DefScanner sc = new DefScanner(fiString);
    frameInfo.clear();
    while (!sc.done()) {
      String id = sc.sId();
      Rect bounds = sc.sRect();
      frameInfo.put(id, bounds);
    }
  }
  private static void writeFrameInfo(DefBuilder sb) {
    sb.append("FRAMES");
    Iterator it = frameInfo.keySet().iterator();
    while (it.hasNext()) {
      String key = (String) it.next();
      Rect bounds = (Rect) frameInfo.get(key);
      sb.append(key);
      sb.append(bounds);
    }
    sb.addCr();
  }

  public static final IConfig CONFIG = new IConfig() {

    @Override
    public boolean process(DefScanner sc, String item) {
      if (item.equals("FRAMES")) {
        readFrameInfo(sc.readLine());
        return true;
      }
      return false;
    }

    @Override
    public void writeTo(DefBuilder sb) {
      if (!frameInfo.isEmpty()) {
        writeFrameInfo(sb);
      }
    }
  };
  //  public JFrame frame() {
  //    return f;
  //  }
  //  private JFrame f;

}
