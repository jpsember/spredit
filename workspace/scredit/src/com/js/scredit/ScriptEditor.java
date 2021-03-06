package com.js.scredit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import java.util.TreeSet;

import javax.swing.*;

import org.json.JSONException;
import org.json.JSONObject;

import tex.*;
import apputil.*;

import com.js.basic.*;
import com.js.editor.Command;
import com.js.editor.Enableable;
import com.js.editor.UserEvent;
import com.js.editor.UserEventManager;
import com.js.editor.UserOperation;
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

  private void readScriptForCurrentEditor() throws IOException, JSONException {
    // read items into script
    getScript().read();
    // copy items from script to editor state
    // Issue a non-undoable command to do this; it will clear the command
    // history as a consequence
    CommandForGeneralChanges command = new CommandForGeneralChanges(
        (String) null);
    mState.setObjects(getScript().items());
    command.finish();
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
        editor().readScriptForCurrentEditor();

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
        editor().readScriptForCurrentEditor();
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
    return editor().mState.getObjects();
  }

  /**
   * Replace ObjArray for current editor
   */
  public static void setItems(EdObjectArray items) {
    editor().mState.setObjects(items);
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
    // if (isProjectOpen())
    // updateEditableObjectStatus();
    sInfoPanel
        .refresh(isProjectOpen() ? editor() : null, project(), sScriptSet);
    sEditorPanelComponent.repaint();
  }

  public static GLPanel getEditorPanel() {
    warning("refactor this, perhaps as method in event or event manager");
    return sEditorPanel;
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

  public static float pickRadius() {
    return 10 / sEditorPanel.getZoom();
  }

  /**
   * Template for operation that modifies selected items only. Performs undo by
   * restoring original items.
   */
  private abstract static class EditSelectedCommand extends Command.Adapter {

    public EditSelectedCommand() {

      // determine selected items, and save for undoing
      EdObjectArray items = ScriptEditor.items();
      slots = items.getSelectedSlots();
      origItems = new EdObject[slots.size()];
      for (int i = 0; i < slots.size(); i++)
        origItems[i] = items.get(slots.get(i));
    }

    @Override
    public Command getReverse() {
      return new Command.Adapter() {

        @Override
        public Command getReverse() {
          return EditSelectedCommand.this;
        }

        @Override
        public void perform() {
          EdObjectArray items = ScriptEditor.items();
          for (int i = 0; i < slots.size(); i++)
            items.set(slots.get(i), origItems[i]);
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
      return slots.get(i);
    }

    public int nSelected() {
      return slots.size();
    }

    private SlotList slots;
    private EdObject[] origItems;
    // private Reversible fwdOper;
  }

  private static class CursorMoveOperation extends UserOperation {
    public CursorMoveOperation(int dir) {
      this.dir = dir;
    }

    private static int[] xm = { -1, 1, 0, 0 };
    private static int[] ym = { 0, 0, 1, -1 };

    private int dir;

    @Override
    public boolean shouldBeEnabled() {
      return items().getSelectedSlots().size() != 0;
    }

    @Override
    public void start() {
      EditSelectedCommand r = new EditSelectedCommand() {
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
        editor().recordCommand(r);
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

    ASSERT(sUserEventManager != null);
    MyMenuBar m = new MyMenuBar(AppTools.frame(), sUserEventManager);

    m.addAppMenu();

    // -----------------------------------
    m.addMenu("File", projectMustBeOpenHandler);
    m.addItem("New", KeyEvent.VK_N, CTRL, new UserOperation() {
      @Override
      public void start() {
        doNewScript();
        repaint();
      }
    });
    m.addItem("Open Script...", KeyEvent.VK_O, CTRL, new UserOperation() {
      @Override
      public void start() {
        openScript(null);
        repaint();
      }
    });

    sRecentScriptsMenuItem = new RecentFiles.Menu("Open Recent Script",
        sUserEventManager, null, new UserOperation() {
          public void start() {
            openScript(sProject.recentScripts().getCurrentFile());
            repaint();
          }
        });
    m.addItem(sRecentScriptsMenuItem);

    m.addItem("Open Layer...", KeyEvent.VK_O, META, new UserOperation() {
      @Override
      public void start() {
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
        new UserOperation() {
          @Override
          public boolean shouldBeEnabled() {
            return editor().hasName();
          }

          @Override
          public void start() {
            openNextFile();
            repaint();
          }
        });

    m.addSeparator();

    m.addItem("Close", KeyEvent.VK_W, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return currentScriptCloseable();
      }

      @Override
      public void start() {
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
    m.addItem("Close All", KeyEvent.VK_W, CTRL | SHIFT, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return currentScriptCloseable();
      }

      @Override
      public void start() {
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

    m.addItem("Save", KeyEvent.VK_S, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return !editor().hasName() || editor().modified();
      }

      @Override
      public void start() {
        editor().doSave(null, false, false);
        repaint();
      }
    });

    m.addItem("Save As...", KeyEvent.VK_A, CTRL | SHIFT, new UserOperation() {
      @Override
      public void start() {
        editor().doSave(null, true, false);
        repaint();
      }
    });
    m.addItem("Save All", KeyEvent.VK_S, CTRL | SHIFT, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        boolean ret = false;
        for (int i = 0; i < sScriptSet.size(); i++) {
          ScriptEditor ed = sScriptSet.get(i);
          if (!ed.getScript().hasName() || ed.modified())
            ret = true;
        }
        return ret;
      }

      @Override
      public void start() {
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
    m.addItem("Save As Next", KeyEvent.VK_S, META | SHIFT, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return editor().hasName();
      }

      @Override
      public void start() {
        File f = AppTools.incrementFile(editor().getFile());
        editor().doSave(f, false, true);
        repaint();
      }
    });

    // -----------------------------------
    m.addMenu("Edit", projectMustBeOpenHandler);
    sUndoMenuItem = m.addItem("Undo", KeyEvent.VK_Z, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return editor().undoManager().undoPossible();
      }

      @Override
      public void start() {
        editor().doUndo();
        repaint();
      }
    });

    sRedoMenuItem = m.addItem("Redo", KeyEvent.VK_Y, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return editor().mUndoManager.redoPossible();
      }

      @Override
      public void start() {
        editor().doRedo();
        repaint();
      }
    });
    m.addSeparator();
    m.addItem("Cut", KeyEvent.VK_X, CTRL, new UserOperation() {

      @Override
      public boolean shouldBeEnabled() {
        r = new CutReversible();
        return r.valid();
      }

      @Override
      public void start() {
        editor().recordCommand(r);
        perform(r);
      }

      private Command r;
    });
    m.addItem("Copy", KeyEvent.VK_C, CTRL, new UserOperation() {

      @Override
      public boolean shouldBeEnabled() {
        r = new CopyReversible();
        return r.valid();
      }

      @Override
      public void start() {
        editor().recordCommand(r);
        perform(r);
      }

      private Command r;
    });

    m.addItem("Paste", KeyEvent.VK_V, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        r = new PasteReversible();
        return r.valid();
      }

      @Override
      public void start() {
        editor().recordCommand(r);
        perform(r);
      }

      private Command r;
    });

    m.addItem("Duplicate", KeyEvent.VK_D, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        r = new DuplicateReversible();
        return r.valid();
      }

      @Override
      public void start() {
        editor().recordCommand(r);
        perform(r);
      }

      private Command r;

    });

    m.addSeparator();
    m.addItem("Select All", KeyEvent.VK_A, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return !items().isEmpty();
      }

      @Override
      public void start() {
        items().selectAll();
        repaint();
      }
    });
    m.addItem("Select None", KeyEvent.VK_ESCAPE, 0, new UserOperation() {
      private SelectNoneOper r;

      public boolean shouldBeEnabled() {
        r = new SelectNoneOper();
        return r.valid();
      }

      @Override
      public void start() {
        r.perform();
        repaint();
      }
    });

    m.addSeparator();

    m.addItem("Move Backward", KeyEvent.VK_OPEN_BRACKET, 0,
        new UserOperation() {
          private Command r;

          public boolean shouldBeEnabled() {
            warning("refactor");
            return false;
            // r = new AdjustSlotsReversible(1, false);
            // return r.valid();
          }

          @Override
          public void start() {
            editor().recordCommand(r);
            perform(r);
          }
        });
    m.addItem("Move Forward", KeyEvent.VK_CLOSE_BRACKET, 0,
        new UserOperation() {
          private Command r;

          public boolean shouldBeEnabled() {
            warning("refactor");
            return false;
            // r = new AdjustSlotsReversible(-1, false);
            // return r.valid();
          }

          @Override
          public void start() {
            editor().recordCommand(r);
            perform(r);
          }
        });
    m.addItem("Move to Rear", KeyEvent.VK_OPEN_BRACKET, CTRL,
        new UserOperation() {
          private Command r;

          public boolean shouldBeEnabled() {
            warning("refactor");
            return false;
            // r = new AdjustSlotsReversible(1, true);
            // return r.valid();
          }

          @Override
          public void start() {
            editor().recordCommand(r);
            perform(r);
          }
        });
    m.addItem("Move to Front", KeyEvent.VK_CLOSE_BRACKET, CTRL,
        new UserOperation() {
          private Command r;

          public boolean shouldBeEnabled() {
            warning("refactor");
            return false;
            // r = new AdjustSlotsReversible(-1, true);
            // return r.valid();
          }

          @Override
          public void start() {
            editor().recordCommand(r);
            perform(r);
          }
        });

    m.addSeparator();

    m.addItem("Group", KeyEvent.VK_G, CTRL, new UserOperation() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = GroupObject.getGroupReversible();
        return r.valid();
      }

      @Override
      public void start() {
        editor().recordCommand(r);
        perform(r);
      }
    });
    m.addItem("Ungroup", KeyEvent.VK_U, CTRL, new UserOperation() {
      private Command r;

      public boolean shouldBeEnabled() {
        r = GroupObject.getUnGroupReversible();
        return r.valid();
      }

      @Override
      public void start() {
        editor().recordCommand(r);
        perform(r);
      }
    });

    m.addSeparator();
    m.addItem("Move Left", KeyEvent.VK_LEFT, 0, new CursorMoveOperation(0));
    m.addItem("Move Right", KeyEvent.VK_RIGHT, 0, new CursorMoveOperation(1));
    m.addItem("Move Up", KeyEvent.VK_UP, 0, new CursorMoveOperation(2));
    m.addItem("Move Down", KeyEvent.VK_DOWN, 0, new CursorMoveOperation(3));
    m.addSeparator();
    m.addItem("Flip Horizontally", KeyEvent.VK_H, SHIFT | CTRL,
        new UserOperation() {
          private Command r;

          public boolean shouldBeEnabled() {
            r = new FlipReversible(true);
            return r.valid();
          }

          @Override
          public void start() {
            editor().recordCommand(r);
            perform(r);
          }
        });
    m.addItem("Flip Vertically", KeyEvent.VK_V, SHIFT | CTRL,
        new UserOperation() {
          private Command r;

          public boolean shouldBeEnabled() {
            r = new FlipReversible(false);
            return r.valid();
          }

          @Override
          public void start() {
            editor().recordCommand(r);
            perform(r);
          }
        });

    m.addItem("Rotate", KeyEvent.VK_R, CTRL, new UserOperation() {
      public boolean shouldBeEnabled() {
        return new RotateOper().shouldBeEnabled();
      }

      @Override
      public void start() {
        sUserEventManager.setOperation(new RotateOper());
        repaint();
      }
    });

    // m.addItem("Reset Rotate", KeyEvent.VK_R, CTRL | SHIFT, new
    // UserOperation() {
    // private Command r;
    //
    // public boolean shouldBeEnabled() {
    // r = RotateOper.getResetOper();
    // return r.valid();
    // }
    //
    // @Override
    // public void start() {
    // editor().recordCommand(r);
    // perform(r);
    // repaint();
    // }
    // });
    m.addItem("Scale", KeyEvent.VK_E, CTRL, new UserOperation() {
      public boolean shouldBeEnabled() {
        return sScaleOperation.shouldBeEnabled();
      }

      @Override
      public void start() {
        sUserEventManager.setOperation(sScaleOperation);
        repaint();
      }
    });
    m.addItem("Reset Scale", KeyEvent.VK_E, CTRL | SHIFT, new UserOperation() {
      // private Command r;

      public boolean shouldBeEnabled() {
        unimp("reset scale");
        return false;
        // r = ScaleOper.getResetOper();
        // return r.valid();
      }

      @Override
      public void start() {
        // editor().recordCommand(r);
        // perform(r);
        // repaint();
      }
    });

    // -----------------------------------
    m.addMenu("View", projectMustBeOpenHandler);
    m.addItem("Zoom In", KeyEvent.VK_EQUALS, CTRL, new UserOperation() {
      @Override
      public void start() {
        doAdjustZoom(-1);
      }

      @Override
      public boolean shouldBeEnabled() {
        return sZoomFactor < 20;
      }
    });

    m.addItem("Zoom Out", KeyEvent.VK_MINUS, CTRL, new UserOperation() {
      @Override
      public void start() {
        doAdjustZoom(1);
      }

      @Override
      public boolean shouldBeEnabled() {
        return sZoomFactor > .1f;
      }
    });
    m.addItem("Zoom Reset", KeyEvent.VK_0, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return sZoomFactor != 1;
      }

      @Override
      public void start() {
        doAdjustZoom(0);
      }
    });
    m.addSeparator();
    m.addItem("Snap to Grid", KeyEvent.VK_G, CTRL | SHIFT, new UserOperation() {
      private Command r;

      @Override
      public boolean shouldBeEnabled() {
        r = Grid.getOper();
        return r.valid();

      }

      @Override
      public void start() {
        editor().recordCommand(r);
        perform(r);
      }
    });

    // m.addItem("Grid...", 0, 0, new ItemHandler() {
    // public void go() {
    // Grid.showParams();
    // }
    // });

    // -----------------------------------
    m.addMenu("Objects", projectMustBeOpenHandler);
    m.addItem("Add Sprite", KeyEvent.VK_S, 0, new AddSpriteOper());
    m.addItem("Select Atlas", KeyEvent.VK_T, CTRL, new UserOperation() {
      @Override
      public void start() {
        doSelectAtlas(null);
      }
    });

    m.addSeparator();
    // ---------------------------------
    m.addItem("Add Rectangle", KeyEvent.VK_R, 0, new UserOperation() {
      public void start() {
        sUserEventManager.setOperation(RectangleObject
            .buildNewObjectOperation());
      }
    });

    // unimp("Palette menu");
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
    // editor().recordCommand(r);
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
    m.addItem("Next", KeyEvent.VK_EQUALS, 0, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return sScriptSet.size() > 1;
      }

      @Override
      public void start() {
        sScriptSet.setCursor(MyMath.myMod(sScriptSet.getCursor() + 1,
            sScriptSet.size()));
        repaint();
      }
    });
    m.addItem("Previous", KeyEvent.VK_MINUS, 0, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return sScriptSet.size() > 1;
      }

      @Override
      public void start() {
        sScriptSet.setCursor(MyMath.myMod(sScriptSet.getCursor() - 1,
            sScriptSet.size()));
        repaint();
      }
    });
    m.addSeparator();
    m.addItem("Open Set", KeyEvent.VK_O, CTRL | SHIFT, new UserOperation() {
      @Override
      public void start() {
        doOpenSet(null);
        repaint();
      }
    });

    sRecentScriptSetsMenuItem = new RecentFiles.Menu("Open Recent Set",
        sUserEventManager, null, new UserOperation() {
          public void start() {
            doOpenSet(project().recentScriptSets().getCurrentFile());
            repaint();
          }
        });
    m.addItem(sRecentScriptSetsMenuItem);
    m.addItem("Save Set As...", KeyEvent.VK_S, CTRL | SHIFT,
        new UserOperation() {
          @Override
          public void start() {
            doSaveSet();
            repaint();
          }
        });
    m.addSeparator();

    // -----------------------------------

    m.addMenu("Project");
    m.addItem("New Project", 0, 0, new UserOperation() {
      @Override
      public void start() {
        doNewProject();
        ScriptEditor.repaint();
      }
    });
    m.addItem("Open Project", 0, 0, new UserOperation() {
      @Override
      public void start() {
        File f = AppTools.chooseFileToOpen("Open Project", ScriptProject.FILES,
            null);
        if (f != null) {
          openProject(f);
          repaint();
        }
      }
    });
    m.addItem(new RecentFiles.Menu("Open Recent Project", sUserEventManager,
        sRecentProjects, new UserOperation() {
          public void start() {
            openProject(sRecentProjects.getCurrentFile());
            repaint();
          }
        }));

    m.addItem("Close Project", 0, 0, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return isProjectOpen();
      }

      @Override
      public void start() {
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
        sUserEventManager.setEnabled(false);
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
      slots = items().getSelectedSlots();
    }

    public void perform() {
      items().clearAllSelected();
      repaint();
    }

    public boolean valid() {
      return slots.size() > 0;
    }

    private SlotList slots;
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
  // editor().recordCommand(r);
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
    EditSelectedCommand r = new EditSelectedCommand() {

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
      editor().recordCommand(r);
      perform(r);
      repaint();
    } else {
      sUserEventManager.setOperation(new AddSpriteOper());
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
    // TODO: we used to reset the current undo manager here; is it still
    // required?
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

    sUserEventManager = new UserEventManager(new DefaultMouseOper());

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
      EditorPanelGL editorPanel = new EditorPanelGL(sInfoPanel,
          sUserEventManager);
      sEditorPanel = editorPanel;

      MouseEventGenerator m = new MouseEventGenerator();
      m.setView(editorPanel, editorPanel.getComponent());
      sUserEventManager.setListener(new UserEvent.Listener() {
        @Override
        public void processUserEvent(UserEvent event) {
          // Request a refresh of the editor view after any event
          repaint();
        }
      });

      sEditorPanelComponent = editorPanel.getComponent();
      pnl.add(sEditorPanelComponent, BorderLayout.CENTER);

      pnl.add(sInfoPanel, BorderLayout.SOUTH);
      p.add(pnl, BorderLayout.CENTER);
    }

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
    }

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      sRecentProjects.put(map, PROJECTS_TAG);
      map.put("ORIGIN", sInfoPanel.isOriginShowing());
      map.put("FADED", sInfoPanel.isFaded());
      map.put("ZOOM", sZoomFactor);
      sFocus.put(map, "FOCUS");
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
        sUserEventManager.setEnabled(true);
        sRecentScriptsMenuItem.setRecentFiles(sProject.recentScripts());
        sRecentScriptSetsMenuItem.setRecentFiles(sProject.recentScriptSets());
        // unimp("update recent atlas list");
        // sRecentAtlases.setAlias(mProject.recentAtlases());
        sRecentProjects.setCurrentFile(sProject.file());

        // unimp("have combo box adjust for recent atlases");
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

  static void perform(Command op) {
    // TODO: should this be part of the UndoManager?
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
    return sScriptSet.size() > 1 || editor().hasName() || !items().isEmpty();
  }

  // ------------------- Instance methods -----------------------------

  public ScriptEditor() {
    assertProjectOpen();
    mScript = new Script(project(), null);
    mState = new ScriptEditorState();
    mUndoManager = new UndoManager();
  }

  public File getFile() {
    return getScript().getFile();
  }

  private boolean hasName() {
    return getScript().hasName();
  }

  public void render(GLPanel panel, boolean toBgnd) {
    EdObjectArray items = items();
    for (int slot = 0; slot < items.size(); slot++) {
      EdObject obj = items.get(slot);
      panel.lineWidth(1.5f / zoomFactor());
      if (toBgnd) {
        obj.render(panel, false, false);
      } else
        obj.render(panel, items.getSelectedSlots().contains(slot),
            items.getEditableSlot(sUserEventManager.getOperation()) == slot);
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

  ScriptEditorState getStateSnapshot() {
    if (mStateSnapshot == null) {
      mStateSnapshot = frozen(mState);
    }
    return mStateSnapshot;
  }

  ScriptEditorState getCurrentState() {
    return mState;
  }

  void disposeOfStateSnapshot() {
    mStateSnapshot = null;
  }

  void setState(ScriptEditorState state) {
    if (state.isMutable())
      throw new IllegalArgumentException();
    disposeOfStateSnapshot();
    mStateSnapshot = state;
    mState = mutableCopyOf(state);
  }

  // ------------------------- Undo Stuff ---------------------------

  UndoManager undoManager() {
    return mUndoManager;
  }

  /**
   * Add a command that has already been performed to the undo stack
   */
  void recordCommand(Command command) {
    mUndoManager.perform(command);

    // Dispose of any cached state snapshot; it's likely invalid since we just
    // performed a command
    disposeOfStateSnapshot();
    updateUndoLabels();
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
      if (mUndoManager.undoPossible()) {
        Command r = mUndoManager.peekUndo();
        lbl = "Undo " + commandDescription(r);
      }
      sUndoMenuItem.setText(lbl);
    }
    {
      String lbl = "Redo";
      if (mUndoManager.redoPossible()) {
        Command r = mUndoManager.peekRedo();
        lbl = "Redo " + commandDescription(r);
      }
      sRedoMenuItem.setText(lbl);
    }
  }

  private void doRedo() {
    sUserEventManager.clearOperation();
    Command oper = mUndoManager.redo();
    setChanges(1);
    oper.perform();
    updateUndoLabels();
  }

  private void doUndo() {
    sUserEventManager.clearOperation();
    Command command = mUndoManager.undo();
    setChanges(-1);
    command.getReverse().perform();
    updateUndoLabels();
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
      // Make sure we're saving the most recent set of items (refactor this?)
      mScript.setItems(items());
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

  // --- Class fields
  private static Component sEditorPanelComponent;
  private static GLPanel sEditorPanel;
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
  private static UserEventManager sUserEventManager;
  private static UserOperation sScaleOperation = new ScaleOper();

  // --- Instance fields

  private int mChangesSinceSaved;
  private UndoManager mUndoManager;
  // The script being edited by this editor. We could make the editor a subclass
  // of Script, but we'll favor composition over inheritance, at the expense of
  // some extra methods to call Script method counterparts (e.g. hasName())
  private Script mScript;
  // The current (and mutable) editor state
  private ScriptEditorState mState;
  // The most recent frozen snapshot of the editor state
  private ScriptEditorState mStateSnapshot;

}
