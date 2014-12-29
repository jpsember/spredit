package com.js.scredit;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

import apputil.*;

public class GroupObject extends EdObject {

  private static final boolean db = false;

  private static class GroupReversible implements Reversible, Reverse {
    public GroupReversible() {
      this(false);
    }

    private GroupReversible(boolean undoFlag) {

      this.undoFlag = undoFlag;
      if (!undoFlag) {
        slots = ScriptEditor.items().getSelected();
      }
      if (db)
        pr("Constructed " + this);
    }

    @Override
    public Reverse getReverse() {
      if (rev != null)
        return rev;

      ASSERT(!undoFlag);

      if (origObj == null) {
        origObj = new ObjArray();
        origObj.addAll(ScriptEditor.items());
        origObj.clearAllSelected();
      }

      GroupReversible gr = new GroupReversible(true);
      gr.rev = this;
      gr.origObj = this.origObj;
      gr.slots = this.slots;
      rev = gr;
      return rev;
    }

    @Override
    public void perform() {
      if (db)
        pr("perform " + this);

      if (undoFlag) {
        ScriptEditor.setItems(origObj);
        origObj.setSelected(slots, true);
      } else {
        getReverse(); // make sure we've constructed origObj
        origObj.setSelected(slots, false);
        ObjArray a = new ObjArray();
        GroupObject group = new GroupObject();
        group.setSelected(true);
        int slot = 0;
        for (int i = 0; i < origObj.size(); i++) {
          EdObject obj = origObj.get(i);
          if (slot < slots.length && slots[slot] == i) {
            group.addParsedObject(obj);
            if (slot == 0)
              a.add(group);
            slot++;
          } else
            a.add(obj);
        }
        ScriptEditor.setItems(a);
      }
    }

    @Override
    public boolean valid() {
      return undoFlag || slots.length >= 2;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Group ");
      if (slots != null)
        sb.append(EdTools.itemsStr(slots.length));
      if (db) {
        // sb.append("\n id=" + id);
        sb.append("\n undoFlag=" + d(undoFlag));
      }

      return sb.toString();
    }

    private int[] slots;
    private ObjArray origObj;

    // true if this object is actually the reversal of the original Group
    // operation
    private boolean undoFlag;

    private GroupReversible rev;
    // private static int uniqueId = 500;
    // private int id;
  };

  private static class UnGroupReversible implements Reversible, Reverse {
    private UnGroupReversible(boolean undoFlag) {
      this.undoFlag = undoFlag;
    }

    public UnGroupReversible() {
      this(false);
      ObjArray a = ScriptEditor.items();
      int[] slots = a.getSelected();
      if (slots.length == 1) {
        EdObject obj = a.get(slots[0]);
        if (obj instanceof GroupObject) {
          group = (GroupObject) obj;
          slot = slots[0];
        }
      }
    }

    public String toString() {
      return "Ungroup";
    }

    private GroupObject group;
    private int slot;
    private UnGroupReversible rev;
    private boolean undoFlag;
    private ObjArray origObj;

    @Override
    public Reverse getReverse() {
      if (db)
        pr("UnGroupReversible.getReverse()");

      if (rev == null) {

        ASSERT(!undoFlag);

        if (origObj == null) {
          if (db)
            pr(" storing ScriptEditor items in origObj");

          origObj = new ObjArray();
          origObj.addAll(ScriptEditor.items());
        }

        UnGroupReversible gr = new UnGroupReversible(true);
        // not sure we need a redo here...
        gr.origObj = origObj;
        gr.slot = slot;
        if (false)
          gr.rev = this;
        rev = gr;
      }
      return rev;
    }

    @Override
    public void perform() {
      if (undoFlag) {
        if (db)
          pr("performing undo, restoring items " + origObj);

        origObj.clearAllSelected();
        origObj.get(slot).setSelected(true);

        if (db)
          pr("  set slot " + slot + " selected");

        ScriptEditor.setItems(origObj);

      } else {
        getReverse(); // make sure we've saved original items
        ObjArray a = ScriptEditor.items();
        ObjArray b = new ObjArray();
        for (int i = 0; i < a.size(); i++) {
          EdObject obj = a.get(i);
          if (i == slot) {
            for (int k = 0; k < group.size(); k++) {
              EdObject o2 = group.obj(k);
              o2 = (EdObject) o2.clone();
              o2.setSelected(true);
              b.add(o2);
            }
          } else
            b.add(obj);
        }
        ScriptEditor.setItems(b);
      }
    }

    @Override
    public boolean valid() {
      return group != null;
    }
  };

  public static Reversible getGroupReversible() {
    return new GroupReversible();
  }

  public static Reversible getUnGroupReversible() {
    return new UnGroupReversible();
  }

  public static final int CODE = 3; // code for group object
  private ArrayList<EdObject> objects = new ArrayList();

  private int size() {
    return objects.size();
  }

  private void setObj(int index, EdObject obj) {
    objects.set(index, obj);
  }

  private EdObject obj(int i) {
    return objects.get(i);
  }

  /**
   * Clone the object
   */
  public Object clone() {
    GroupObject g = (GroupObject) super.clone();
    g.objects = new ArrayList();
    for (int i = 0; i < size(); i++)
      g.objects.add((EdObject) obj(i).clone());
    return g;
  }

  @Override
  public Rect boundingRect() {
    if (bounds == null) {
      for (int i = 0; i < size(); i++) {
        EdObject obj = obj(i);
        Rect b = obj.boundingRect();
        if (bounds == null)
          bounds = new Rect(b);
        else
          bounds.include(b);
      }
    }
    return bounds;
  }

  public boolean contains(Point pt) {
    return boundingRect().contains(pt);
  }

  // @Override
  // public Point getPoint(int index) {
  // return location();
  // }
  // @Override
  // public int nPoints() {
  // return 1;
  // }
  @Override
  public Point location() {
    return boundingRect().midPoint();
  }

  @Override
  public void setLocation(Point pt) {
    Point prev = location();
    Point diff = Point.difference(pt, prev);
    bounds = null;
    for (int i = 0; i < size(); i++) {
      EdObject obj = obj(i);
      Point ploc = obj.location();
      obj.setLocation(Point.sum(ploc, diff));
    }
  }

  private float rotation;

  @Override
  public float rotation() {
    return rotation;
  }

  @Override
  public float scale() {
    if (scale == 0)
      scale = 1;
    return scale;
  }

  @Override
  public void setRotation(float angle) {
    for (int i = 0; i < size(); i++) {
      obj(i).setRotation(angle);
    }
    bounds = null;
  }

  @Override
  public void rotAndScale(EdObject origObject, float scaleFactor, Point origin,
      float rotAngle) {
    GroupObject origGroup = (GroupObject) origObject;

    for (int i = 0; i < size(); i++) {
      obj(i).rotAndScale(origGroup.obj(i), scaleFactor, origin, rotAngle);
    }
    bounds = null;
  }

  @Override
  public void setScale(float s) {
    for (int i = 0; i < size(); i++)
      obj(i).setScale(s);
    bounds = null;

  }

  private float scale;
  private Rect bounds;
  private int expectedSize;

  @Override
  public EdObject applyColor(Color color) {
    EdObject ret = this;

    GroupObject g = (GroupObject) this.clone();
    for (int i = 0; i < g.size(); i++) {
      EdObject obj = obj(i);
      EdObject obj2 = obj.applyColor(color);
      if (obj2 != obj) {
        ret = g;
        g.setObj(i, obj2);
      }
    }
    return ret;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Group of ");
    sb.append(size());
    sb.append(" objects");

    return sb.toString();
  }

  @Override
  public EdObjectFactory getFactory() {
    return FACTORY;
  }

  @Override
  public void render(GLPanel panel) {

    if (isSelected()) {
      panel.setRenderColor(Color.YELLOW);
      panel.drawFrame(boundingRect());
    }

    for (int i = 0; i < size(); i++) {
      EdObject ob = obj(i);
      ob.render(panel);
    }
  }

  public static EdObjectFactory FACTORY = new OurFactory();

  private static class OurFactory extends EdObjectFactory {

    @Override
    public String getTag() {
      return "G";
    }

    @Override
    public EdObject parse(Script script, JSONObject map) throws JSONException {
      GroupObject so = new GroupObject();
      so.expectedSize = map.getInt("size");

      // the caller will handle reading additional objects and adding them to
      // the group
      // until it is complete.

      return so;
    }

    @Override
    public void write(Script script, JSONObject map, EdObject obj)
        throws JSONException {
      GroupObject g = (GroupObject) obj;
      map.put("size", g.size());
      unimp("parse objects from group");
      // for (int i = 0; i < g.size(); i++) {
      // EdObject o2 = g.obj(i);
      // EdObjectFactory f = o2.getFactory();
      // sb.append(f.getTag());
      // f.write(script, sb, o2);
      // sb.addCr();
      // }
    }

    public void write(ScriptsFile sf, EdObject obj) throws IOException {
      GroupObject so = (GroupObject) obj;
      for (int i = 0; i < so.size(); i++) {
        EdObject o2 = so.obj(i);
        o2.getFactory().write(sf, o2);
      }
    }

    @Override
    public int getCode() {
      return CODE;
    }

  }

  public void addParsedObject(EdObject obj) {
    objects.add(obj); // obj.clone());
  }

  public boolean parseComplete() {
    return size() == expectedSize;
  }
}
