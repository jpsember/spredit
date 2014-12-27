package apputil;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;

import org.json.JSONException;
import org.json.JSONObject;

import static com.js.basic.Tools.*;

import com.js.geometry.Rect;

public class MyFrame extends JFrame {

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

  private static JSONObject constructFrameInfo() throws JSONException {
    JSONObject framesMap = new JSONObject();
    Iterator<String> it = frameInfo.keySet().iterator();
    while (it.hasNext()) {
      String key = it.next();
      Rect bounds = (Rect) frameInfo.get(key);
      framesMap.put(key, bounds.toJSON());
    }
    return framesMap;
  }

  public static final IConfig CONFIG = new IConfig() {
    private static final String TAG = "FRAMES";

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      if (!frameInfo.isEmpty()) {
        map.put(TAG, constructFrameInfo());
      }
    }

    @Override
    public void readFrom(JSONObject map) throws JSONException {
      JSONObject map2 = map.optJSONObject(TAG);
      if (map2 == null)
        return;
      Iterator<String> it = map2.keys();
      while (it.hasNext()) {
        String key = it.next();
        Rect bounds = Rect.parseJSON(map2.getJSONArray(key));
        frameInfo.put(key, bounds);
      }
    }
  };

}
