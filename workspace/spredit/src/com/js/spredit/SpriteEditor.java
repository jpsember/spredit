package com.js.spredit;

import static apputil.MyMenuBar.*;

import com.js.editor.Enableable;
import com.js.editor.UserEvent;
import com.js.editor.UserEventManager;
import com.js.editor.UserOperation;
import com.js.geometry.*;

import static com.js.basic.Tools.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.json.JSONException;
import org.json.JSONObject;

import tex.*;
import apputil.*;

public class SpriteEditor {

  public static void init(JComponent c) {

    // UserOperation.add(new MoveFocusOper());
    // UserOperation.add(new MoveCPOper());

    JPanel pnl = new JPanel(new BorderLayout());

    sUserEventManager = new UserEventManager(new DefaultMouseOper());

    imgDir = new ImgDirectory();
    spritePanel = new SpritePanel(sUserEventManager);

    MouseEventGenerator m = new MouseEventGenerator();
    m.setView(spritePanel, spritePanel.getComponent());
    sUserEventManager.setListener(new UserEvent.Listener() {
      @Override
      public void processUserEvent(UserEvent event) {
        // Request a refresh of the editor view after any event
        repaint();
      }
    });

    pnl.add(spritePanel.getComponent(), BorderLayout.CENTER);

    c.add(imgDir, BorderLayout.EAST);

    infoPanel = new InfoPanel();
    pnl.add(infoPanel, BorderLayout.SOUTH);

    spritePanel.setCenterPointCheckBox(cpt);
    spritePanel.setShowClipCheckBox(showClip);
    c.add(pnl, BorderLayout.CENTER);

    addMenus();

    {
      File base = sRecentProjects.getMostRecentFile();
      if (base != null && !TexProject.FILES.accept(base))
        base = null;
      if (base != null && base.exists()) {
        openProject(base);
      }
    }
    //
    // if (lastProjectPath != null && lastProjectPath.exists())
    // openProject(lastProjectPath);
  }

  public static ConfigSet.Interface CONFIG = new ConfigSet.Interface() {
    private static final String OUR_TAG = "SpriteEditor";
    private static final String PROJECTS_TAG = "RecentProjects";
    private static final String CPT_TAG = "ShowCenterpoint";
    private static final String CLIP_TAG = "ShowClip";

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      JSONObject map2 = new JSONObject();
      sRecentProjects.put(map2, PROJECTS_TAG);
      map2.put(CPT_TAG, cpt.isSelected());
      map2.put(CLIP_TAG, showClip.isSelected());
      map.put(OUR_TAG, map2);
    }

    @Override
    public void readFrom(JSONObject map) throws JSONException {
      JSONObject map2 = map.optJSONObject(OUR_TAG);
      if (map2 == null)
        return;
      sRecentProjects.restore(map2, PROJECTS_TAG);
      cpt.setSelected(map2.getBoolean(CPT_TAG));
      showClip.setSelected(map2.getBoolean(CLIP_TAG));
    }

  };

  public static boolean doCloseProject() {

    final boolean db = false;

    if (db) {
      warning("db:doCloseProject");
      pr("doCloseProject " + project);
    }
    do {
      if (project == null)
        break;

      flush();
      setProjectTo(null);
      // closeProject();

    } while (false);
    return project == null;

  }

  private static void newProject() {
    do {
      File f = AppTools.chooseFileToSave("Create New Project",
          TexProject.FILES, null);
      if (f == null)
        break;
      //
      // )
      // JFileChooser fc = new JFileChooser(lastProjectPath);
      // fc.setFileFilter(TexProject.FILES_AND_DIRS);
      //
      // int result = fc.showDialog(null, "Create New Project");
      // if (result != JFileChooser.APPROVE_OPTION)
      // break;

      TexProject newProj;
      // File f = Streams.changeExtension(fc.getSelectedFile(),
      // TexProject.SRC_EXT);

      if (!SprUtils.verifyCreateTexProject(f))
        break;

      try {
        newProj = TexProject.create(f);
      } catch (IOException e) {
        AppTools.showError("opening project", e);
        break;
      }
      setProjectTo(newProj);
    } while (false);
  }

  private static void closeProject() {
    if (project != null) {

      imgDir.flushTo(project);
      flushTo(project);

      try {
        project.flush();
      } catch (IOException e) {
        AppTools.showError("flushing project", e);
      }
      project = null;
    }
  }

  private static void setProjectTo(TexProject p) {
    final boolean db = false;

    if (db)
      pr("setProjectTo " + p);

    if (project == p)
      return;

    if (project != null)
      closeProject();

    project = p;

    sRecentProjects.setCurrentFile(project == null ? null : project.file());

    // MyMenuBar.updateRecentFilesFor(recentProjectsMenuItem, recentProjects);
    //
    //
    // lastProjectPath = project.file();

    if (p != null) {
      if (db)
        pr("SpritePanel, setProject to " + p);

      float z = (float) p.getDefaults().optDouble(TAG, 1.0);
      spritePanel.setZoom(z);
      spritePanel.repaint(50);
      try {
        BuildParms.parseFrom(p.getDefaults().optJSONObject("BUILD"));
      } catch (JSONException e) {
        die(e);
      }
    } else {
      setSprite(null);
    }

    imgDir.setProject(p);

    // have imgDir notify spritePanel initially, now that both set up and ready
    // to listen.
    SpriteInfo si = imgDir.getSelectedSprite();
    setSprite(si);

    repaint();

  }

  private static AtlasDisplay atlasDisp;

  private static void doBuild() {
    if (project == null)
      return;
    flush();
    try {

      Builder b = new Builder();
      b.includePalette();

      b.setProject(project);
      b.gatherSprites();

      Atlas built = b.build(project.atlasFile(), BuildParms.texSize(), null, 1);
      if (built == null) {
        AppTools.showMsg("Not all sprites fit into atlas!");
      }

      // unimp("have special SpriteInfo flag for sprites that didn't make it into the atlas, and scroll to one of them in the AtlasPanel.");
      if (atlasDisp != null) {
        atlasDisp.setVisible(false);
        atlasDisp = null;
      }
      project.discardAtlas();

      if (built != null) {
        atlasDisp = AtlasDisplay.create(project);
        AppTools.frame().toFront();
      }
    } catch (IOException e) {
      AppTools.showError("building atlas", e);
    }

  }

  private static void openProject(File projFile) {
    if (projFile == null) {

      File base = sRecentProjects.getMostRecentFile();

      projFile = AppTools.chooseFileToOpen("Open Project", TexProject.FILES,
          base);
    }
    if (projFile != null) {
      try {
        TexProject tp = new TexProject(projFile);
        if (tp != null)
          setProjectTo(tp);
        sUserEventManager.setEnabled(true);
      } catch (IOException e) {
        AppTools.showError("opening project", e);
      }
    }
  }

  private static boolean isProjectOpen() {
    return project != null;
  }

  private static void repaint() {
    infoPanel.refresh();
    spritePanel.repaint();
  }

  private static final int XMOVE = 14, YMOVE = 22;

  private static void addMenus() {

    Enableable projectMustBeOpenHandler = new Enableable() {
      @Override
      public boolean shouldBeEnabled() {
        return isProjectOpen();
      }
    };

    MyMenuBar m = new MyMenuBar(AppTools.frame(), sUserEventManager);

    m.addAppMenu();
    //
    // if (AppTools.isMac()) {
    // MacUtils.setQuitHandler();
    // } else {
    // m.addMenu("ScrEdit");
    // m.addItem("Quit", KeyEvent.VK_Q, CTRL, new ActionHandler() {
    // public void go() {
    // if (AppTools.app().exitProgram())
    // System.exit(0);
    // }
    // });
    // }

    // -----------------------------------
    m.addMenu("View", projectMustBeOpenHandler);
    m.addItem("Zoom In", KeyEvent.VK_EQUALS, CTRL, new UserOperation() {
      @Override
      public boolean shouldBeEnabled() {
        return spritePanel.getZoom() < 20;
      }

      @Override
      public void start() {
        doAdjustZoom(-1);
      }
    });

    m.addItem("Zoom Out", KeyEvent.VK_MINUS, CTRL, new UserOperation() {
      public void start() {
        doAdjustZoom(1);
      }

      public boolean shouldBeEnabled() {
        return spritePanel.getZoom() > .1f;
      }
    });
    m.addItem("Zoom Reset", KeyEvent.VK_0, CTRL, new UserOperation() {
      public boolean shouldBeEnabled() {
        return spritePanel.getZoom() != 1;
      }

      public void start() {
        doAdjustZoom(0);
      }
    });

    // -----------------------------------
    m.addMenu("Sprite", projectMustBeOpenHandler);

    // m.addItem("Increase Resolution", KeyEvent.VK_EQUALS, SHIFT,
    // new ItemHandler() {
    // public void go() {
    // doAdjustCompress(-1);
    // }
    //
    // public boolean isEnabled() {
    // return defined() && spriteInfo.compressionFactor() < 1;
    // }
    // });
    //
    // m.addItem("Decrease Resolution", KeyEvent.VK_MINUS, SHIFT,
    // new ItemHandler() {
    // public void go() {
    // doAdjustCompress(1);
    // }
    //
    // public boolean isEnabled() {
    // return defined() && spriteInfo.compressionFactor() > .1f;
    // }
    // });
    // m.addItem("Full Resolution", KeyEvent.VK_0, SHIFT, new ItemHandler() {
    // public boolean isEnabled() {
    // return defined() && spriteInfo.compressionFactor() != 1;
    // }
    //
    // public void go() {
    // doAdjustCompress(0);
    // }
    // });
    // m.addSeparator();

    m.addItem("Reset Clip", KeyEvent.VK_R, CTRL, new UserOperation() {
      public void start() {
        doResetClip();
      }

      public boolean shouldBeEnabled() {
        return defined();
      }
    });
    m.addSeparator();

    // temporary items for helping to edit fonts
    if (true) {
      m.addItem("Left", KeyEvent.VK_LEFT, CTRL, new UserOperation() {
        public void start() {
          move(-XMOVE, 0);
        }

        public boolean shouldBeEnabled() {
          return defined();
        }
      });
      m.addItem("Right", KeyEvent.VK_RIGHT, CTRL, new UserOperation() {
        public void start() {
          move(XMOVE, 0);
        }

        public boolean shouldBeEnabled() {
          return defined();
        }
      });
      m.addItem("Up", KeyEvent.VK_UP, CTRL, new UserOperation() {
        public void start() {
          move(0, YMOVE);
        }

        public boolean shouldBeEnabled() {
          return defined();
        }
      });
      m.addItem("Down", KeyEvent.VK_DOWN, CTRL, new UserOperation() {
        public void start() {
          move(0, -YMOVE);

        }

        public boolean shouldBeEnabled() {
          return defined();
        }
      });
      m.addSeparator();
    }

    m.addItem("Create Alias", KeyEvent.VK_A, SHIFT, new UserOperation() {
      public boolean shouldBeEnabled() {
        return defined();
      }

      public void start() {
        doCreateAlias();
      }
    });
    m.addItem("Delete Alias", KeyEvent.VK_D, SHIFT | CTRL, new UserOperation() {
      public boolean shouldBeEnabled() {
        return defined() && spriteInfo.isAlias();
      }

      public void start() {
        doDeleteAlias();
      }
    });

    // -----------------------------------
    m.addMenu("Project");
    m.addItem("New", KeyEvent.VK_N, CTRL, new UserOperation() {
      public void start() {
        newProject();
        repaint();
      }
    });
    m.addItem("Open", KeyEvent.VK_O, CTRL, new UserOperation() {
      public void start() {
        openProject(null);
        repaint();
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

    m.addItem("Close", KeyEvent.VK_W, CTRL, new UserOperation() {
      public boolean shouldBeEnabled() {
        return SpriteEditor.isProjectOpen();
      };

      public void start() {
        setProjectTo(null);
        // closeProject();
        repaint();
      }
    });
    m.addSeparator();

    m.addItem("Build", KeyEvent.VK_B, CTRL, new UserOperation() {
      public boolean shouldBeEnabled() {
        return SpriteEditor.isProjectOpen();
      };

      public void start() {
        doBuild();
        repaint();
      }
    });
    m.addItem("Build Parameters", KeyEvent.VK_B, SHIFT | CTRL,
        new UserOperation() {
          public boolean shouldBeEnabled() {
            return SpriteEditor.isProjectOpen();
          }

          public void start() {
            BuildParms.showDialog();
          }
        });

  }

  private static void doResetClip() {
    spriteInfo.resetClip();
    repaint();
  }

  private static void doCreateAlias() {
    final boolean db = false;
    File fm = spriteInfo.metaPath();
    if (db)
      pr("doCreateAlias original meta=" + fm);

    File af = AppTools.incrementFile(fm);
    if (db)
      pr("alias meta=" + af);

    SpriteInfo si = spriteInfo.createAlias(af);
    si.flush();

    imgDir.add(si);
  }

  private static void move(int x, int y) {
    IRect r = spriteInfo.cropRect();
    r.x += x;
    r.y += y;
    spriteInfo.setCropRect(r);
    Point cp = spriteInfo.centerpoint();
    cp.x += x;
    cp.y += y;
    spriteInfo.setCenterpoint(cp);
    repaint();
  }

  private static void doDeleteAlias() {
    final boolean db = false;
    if (db)
      pr("doDeleteAlias " + spriteInfo);

    do {
      SpriteInfo si = spriteInfo;

      if (!si.isAlias())
        break;

      File fm = si.metaPath();

      setSprite(null);

      boolean wasDel = fm.delete();
      if (!wasDel) {
        AppTools.showMsg("Could not delete file: " + fm);
        break;
      }
      imgDir.remove(si);

      imgDir.select(si.id());
    } while (false);

  }

  /**
   * Adjust zoom factor
   * 
   * @param code
   *          0 to reset, -1 to decrease, +1 to increase
   */
  private static void doAdjustZoom(int code) {
    float zoomFactor = spritePanel.getZoom();
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
    spritePanel.setZoom(zoomFactor);
    spritePanel.invalidateFocus();

    repaint();
  }

  // /**
  // * Adjust resolution
  // *
  // * @param code
  // * 0 to reset, -1 to zoom out, +1 to zoom in
  // */
  // private static void doAdjustCompress(int code) {
  // float f = spriteInfo.compressionFactor();
  // switch (code) {
  // default:
  // f = 1;
  // break;
  // case 1:
  // f *= .8f;
  // break;
  // case -1:
  // f = Math.min(1, f * 1 / .8f);
  // break;
  // }
  // spriteInfo.setCompressionFactor(f);
  // repaint();
  // }

  private static class InfoPanel extends JPanel implements ActionListener {

    public InfoPanel() {

      setBorder(BorderFactory.createRaisedBevelBorder());
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      spc(5);

      add(new JLabel("CP:"));
      add(cpt);
      cpt.addActionListener(this);
      cpt.setToolTipText("Plot centerpoint");

      add(new JLabel("B:"));
      add(showClip);
      showClip.addActionListener(this);
      showClip.setToolTipText("Show bounding rectangle");

      add(new JLabel("Sprite:"));
      sprt = text(30);
      add(sprt);

      add(Box.createHorizontalGlue());

      add(new JLabel("Project:"));
      projPath = text(18);
      add(projPath);

      spc(5);

      refresh();
    }

    private static JTextField text(int chars) {
      JTextField tf = new JTextField();
      tf.setEditable(false);
      tf.setFont(AppTools.getFixedWidthFont());
      size(tf, chars);
      return tf;
    }

    private static void size(Component c, int chars) {
      Dimension d = new Dimension(chars * 13, Short.MAX_VALUE);
      Dimension cs = c.getPreferredSize();
      c.setPreferredSize(new Dimension(d.width, cs.height));
      c.setMaximumSize(d);
    }

    private void spc(int x) {
      add(Box.createRigidArea(new Dimension(x, 0)));
    }

    public void refresh() {
      final boolean db = false;
      if (db)
        pr("InfoPanel update(); projectOpen=" + isProjectOpen());

      if (isProjectOpen()) {
        upd(projPath, project.file().getName());
        upd(sprt, spriteId());
      } else {
        upd(sprt, null);
        upd(projPath, null);
      }

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

    private JTextField projPath;
    private JTextField sprt;

    @Override
    public void actionPerformed(ActionEvent arg0) {
      SpriteEditor.repaint();
    }
  }

  private static InfoPanel infoPanel;
  // private static float zoomFactor = 1;

  private static TexProject project;
  // private static File lastProjectPath;
  private static ImgDirectory imgDir;
  private static SpritePanel spritePanel;

  private static final String TAG = "SPRPANEL";

  public static void flushTo(TexProject p) {
    try {
      p.getDefaults().put(TAG, spritePanel.getZoom());
      JSONObject map = new JSONObject();
      BuildParms.encodeTo(map);
      p.getDefaults().put("BUILD", map);
    } catch (JSONException e) {
      die(e);
    }
  }

  private static void flush() {
    if (spriteInfo != null)
      spriteInfo.flush();
  }

  private static String spriteId() {
    return sprite == null ? null : sprite.id();
  }

  private static Sprite sprite;
  private static SpriteInfo spriteInfo;

  public static boolean defined() {
    return spriteInfo != null;
  }

  public static SpriteInfo getSpriteInfo() {
    if (!defined())
      throw new IllegalStateException();
    return spriteInfo;
  }

  public static void setSprite(SpriteInfo sp) {

    final boolean db = false;
    if (db)
      pr("setSprite " + sp + " (was " + spriteInfo + ")\n" + stackTrace(8));

    if (spriteInfo != sp) {
      flush();

      spritePanel.invalidateFocus();
      if (spriteInfo != null) {
        spriteInfo.releaseImage();
      }
      spriteInfo = sp;
      sprite = null;
      spritePanel.setSpriteInfo(spriteInfo);
      if (spriteInfo != null)
        sprite = spriteInfo.sprite();
      repaint();
    }
  }

  /**
   * Construct IPoint from MouseEvent
   * 
   * @param ev
   *          mouse event
   * @return IPoint containing mouse (view) coordinates
   */
  private static IPoint viewLoc(MouseEvent ev) {
    return new IPoint(ev.getX(), ev.getY());
  }

  /* private */static class MoveFocusOper extends OldUserOperation {

    @Override
    public void processUserEvent(UserEvent event) {
      die("not implemented yet");
    }

    @Override
    public boolean mouseDown() {
      boolean f = false;
      do {
        if (// !ev.isRight() ||
        ev.isControlDown() || ev.isShiftDown())
          break;
        startOrigin = new Point(spritePanel.getOrigin());
        f = true;
      } while (false);
      return f;
    }

    @Override
    public void mouseMove(boolean drag) {
      if (!drag)
        return;

      Point trans = new Point(IPoint.difference(startPtView, viewLoc(ev)));
      // compensate for view and world having flipped y axes
      trans.y = -trans.y;

      trans.applyScale(1 / spritePanel.getZoom());
      trans.add(startOrigin);
      spritePanel.setOrigin(trans);
      repaint();
    }

    private Point startOrigin;
  }

  public static SpritePanel getSpritePanel() {
    return spritePanel;
  }

  private static RecentFiles sRecentProjects = new RecentFiles(null);
  private static JCheckBox cpt = new JCheckBox();
  private static JCheckBox showClip = new JCheckBox();
  private static UserEventManager sUserEventManager;

}
