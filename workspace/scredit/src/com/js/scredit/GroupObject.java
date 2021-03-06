package com.js.scredit;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.js.basic.Freezable;
import com.js.editor.Command;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

public class GroupObject extends EdObject {

  private GroupObject(EdObject source) {
    super(source);
  }

  private static final boolean db = false;

  private static class GroupReversible extends Command.Adapter {
    public GroupReversible() {
      this(false);
    }

    private GroupReversible(boolean undoFlag) {

      this.undoFlag = undoFlag;
      if (!undoFlag) {
        slots = ScriptEditor.items().getSelectedSlots();
      }
      if (db)
        pr("Constructed " + this);
    }

    @Override
    public Command getReverse() {
      if (rev != null)
        return rev;

      ASSERT(!undoFlag);

      if (origObj == null) {
        origObj = frozen(ScriptEditor.items());
        // observe we are actually mutating the frozen object here; later
        // perhaps we'll move selected flags out of the EdObjectArray
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
        origObj.setSelected(slots);
      } else {
        int groupSlot = -1;
        getReverse(); // make sure we've constructed origObj
        EdObjectArray a = new EdObjectArray();
        GroupObject group = new GroupObject(null);
        group.setSelected(true);
        int slot = 0;
        for (int i = 0; i < origObj.size(); i++) {
          EdObject obj = origObj.get(i);
          if (slot < slots.size() && slots.get(slot) == i) {
            group.addParsedObject(obj);
            if (slot == 0)
              groupSlot = a.add(group);
            slot++;
          } else
            a.add(obj);
        }
        origObj.setSelected(new SlotList(groupSlot));

        ScriptEditor.setItems(a);
      }
    }

    @Override
    public boolean valid() {
      return undoFlag || slots.size() >= 2;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Group ");
      if (slots != null)
        sb.append(EdTools.itemsStr(slots.size()));
      if (db) {
        // sb.append("\n id=" + id);
        sb.append("\n undoFlag=" + d(undoFlag));
      }

      return sb.toString();
    }

    private SlotList slots;
    private EdObjectArray origObj;

    // true if this object is actually the reversal of the original Group
    // operation
    private boolean undoFlag;

    private GroupReversible rev;
    // private static int uniqueId = 500;
    // private int id;

  };

  private static class UnGroupReversible extends Command.Adapter {
    private UnGroupReversible(boolean undoFlag) {
      this.undoFlag = undoFlag;
    }

    public UnGroupReversible() {
      this(false);
      EdObjectArray a = ScriptEditor.items();
      SlotList slots = a.getSelectedSlots();
      if (slots.size() == 1) {
        EdObject obj = a.get(slots.get(0));
        if (obj instanceof GroupObject) {
          group = (GroupObject) obj;
          slot = slots.get(0);
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
    private EdObjectArray origObj;

    @Override
    public Command getReverse() {
      if (db)
        pr("UnGroupReversible.getReverse()");

      if (rev == null) {

        ASSERT(!undoFlag);

        if (origObj == null) {
          if (db)
            pr(" storing ScriptEditor items in origObj");

          die("unsupported");
          // origObj = new EdObjectArray(ScriptEditor.items());
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
        EdObjectArray a = ScriptEditor.items();
        EdObjectArray b = new EdObjectArray();
        for (int i = 0; i < a.size(); i++) {
          EdObject obj = a.get(i);
          if (i == slot) {
            for (int k = 0; k < group.size(); k++) {
              EdObject o2 = copyOf(group.obj(k));
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

  public static Command getGroupReversible() {
    return new GroupReversible();
  }

  public static Command getUnGroupReversible() {
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

  @Override
  public Freezable getMutableCopy() {
    GroupObject g = new GroupObject(this);
    g.objects = new ArrayList();
    // If we make objects immutable, we don't need to get copies of them...?
    for (int i = 0; i < size(); i++)
      g.objects.add(frozen(obj(i)));
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
    mutate();
    Point prev = location();
    Point diff = Point.difference(pt, prev);
    bounds = null;
    for (int i = 0; i < size(); i++) {
      EdObject obj = obj(i);
      Point ploc = obj.location();
      obj.setLocation(Point.sum(ploc, diff));
    }
  }

  private Rect bounds;
  private int expectedSize;

  @Override
  public EdObject applyColor(Color color) {
    EdObject ret = this;

    GroupObject g = mutableCopyOf(this);
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
  public void render(GLPanel panel, boolean isSelected, boolean isEditable) {

    if (isSelected) {
      panel.setRenderColor(Color.YELLOW);
      panel.drawFrame(boundingRect());
    }

    for (int i = 0; i < size(); i++) {
      EdObject ob = obj(i);
      ob.render(panel, false, false);
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
      GroupObject so = new GroupObject(null);
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
    objects.add(obj);
  }

  public boolean parseComplete() {
    return size() == expectedSize;
  }
}
