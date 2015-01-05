package apputil;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.json.*;

import static com.js.basic.Tools.*;

import com.js.basic.Tools;
import com.js.geometry.IRect;

public class MyFrame extends JFrame {

  private String persistId2;
  private boolean boundsDefined;

  @Override
  public void setVisible(boolean f) {
    if (f) {
      if (!isVisible()) {
        if (!restoreBounds())
          pack();
        super.setVisible(f);
      }
    } else
      super.setVisible(f);
  }

  private boolean restoreBounds() {
    if (!boundsDefined) {
      try {
        IRect r = IRect.opt(sFrameMap, persistId2);
        if (r != null) {
          setBounds(r.toRectangle());
          boundsDefined = true;
        }
      } catch (JSONException e) {
        die(e);
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
          try {
            r.put(sFrameMap, persistId2);
          } catch (JSONException e) {
            die(e);
          }
        }

        @Override
        public void componentResized(ComponentEvent arg0) {
        }

        @Override
        public void componentShown(ComponentEvent arg0) {
        }
      });

    Tools.quitProgramAfterDelay(this, 5 * 60);
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

  public static final ConfigSet.Interface CONFIG = new ConfigSet.Interface() {
    private static final String TAG = "FRAMES";

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      map.put(TAG, sFrameMap);
    }

    @Override
    public void readFrom(JSONObject map) throws JSONException {
      sFrameMap = map.optJSONObject(TAG);
      if (sFrameMap == null)
        sFrameMap = new JSONObject();
    }
  };

  private static JSONObject sFrameMap = new JSONObject();
}
