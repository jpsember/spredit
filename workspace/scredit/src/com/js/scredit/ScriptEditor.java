package com.js.scredit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import javax.swing.*;

import org.json.JSONException;
import org.json.JSONObject;

import tex.*;
import apputil.*;

import com.js.basic.*;
import com.js.editor.Command;
import com.js.editor.MouseEventGenerator;
import com.js.editor.MouseOper;
import com.js.editor.UserEvent;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;
import static apputil.MyMenuBar.*;

public class ScriptEditor {

  /**
   * If current script defined, and has changes, save them.
   * 
   * @param askUser
   *          if true, and save required, asks user
   * @return true if success (false if error, or user cancelled save)
   */
  private static boolean flush(boolean askUser) {
    if (!isProjectOpen())
      return true;
    return editor().flushAux(askUser);
  }

  public static Color color() {
    return sPalettePanel.getSelectedColor();
  }

  /**
   * Get focus of editor window
   * 
   * @return world location of center of editor window
   */
  public static IPoint focus() {
    return sFocus;
  }

  /**
   * Replace current editor with a fresh, anonymous editor; does nothing if no
   * project is open
   */
  private static void disposeOfCurrentEditor() {
    assertProjectOpen();
    sScriptSet.setCursorFile(null);
  }

  private static void readScriptForCurrentEditor() throws IOException,
      JSONException {
    editor().getScript().read();
  }

  /**
   * Read script into current layer
   * 
   * @return true if successful
   */
  private static boolean openScript(File f) {
    boolean success = false;
    do {
      if (!flush(true)) {
        break;
      }

      if (f == null) {
        f = AppTools.chooseFileToOpen("Open Script", SCRIPT_FILEFILTER,
            sProject
                .replaceIfMissing(sProject.recentScripts().getCurrentFile()));
      }
      if (f == null) {
        break;
      }
      if (notInProject(f)) {
        break;
      }

      try {
        unimp("if editor exists in another slot, don't build new");

        int slot = sScriptSet.findEditorForNamedFile(f);
        if (slot >= 0) {
          sScriptSet.setCursorFile(f);
          success = true;
          break;
        }

        sScriptSet.setCursorFile(f);
        readScriptForCurrentEditor();

        success = true;
        sProject.setLastScriptPath(f);
      } catch (Throwable e) {
        AppTools.showError("opening script", e);
      }
    } while (false);
    return success;
  }

  /**
   * Set current script set, and read each script from its respective file (if
   * it has one)
   */
  private static void setScriptSet(ScriptSet set) {
    if (set == null)
      throw new IllegalArgumentException();
    sScriptSet = set;

    int originalSlot = set.getCursor();
    for (int i = 0; i < set.size(); i++) {
      set.setCursor(i);
      if (!editor().hasName())
        continue;
      try {
        readScriptForCurrentEditor();
      } catch (Exception e) {
        AppTools.showMsg("Problem reading " + editor().getFile() + ": " + e);
        // Since an error occurred, throw out any following editors
        originalSlot = i;
        i++;
        while (set.size() != i)
          sScriptSet.remove(set.size() - 1);
        break;
      }
    }
    set.setCursor(originalSlot);
  }

  /**
   * Get the ObjArray manipulated by the current editor
   */
  public static EdObjectArray items() {
    return editor().mScript.items();
  }

  /**
   * Replace ObjArray for current editor
   */
  public static void setItems(EdObjectArray items) {
    editor().mScript.setItems(items);
  }

  /**
   * Get the active editor, or null if there isn't one
   */
  public static ScriptEditor editor() {
    assertProjectOpen();
    return sScriptSet.get();
  }

  /**
   * Repaint the current editor, and its associated components
   */
  public static void repaint() {
    updateEditableObjectStatus();
    sInfoPanel
        .refresh(isProjectOpen() ? editor() : null, project(), sScriptSet);
    sEditorPanelComponent.repaint();
  }

  /**
   * Adjust zoom factor
   * 
   * @param code
   *          0 to reset, -1 to zoom out, +1 to zoom in
   */
  private static void doAdjustZoom(int code) {
    switch (code) {
    default:
      sZoomFactor = 1;
      break;
    case 1:
      sZoomFactor *= .8f;
      break;
    case -1:
      sZoomFactor *= 1 / .8f;
      break;
    }
    repaint();
  }

  public static boolean isProjectOpen() {
    return project() != null;
  }

  private static boolean notInProject(File scriptFile) {
    boolean inProj = sProject.isDescendant(scriptFile);
    if (!inProj)
      JOptionPane.showMessageDialog(AppTools.frame(), "File '" + scriptFile
          + "' is not within the project", "Error", JOptionPane.ERROR_MESSAGE);

    return !inProj;
  }

  /**
   * Template for operation that modifies selected items only. Performs undo by
   * restoring original items.
   */
  private abstract static class EditSelectedOper extends Command.Adapter {

    public EditSelectedOper() {

      // determine selected items, and save for undoing
      EdObjectArray items = ScriptEditor.items();
      slots = items.getSelected();
      origItems = new EdObject[slots.length];
      for (int i = 0; i < slots.length; i++)
        origItems[i] = items.get(slots[i]);
    }

    @Override
    public Command getReverse() {
      return new Command.Adapter() {

        @Override
        public Command getReverse() {
          return EditSelectedOper.this;
        }

        @Override
        public void perform() {
          EdObjectArray items = ScriptEditor.items();
          for (int i = 0; i < slots.length; i++)
            items.set(slots[i], origItems[i]);
        }
      };
    }

    // @Override
    // public void perform() {
    // ObjArray items = ScriptEditor.items();
    // for (int i = 0; i < slots.length; i++)
    // items.set(slots[i], origItems[i]);
    // }
    public int slot(int i) {
      return slots[i];
    }

    public int nSelected() {
      return slots.length;
    }

    private int[] slots;
    private EdObject[] origItems;
    // private Reversible fwdOper;
  }

  private static class CursorMoveHandler extends ActionHandler {
    public CursorMoveHandler(int dir) {
      this.dir = dir;
    }

    private static int[] xm = { -1, 1, 0, 0 };
    private static int[] ym = { 0, 0, 1, -1 };

    private int dir;

    public boolean shouldBeEnabled() {
      return items().getSelected().length != 0;
    }

    public void go() {
      EditSelectedOper r = new EditSelectedOper() {
        @Override
        public void perform() {
          for (int i = 0; i < nSelected(); i++) {
            EdObject obj = items().get(slot(i));
            Point pt = new Point(obj.location());
            pt.x += xm[dir];
            pt.y += ym[dir];
            obj.setLocation(pt);
          }
        }

        @Override
        public boolean valid() {
          return nSelected() > 0;
        }
      };
      if (r.nSelected() > 0) {
        editor().registerPush(r);
        perform(r);

        repaint();
      }
    }
  }

  private static void addMenus() {

    Enableable projectMustBeOpenHandler = new Enableable() {
      @Override
      public boolean shouldBeEnabled() {
        return isProjectOpen();
      }
    };

    MyMenuBar m = new MyMenuBar(AppTools.frame());

    m.addAppMenu();

    // -----------------------------------
    m.addMenu("File", projectMustBeOpenHandler);
    m.addItem("New", KeyEvent.VK_N, CTRL, new ActionHandler() {
      public void go() {
        doNewScript();
        repaint();
      }
    });
    m.addItem("Open Script...", KeyEvent.VK_O, CTRL, new ActionHandler() {
      public void go() {
        openScript(null);
        repaint();
      }
    });

    sRecentScriptsMenuItem = new RecentFiles.Menu("Open Recent Script", null,
        new ActionHandler() {
          public void go() {
            openScript(sProject.recentScripts().getCurrentFile());
            repaint();
          }
        });
    m.addItem(sRecentScriptsMenuItem);

    m.addItem("Open Layer...", KeyEvent.VK_O, META, new ActionHandler() {
      public void go() {
        // If current editor is anonymous, and unmodified, don't insert a new
        // one
        if (editor().hasName() || editor().modified()) {
          sScriptSet.insert(sScriptSet.getCursor(), null);
        }
        openScript(null);
        repaint();
      }
    });
    m.addItem("Open Next File...", KeyEvent.VK_O, CTRL | SHIFT,
        new ActionHandler() {
          public boolean shouldBeEnabled() {
            return editor().hasName();
          }

          public void go() {
            openNextFile();
            repaint();
          }
        });

    m.addSeparator();

    m.addItem("Close", KeyEvent.VK_W, CTRL, new ActionHandler() {
      @Override
      public boolean shouldBeEnabled() {
        return currentScriptCloseable();
      }

      @Override
      public void go() {
        do {
          if (!flush(true))
            break;
          disposeOfCurrentEditor();
          // If this is not the only editor in the set, remove it
          if (sScriptSet.size() > 1)
            sScriptSet.remove(sScriptSet.getCursor());
        } while (false);
        repaint();
      }
    });
    m.addItem("Close All", KeyEvent.VK_W, CTRL | SHIFT, new ActionHandler() {
      @Override
      public boolean shouldBeEnabled() {
        return currentScriptCloseable();
      }

      @Override
      public void go() {
        sScriptSet.setCursor(sScriptSet.size() - 1);
        do {
          repaint();
          if (!flush(true))
            break;
          disposeOfCurrentEditor();
          if (sScriptSet.size() > 1)
            sScriptSet.remove(sScriptSet.size() - 1);
          else
            break;
        } while (true);
        repaint();
      }
    });

    m.addItem("Save", KeyEvent.VK_S, CTRL, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return !editor().hasName() || editor().modified();
      }

      public void go() {
        editor().doSave(null, false, false);
        repaint();
      }
    });

    m.addItem("Save As...", KeyEvent.VK_A, CTRL | SHIFT, new ActionHandler() {
      public void go() {
        editor().doSave(null, true, false);
        repaint();
      }
    });
    m.addItem("Save All", KeyEvent.VK_S, CTRL | SHIFT, new ActionHandler() {
      public boolean shouldBeEnabled() {
        boolean ret = false;
        for (int i = 0; i < sScriptSet.size(); i++) {
          ScriptEditor ed = sScriptSet.get(i);
          if (!ed.getScript().hasName() || ed.modified())
            ret = true;
        }
        return ret;
      }

      public void go() {
        int previousSlot = sScriptSet.getCursor();
        for (int i = 0; i < sScriptSet.size(); i++) {
          ScriptEditor ed = sScriptSet.get(i);
          if (!ed.getScript().hasName() || ed.modified()) {
            sScriptSet.setCursor(i);
            editor().doSave(null, false, false);
          }
        }
        sScriptSet.setCursor(previousSlot);
        repaint();
      }
    });
    m.addItem("Save As Next", KeyEvent.VK_S, META | SHIFT, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return editor().hasName();
      }

      public void go() {
        File f = AppTools.incrementFile(editor().getFile());
        editor().doSave(f, false, true);
        repaint();
      }
    });

    // -----------------------------------
    m.addMenu("Edit", projectMustBeOpenHandler);
    sUndoMenuItem = m.addItem("Undo", KeyEvent.VK_Z, CTRL, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return editor().mUndoCursor > 0;
      }

      public void go() {
        editor().doUndo();
        repaint();
      }
    });

    sRedoMenuItem = m.addItem("Redo", KeyEvent.VK_Y, CTRL, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return editor().getRedoOper() != null;
      }

      public void go() {
        editor().doRedo();
        repaint();
      }
    });
    m.addSeparator();
    m.addItem("Cut", KeyEvent.VK_X, CTRL, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = new CutReversible();
        return r.valid();
      }

      public void go() {
        // private void doCut() {
        // final boolean db = false;
        //
        // int[] si = items.getSelected();
        // if (db)
        // pr("doCut " + f(si));
        // if (si.length > 0) {
        // Reversible r = new CutOper(si);
        // if (db)
        // pr(" performing " + r);

        editor().registerPush(r);
        perform(r);
        // }
        // }

        // editor.doCut();
      }
    });
    m.addItem("Copy", KeyEvent.VK_C, CTRL, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = new CopyReversible();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });

    m.addItem("Paste", KeyEvent.VK_V, CTRL, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = new PasteReversible();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
        // editor.doPaste();
      }
    });

    m.addItem("Duplicate", KeyEvent.VK_D, CTRL, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = new DuplicateReversible();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });

    m.addSeparator();
    m.addItem("Select All", KeyEvent.VK_A, CTRL, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return !items().isEmpty();
      }

      public void go() {
        EdObjectArray a = items();
        for (EdObject obj : a)
          obj.setSelected(true);
        repaint();
      }
    });
    m.addItem("Select None", KeyEvent.VK_ESCAPE, 0, new ActionHandler() {
      private SelectNoneOper r;

      public boolean shouldBeEnabled() {
        r = new SelectNoneOper();
        return r.valid();
      }

      public void go() {
        r.perform();
        repaint();
      }
    });

    m.addSeparator();

    m.addItem("Move Backward", KeyEvent.VK_OPEN_BRACKET, 0,
        new ActionHandler() {
          private Command r;

          public boolean shouldBeEnabled() {
            r = new AdjustSlotsReversible(1, false);
            return r.valid();
          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });
    m.addItem("Move Forward", KeyEvent.VK_CLOSE_BRACKET, 0,
        new ActionHandler() {
          private Command r;

          public boolean shouldBeEnabled() {
            r = new AdjustSlotsReversible(-1, false);
            return r.valid();
          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });
    m.addItem("Move to Rear", KeyEvent.VK_OPEN_BRACKET, CTRL,
        new ActionHandler() {
          private Command r;

          public boolean shouldBeEnabled() {
            r = new AdjustSlotsReversible(1, true);
            return r.valid();
          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });
    m.addItem("Move to Front", KeyEvent.VK_CLOSE_BRACKET, CTRL,
        new ActionHandler() {
          private Command r;

          public boolean shouldBeEnabled() {
            r = new AdjustSlotsReversible(-1, true);
            return r.valid();
          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });

    m.addSeparator();

    m.addItem("Group", KeyEvent.VK_G, CTRL, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = GroupObject.getGroupReversible();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });
    m.addItem("Ungroup", KeyEvent.VK_U, CTRL, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = GroupObject.getUnGroupReversible();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });

    m.addSeparator();
    m.addItem("Move Left", KeyEvent.VK_LEFT, 0, new CursorMoveHandler(0));
    m.addItem("Move Right", KeyEvent.VK_RIGHT, 0, new CursorMoveHandler(1));
    m.addItem("Move Up", KeyEvent.VK_UP, 0, new CursorMoveHandler(2));
    m.addItem("Move Down", KeyEvent.VK_DOWN, 0, new CursorMoveHandler(3));
    m.addSeparator();
    m.addItem("Flip Horizontally", KeyEvent.VK_H, SHIFT | CTRL,
        new ActionHandler() {
          private Command r;

          public boolean shouldBeEnabled() {
            r = new FlipReversible(true);
            return r.valid();
          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });
    m.addItem("Flip Vertically", KeyEvent.VK_V, SHIFT | CTRL,
        new ActionHandler() {
          private Command r;

          public boolean shouldBeEnabled() {
            r = new FlipReversible(false);
            return r.valid();
          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });

    m.addItem("Rotate", KeyEvent.VK_R, CTRL, new ActionHandler() {
      private RotateOper r;

      public boolean shouldBeEnabled() {
        r = new RotateOper();
        return r.shouldBeEnabled();
      }

      public void go() {
        MouseOper.setOperation(r);
      }
    });
    m.addItem("Reset Rotate", KeyEvent.VK_R, CTRL | SHIFT, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = RotateOper.getResetOper();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
        repaint();
      }
    });
    m.addItem("Scale", KeyEvent.VK_E, CTRL, new ActionHandler() {
      private ScaleOper oper;

      public boolean shouldBeEnabled() {
        oper = new ScaleOper();
        return oper.shouldBeEnabled();
      }

      public void go() {
        MouseOper.setOperation(oper);
      }
    });
    m.addItem("Reset Scale", KeyEvent.VK_E, CTRL | SHIFT, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = ScaleOper.getResetOper();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
        repaint();
      }
    });

    // -----------------------------------
    m.addMenu("View", projectMustBeOpenHandler);
    m.addItem("Zoom In", KeyEvent.VK_EQUALS, CTRL, new ActionHandler() {
      public void go() {
        doAdjustZoom(-1);
      }

      public boolean shouldBeEnabled() {
        return sZoomFactor < 20;
      }
    });

    m.addItem("Zoom Out", KeyEvent.VK_MINUS, CTRL, new ActionHandler() {
      public void go() {
        doAdjustZoom(1);
      }

      public boolean shouldBeEnabled() {
        return sZoomFactor > .1f;
      }
    });
    m.addItem("Zoom Reset", KeyEvent.VK_0, CTRL, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return sZoomFactor != 1;
      }

      public void go() {
        doAdjustZoom(0);
      }
    });
    m.addSeparator();
    m.addItem("Snap to Grid", KeyEvent.VK_G, CTRL | SHIFT, new ActionHandler() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = Grid.getOper();
        return r.valid();

      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });

    // m.addItem("Grid...", 0, 0, new ItemHandler() {
    // public void go() {
    // Grid.showParams();
    // }
    // });

    m.addSeparator();
    m.addItem("Polygon Vertices", KeyEvent.VK_P, CTRL | SHIFT,
        new ActionHandler() {
          public void go() {
            PolygonObject.showVertices ^= true;
            repaint();
          }
        });

    // -----------------------------------
    m.addMenu("Objects", projectMustBeOpenHandler);
    m.addItem("Add Sprite", KeyEvent.VK_S, 0, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return sSelectedSprite != null;
      }

      public void go() {
        MouseOper.setOperation(new AddSpriteOper());
      }
    });
    m.addItem("Select Atlas", KeyEvent.VK_T, CTRL, new ActionHandler() {
      public void go() {
        doSelectAtlas(null);
      }
    });

    m.addSeparator();
    m.addItem("Add Polygon", KeyEvent.VK_P, 0, new ActionHandler() {
      public void go() {
        MouseOper.setOperation(new EdPolygonOper(items().size(), 0, false));
      }
    });
    m.addItem("Delete Vertex", KeyEvent.VK_DELETE, 0,
        PolygonObject.DELETE_VERTEX);
    m.addItem("Previous Vertex", KeyEvent.VK_COMMA, 0,
        PolygonObject.PREV_VERTEX);
    m.addItem("Next Vertex", KeyEvent.VK_PERIOD, 0, PolygonObject.NEXT_VERTEX);
    m.addItem("Toggle Vertex Direction", KeyEvent.VK_SLASH, 0,
        PolygonObject.TOGGLE_VERTEX_DIR);

    // ---------------------------------
    m.addItem("Add Rectangle", KeyEvent.VK_R, 0, new ActionHandler() {

      public void go() {
        MouseOper.setOperation(RectangleObject.buildNewObjectOperation());
      }
    });

    unimp("Palette menu");
    // m.addMenu("Palette", projectMustBeOpenHandler);
    // m.addItem("Color Objects", KeyEvent.VK_BACK_QUOTE, 0, new ItemHandler() {
    // private Reversible r;
    //
    // public boolean isEnabled() {
    // r = pPanel.getColorReversible(pPanel.getSelectedColor());
    // return r.valid();
    // }
    //
    // public void go() {
    // editor().registerPush(r);
    // perform(r);
    // repaint();
    // }
    // });
    // m.addItem("Darker", KeyEvent.VK_LEFT, CTRL, new ColorItemHandler(
    // ColorItemHandler.STD, -1));
    // m.addItem("Lighter", KeyEvent.VK_RIGHT, CTRL, new ColorItemHandler(
    // ColorItemHandler.STD, 1));
    // m.addItem("Less Transparent", KeyEvent.VK_LEFT, CTRL | SHIFT,
    // new ColorItemHandler(ColorItemHandler.ALPHA, -1));
    // m.addItem("More Transparent", KeyEvent.VK_RIGHT, CTRL | SHIFT,
    // new ColorItemHandler(ColorItemHandler.ALPHA, 1));
    // m.addItem("Previous Favorite", KeyEvent.VK_LEFT, SHIFT,
    // new ColorItemHandler(ColorItemHandler.FAV, -1));
    // m.addItem("Next Favorite", KeyEvent.VK_RIGHT, SHIFT, new
    // ColorItemHandler(
    // ColorItemHandler.FAV, 1));
    // m.addItem("Grab Color of Object", KeyEvent.VK_BACK_QUOTE, SHIFT,
    // new ItemHandler() {
    // public void go() {
    // pPanel.setSelectedColor(grabIndex, false);
    // }
    //
    // public boolean isEnabled() {
    // boolean valid = false;
    //
    // int[] sel = items().getSelected();
    // for (int i = 0; i < sel.length; i++) {
    // EdObject obj = item(sel[i]);
    // Color color = obj.getColor();
    // if (color != null) {
    // if (!color.equals(color())) {
    // valid = true;
    // unimp("set grab index");
    // // grabIndex = color;
    // break;
    // }
    // }
    // }
    // return valid;
    // }
    //
    // private int grabIndex;
    // });
    //
    // -----------------------------------
    m.addMenu("Layer", projectMustBeOpenHandler);
    m.addItem("Next", KeyEvent.VK_EQUALS, 0, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return sScriptSet.size() > 1;
      }

      public void go() {
        sScriptSet.setCursor(MyMath.myMod(sScriptSet.getCursor() + 1,
            sScriptSet.size()));
        repaint();
      }
    });
    m.addItem("Previous", KeyEvent.VK_MINUS, 0, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return sScriptSet.size() > 1;
      }

      public void go() {
        sScriptSet.setCursor(MyMath.myMod(sScriptSet.getCursor() - 1,
            sScriptSet.size()));
        repaint();
      }
    });
    m.addSeparator();
    m.addItem("Open Set", KeyEvent.VK_O, CTRL | SHIFT, new ActionHandler() {
      public void go() {
        doOpenSet(null);
        repaint();
      }
    });

    sRecentScriptSetsMenuItem = new RecentFiles.Menu("Open Recent Set", null,
        new ActionHandler() {
          public void go() {
            doOpenSet(project().recentScriptSets().getCurrentFile());
            repaint();
          }
        });
    m.addItem(sRecentScriptSetsMenuItem);
    m.addItem("Save Set As...", KeyEvent.VK_S, CTRL | SHIFT,
        new ActionHandler() {
          public void go() {
            doSaveSet();
            repaint();
          }
        });
    m.addSeparator();

    // -----------------------------------

    m.addMenu("Project");
    m.addItem("New Project", 0, 0, new ActionHandler() {
      public void go() {
        doNewProject();
        ScriptEditor.repaint();
      }
    });
    m.addItem("Open Project", 0, 0, new ActionHandler() {
      public void go() {
        File f = AppTools.chooseFileToOpen("Open Project", ScriptProject.FILES,
            null);
        if (f != null) {
          openProject(f);
          repaint();
        }
      }
    });
    m.addItem(new RecentFiles.Menu("Open Recent Project", sRecentProjects,
        new ActionHandler() {
          @Override
          public void go() {
            openProject(sRecentProjects.getCurrentFile());
            repaint();
          }
        }));

    m.addItem("Close Project", 0, 0, new ActionHandler() {
      public boolean shouldBeEnabled() {
        return isProjectOpen();
      }

      public void go() {
        doCloseProject();
        ScriptEditor.repaint();
      }
    });
  }

  public static boolean doCloseProject() {
    do {
      if (!isProjectOpen())
        break;

      try {
        if (!flushAll())
          break;
        writeProjectDefaults();
        sProject.flush();
        sProject = null;
        MouseOper.setEnabled(false);
        sRecentScriptsMenuItem.setRecentFiles(null);
        sRecentScriptSetsMenuItem.setRecentFiles(null);
        unimp("update recent atlas list");
        // sRecentAtlases.setAlias(null);
        sRecentProjects.setCurrentFile(null);
        sAtlasCB.removeAllItems();
        sAtlasCB.setEnabled(false);
        sAtlasSelectButton.setEnabled(false);
        sScriptSet = null;
        selectAtlas(null);
      } catch (JSONException e) {
        AppTools.showError("closing project", e);
      } catch (IOException e) {
        AppTools.showError("closing project", e);
      }
    } while (false);
    return !isProjectOpen();
  }

  private static class SelectNoneOper {
    public SelectNoneOper() {
      slots = items().getSelected();
    }

    public void perform() {
      items().clearAllSelected();
      repaint();
    }

    public boolean valid() {
      return slots.length > 0;
    }

    private int[] slots;
  }

  // private static class ColorItemHandler extends ItemHandler {
  // public static final int STD = 0, ALPHA = 1, FAV = 2;
  //
  // private static final boolean db = false;
  //
  // private int amt;
  // private Reversible r;
  // private int mode;
  //
  // public ColorItemHandler(int mode, int amt) {
  // this.mode = mode;
  // this.amt = amt;
  // }
  //
  // @Override
  // public boolean isEnabled() {
  //
  // int curr = color();
  // int next = calcNextColor();
  //
  // if (db)
  // pr("isEnabled(" + this + "): curr=" + curr + " next=" + next);
  //
  // if (curr == next)
  // return false;
  //
  // r = pPanel.getColorReversible(next);
  // return r.valid();
  // }
  //
  // @Override
  // public void go() {
  // editor().registerPush(r);
  // perform(r);
  // repaint();
  // }
  //
  // private int calcNextColor() {
  // int ci = color();
  // int nc = 0;
  //
  // int shade = ci % Palette.COLOR_SET_SIZE;
  // int base = ci - shade;
  //
  // switch (mode) {
  // case STD: {
  // int currBright = Palette.OFFSET_STDBRIGHTNESS;
  // if (shade < Palette.OFFSET_TRANSPARENT)
  // currBright = shade;
  // nc = MyMath.clamp(currBright + amt, 0, Palette.OFFSET_TRANSPARENT - 1);
  //
  // }
  // nc += base;
  // break;
  // case ALPHA: {
  // int currAlpha = 0; // Palette.OFFSET_STDBRIGHTNESS;
  // if (shade >= Palette.OFFSET_TRANSPARENT)
  // currAlpha = 1 + (shade - Palette.OFFSET_TRANSPARENT);
  // nc = currAlpha + amt;
  // nc = MyMath.clamp(nc, 0, Palette.TRANSP_COUNT);
  // if (nc == 0)
  // nc = Palette.OFFSET_STDBRIGHTNESS;
  // else
  // nc = Palette.OFFSET_TRANSPARENT + (nc - 1);
  // }
  // nc += base;
  // break;
  // case FAV: {
  // int[] fv = pPanel.getFavoriteColors();
  // int dir = amt;
  // int currSlot = pPanel.favoriteSlot();
  // if (currSlot < 0) {
  // dir = 1;
  // }
  //
  // nc = color();
  // for (int j = currSlot + dir; j >= 0 && j < fv.length; j += dir) {
  // if (fv[j] >= 0) {
  // nc = fv[j];
  // break;
  // }
  // }
  // }
  // break;
  // }
  //
  // return nc;
  // }
  //
  // }

  private static void openNextFile() {
    File currentFile = editor().getFile();
    TreeSet<File> candidates = new TreeSet();
    File[] array = currentFile.getParentFile().listFiles(
        (FilenameFilter) SCRIPT_FILEFILTER);
    candidates.addAll(Arrays.asList(array));
    if (candidates.isEmpty())
      return;
    File candidate = candidates.higher(currentFile);
    if (candidate == null)
      candidate = candidates.first();
    if (candidate.equals(currentFile))
      return;
    sScriptSet.insert(sScriptSet.getCursor(), null);
    openScript(candidate);
  }

  /**
   * Process "Project:new" menu item
   */
  private static void doNewProject() {
    do {

      File projFile = AppTools.chooseFileToSave("Create New Project",
          ScriptProject.FILES, null);
      if (projFile == null)
        break;

      {

        try {
          ScriptProject newProj;
          newProj = new ScriptProject(projFile);
          newProj.flush();

          openProject(projFile);
        } catch (IOException e) {
          AppTools.showError("opening project", e);
          break;
        }
      }

    } while (false);
  }

  private static void doSelectAtlas(File fx) {

    final boolean db = false;

    do {
      if (db)
        pr("selectAtlas");

      if (fx == null)
        fx = AppTools.chooseFileToOpen("Select Atlas", Atlas.DATA_FILES,
            sProject.replaceIfMissing(sAtlasPanel.file()));

      if (fx == null)
        break;
      if (notInProject(fx))
        break;

      selectAtlas(fx);
    } while (false);
  }

  private static void selectAtlas(File f) {
    if (f != null)
      sProject.recentAtlases().setCurrentFile(f);
  }

  private static void doNewScript() {
    // If current editor is an anonymous script, replace it with a fresh one
    // (after verifying with user);
    // otherwise, insert a fresh one
    if (editor().hasName()) {
      sScriptSet.insert(sScriptSet.getCursor(), null);
    } else {
      if (flush(true))
        disposeOfCurrentEditor();
    }
  }

  private static void doOpenSet(File f) {

    do {
      if (!flushAll())
        break;

      if (f == null)
        f = AppTools.chooseFileToOpen("Open Set", SCRIPT_SET_FILEFILTER,
            sProject.replaceIfMissing(sProject.recentScriptSets()
                .getCurrentFile()));
      if (f == null)
        break;

      if (notInProject(f))
        break;
      try {

        ScriptSet ss = new ScriptSet(sProject.directory(), new JSONObject(
            Files.readString(f)));

        setScriptSet(ss);
        setRecentSetPath(f);

      } catch (Throwable e) {
        AppTools.showError("reading set", e);
      }

    } while (false);
    repaint();
  }

  private static void doSaveSet() {

    /**
     * <pre>
     * 
     * [] flush files 
     * [] if any are still orphans, or are modified, abort
     * [] serialize layers into string 
     * [] write string to file
     * 
     * </pre>
     * 
     */
    int origLayer = sScriptSet.getCursor();
    do {

      if (!flushAll())
        break;

      File f = AppTools.chooseFileToSave("Save Set", SCRIPT_SET_FILEFILTER,
          sProject.replaceIfMissing(sProject.recentScriptSets()
              .getCurrentFile()));

      if (f == null)
        break;
      if (notInProject(f))
        break;

      setRecentSetPath(f);

      try {
        Files.writeStringToFileIfChanged(f, sScriptSet.encode().toString(2));
      } catch (Throwable e) {
        AppTools.showError("writing script set", e);
      }
    } while (false);
    sScriptSet.setCursor(origLayer);
    repaint();
  }

  /**
   * Add script set file to 'most recently used' set, then use 'null' so it
   * shows up in menu bar (we don't store 'current set' in our state)
   * 
   * @param f
   *          script set file
   */
  private static void setRecentSetPath(File f) {
    sProject.setLastSetPath(f);
    sProject.setLastSetPath(null);
  }

  public static SpriteObject lastSprite() {
    return sSelectedSprite;
  }

  /**
   * Perform operation: change selected sprites
   * 
   * @param spriteInfo
   *          object specifying new atlas and sprite
   */
  public static void doSetSpritesTo(SpriteObject si) {
    sSelectedSprite = si;

    final SpriteObject si2 = si;
    // final SpriteObject spriteInfo = si;
    EditSelectedOper r = new EditSelectedOper() {

      @Override
      public void perform() {
        for (int i = 0; i < nSelected(); i++) {
          EdObject obj = items().get(slot(i));
          if (!(obj instanceof SpriteObject))
            continue;
          SpriteObject s = (SpriteObject) items().get(slot(i));
          s.setSprite(si2);
        }
      }

      @Override
      public boolean valid() {
        return nSelected() > 0;
      }
    };

    if (r.nSelected() > 0) {
      editor().registerPush(r);
      perform(r);
      repaint();
    } else {
      MouseOper.setOperation(new AddSpriteOper());
    }
  }

  public static EdObjectArray clipboard() {
    return sClipboard;
  }

  /**
   * Set clipboard
   */
  public static void setClipboard(EdObjectArray newClip) {
    sClipboard = frozen(newClip);
  }

  private static boolean flushAll() {
    editor().resetUndo();
    int currLayer = sScriptSet.getCursor();
    boolean success = true;
    for (int i = 0; i < sScriptSet.size(); i++) {
      sScriptSet.setCursor(i);
      if (!flush(true)) {
        success = false;
        break;
      }
    }
    if (success) {
      sScriptSet.setCursor(currLayer);
    }
    return success;
  }

  public static void init(JComponent p) {

    MouseOper.setEnabled(false);

    JPanel ac = new JPanel(new BorderLayout());
    p.add(ac, BorderLayout.EAST);

    sAtlasPanel = new AtlasPanel();
    ac.add(sAtlasPanel, BorderLayout.CENTER);
    sPalettePanel = new PalettePanel();
    ac.add(sPalettePanel, BorderLayout.SOUTH);

    {
      JPanel pnl = new JPanel();

      sAtlasCB = new JComboBox();
      sAtlasCB.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ev) {
          final boolean db = false;
          if (db)
            pr("atlasCB event received");
          File af = null;

          if (isProjectOpen())
            af = sProject.recentAtlases().getCurrentFile();
          if (sAtlasPanel != null)
            sAtlasPanel.setAtlas(sProject, af);
        }
      });

      sAtlasSelectButton = new JButton("Open");
      sAtlasSelectButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doSelectAtlas(null);
        }
      });

      pnl.add(sAtlasSelectButton);

      pnl.add(sAtlasCB);
      ac.add(pnl, BorderLayout.NORTH);
    }

    addMenus();

    {
      File base = sRecentProjects.getMostRecentFile();
      if (base != null && !ScriptProject.FILES.accept(base))
        base = null;
      if (base != null && base.exists()) {
        openProject(base);
      }
    }

    {
      JPanel pnl = new JPanel(new BorderLayout());
      EditorPanelGL editorPanel = new EditorPanelGL(sInfoPanel);

      MouseEventGenerator m = new MouseEventGenerator();
      m.setView(editorPanel, editorPanel.getComponent());
      m.setListener(new UserEvent.Listener() {
        @Override
        public void handleUserEvent(UserEvent event) {
          ScriptEditor.handleUserEvent(event);
        }
      });

      sEditorPanelComponent = editorPanel.getComponent();
      pnl.add(sEditorPanelComponent, BorderLayout.CENTER);

      pnl.add(sInfoPanel, BorderLayout.SOUTH);
      p.add(pnl, BorderLayout.CENTER);
    }

    MouseOper.setDefaultOperation(new DefaultMouseOper());

    // add mouse edit operations, in the order they
    // are to be tested for activation
    MouseOper.add(new MoveFocusOper());
    MouseOper.add(new MouseOperSelectItems());
    repaint();
  }

  public static void setInfo(EdObject obj) {
    if (obj == null)
      setInfo("");
    else
      setInfo(obj.getInfoMsg());
  }

  public static void setInfo(String msg) {
    sInfoPanel.setMessage(msg);
  }

  public static ConfigSet.Interface CONFIG = new ConfigSet.Interface() {
    private static final String PROJECTS_TAG = "RECENTPROJECTS";

    @Override
    public void readFrom(JSONObject map) throws JSONException {
      sRecentProjects.restore(map, PROJECTS_TAG);
      sInfoPanel.setOriginShowing(map.optBoolean("ORIGIN", true));
      sInfoPanel.setFaded(map.optBoolean("FADED"));
      sZoomFactor = (float) map.optDouble("ZOOM", 1);
      IPoint focus = IPoint.opt(map, "FOCUS");
      if (focus != null)
        setFocus(focus);
      if (map.optBoolean("POLYVERTS"))
        PolygonObject.showVertices = true;
    }

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      sRecentProjects.put(map, PROJECTS_TAG);
      map.put("ORIGIN", sInfoPanel.isOriginShowing());
      map.put("FADED", sInfoPanel.isFaded());
      map.put("ZOOM", sZoomFactor);
      sFocus.put(map, "FOCUS");
      map.put("POLYVERTS", PolygonObject.showVertices);
    }

  };

  private static void writeProjectDefaults() throws JSONException {
    sProject.getDefaults().put("LAYERS", sScriptSet.encode());
    ASSERT(sPalettePanel != null);
    sProject.getDefaults().put("PALETTE", sPalettePanel.encodeDefaults());
  }

  public static void setFocus(IPoint trans) {
    sFocus = new IPoint(trans);
  }

  private static void readProjectDefaults() {
    if (sPalettePanel == null) {
      warning("project panel null");
    } else {
      sPalettePanel.decodeDefaults(sProject.getDefaults().optJSONObject(
          "PALETTE"));
    }

    ScriptSet defaultScriptSet = new ScriptSet(sProject.directory());
    try {
      JSONObject map = sProject.getDefaults().optJSONObject("LAYERS");
      if (map != null) {
        defaultScriptSet = new ScriptSet(sProject.directory(), map);
      }
    } catch (JSONException e) {
      AppTools.showError("Problem reading project defaults", e);
    }
    setScriptSet(defaultScriptSet);
  }

  /**
   * Open project
   * 
   * @param f
   * @throws IOException
   */
  private static void openProject(File f) {
    final boolean db = false;

    do {
      if (db)
        pr("openProject: " + f);

      if (!doCloseProject())
        break;
      try {
        ScriptProject newProject = new ScriptProject(f);

        if (db)
          pr("ScrMain, openProject " + f + ":" + newProject);

        sProject = newProject;
        MouseOper.setEnabled(true);
        sRecentScriptsMenuItem.setRecentFiles(sProject.recentScripts());
        sRecentScriptSetsMenuItem.setRecentFiles(sProject.recentScriptSets());
        unimp("update recent atlas list");
        // sRecentAtlases.setAlias(mProject.recentAtlases());
        sRecentProjects.setCurrentFile(sProject.file());

        unimp("have combo box adjust for recent atlases");
        /*
         * mProject.recentAtlases().addListener(new RecentFiles.Listener() {
         * 
         * @Override public void mostRecentFileChanged(RecentFiles recentFiles)
         * { JComboBox cb = atlasCB; cb.removeAllItems(); for (int i = 0; i <
         * recentFiles.size(); i++) { unimp("where do we handle such events?");
         * cb.addItem(recentFiles.get(i)); // )new //
         * ComboBoxItem(recentFiles.get(i))); } } });
         */

        sAtlasCB.setEnabled(true);
        sAtlasSelectButton.setEnabled(true);
        readProjectDefaults();

      } catch (IOException e) {
        AppTools.showError("Problem opening project", e);
      }
    } while (false);
  }

  /**
   * Get zoom factor for editor window
   */
  public static float zoomFactor() {
    return sZoomFactor;
  }

  /**
   * Get the structure describing the current editor layers
   */
  public static ScriptSet layers() {
    assertProjectOpen();
    return sScriptSet;
  }

  public static ScriptProject project() {
    return sProject;
  }

  public static void perform(Command op) {
    final boolean db = DBUNDO;
    if (db)
      pr("perform reversible:\n" + op);

    op.perform();
    repaint();
  }

  private static void assertProjectOpen() {
    if (!isProjectOpen())
      throw new IllegalStateException();
  }

  /**
   * Determine if the current script can be closed. It can unless it's the only
   * script open, it has no name, and is empty
   */
  private static boolean currentScriptCloseable() {
    return sScriptSet.size() > 1 || editor().hasName()
        || !editor().getScript().items().isEmpty();
  }

  /**
   * Make an object editable if it is the only selected object (and current
   * operation allows editabler highlighting). We perform this operation with
   * each refresh, since this is simpler than trying to maintain the editable
   * state while the editor objects undergo various editing operations. Also,
   * update the last editable object type to reflect the editable object (if one
   * exists)
   */
  private static void updateEditableObjectStatus() {
    items().updateEditableObjectStatus(
        MouseOper.getOperation().allowEditableObject());
  }

  // ------------------- Instance methods -----------------------------

  public ScriptEditor() {
    assertProjectOpen();
    mScript = new Script(project(), null);
    resetUndo();
  }

  public File getFile() {
    return getScript().getFile();
  }

  private boolean hasName() {
    return getScript().hasName();
  }

  public void render(GLPanel panel, boolean toBgnd) {
    EdObjectArray items = mScript.items();
    for (EdObject obj : items) {
      panel.lineWidth(1.5f / zoomFactor());
      if (toBgnd) {
        boolean f = obj.isSelected();
        obj.setSelected(false);
        obj.render(panel);
        obj.setSelected(f);
      } else
        obj.render(panel);
    }
  }

  private void doSave(File initialPath, boolean alwaysAskForPath,
      boolean alwaysVerifyReplaceExisting) {
    File f = initialPath;
    if (f == null)
      f = getFile();
    if (f == null || alwaysAskForPath) {
      f = AppTools.chooseFileToSave("Save Script", SCRIPT_FILEFILTER,
          sProject.replaceIfMissing(sProject.recentScripts().getCurrentFile()));
      if (f == null)
        return;
      if (notInProject(f))
        return;
      alwaysVerifyReplaceExisting = true;
    }

    if (f.exists() && (alwaysVerifyReplaceExisting || !f.equals(getFile()))) {
      int result = JOptionPane.showConfirmDialog(
          AppTools.frame(),
          "Replace existing file: '"
              + Files.fileWithinDirectory(f, sProject.directory()) + "'?",
          "Save Script", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.CANCEL_OPTION
          || result == JOptionPane.CLOSED_OPTION)
        return;
    }

    if (saveAs(f)) {
      sProject.setLastScriptPath(f);
      sScriptSet.setName(sScriptSet.getCursor(), f);
      setChanges(0);
    }
  }

  private void resetUndo() {
    mUndoCursor = 0;
    mUndoList.clear();
  }

  public boolean modified() {
    return mChangesSinceSaved != 0;
  }

  /**
   * Adjust the 'changes since saved' variable. Refreshes infoPanel if modified.
   * 
   * @param n
   *          zero, to clear it to zero; else, amount to add to existing value
   */
  private void setChanges(int n) {
    int newVal = mChangesSinceSaved;
    if (n == 0) {
      newVal = 0;
    } else
      newVal += n;

    if (newVal != mChangesSinceSaved) {
      mChangesSinceSaved = newVal;
      sInfoPanel.refresh(editor(), project(), sScriptSet);
    }
  }

  public void setState(ScriptEditorState state) {
    Script script = getScript();
    script.setItems(state.getObjects().getMutableCopy());
    script.items().setSelected(state.getSelectedSlots());
    setClipboard(state.getClipboard());
  }

  // ------------------------- Undo Stuff ---------------------------

  private static final boolean DBUNDO = false;

  public void registerPush(Command op) {
    final boolean db = DBUNDO;
    final boolean db2 = db && false;

    if (db)
      pr("registerPush: " + op);

    int trim = mUndoList.size() - mUndoCursor;
    if (trim > 0) {
      if (db2)
        pr(" removing " + trim + " 'redoable' operations from list");
      remove(mUndoList, mUndoCursor, trim);
    }
    mUndoList.add(op);
    mUndoCursor++;
    editor().setChanges(1);

    if (db2)
      pr(" undoCursor " + mUndoCursor + " of total " + mUndoList.size());

    // limit # undo operations to something reasonable
    trim = mUndoCursor - 50;

    if (trim > 0) {
      remove(mUndoList, 0, trim);
      mUndoCursor -= trim;
    }
    updateUndoLabels();
  }

  /**
   * Peek at topmost reversible on stack
   * 
   * @return topmost reversible, or null if stack is empty
   */
  public Command registerPeek() {
    Command r = null;
    if (mUndoCursor > 0)
      r = mUndoList.get(mUndoCursor - 1);
    return r;
  }

  /**
   * Pop topmost reversible from stack
   * 
   * @return topmost reversible
   */
  public Command registerPop() {
    final boolean db = DBUNDO;

    Command rev = registerPeek();
    if (db)
      pr("registerPop: " + rev + " undoCursor=" + mUndoCursor);

    mUndoCursor--;
    int trim = mUndoList.size() - mUndoCursor;
    if (db)
      pr(" trim=" + trim + ", undoCursor=" + mUndoCursor + ", undoList.size="
          + mUndoList.size());

    if (trim > 0) {
      if (db)
        pr(" removing " + trim + " 'redoable' operations from list");
      remove(mUndoList, mUndoCursor, trim);
    }
    setChanges(-1);
    updateUndoLabels();
    return rev;
  }

  private String commandDescription(Command r) {
    String description = r.getDescription();
    if (description == null)
      description = "Command";
    return description;
  }

  private void updateUndoLabels() {
    {
      String lbl = "Undo";
      Command r = registerPeek();
      if (r != null)
        lbl = "Undo " + commandDescription(r);
      sUndoMenuItem.setText(lbl);
    }
    {
      String lbl = "Redo";
      Command r = editor().getRedoOper();
      if (r != null) {
        lbl = "Redo " + commandDescription(r);
      }
      sRedoMenuItem.setText(lbl);
    }
  }

  private Command getRedoOper() {
    Command oper = null;
    if (mUndoCursor < mUndoList.size()) {
      oper = mUndoList.get(mUndoCursor);
    }
    return oper;
  }

  private void doRedo() {
    final boolean db = DBUNDO;

    MouseOper.clearOperation();
    Command oper = editor().getRedoOper();
    if (oper != null) {
      if (db)
        pr("redo: " + oper);
      mUndoCursor++;
      setChanges(1);
      oper.perform();
      updateUndoLabels();
    }
  }

  private void doUndo() {
    final boolean db = DBUNDO;
    if (db && false)
      pr("doUndo, undoCursor " + mUndoCursor + " of total " + mUndoList.size());

    MouseOper.clearOperation();

    if (mUndoCursor > 0) {
      // unselectAll();
      Command oper = mUndoList.get(mUndoCursor - 1);
      mUndoCursor--;
      setChanges(-1);
      if (db)
        pr("undo: " + oper);
      oper.getReverse().perform();
      updateUndoLabels();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(nameOf(this));
    File f = getFile();
    if (f == null)
      sb.append(" <orphan>");
    else
      sb.append(" " + Files.fileWithinDirectory(f, sProject.directory()));
    return sb.toString();
  }

  private boolean flushAux(boolean askUser) {

    if (!modified())
      return true;
    if (true) {
      warning("never asking user if has name");
      if (hasName())
        askUser = false;
    }
    if (askUser) {
      String prompt;
      if (!hasName())
        prompt = "Save changes to new file?";
      else {
        prompt = "Save changes to '"
            + Files.fileWithinDirectory(getFile(), sProject.directory()) + "'?";
      }
      int code = JOptionPane.showConfirmDialog(AppTools.frame(), prompt,
          "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.WARNING_MESSAGE);

      if (code == JOptionPane.CANCEL_OPTION
          || code == JOptionPane.CLOSED_OPTION)
        return false;
      if (code == JOptionPane.NO_OPTION) {
        return true;
      }
    }
    doSave(null, false, false);
    return !modified();
  }

  /**
   * Save script
   * 
   * @param f
   * @return true if successfully saved
   */
  private boolean saveAs(File f) {
    boolean success = false;
    try {
      // Script s = new Script(sProject);
      unimp("disallow saving as script already in set");
      mScript.setFile(f);
      sScriptSet.setName(sScriptSet.getCursor(), mScript.getFile());
      mScript.write();
      success = true;
    } catch (IOException e) {
      AppTools.showError("saving script", e);
    } catch (JSONException e) {
      AppTools.showError("saving script", e);
    }
    return success;
  }

  public Script getScript() {
    return mScript;
  }

  // --- New MouseOper handling

  private static void handleUserEvent(UserEvent event) {
    // event.printProcessingMessage("ScriptEditor processing");

    // Save most recent event for rendering cursor
    mLastMouseEvent = event;

    MouseOper.getOperation().processUserEvent(event);

    // Request a refresh of the editor view after any event
    repaint();
  }

  // private static MouseOper mCurrentOperation;
  /* private */static UserEvent mLastMouseEvent;

  // --- Class fields
  private static Component sEditorPanelComponent;
  private static float sZoomFactor = 1.0f;
  private static SpriteObject sSelectedSprite;
  private static IPoint sFocus = new IPoint();
  private static EdObjectArray sClipboard = frozen(new EdObjectArray());
  private static ScriptProject sProject;
  private static ScriptSet sScriptSet;
  private static AtlasPanel sAtlasPanel;
  private static PalettePanel sPalettePanel;
  private static JComboBox sAtlasCB;
  private static JButton sAtlasSelectButton;
  private static InfoPanel sInfoPanel = new InfoPanel();
  private static RecentFiles sRecentProjects = new RecentFiles(null);
  private static RecentFiles.Menu sRecentScriptsMenuItem;
  private static RecentFiles.Menu sRecentScriptSetsMenuItem;
  private static JMenuItem sUndoMenuItem, sRedoMenuItem;
  private static MyFileFilter SCRIPT_SET_FILEFILTER = new MyFileFilter(
      "Script project files", "set");
  private static MyFileFilter SCRIPT_FILEFILTER = new MyFileFilter(
      "Script files", "scr");

  // --- Instance fields

  private int mChangesSinceSaved;
  private ArrayList<Command> mUndoList = new ArrayList();
  private int mUndoCursor;
  // The script being edited by this editor. We could make the editor a subclass
  // of Script, but we'll favor composition over inheritance, at the expense of
  // some extra methods to call Script method counterparts (e.g. hasName())
  private final Script mScript;

}
