package apputil;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import static com.js.basic.Tools.*;

import com.js.geometry.Rect;

public class MyFrame extends JFrame {
  private static final boolean db = false;

  private String persistId2;
  private boolean boundsDefined;

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
        if (db)
          pr("calling super.setVisible");

        super.setVisible(f);
      }
    } else
      super.setVisible(f);
  }

  private boolean restoreBounds() {
    final boolean db = false;
    if (!boundsDefined) {
      Rect r = (Rect) frameInfo.get(persistId2);
      if (r != null) {
        setBounds(new Rectangle((int) r.x, (int) r.y, (int) r.width,
            (int) r.height));
        boundsDefined = true;
      }
    }
    return boundsDefined;
  }

  public MyFrame(String persistId) {
    this.persistId2 = persistId;
    // set min width to accomodate title
    setMinimumSize(new Dimension(300, 1));

    if (persistId != null)
      addComponentListener(new ComponentListener() {

        @Override
        public void componentHidden(ComponentEvent arg0) {
        }

        @Override
        public void componentMoved(ComponentEvent ev) {
          Rect r = new Rect(getBounds());
          frameInfo.put(persistId2, r);
        }

        @Override
        public void componentResized(ComponentEvent arg0) {
        }

        @Override
        public void componentShown(ComponentEvent arg0) {
        }
      });

    if (true) {
      warning("will close frame after ~ 1 minute");
      // Close frame automatically after several seconds
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          MyFrame.this.dispatchEvent(new WindowEvent(MyFrame.this,
              WindowEvent.WINDOW_CLOSING));
        }
      }, 60 * 1000);
    }
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
  }

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

}
