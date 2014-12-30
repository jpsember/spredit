package apputil;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.json.JSONException;
import org.json.JSONObject;

import static com.js.basic.Tools.*;

import com.js.basic.Tools;
import com.js.geometry.IRect;

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
      IRect r = frameInfo.get(persistId2);
      if (r != null) {
        setBounds(r.toRectangle());
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
          IRect r = new IRect(getBounds());
          frameInfo.put(persistId2, r);
        }

        @Override
        public void componentResized(ComponentEvent arg0) {
        }

        @Override
        public void componentShown(ComponentEvent arg0) {
        }
      });

    Tools.quitProgramAfterDelay(this, 60);
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

  private static Map<String, IRect> frameInfo = new HashMap();

  private static JSONObject constructFrameInfo() throws JSONException {
    JSONObject framesMap = new JSONObject();
    Iterator<String> it = frameInfo.keySet().iterator();
    while (it.hasNext()) {
      String key = it.next();
      IRect bounds = frameInfo.get(key);
      framesMap.put(key, bounds.toJSON());
    }
    return framesMap;
  }

  public static final ConfigSet.Interface CONFIG = new ConfigSet.Interface() {
    private static final String TAG = "FRAMES";

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      if (!frameInfo.isEmpty()) {
        map.put(TAG, constructFrameInfo());
      }
    }

    @Override
    public void readFrom(JSONObject map) throws JSONException {
      JSONObject framesMap = map.optJSONObject(TAG);
      if (framesMap == null)
        return;
      Iterator<String> it = framesMap.keys();
      while (it.hasNext()) {
        String key = it.next();
        IRect bounds = IRect.parseJSON(framesMap.getJSONArray(key));
        frameInfo.put(key, bounds);
      }
    }
  };

}
