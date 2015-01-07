package com.js.scredit;

import java.awt.Component;

import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import apputil.*;

import com.js.editor.Command;
import com.js.geometry.MyMath;
import com.js.geometry.Point;

public class Grid {

  private static class Rev extends ModifyObjectsReversible {

    public Rev() {
      setName("Snap");
    }

    @Override
    public EdObject perform(EdObject orig) {
      return orig.snapToGrid();
    }
  }

  public static Command getOper() {
    return new Rev();
  }

  public static boolean intGridActive() {
    return intGridActive.isSelected();
  }

  public static Point snapToGrid(Point pt, boolean onlyIfActive) {
    do {
      int gs = 1;

      if (intGridActive())
        gs = gridSize.getNumber().intValue();

      Point s = new Point(pt);
      s.snapToGrid(gs);
      if (MyMath.squaredDistanceBetween(s, pt) != 0)
        pt = s;
    } while (false);
    return pt;
  }

  public static int gridSize() {
    return gridSize.getNumber().intValue();
  }

  public static void setIntGrid(boolean f) {
    intGridActive.setSelected(f);
  }

  public static ConfigSet.Interface CONFIG = new ConfigSet.Interface() {
    @Override
    public void readFrom(JSONObject map) throws JSONException {
      JSONArray list = map.optJSONArray("grid");
      if (list == null)
        return;
      int c = 0;
      setIntGrid(list.getBoolean(c++));
      gridSize.setValue(list.getInt(c++));
    }

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      map.put("grid", new JSONArray().put(intGridActive()).put(gridSize()));
    }
  };
  private static JToggleButton intGridActive = new JCheckBox("Grid");
  private static SpinnerNumberModel gridSize = new SpinnerNumberModel(10, 1,
      500, 1);

  public static Component intGridActiveCtrl() {
    return intGridActive;
  }

  public static Component gridSizeCtrl() {
    return new JSpinner(gridSize);
  }
}
