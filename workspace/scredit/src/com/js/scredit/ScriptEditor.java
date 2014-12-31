package com.js.scredit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tex.*;
import apputil.*;

import com.js.basic.*;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;
import static apputil.MyMenuBar.*;

/*
 [] should NEW always insert layer? probably, if current is not orphan.
 [] figure out relationship between 'Reversible' operations and MouseEditOpers;
 maybe they are orthogonal.
 */
public class ScriptEditor {

  public ScriptEditor() {
    MouseOper.addListener(new MouseOper.Listener() {
      @Override
      public void operationChanged(MouseOper oper) {
        if (selectNoneMenuItem != null) {
          if (oper != null)
            selectNoneMenuItem.setText("Stop " + oper);
          else
            selectNoneMenuItem.setText("Select None");
        }
      }
    });
    resetUndo();
  }

  /**
   * If current script defined, and has changes, save them.
   * 
   * Doesn't save changes if there is an additional copy of this editor in
   * another slot.
   * 
   * @param askUser
   *          if true, and save required, asks user
   * @return true if success (false if error, or user cancelled save)
   */
  private static boolean flush(boolean askUser) {

    boolean success = false;
    outer: do {
      if (!isProjectOpen() || !editor.modified()) {
        success = true;
        break;
      }

      // if there is a copy of this editor in another layer,
      // don't save before closing.
      if (editor.path != null) {
        for (int i = 0; i < layers.size(); i++) {
          if (i != layers.currentSlot()
              && editor.path().equals(layers.layer(i).path())) {
            pr("copy of current editor (" + layers.currentSlot()
                + ") exists in layer " + i + ", not flushing");
            success = true;
            break outer;
          }
        }
      }

      if (allowQuitWithoutSave()) {
        // warn("skipping save warning during test");
        if (askUser) {
          success = true;
          break;
        }
      }

      if (askUser) {
        String prompt;
        if (isOrphan())
          prompt = "Save changes to new file?";
        else {
          prompt = "Save changes to '"
              + new RelPath(mProject.directory(), editor.path).display()
              // + RelPath.toString(project.directory(), editor.path)
              + "'?";
        }
        int code = JOptionPane.showConfirmDialog(AppTools.frame(), prompt,
            "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (code == JOptionPane.CANCEL_OPTION
            || code == JOptionPane.CLOSED_OPTION)
          break;
        if (code == JOptionPane.NO_OPTION) {
          success = true;
          break;
        }
      }
      editor.doSave(null, false, false);
      success = !editor.modified();
    } while (false);
    return success;
  }

  private static boolean currentModified() {
    return editor.modified();
  }

  public static Color color() {
    return pPanel.getSelectedColor();
  }

  /**
   * Get focus of editor window
   * 
   * @return world location of center of editor window
   */
  public static IPoint focus() {
    return focus;
  }

  /**
   * Close current script if it exists
   */
  private static void close() {
    do {

      if (!isProjectOpen())
        break;
      layers.resetCurrent();

      // clearCurrentSet();

    } while (false);
  }

  /**
   * Read script into current layer
   * 
   * @return true if successful
   */
  private static boolean open(File f) {
    boolean success = false;
    do {
      if (!flush(true)) {
        break;
      }

      if (f == null) {
        f = AppTools.chooseFileToOpen("Open Script", Script.FILES_AND_DIRS,
            mProject
                .replaceIfMissing(mProject.recentScripts().getCurrentFile()));
      }
      if (f == null) {
        break;
      }
      if (notInProject(f)) {
        break;
      }
      close();

      // if layers contain this file elsewhere, just use its editor

      int slot = layers.indexOf(f);
      if (slot >= 0) {
        layers.useCopyOf(slot);
        success = true;
      } else {

        try {
          Script s = new Script(mProject, f);
          editor.items = s.items();
          editor.path = f;

          success = true;
          if (!updateLastScriptDisabled)
            mProject.setLastScriptPath(f);
        } catch (Throwable e) {
          AppTools.showError("opening script", e);
        }
      }
    } while (false);
    updateLastScriptDisabled = false;
    return success;
  }

  /**
   * Encode object to string
   * 
   * @return
   */
  private static JSONObject encodeLayers() {
    ScriptSet ss = new ScriptSet(mProject.directory(), layers.size(),
        layers.currentSlot(), layers.foregroundStart());
    for (int i = 0; i < ss.size(); i++) {
      ScriptEditor ed = layers.layer(i);
      File f = ed.path();
      if (f != null) {
        ss.setFile(i, f);
      }
    }
    return ss.encode();
  }

  private static void decodeLayers(ScriptSet set) {
    layers.reset();
    {
      for (int i = 0; i < set.size(); i++) {
        if (i > 0)
          layers.insert(true);
        File f = set.file(i);
        if (f != null) {
          updateLastScriptDisabled = true;
          if (!open(f)) {
            AppTools.showMsg("Unable to open " + f);
            break;
          }
        }
      }
      layers.setForeground(set.getForegroundLayer());
      layers.select(set.getCurrentLayer());
    }
  }

  // /**
  // * Decode object from string
  // * @param s
  // */
  // private static void decodeLayers(String s) {
  // layers.reset();
  // if (s != null) {
  // DefScanner sc = new DefScanner(s);
  // int sz = sc.sInt();
  // int curr = sc.sInt();
  // int fg = sc.sInt();
  // for (int i = 0; i < sz; i++) {
  // if (i > 0)
  // layers.insert(true); //layers.insert(i > 0);
  // if (sc.sBool()) {
  // File f = sc.sPath(project.directory());
  // updateLastScriptDisabled = true;
  // if (!open(f)) {
  // AppTools.showMsg("Unable to open " + f);
  // break;
  // }
  // }
  // }
  // layers.setForeground(fg);
  // layers.select(curr);
  // }
  // }

  private static boolean updateLastScriptDisabled;

  private static void setProject(ScriptProject project) {
    updateProject(project);
    closeAll();
  }

  public void render(GLPanel panel, boolean toBgnd) {
    for (int i = 0; i < items.size(); i++) {

      panel.lineWidth(1.5f / zoomFactor());

      EdObject obj = items.get(i);
      if (toBgnd) {
        boolean f = obj.isSelected();
        obj.setSelected(false);
        obj.render(panel);
        obj.setSelected(f);
      } else
        obj.render(panel);
    }
  }

  /**
   * Get the ObjArray manipulated by the current editor
   */
  public static ObjArray items() {
    return editor.items;
  }

  public static EdObject item(int i) {
    return editor.items.get(i);
  }

  public static void setItems(ObjArray items) {
    ASSERT(items != null);
    editor.items = items;
  }

  /**
   * Get the active editor
   * 
   * @return Editor
   */
  public static ScriptEditor editor() {
    return editor;
  }

  public static void repaint() {
    infoPanel.refresh();
    editorPanel.repaint();
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
      zoomFactor = 1;
      break;
    case 1:
      zoomFactor *= .8f;
      break;
    case -1:
      zoomFactor *= 1 / .8f;
      break;
    }
    repaint();
  }

  /**
   * Determine if an editor is open
   */
  private static boolean isProjectOpen() {
    return project() != null;
  }

  private static boolean notInProject(File scriptFile) {
    boolean inProj = mProject.isDescendant(scriptFile);
    if (!inProj)
      JOptionPane.showMessageDialog(AppTools.frame(), "File '" + scriptFile
          + "' is not within the project", "Error", JOptionPane.ERROR_MESSAGE);

    return !inProj;
  }

  private void doSave(File initialPath, boolean alwaysAskForPath,
      boolean alwaysVerifyReplaceExisting) {

    final boolean db = false;

    if (db)
      pr("doSave ask=" + alwaysAskForPath + " path=" + path);

    do {
      File f = initialPath;
      if (f == null)
        f = path;
      if (f == null || alwaysAskForPath) {

        f = AppTools.chooseFileToSave("Save Script", Script.FILES_AND_DIRS,
            mProject
                .replaceIfMissing(mProject.recentScripts().getCurrentFile()));

        if (f == null)
          break;

        if (db)
          pr(" requested " + f + " (project=" + mProject.directory() + ")");

        if (notInProject(f))
          break;

        alwaysVerifyReplaceExisting = true;
      }

      if (f.exists() && (alwaysVerifyReplaceExisting || !f.equals(path))) {
        // custom title, warning icon
        int result = JOptionPane.showConfirmDialog(
            AppTools.frame(),
            "Replace existing file: '"
                + new RelPath(mProject.directory(), f).display()
                // + RelPath.toString(project.directory(), f)
                + "'?", "Save Script", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.CANCEL_OPTION
            || result == JOptionPane.CLOSED_OPTION)
          break;
      }

      if (saveAs(f)) {
        mProject.setLastScriptPath(f);
        path = f;
        setChanges(0);
        // resetUndo();
      }
    } while (false);
  }

  /**
   * Template for operation that modifies selected items only. Performs undo by
   * restoring original items.
   */
  private abstract static class EditSelectedOper implements Reversible {

    public EditSelectedOper() {

      // determine selected items, and save for undoing
      ObjArray items = ScriptEditor.items();
      slots = items.getSelected();
      origItems = new EdObject[slots.length];
      for (int i = 0; i < slots.length; i++)
        origItems[i] = items.get(slots[i]);
    }

    @Override
    public Reverse getReverse() {
      return new Reverse() {

        // @Override
        // public Reversible getReverse() {
        // // TODO Auto-generated method stub
        // return EditSelectedOper.this;
        // }

        @Override
        public void perform() {
          ObjArray items = ScriptEditor.items();
          for (int i = 0; i < slots.length; i++)
            items.set(slots[i], origItems[i]);
        }
        // @Override
        // public boolean valid() {
        // return getReverse().valid();
        // }
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

  private static class CursorMoveHandler extends ItemHandler {
    public CursorMoveHandler(int dir) {
      this.dir = dir;
    }

    private static int[] xm = { -1, 1, 0, 0 };
    private static int[] ym = { 0, 0, 1, -1 };

    private int dir;

    public boolean isEnabled() {
      return editor.items.hasSelected();
    }

    public void go() {
      EditSelectedOper r = new EditSelectedOper() {
        @Override
        public void perform() {
          for (int i = 0; i < nSelected(); i++) {
            EdObject obj = items().getCopy(slot(i));
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

    MenuHandler projectMustBeOpenHandler = new MenuHandler() {
      @Override
      public boolean isEnabled() {
        return ScriptEditor.isProjectOpen();
      }
    };

    MyMenuBar m = new MyMenuBar(AppTools.frame());

    if (!AppTools.isMac()) {
      m.addMenu("ScrEdit");
      m.addItem("Quit", KeyEvent.VK_Q, CTRL, new ItemHandler() {
        public void go() {
          if (AppTools.app().exitProgram())
            System.exit(0);
        }
      });
    }
    // -----------------------------------
    m.addMenu("File", projectMustBeOpenHandler);
    m.addItem("New", KeyEvent.VK_N, CTRL, new ItemHandler() {
      public void go() {
        doNewScript();
        repaint();
      }
    });
    m.addItem("Open File...", KeyEvent.VK_O, CTRL, new ItemHandler() {
      public void go() {
        open(null);
        repaint();
      }
    });

    recentScriptsMenuItem = m.addRecentFilesList("Open Recent Script", null,
        new ItemHandler() {
          @Override
          public boolean isEnabled() {
            return ScriptEditor.isProjectOpen();
          }

          public void go() {
            open(mProject.recentScripts().getCurrentFile());
            repaint();
          }
        });

    m.addItem("Open Layer...", KeyEvent.VK_O, META, new ItemHandler() {
      public void go() {
        if (!(isOrphan() && !currentModified())) {
          layers.insert(true);
        }
        open(null);
        repaint();
      }
    });
    m.addItem("Open Next File...", KeyEvent.VK_O, META | SHIFT,
        new ItemHandler() {
          public boolean isEnabled() {
            return !isOrphan();
          }

          public void go() {
            openNextFile();
            repaint();
          }
        });

    m.addSeparator();
    m.addItem("Close", KeyEvent.VK_W, CTRL, new ItemHandler() {
      public void go() {
        do {
          if (!flush(true))
            break;
          close();
          if (layers.size() > 1)
            layers.delete();
        } while (false);
        repaint();
      }
    });
    m.addItem("Close All", KeyEvent.VK_W, CTRL | SHIFT, new ItemHandler() {
      public void go() {
        layers.select(layers.size() - 1);
        do {
          repaint();
          if (!flush(true))
            break;
          close();
          if (layers.size() > 1)
            layers.delete();
          else
            break;
        } while (true);
        repaint();
      }
    });

    m.addItem("Save", KeyEvent.VK_S, CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return isOrphan() || editor.modified();
      }

      public void go() {
        editor.doSave(null, false, false);
        repaint();
      }
    });

    m.addItem("Save As...", KeyEvent.VK_A, CTRL | SHIFT, new ItemHandler() {
      public void go() {
        editor.doSave(null, true, false);
        repaint();
      }
    });
    m.addItem("Save All", KeyEvent.VK_S, CTRL | SHIFT, new ItemHandler() {

      public boolean isEnabled() {
        boolean ret = false;
        for (int i = 0; i < layers.size(); i++) {
          ScriptEditor ed = layers.layer(i);
          if (ed.path() == null || ed.modified())
            ret = true;
        }
        return ret;
      }

      public void go() {
        for (int i = 0; i < layers.size(); i++) {
          ScriptEditor ed = layers.layer(i);
          if (ed.path() == null || ed.modified()) {
            layers.select(i);
            editor.doSave(null, false, false);
          }
        }
        repaint();
      }
    });
    m.addItem("Save As Next", KeyEvent.VK_S, META | SHIFT, new ItemHandler() {
      public boolean isEnabled() {
        return !isOrphan();
      }

      public void go() {
        File f = AppTools.incrementFile(editor.path);
        editor.doSave(f, false, true);
        repaint();
      }
    });
    // ------------------------------------

    // -----------------------------------
    m.addMenu("Edit", projectMustBeOpenHandler);
    undoMenuItem = m.addItem("Undo", KeyEvent.VK_Z, CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return editor.undoCursor > 0;
      }

      public void go() {
        editor.doUndo();
        repaint();
      }
    });

    redoMenuItem = m.addItem("Redo", KeyEvent.VK_Y, CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return editor.getRedoOper() != null;
      }

      public void go() {
        editor.doRedo();
        repaint();
      }
    });
    m.addSeparator();
    m.addItem("Cut", KeyEvent.VK_X, CTRL, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = new CutReversible();
        return r.valid();
        // return editor.items.hasSelected();
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
    m.addItem("Copy", KeyEvent.VK_C, CTRL, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = new CopyReversible();
        return r.valid();
        // return editor.items.hasSelected();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
        // editor.doCopy();
      }
    });

    m.addItem("Paste", KeyEvent.VK_V, CTRL, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = new PasteReversible();
        return r.valid();
        // return clipboard().size() > 0;
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
        // editor.doPaste();
      }
    });

    m.addItem("Duplicate", KeyEvent.VK_D, CTRL, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = new DuplicateReversible();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });

    m.addSeparator();
    m.addItem("Select All", KeyEvent.VK_A, CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return !editor.items.isEmpty();
      }

      public void go() {
        ObjArray a = editor.items;
        for (int i = 0; i < a.size(); i++)
          a.get(i).setSelected(true);
        repaint();
      }
    });
    selectNoneMenuItem = m.addItem("Select None", KeyEvent.VK_ESCAPE, 0,
        new ItemHandler() {
          private SelectNoneOper r;

          public boolean isEnabled() {
            r = new SelectNoneOper();
            return r.valid();
          }

          public void go() {
            r.perform();
            repaint();
          }
        });

    m.addSeparator();

    m.addItem("Move Backward", KeyEvent.VK_OPEN_BRACKET, 0, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = new AdjustSlotsReversible(1, false);
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });
    m.addItem("Move Forward", KeyEvent.VK_CLOSE_BRACKET, 0, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = new AdjustSlotsReversible(-1, false);
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });
    m.addItem("Move to Rear", KeyEvent.VK_OPEN_BRACKET, CTRL,
        new ItemHandler() {
          private Reversible r;

          public boolean isEnabled() {
            r = new AdjustSlotsReversible(1, true);
            return r.valid();
          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });
    m.addItem("Move to Front", KeyEvent.VK_CLOSE_BRACKET, CTRL,
        new ItemHandler() {
          private Reversible r;

          public boolean isEnabled() {
            r = new AdjustSlotsReversible(-1, true);
            return r.valid();
          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });

    m.addSeparator();

    m.addItem("Group", KeyEvent.VK_G, CTRL, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = GroupObject.getGroupReversible();
        return r.valid();
      }

      public void go() {
        editor().registerPush(r);
        perform(r);
      }
    });
    m.addItem("Ungroup", KeyEvent.VK_U, CTRL, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
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
        new ItemHandler() {
          private Reversible r;

          public boolean isEnabled() {
            r = new FlipReversible(true);
            return r.valid();

          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });
    m.addItem("Flip Vertically", KeyEvent.VK_V, SHIFT | CTRL,
        new ItemHandler() {
          private Reversible r;

          public boolean isEnabled() {
            r = new FlipReversible(false);
            return r.valid();

          }

          public void go() {
            editor().registerPush(r);
            perform(r);
          }
        });

    m.addItem("Rotate", KeyEvent.VK_R, CTRL, new ItemHandler() {
      private RotateOper r;

      public boolean isEnabled() {
        r = new RotateOper();
        return r.valid();
        // return editor.items.getSelected().length != 0;
      }

      public void go() {
        //
        // int[] slots = editor.items.getSelected();
        // if (slots.length == 0)
        // return;
        MouseOper.setOperation(r); // new RotateOper(slots));
      }
    });
    m.addItem("Reset Rotate", KeyEvent.VK_R, CTRL | SHIFT, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = RotateOper.getResetOper(); // new ResetRotateOper();
        return r.valid();
      }

      public void go() {
        // if (r.valid()) {
        editor().registerPush(r);
        perform(r);
        repaint();
        // }
      }
    });
    m.addItem("Scale", KeyEvent.VK_E, CTRL, new ItemHandler() {
      private ScaleOper oper;

      public boolean isEnabled() {
        oper = new ScaleOper();
        return oper.valid();
        // return editor.items.getSelected().length != 0;
      }

      public void go() {
        // int[] slots = editor.items.getSelected();
        // if (slots.length == 0)
        // return;
        MouseOper.setOperation(oper); // new RotateOper(slots));
      }
    });
    m.addItem("Reset Scale", KeyEvent.VK_E, CTRL | SHIFT, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
        r = ScaleOper.getResetOper(); // new Z_ResetScaleOper();
        return r.valid();
      }

      public void go() {
        // if (r.valid()) {
        editor().registerPush(r);
        perform(r);
        repaint();
        // }
      }
    });

    // -----------------------------------
    m.addMenu("View", projectMustBeOpenHandler);
    m.addItem("Zoom In", KeyEvent.VK_EQUALS, CTRL, new ItemHandler() {
      public void go() {
        doAdjustZoom(-1);
      }

      public boolean isEnabled() {
        return zoomFactor < 20;
      }
    });

    m.addItem("Zoom Out", KeyEvent.VK_MINUS, CTRL, new ItemHandler() {
      public void go() {
        doAdjustZoom(1);
      }

      public boolean isEnabled() {
        return zoomFactor > .1f;
      }
    });
    m.addItem("Zoom Reset", KeyEvent.VK_0, CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return zoomFactor != 1;
      }

      public void go() {
        doAdjustZoom(0);
      }
    });
    m.addSeparator();
    m.addItem("Snap to Grid", KeyEvent.VK_G, CTRL | SHIFT, new ItemHandler() {
      private Reversible r;

      public boolean isEnabled() {
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
        new ItemHandler() {
          public void go() {
            PolygonObject.showVertices ^= true;
            repaint();
          }
        });

    // -----------------------------------
    m.addMenu("Objects", projectMustBeOpenHandler);
    m.addItem("Add Sprite", KeyEvent.VK_S, 0, new ItemHandler() {
      public boolean isEnabled() {
        return lastSprite != null;
      }

      public void go() {
        MouseOper.setOperation(new AddSpriteOper());
      }
    });
    m.addItem("Select Atlas", KeyEvent.VK_T, CTRL, new ItemHandler() {
      public void go() {
        doSelectAtlas(null);
      }
    });

    m.addSeparator();
    m.addItem("Add Polygon", KeyEvent.VK_P, 0, new ItemHandler() {
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
    m.addItem("Add Rectangle", KeyEvent.VK_R, 0, new ItemHandler() {

      public void go() {
        MouseOper.setOperation(new EdRectangleOper(null, -1, -1));
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
    m.addItem("Next", KeyEvent.VK_EQUALS, 0, new ItemHandler() {
      public boolean isEnabled() {
        return layers.size() > 1;
      }

      public void go() {
        layers.next();
        repaint();
      }
    });
    m.addItem("Previous", KeyEvent.VK_MINUS, 0, new ItemHandler() {
      public boolean isEnabled() {
        return layers.size() > 1;
      }

      public void go() {
        layers.prev();
        repaint();
      }
    });
    m.addSeparator();
    m.addItem("Open Set", KeyEvent.VK_O, CTRL | SHIFT, new ItemHandler() {
      public void go() {
        doOpenSet(null);
        repaint();
      }
    });

    recentScriptSetsMenuItem = m.addRecentFilesList("Open Recent Set", null,
        new ItemHandler() {
          @Override
          public boolean isEnabled() {
            return ScriptEditor.isProjectOpen();
          }

          public void go() {
            doOpenSet(mProject.recentScriptSets().getCurrentFile());
            repaint();
          }
        });

    m.addItem("Save Set As...", KeyEvent.VK_S, CTRL | SHIFT, new ItemHandler() {
      public void go() {
        doSaveSet();
        repaint();
      }
    });
    m.addSeparator();

    // -----------------------------------

    m.addMenu("Project");
    m.addItem("New Project", 0, 0, new ItemHandler() {
      public void go() {
        doNewProject();
        ScriptEditor.repaint();
      }
    });
    m.addItem("Open Project", 0, 0, new ItemHandler() {
      public void go() {
        File f = AppTools.chooseFileToOpen("Open Project",
            ScriptProject.FILES_AND_DIRS, null);
        if (f != null) {
          openProject(f);
          repaint();
        }
      }
    });
    recentProjectsMenuItem = m.addRecentFilesList("Open Recent Project",
        recentProjects, new ItemHandler() {
          @Override
          public void go() {
            openProject(recentProjects.getCurrentFile());
            repaint();
          }
        });

    m.addItem("Close Project", 0, 0, new ItemHandler() {
      public boolean isEnabled() {
        return isProjectOpen();
      }

      public void go() {
        doCloseProject();
        ScriptEditor.repaint();
      }
    });
  }

  // private static MyFrame gridFrame;

  public static boolean doCloseProject() {
    final boolean db = false;

    if (db) {
      warning("db:doCloseProject");
      pr("doCloseProject " + mProject);
    }
    do {
      if (!isProjectOpen())
        break;

      try {
        if (db)
          pr(" flushAll()");

        if (!allowQuitWithoutSave()) {
          if (!flushAll())
            break;
        }

        if (db)
          pr(" writeProjectDefaults()");

        writeProjectDefaults();

        if (db)
          pr(" project.flush()");

        mProject.flush();

        if (db)
          pr(" closeAll()");

        closeAll();

        updateProject(null);

        if (db)
          pr(" selectAtlas(null)");

        selectAtlas(null);

        editor = null;

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
      cancelOper = MouseOper.getOperation() != null;
      if (!cancelOper) {
        slots = items().getSelected();
      }
    }

    public void perform() {
      if (cancelOper) {
        MouseOper.clearOperation();
      } else {
        items().clearAllSelected();
      }
      repaint();
    }

    public boolean valid() {
      return cancelOper || slots.length > 0;
    }

    private boolean cancelOper;
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
    final boolean db = false;
    // unimp("if attempting to load file that is already in another editor, just copy that editor");

    do {
      File prev = editor.path;
      if (db)
        pr("openNextFile prev=" + prev);
      File dir = prev.getParentFile();
      File[] fs = Script.FILES_ONLY.list(dir);
      File next = null;

      if (fs.length > 0 && !fs[0].equals(prev))
        next = fs[0];

      for (int i = 0; i < fs.length; i++) {
        if (db)
          pr(" ..." + fs[i]);
        if (fs[i].equals(prev)) {
          i++;
          if (i < fs.length)
            next = fs[i];
          break;
        }
      }
      if (next == null) {
        if (db)
          pr("can't find next file");
        break;
      }
      layers.insert(true);
      open(next);
    } while (false);
  }

  /**
   * Process "Project:new" menu item
   */
  private static void doNewProject() {
    do {

      File projFile = AppTools.chooseFileToSave("Create New Project",
          ScriptProject.FILES_AND_DIRS, null);
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
        fx = AppTools.chooseFileToOpen("Select Atlas",
            Atlas.DATA_FILES_AND_DIRS,
            mProject.replaceIfMissing(atlasPanel.file()));

      if (fx == null)
        break;
      if (notInProject(fx))
        break;

      selectAtlas(fx);
    } while (false);
  }

  private static void selectAtlas(File f) {
    if (f != null)
      mProject.recentAtlases().setCurrentFile(f);
  }

  private static void doNewScript() {
    do {
      if (!isOrphan()) {
        layers.insert(true);
      }
      if (!flush(true))
        break;

      close();
    } while (false);
  }

  private static void doOpenSet(File f) {

    do {
      if (!flushAll())
        break;

      if (f == null)
        f = AppTools.chooseFileToOpen("Open Set", Script.SET_FILES_AND_DIRS,
            mProject.replaceIfMissing(mProject.recentScriptSets()
                .getCurrentFile()));
      if (f == null)
        break;

      if (notInProject(f))
        break;
      try {

        ScriptSet ss = new ScriptSet(mProject.directory(), new JSONObject(
            FileUtils.readFileToString(f)));

        decodeLayers(ss);
        setRecentSetPath(f);

      } catch (Throwable e) {
        AppTools.showError("reading set", e);
      }

    } while (false);
    repaint();
  }

  private static void doSaveSet() {

    /*
     * 
     * [] flush files [] if any are still orphans, or are modified, abort []
     * serialize layers into string [] write string to file
     */
    int origLayer = layers.currentSlot();
    // outer:
    do {

      if (!flushAll())
        break;

      File f = AppTools.chooseFileToSave("Save Set", Script.SET_FILES_AND_DIRS,
          mProject.replaceIfMissing(mProject.recentScriptSets()
              .getCurrentFile()));

      if (f == null)
        break;
      if (notInProject(f))
        break;

      setRecentSetPath(f);

      try {
        Files.writeStringToFileIfChanged(f, encodeLayers().toString(2));
      } catch (Throwable e) {
        AppTools.showError("writing script set", e);
      }
    } while (false);
    layers.select(origLayer);
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
    mProject.setLastSetPath(f);
    MyMenuBar.updateRecentFilesFor(recentScriptSetsMenuItem,
        mProject.recentScriptSets());
    mProject.setLastSetPath(null);
  }

  public static void unselectAll() {
    items().clearAllSelected();
  }

  public static SpriteObject lastSprite() {
    return lastSprite;
  }

  /**
   * Perform operation: change selected sprites
   * 
   * @param spriteInfo
   *          object specifying new atlas and sprite
   */
  public static void doSetSpritesTo(SpriteObject si) {
    lastSprite = si;

    final SpriteObject si2 = si;
    // final SpriteObject spriteInfo = si;
    EditSelectedOper r = new EditSelectedOper() {

      @Override
      public void perform() {
        for (int i = 0; i < nSelected(); i++) {
          EdObject obj = items().get(slot(i));
          if (!(obj instanceof SpriteObject))
            continue;
          obj = items().getCopy(slot(i));
          SpriteObject s = (SpriteObject) obj;
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

  public static ObjArray clipboard() {
    return clipboard;
  }

  /**
   * Set clipboard
   * 
   * @param newClip
   *          new clipboard
   */
  public static void setClipboard(ObjArray newClip) {
    clipboard = newClip;
  }

  /**
   * Save script
   * 
   * @param f
   * @return true if successfully saved
   */
  private boolean saveAs(File f) {
    final boolean db = false;

    if (db)
      pr("saveAs " + f);

    boolean success = false;
    try {
      Script s = new Script(mProject);
      s.setPath(f);
      if (db)
        pr(" constructed script=" + s + ", lastAtlas=" + s.lastAtlas());

      writeScript(s);

      if (db)
        pr(" flushing script");

      s.flush();
      success = true;
      this.path = f;

    } catch (IOException e) {
      AppTools.showError("saving script", e);
    }
    return success;
  }

  private void writeScript(Script s) {
    s.setItems(items);

  }

  private static boolean flushAll() {
    editor.resetUndo();
    int currLayer = layers.currentSlot();
    boolean success = true;
    for (int i = 0; i < layers.size(); i++) {
      layers.select(i);
      if (!flush(true)) {
        success = false;
        break;
      }
    }
    if (success)
      layers.select(currLayer);
    return success;
  }

  private static void closeAll() {
    layers.reset();
  }

  public static void init(JComponent p) {

    JPanel ac = new JPanel(new BorderLayout());
    p.add(ac, BorderLayout.EAST);

    atlasPanel = new AtlasPanel();
    ac.add(atlasPanel, BorderLayout.CENTER);
    pPanel = new PalettePanel();
    ac.add(pPanel, BorderLayout.SOUTH);

    {
      JPanel pnl = new JPanel();

      atlasCB = new JComboBox();
      atlasCB.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ev) {
          final boolean db = false;
          if (db)
            pr("atlasCB event received");
          File af = null;

          if (isProjectOpen())
            af = mProject.recentAtlases().getCurrentFile();
          if (atlasPanel != null)
            atlasPanel.setAtlas(mProject, af);
        }
      });

      atlasSelectButton = new JButton("Open");
      atlasSelectButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doSelectAtlas(null);
        }
      });

      pnl.add(atlasSelectButton);

      pnl.add(atlasCB);
      ac.add(pnl, BorderLayout.NORTH);
    }

    layers = new LayerSet(new LayerSet.ICallback() {
      public void useEditor(ScriptEditor ed) {
        editor = ed;
        // resetUndo();
      }
    });

    addMenus();

    {
      File base = recentProjects.getMostRecentFile();
      if (base != null && !ScriptProject.FILES_ONLY.accept(base))
        base = null;
      if (base != null && base.exists()) {
        openProject(base);
      }
    }

    {
      JPanel pnl = new JPanel(new BorderLayout());
      editorPanel = new EditorPanelGL();

      pnl.add(editorPanel.getComponent(), BorderLayout.CENTER);

      infoPanel = new InfoPanel(layers);
      pnl.add(infoPanel, BorderLayout.SOUTH);
      p.add(pnl, BorderLayout.CENTER);
    }

    // add mouse edit operations, in the order they
    // are to be tested for activation
    MouseOper.add(new MoveFocusOper());
    MouseOper.add(new EditSelectedItemOper());

    MouseOper.add(new MouseOperSelectItems());
    // {
    // MouseOper.add(new MouseOperMoveItems());
    // MouseOper.add(new BoxOper());
    // }
  }

  private static InfoPanel infoPanel;

  public static void setInfo(EdObject obj) {
    if (obj == null)
      setInfo("");
    else
      setInfo(obj.getInfoMsg());
  }

  public static void setInfo(String msg) {
    if (msg == null)
      msg = "";
    infoPanel.msg.setText(msg);
    // infoPanel.msg.setText(msg);
    // infoPanel.refresh();
    // infoLabel.setText(msg);
  }

  /**
   * Read 'show origin' button value
   * 
   * @return true to plot origin
   */
  public static boolean showOrigin() {
    return origin.isSelected();
  }

  private static void setOrigin(boolean f) {
    origin.setSelected(f);
  }

  /**
   * Read 'faded' option
   * 
   * @return faded value
   */
  public static boolean faded() {
    return faded.isSelected();
  }

  private static void setFaded(boolean f) {
    faded.setSelected(f);
  }

  private static class InfoPanel extends MyPanel implements ActionListener {
    private LayerSet layers;

    public InfoPanel(LayerSet lyr) {
      super(true);

      this.layers = lyr;

      setBorder(BorderFactory.createRaisedBevelBorder());

      {
        JPanel c1 = vertPanel();

        {
          JPanel c = horzPanel();

          c.add(new JLabel("Script:"));
          slotNumber = text(5, false);
          slotNumber.setHorizontalAlignment(SwingConstants.CENTER);
          c.add(slotNumber);
          filePath = text(24, false);
          c.add(filePath);
          c.add(stretch());

          c1.add(c);
        }
        {
          JPanel c = horzPanel();
          bgnd = new JCheckBox("Bgnd");
          bgnd.addActionListener(this);
          bgnd.setToolTipText("Plot this, and preceding scripts, as background");
          c.add(bgnd);

          c.add(origin);
          origin.addActionListener(this);
          origin.setToolTipText("Plot origin crosshairs");

          c.add(faded);
          faded.addActionListener(this);
          faded.setToolTipText("Plot faded previous foreground script");

          c.add(Grid.intGridActiveCtrl());
          c.add(new JLabel("Size:"));
          c.add(Grid.gridSizeCtrl());

          c.add(stretch());

          c1.add(c);
        }
        {
          JPanel c = horzPanel();

          msg = text(50, true);
          c.add(msg);

          // infoLabel = new JLabel();
          // infoLabel.setFont(AppTools.getSmallFixedWidthFont());
          // infoLabel.setPreferredSize(new Dimension(30, infoLabel.getFont()
          // .getSize()));
          // size(infoLabel, 40);
          // c.add(infoLabel);
          // infoLabel.setText("har");
          c.add(stretch());
          c1.add(c);
        }

        c1.add(stretch());

        this.add(c1);
      }
      this.add(hSpace(8));

      this.add(stretch());
      refresh();
    }

    private static JTextField text(int chars, boolean small) {
      JTextField tf = new JTextField();
      tf.setEditable(false);
      tf.setFont(small ? AppTools.getSmallFixedWidthFont() : AppTools
          .getFixedWidthFont());
      size(tf, chars);
      return tf;
    }

    private static void size(Component c, int chars) {
      Dimension d = new Dimension(chars * 13, Short.MAX_VALUE);
      Dimension cs = c.getPreferredSize();
      c.setPreferredSize(new Dimension(d.width, cs.height));
    }

    public void refresh() {
      final boolean db = false;
      if (db)
        pr("InfoPanel update(); projectOpen=" + isProjectOpen());

      if (isProjectOpen()) {
        {
          int i = layers.currentSlot();

          if (layers.size() > 1) {
            upd(slotNumber, (i + 1) + "/" + (layers.size()));
          } else
            upd(slotNumber, null);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(currentModified() ? "*" : " ");
        if (!isOrphan())
          sb.append(new RelPath(mProject.directory(), editor.path).display());
        upd(filePath, sb);
        displayProjectPath(mProject.file().getName());
        // upd(projectPath, project.file().getName());
        upd(bgnd, true, layers.isBackground());

      } else {
        upd(slotNumber, null);
        upd(filePath, null);
        displayProjectPath(null);
        // upd(projectPath, null);
        upd(bgnd, false, false);
      }
    }

    private void displayProjectPath(String s) {
      StringBuilder t = new StringBuilder("ScrEdit");
      if (s != null) {
        t.append(" (Project:");
        t.append(s);
        t.append(")");
      }
      String nt = t.toString();
      if (!nt.equals(prevTitle)) {
        prevTitle = nt;
        AppTools.frame().setTitle(prevTitle);
      }
    }

    private static String prevTitle;

    private void upd(JCheckBox cb, boolean enabled, boolean value) {
      cb.setSelected(enabled && value);
      cb.setEnabled(enabled);
    }

    private void upd(JTextField tf, Object content) {
      if (content == null)
        content = "";
      String cs = content.toString();
      String prev = tf.getText();
      if (!prev.equals(cs)) {
        tf.setText(cs);
      }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      layers.setBackground(bgnd.isSelected());
      ScriptEditor.repaint();
    }

    private JCheckBox bgnd;
    // private JTextField projectPath;
    private JTextField slotNumber;
    private JTextField filePath;
    private JTextField msg;
  }

  // private static class InfoPanel extends JPanel implements ActionListener {
  // private LayerSet layers;
  // public InfoPanel(LayerSet lyr) {
  // this.layers = lyr;
  // setBorder(BorderFactory.createRaisedBevelBorder());
  // setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
  // spc(5);
  //
  // add(new JLabel("B:"));
  // bgnd = new JCheckBox();
  // add(bgnd);
  // bgnd.addActionListener(this);
  // bgnd.setToolTipText("Plot this, and preceding scripts, as background");
  //
  // add(new JLabel("Script:"));
  // slotNumber = text(5);
  // slotNumber.setHorizontalAlignment(SwingConstants.CENTER);
  // add(slotNumber);
  //
  // filePath = text(24);
  // add(filePath);
  //
  // add(new JLabel("O:"));
  // add(origin);
  // origin.addActionListener(this);
  // origin.setToolTipText("Plot origin crosshairs");
  // add(new JLabel("F:"));
  // add(faded);
  // faded.addActionListener(this);
  // faded.setToolTipText("Plot faded previous foreground script");
  //
  //
  // add(Box.createHorizontalGlue());
  //
  // add(new JLabel("Project:"));
  // projectPath = text(18);
  // add(projectPath);
  // spc(5);
  //
  // refresh();
  // }
  //
  //
  // private static JTextField text(int chars) {
  // JTextField tf = new JTextField();
  // tf.setEditable(false);
  // tf.setFont(AppTools.getFixedWidthFont());
  // size(tf, chars);
  // return tf;
  // }
  //
  // private static void size(Component c, int chars) {
  // Dimension d = new Dimension(chars * 13, Short.MAX_VALUE);
  // Dimension cs = c.getPreferredSize();
  // c.setPreferredSize(new Dimension(d.width, cs.height));
  // c.setMaximumSize(d);
  // }
  // private void spc(int x) {
  // add(Box.createRigidArea(new Dimension(x, 0)));
  // }
  //
  // public void refresh() {
  // final boolean db = false;
  // if (db)
  // pr("InfoPanel update(); projectOpen=" + isProjectOpen());
  //
  // if (isProjectOpen()) {
  // {
  // int i = layers.currentSlot();
  //
  // if (layers.size() > 1) {
  // upd(slotNumber, (i + 1) + "/" + (layers.size()));
  // } else
  // upd(slotNumber, null);
  // }
  //
  // StringBuilder sb = new StringBuilder();
  // sb.append(currentModified() ? "*" : " ");
  // //sb.append("<" + editor.ourID + ">");
  // if (!isOrphan())
  // sb.append(new RelPath(project.directory(), editor.path).display());
  // upd(filePath, sb);
  // upd(projectPath, project.file().getName());
  // upd(bgnd, true, layers.isBackground());
  //
  // } else {
  // upd(slotNumber, null);
  // upd(filePath, null);
  // upd(projectPath, null);
  // upd(bgnd, false, false);
  // }
  // }
  // private void upd(JCheckBox cb, boolean enabled, boolean value) {
  // cb.setSelected(enabled && value);
  // cb.setEnabled(enabled);
  // }
  //
  // private void upd(JTextField tf, Object content) {
  // if (content == null)
  // content = "";
  // String cs = content.toString();
  // String prev = tf.getText();
  // if (!prev.equals(cs)) {
  // tf.setText(cs);
  // }
  // }
  // private JCheckBox bgnd;
  // private JTextField projectPath;
  // private JTextField slotNumber;
  // private JTextField filePath;
  // @Override
  // public void actionPerformed(ActionEvent arg0) {
  // layers.setBackground(bgnd.isSelected());
  // ScriptEditor.repaint();
  // }
  // }
  private static final String NOSAVENECESSARY_TAG = "NOSAVEPROMPT";

  public static ConfigSet.Interface CONFIG = new ConfigSet.Interface() {
    private static final String PROJECTS_TAG = "RECENTPROJECTS";

    @Override
    public void readFrom(JSONObject map) throws JSONException {
      recentProjects.decode(map.optJSONObject(PROJECTS_TAG));
      setOrigin(map.optBoolean("ORIGIN", true));
      setFaded(map.optBoolean("FADED"));
      zoomFactor = (float) map.optDouble("ZOOM", 1);
      JSONArray a = map.optJSONArray("FOCUS");
      if (a != null)
        setFocus(IPoint.parseJSON(a));
      if (map.optBoolean(NOSAVENECESSARY_TAG))
        noSaveNecessary = true;
      if (map.optBoolean("POLYVERTS"))
        PolygonObject.showVertices = true;
    }

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      map.put(PROJECTS_TAG, recentProjects.encode());
      map.put("ORIGIN", showOrigin());
      map.put("FADED", faded());
      map.put("ZOOM", zoomFactor);
      map.put("FOCUS", focus.toJSON());
      if (noSaveNecessary) {
        map.put(NOSAVENECESSARY_TAG, true);
      }
      map.put("POLYVERTS", PolygonObject.showVertices);
    }

  };
  private static boolean noSaveNecessary;

  public static boolean allowQuitWithoutSave() {
    return noSaveNecessary;
  }

  private static boolean isOrphan() {
    return editor.path == null;
  }

  private static JMenuItem recentScriptsMenuItem, recentProjectsMenuItem,
      recentScriptSetsMenuItem;

  private static void writeProjectDefaults() throws JSONException {
    mProject.getDefaults().put("LAYERS", encodeLayers());
    ASSERT(pPanel != null);
    mProject.getDefaults().put("PALETTE", pPanel.encodeDefaults());
  }

  public static void setFocus(IPoint trans) {
    focus = new IPoint(trans);
  }

  private static void readProjectDefaults() {
    if (pPanel == null) {
      warning("project panel null");
    } else {
      pPanel.decodeDefaults(mProject.getDefaults().optJSONObject("PALETTE"));
    }

    ScriptSet ss = null;
    try {
      JSONObject map = mProject.getDefaults().optJSONObject("LAYERS");
      if (map != null)
        ss = new ScriptSet(mProject.directory(), map);
    } catch (JSONException e) {
      AppTools.showError("Problem reading project defaults", e);
    }
    if (ss != null)
      decodeLayers(ss);
    else
      layers.select(0);
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

        setProject(newProject);

        readProjectDefaults();

      } catch (IOException e) {
        AppTools.showError("Problem opening project", e);
      }
    } while (false);
  }

  /**
   * Get zoom factor for editor window
   * 
   * @return zoom factor
   */
  public static float zoomFactor() {
    return zoomFactor;
  }

  /**
   * Get the structure describing the current editor layers
   * 
   * @return Layer object
   */
  public static LayerSet layers() {
    return layers;
  }

  public File path() {
    return path;
  }

  private static void setProjectField(ScriptProject p) {
    mProject = p;
  }

  private static void updateProject(ScriptProject p) {

    final boolean db = false;

    if (db)
      pr("updateProject from " + mProject + " to " + p);

    setProjectField(p);
    MyMenuBar.updateRecentFilesFor(recentScriptsMenuItem,
        isProjectOpen() ? mProject.recentScripts() : null);

    MyMenuBar.updateRecentFilesFor(recentScriptSetsMenuItem,
        isProjectOpen() ? mProject.recentScriptSets() : null);
    recentProjects.setCurrentFile(isProjectOpen() ? mProject.file() : null);

    if (isProjectOpen()) {
      mProject.recentAtlases().setComboBox(atlasCB);
      atlasCB.setEnabled(true);
      atlasSelectButton.setEnabled(true);
    } else {
      atlasCB.removeAllItems();
      atlasCB.setEnabled(false);
      atlasSelectButton.setEnabled(false);
    }
    if (db)
      pr(" updateRecentFilesFor " + recentProjectsMenuItem
          + "\n recentProjects=\n" + recentProjects);

    MyMenuBar.updateRecentFilesFor(recentProjectsMenuItem, recentProjects);

  }

  public static ScriptProject project() {
    return mProject;
  }

  private void resetUndo() {
    undoCursor = 0;
    undoList.clear();
  }

  private boolean modified() {
    return changesSinceSaved != 0;
  }

  /**
   * Adjust the 'changes since saved' variable. Refreshes infoPanel if modified.
   * 
   * @param n
   *          zero, to clear it to zero; else, amount to add to existing value
   */
  private void setChanges(int n) {
    int newVal = changesSinceSaved;
    if (n == 0) {
      newVal = 0;
    } else
      newVal += n;

    if (newVal != changesSinceSaved) {
      changesSinceSaved = newVal;
      infoPanel.refresh();
    }
  }

  // ------------------------- Undo Stuff ---------------------------

  private static final boolean DBUNDO = false;

  public void registerPush(Reversible op) {
    final boolean db = DBUNDO;
    final boolean db2 = db && false;

    if (db)
      pr("registerPush: " + op);

    int trim = undoList.size() - undoCursor;
    if (trim > 0) {
      if (db2)
        pr(" removing " + trim + " 'redoable' operations from list");
      remove(undoList, undoCursor, trim);
    }
    undoList.add(op);
    undoCursor++;
    editor.setChanges(1);

    if (db2)
      pr(" undoCursor " + undoCursor + " of total " + undoList.size());

    // limit # undo operations to something reasonable
    trim = undoCursor - 50;

    if (trim > 0) {
      remove(undoList, 0, trim);
      undoCursor -= trim;
    }
    updateUndoLabels();
  }

  /**
   * Peek at topmost reversible on stack
   * 
   * @return topmost reversible, or null if stack is empty
   */
  public Reversible registerPeek() {
    Reversible r = null;
    if (undoCursor > 0)
      r = (Reversible) undoList.get(undoCursor - 1);
    return r;
  }

  /**
   * Pop topmost reversible from stack
   * 
   * @return topmost reversible
   */
  public Reversible registerPop() {
    final boolean db = DBUNDO;

    Reversible rev = registerPeek();
    if (db)
      pr("registerPop: " + rev + " undoCursor=" + undoCursor);

    undoCursor--;
    int trim = undoList.size() - undoCursor;
    if (db)
      pr(" trim=" + trim + ", undoCursor=" + undoCursor + ", undoList.size="
          + undoList.size());

    if (trim > 0) {
      if (db)
        pr(" removing " + trim + " 'redoable' operations from list");
      remove(undoList, undoCursor, trim);
    }
    setChanges(-1);
    updateUndoLabels();
    return rev;
  }

  private void updateUndoLabels() {
    {
      String lbl = "Undo";
      Reversible r = registerPeek();
      if (r != null)
        lbl = "Undo " + r;
      undoMenuItem.setText(lbl);
    }
    {
      String lbl = "Redo";
      Reversible r = editor.getRedoOper();
      if (r != null) {
        lbl = "Redo " + r;
      }
      redoMenuItem.setText(lbl);
    }
  }

  private Reversible getRedoOper() {
    Reversible oper = null;
    if (undoCursor < undoList.size()) {
      oper = (Reversible) undoList.get(undoCursor);
    }
    return oper;
  }

  public static void perform(Reversible op) {
    final boolean db = DBUNDO;
    if (db)
      pr("perform reversible:\n" + op);

    op.perform();
    repaint();
  }

  private void doRedo() {
    final boolean db = DBUNDO;

    MouseOper.clearOperation();
    Reversible oper = editor.getRedoOper();
    if (oper != null) {
      if (db)
        pr("redo: " + oper);
      undoCursor++;
      setChanges(1);
      oper.perform();
      updateUndoLabels();
    }
  }

  private void doUndo() {
    final boolean db = DBUNDO;
    if (db && false)
      pr("doUndo, undoCursor " + undoCursor + " of total " + undoList.size());

    MouseOper.clearOperation();

    if (undoCursor > 0) {
      // unselectAll();
      Reversible oper = (Reversible) undoList.get(undoCursor - 1);
      undoCursor--;
      setChanges(-1);
      if (db)
        pr("undo: " + oper);
      oper.getReverse().perform();
      updateUndoLabels();
    }
  }

  // --- Class fields
  private static EditorPanelGL editorPanel;
  // private static Component editorPanel;

  // the active editor
  private static ScriptEditor editor;
  private static float zoomFactor = 1.0f;
  private static SpriteObject lastSprite;

  private static IPoint focus = new IPoint();
  private static ObjArray clipboard = new ObjArray();
  private static ScriptProject mProject;
  private static LayerSet layers;
  private static AtlasPanel atlasPanel;
  private static PalettePanel pPanel;
  private static JComboBox atlasCB;
  private static JButton atlasSelectButton;

  private static JToggleButton origin = new JCheckBox("Origin");
  private static JToggleButton faded = new JCheckBox("Onion skin");

  // private static JIntSpinner gridSize = new JIntSpinner(
  private static RecentFiles recentProjects = new RecentFiles(null);
  private static JMenuItem undoMenuItem, redoMenuItem;
  private static JMenuItem selectNoneMenuItem;

  // --- Instance fields

  private ObjArray items = new ObjArray();
  private File path;
  private int changesSinceSaved;
  private ArrayList<Reversible> undoList = new ArrayList();
  private int undoCursor;
  // private static JLabel infoLabel;

}
