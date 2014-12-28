package com.js.spredit;

import static apputil.MyMenuBar.*;

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

  private static final boolean db = false;

  public static void init(JComponent c) {

    if (AppTools.isMac()) {

      MacUtils.setQuitHandler(SprMain.app());
    }

    MouseOper.add(new MoveFocusOper());
    for (int i = 0; i < 4; i++)
      MouseOper.add(new CornerOper(i));
    for (int i = 0; i < 4; i++)
      MouseOper.add(new EdgeOper(i));
    MouseOper.add(new MoveClipOper());
    MouseOper.add(new MoveCPOper());

    JPanel pnl = new JPanel(new BorderLayout());

    imgDir = new ImgDirectory();
    spritePanel = new SpritePanel();
    pnl.add(spritePanel.getComponent(), BorderLayout.CENTER);

    c.add(imgDir, BorderLayout.EAST);

    infoPanel = new InfoPanel();
    pnl.add(infoPanel, BorderLayout.SOUTH);

    spritePanel.setCenterPointCheckBox(cpt);
    spritePanel.setShowClipCheckBox(showClip);
    c.add(pnl, BorderLayout.CENTER);

    addMenus();

    {
      File base = null;
      if (recentProjects.size() > 0)
        base = recentProjects.get(0);
      if (base != null && !TexProject.FILES_ONLY.accept(base))
        base = null;
      if (base != null && base.exists()) {
        openProject(base);
      }
    }
    //
    // if (lastProjectPath != null && lastProjectPath.exists())
    // openProject(lastProjectPath);
  }

  public static IConfig CONFIG = new IConfig() {
    private static final String OUR_TAG = "SpriteEditor";
    private static final String PROJECTS_TAG = "RecentProjects";
    private static final String CPT_TAG = "ShowCenterpoint";
    private static final String CLIP_TAG = "ShowClip";

    @Override
    public void writeTo(JSONObject map) throws JSONException {
      JSONObject map2 = new JSONObject();
      map2.put(PROJECTS_TAG, recentProjects.encode());
      map2.put(CPT_TAG, cpt.isSelected());
      map2.put(CLIP_TAG, showClip.isSelected());
      map.put(OUR_TAG, map2);
    }

    @Override
    public void readFrom(JSONObject map) throws JSONException {
      JSONObject map2 = map.optJSONObject(OUR_TAG);
      if (map2 == null)
        return;
      recentProjects.decode(map2.getString(PROJECTS_TAG));
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
          TexProject.FILES_AND_DIRS, null);
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

    recentProjects.use(project == null ? null : project.file());

    MyMenuBar.updateRecentFilesFor(recentProjectsMenuItem, recentProjects);
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

      File base = null;
      if (recentProjects.size() > 0)
        base = recentProjects.get(0);

      projFile = AppTools.chooseFileToOpen("Open Project",
          TexProject.FILES_AND_DIRS, base); // lastProjectPath);
    }
    if (projFile != null) {
      try {
        TexProject tp = new TexProject(projFile);
        if (tp != null)
          setProjectTo(tp);
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

    MenuHandler projectMustBeOpenHandler = new MenuHandler() {
      @Override
      public boolean isEnabled() {
        return isProjectOpen();
      }
    };

    MyMenuBar m = new MyMenuBar(AppTools.frame());

    if (!AppTools.isMac()) {
      m.addMenu("ScrEdit");
      m.addItem("Quit", KeyEvent.VK_Q, CTRL, new ItemHandler() {
        public void go() {
          if (SprMain.app().exitProgram())
            System.exit(0);
        }
      });
    }

    // -----------------------------------
    m.addMenu("View", projectMustBeOpenHandler);
    m.addItem("Zoom In", KeyEvent.VK_EQUALS, CTRL, new ItemHandler() {
      public void go() {
        doAdjustZoom(-1);
      }

      public boolean isEnabled() {
        return spritePanel.getZoom() < 20;
      }
    });

    m.addItem("Zoom Out", KeyEvent.VK_MINUS, CTRL, new ItemHandler() {
      public void go() {
        doAdjustZoom(1);
      }

      public boolean isEnabled() {
        return spritePanel.getZoom() > .1f;
      }
    });
    m.addItem("Zoom Reset", KeyEvent.VK_0, CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return spritePanel.getZoom() != 1;
      }

      public void go() {
        doAdjustZoom(0);
      }
    });

    // -----------------------------------
    m.addMenu("Sprite", projectMustBeOpenHandler);

    m.addItem("Scale Up", KeyEvent.VK_EQUALS, 0, new ItemHandler() {
      public void go() {
        doAdjustScale(1);
      }

      public boolean isEnabled() {
        return defined() && spriteInfo.scaleFactor() < 1;
      }
    });

    m.addItem("Scale Down", KeyEvent.VK_MINUS, 0, new ItemHandler() {
      public void go() {
        doAdjustScale(-1);
      }

      public boolean isEnabled() {
        return defined() && spriteInfo.scaleFactor() > .1f;
      }
    });
    m.addItem("Original Size", KeyEvent.VK_0, 0, new ItemHandler() {
      public boolean isEnabled() {
        return defined() && spriteInfo.scaleFactor() != 1;
      }

      public void go() {
        doAdjustScale(0);
      }
    });

    // -----------------------------------
    m.addSeparator();

    m.addItem("Increase Resolution", KeyEvent.VK_EQUALS, SHIFT,
        new ItemHandler() {
          public void go() {
            doAdjustCompress(-1);
          }

          public boolean isEnabled() {
            return defined() && spriteInfo.compressionFactor() < 1;
          }
        });

    m.addItem("Decrease Resolution", KeyEvent.VK_MINUS, SHIFT,
        new ItemHandler() {
          public void go() {
            doAdjustCompress(1);
          }

          public boolean isEnabled() {
            return defined() && spriteInfo.compressionFactor() > .1f;
          }
        });
    m.addItem("Full Resolution", KeyEvent.VK_0, SHIFT, new ItemHandler() {
      public boolean isEnabled() {
        return defined() && spriteInfo.compressionFactor() != 1;
      }

      public void go() {
        doAdjustCompress(0);
      }
    });
    m.addSeparator();

    m.addItem("Reset Clip", KeyEvent.VK_R, CTRL, new ItemHandler() {
      public void go() {
        doResetClip();
      }

      public boolean isEnabled() {
        return defined();
      }
    });
    m.addSeparator();

    // temporary items for helping to edit fonts
    if (true) {
      m.addItem("Left", KeyEvent.VK_LEFT, CTRL, new ItemHandler() {
        public void go() {
          move(-XMOVE, 0);
        }

        public boolean isEnabled() {
          return defined();
        }
      });
      m.addItem("Right", KeyEvent.VK_RIGHT, CTRL, new ItemHandler() {
        public void go() {
          move(XMOVE, 0);
        }

        public boolean isEnabled() {
          return defined();
        }
      });
      m.addItem("Up", KeyEvent.VK_UP, CTRL, new ItemHandler() {
        public void go() {
          move(0, YMOVE);
        }

        public boolean isEnabled() {
          return defined();
        }
      });
      m.addItem("Down", KeyEvent.VK_DOWN, CTRL, new ItemHandler() {
        public void go() {
          move(0, -YMOVE);

        }

        public boolean isEnabled() {
          return defined();
        }
      });
      m.addSeparator();
    }

    m.addItem("Create Alias", KeyEvent.VK_A, SHIFT, new ItemHandler() {
      public boolean isEnabled() {
        return defined();
      }

      public void go() {
        doCreateAlias();
      }
    });
    m.addItem("Delete Alias", KeyEvent.VK_D, SHIFT | CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return defined() && spriteInfo.isAlias();
      }

      public void go() {
        doDeleteAlias();
      }
    });

    // -----------------------------------
    m.addMenu("Project");
    m.addItem("New", KeyEvent.VK_N, CTRL, new ItemHandler() {
      public void go() {
        newProject();
        repaint();
      }
    });
    m.addItem("Open", KeyEvent.VK_O, CTRL, new ItemHandler() {
      public void go() {
        openProject(null);
        repaint();
      }
    });

    recentProjectsMenuItem = m.addRecentFilesList("Open Recent Project",
        recentProjects, new ItemHandler() {
          @Override
          public void go() {
            openProject(recentProjects.current());
            repaint();
          }
        });

    m.addItem("Close", KeyEvent.VK_W, CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return SpriteEditor.isProjectOpen();
      };

      public void go() {
        setProjectTo(null);
        // closeProject();
        repaint();
      }
    });
    m.addSeparator();

    m.addItem("Build", KeyEvent.VK_B, CTRL, new ItemHandler() {
      public boolean isEnabled() {
        return SpriteEditor.isProjectOpen();
      };

      public void go() {
        doBuild();
        repaint();
      }
    });
    m.addItem("Build Parameters", KeyEvent.VK_B, SHIFT | CTRL,
        new ItemHandler() {
          public boolean isEnabled() {
            return SpriteEditor.isProjectOpen();
          }

          public void go() {
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
    IPoint cp = spriteInfo.centerPoint();
    cp.x += x;
    cp.y += y;
    spriteInfo.setCenterPoint(cp);
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

  /**
   * Adjust resolution
   * 
   * @param code
   *          0 to reset, -1 to zoom out, +1 to zoom in
   */
  private static void doAdjustCompress(int code) {
    float f = spriteInfo.compressionFactor();
    switch (code) {
    default:
      f = 1;
      break;
    case 1:
      f *= .8f;
      break;
    case -1:
      f = Math.min(1, f * 1 / .8f);
      break;
    }
    spriteInfo.setCompressionFactor(f);
    repaint();
  }

  /**
   * Adjust scale of sprite
   * 
   * @param code
   *          0 to reset, -1 to shrink, +1 to expand
   */
  private static void doAdjustScale(int code) {
    float f = spriteInfo.scaleFactor();
    switch (code) {
    default:
      f = 1;
      break;
    case -1:
      f *= .8f;
      break;
    case 1:
      f = Math.min(1, f * 1 / .8f);
      break;
    }
    spriteInfo.setScaleFactor(f);
    spritePanel.invalidateFocus();

    repaint();
  }

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

  private static int snapclamp(int v, int min, int max) {
    v = (int) MyMath.snapToGrid(v, 1);
    return MyMath.clamp(v, min, max);
  }

  private static final int HOT_DIST = 15;

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

  private static class MoveFocusOper extends MouseOper {

    @Override
    public boolean mouseDown() {
      boolean f = false;
      do {
        if (!right() || ev.isControlDown() || ev.isShiftDown())
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

  private static class MoveClipOper extends MouseOper {

    @Override
    public boolean mouseDown() {
      warning("snap to pixel boundaries");

      boolean f = false;
      do {

        if (!defined() || !right() || ev.isControlDown() || !ev.isShiftDown())
          break;

        IRect clip = spriteInfo.cropRect();
        float dist = clip.distanceFrom(startPt)
            * spritePanel.getZoom();

        if (dist > HOT_DIST)
          break;
        origLoc = clip.bottomLeft();
        spritePanel.setHighlightClip(true);

        f = true;
      } while (false);
      return f;
    }

    @Override
    public void mouseUp() {
      spritePanel.setHighlightClip(false);
      clearOperation();
    }

    @Override
    public void mouseMove(boolean drag) {
      ASSERT(drag);
      IPoint loc = new IPoint(Point.difference(currentPtF, startPtF));
      IRect clip = spriteInfo.cropRect();
      clip.x = origLoc.x + loc.x;
      clip.y = origLoc.y + loc.y;

      // don't let the clip region move outside of the original bounds
      clip.x = snapclamp(clip.x, 0, spriteInfo.workImageSize().x - clip.width);
      clip.y = snapclamp(clip.y, 0, spriteInfo.workImageSize().y - clip.height);
    }

    // bottom left of clip rectangle at start of operation
    private IPoint origLoc;
  }

  private static class MoveCPOper extends MouseOper {

    @Override
    public boolean mouseDown() {
      boolean f = false;
      do {

        if (!defined() || right() // || ev.isControlDown()
        )
          break;

        if (ev.isShiftDown())
          spriteInfo.setCenterPoint(startPt);

        origLoc = new Point(spriteInfo.centerPoint());
        float dist = MyMath.distanceBetween(origLoc, startPtF)
            * spritePanel.getZoom();
        if (dist > HOT_DIST * 3)
          break;

        // startOperPt = worldLoc;
        spritePanel.setHighlightCenterpoint(true);
        f = true;
      } while (false);
      return f;
    }

    @Override
    public void mouseUp() {
      spritePanel.setHighlightCenterpoint(false);
      clearOperation();
    }

    @Override
    public void mouseMove(boolean drag) {
      if (!drag)
        return;
      Point loc = Point.difference(currentPtF, startPtF);
      spriteInfo.setCenterPoint(new IPoint(Point.sum(origLoc, loc)));
    }

    // location of centerpoint at start of operation
    private Point origLoc;
  }

  private static class EdgeOper extends MouseOper {

    public EdgeOper(int edgeNum) {
      this.num = edgeNum;
    }

    private int num;

    @Override
    public boolean mouseDown() {
      final boolean db = false;
      boolean f = false;
      do {

        if (!defined() || right() || ev.isControlDown() || ev.isShiftDown())
          break;

        if (db)
          pr("EdgeOper mouseDown, world=" + startPt);

        IPoint pt1, pt2;
        {
          IRect clip = spriteInfo.cropRect();
          switch (num) {
          default:
            pt1 = clip.bottomLeft();
            pt2 = clip.bottomRight();
            break;
          case 1:
            pt1 = clip.bottomRight();
            pt2 = clip.topRight();
            break;
          case 2:
            pt1 = clip.topRight();
            pt2 = clip.topLeft();
            break;
          case 3:
            pt1 = clip.topLeft();
            pt2 = clip.bottomLeft();
            break;
          }
        }

        float dist = MyMath.ptDistanceToSegment(new Point(startPt), new Point(
            pt1), new Point(pt2), null)
            * spritePanel.getZoom();

        if (dist > HOT_DIST)
          break;
        origClip = new IRect(spriteInfo.cropRect());
        spritePanel.setHighlightClip(true);
        f = true;
      } while (false);
      return f;
    }

    @Override
    public void mouseMove(boolean drag) {
      if (db)
        pr("EdgeOper mouseMove, drag=" + drag);

      if (!drag)
        return;
      IPoint loc = new IPoint(Point.difference(currentPtF, startPtF));
      IPoint bounds = spriteInfo.workImageSize();

      IRect clip = spriteInfo.cropRect();
      int x1 = clip.x;
      int y1 = clip.y;
      int x2 = clip.endX();
      int y2 = clip.endY();

      switch (num) {
      case 0:
        y1 = snapclamp(origClip.y + loc.y, 0, y2 - 1);
        break;
      case 1:
        x2 = snapclamp(origClip.endX() + loc.x, x1 + 1, bounds.x);
        break;
      case 2:
        y2 = snapclamp(origClip.endY() + loc.y, y1 + 1, bounds.y);
        break;
      case 3:
        x1 = snapclamp(origClip.x + loc.x, 0, x2 - 1);
        break;
      }
      spriteInfo.setCropRect(new IRect(x1, y1, x2 - x1, y2 - y1));
    }

    @Override
    public void mouseUp() {
      spritePanel.setHighlightClip(false);
      clearOperation();
    }

    // clip rectangle at start of operation
    private IRect origClip;
  };

  private static class CornerOper extends MouseOper {
    public CornerOper(int corner) {
      this.num = corner;
    }

    private int num;

    @Override
    public boolean mouseDown() {
      final boolean db = false;
      boolean f = false;
      do {

        if (!defined() || right() || ev.isControlDown() || ev.isShiftDown())
          break;

        if (db)
          pr("start, world=" + startPt);

        IPoint pt1;
        {
          IRect clip = spriteInfo.cropRect();
          switch (num) {
          default:
            pt1 = clip.bottomLeft();
            break;
          case 1:
            pt1 = clip.bottomRight();
            break;
          case 2:
            pt1 = clip.topRight();
            break;
          case 3:
            pt1 = clip.topLeft();
            break;
          }
        }

        float dist = MyMath.distanceBetween(startPtF, new Point(pt1))
            * spritePanel.getZoom();
        if (dist > HOT_DIST)
          break;

        origClip = new IRect(spriteInfo.cropRect());
        spritePanel.setHighlightClip(true);
        f = true;
      } while (false);
      return f;
    }

    @Override
    public void mouseMove(boolean drag) {
      IPoint loc = new IPoint(Point.difference(currentPtF, startPtF));
      IPoint bounds = spriteInfo.workImageSize();

      IRect clip = spriteInfo.cropRect();
      int x1 = clip.x;
      int y1 = clip.y;
      int x2 = clip.endX();
      int y2 = clip.endY();

      switch (num) {
      case 0:
        x1 = (int) snapclamp(origClip.x + loc.x, 0, x2 - 1);
        y1 = (int) snapclamp(origClip.y + loc.y, 0, y2 - 1);
        break;
      case 1:
        x2 = (int) snapclamp(origClip.endX() + loc.x, x1 + 1, bounds.x);
        y1 = (int) snapclamp(origClip.y + loc.y, 0, y2 - 1);
        break;
      case 2:
        x2 = (int) snapclamp(origClip.endX() + loc.x, x1 + 1, bounds.x);
        y2 = (int) snapclamp(origClip.endY() + loc.y, y1 + 1, bounds.y);
        break;
      case 3:
        y2 = (int) snapclamp(origClip.endY() + loc.y, y1 + 1, bounds.y);
        x1 = (int) snapclamp(origClip.x + loc.x, 0, x2 - 1);
        break;
      }
      spriteInfo.setCropRect(new IRect(x1, y1, x2 - x1, y2 - y1));
    }

    @Override
    public void mouseUp() {
      spritePanel.setHighlightClip(false);
      clearOperation();
    }

    // clip rectangle at start of operation
    private IRect origClip;
  };

  private static RecentFiles recentProjects = new RecentFiles(null);
  private static JMenuItem recentProjectsMenuItem;
  private static JCheckBox cpt = new JCheckBox();
  private static JCheckBox showClip = new JCheckBox();

}
