package com.js.spredit;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import apputil.*;
import tex.*;
import static com.js.basic.Tools.*;

/*
   Features to add:
   
   have 'up' button to move to parent folder (if one exists)
   have 'back', 'fwd' buttons to move to prev, next folders
 */
public class ImgDirectory extends JPanel implements ListSelectionListener {

  /**
   * Constructor
   */
  public ImgDirectory() {
    super(new BorderLayout());

    entries = new TreeMap();

    listModel = new DefaultListModel();

    //Create the list and put it in a scroll pane.
    list = new JList(listModel);
    // list.setFocusable(false);
    list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setCellRenderer(new MyCellRenderer());
    list.setVisibleRowCount(-1);

    list.setSelectedIndex(0);
    list.addListSelectionListener(this);
    JScrollPane listScrollPane = new JScrollPane(list);

    add(listScrollPane, BorderLayout.CENTER);
  }

  private static class ProjDefaults {
    String lastSpriteName = "";
  };
  private ProjDefaults def = new ProjDefaults();

  public void setProject(TexProject p) {
    final boolean db = false;

    if (db)
      pr("ImgDirectory, setProject to " + p);

    this.project = p;
    def = new ProjDefaults();

    if (project != null) {
      DefScanner sc = new DefScanner(project.getDefaults("IMGDIR", null));
      if (!sc.done())
        def.lastSpriteName = sc.sLabel();
    }
    repopulateList();
  }

  public void flushTo(TexProject p) {
    DefBuilder sb = new DefBuilder();
    sb.appendLabel(def.lastSpriteName);
    p.storeDefaults("IMGDIR", sb);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(400, 400);
  }
  public void remove(SpriteInfo si) {
    entries.remove(si.id());
    listModel.removeElement(si);
  }

  public void add(SpriteInfo si) {

    SpriteInfo ent = entryFor(si.id());
    if (ent == null) {
      entries.put(si.id(), si);

      // insert entry into list
      int j = 0;
      for (; j < listModel.size(); j++) {
        SpriteInfo sj = (SpriteInfo) listModel.elementAt(j);
        if (String.CASE_INSENSITIVE_ORDER.compare(sj.id(), si.id()) >= 0) {
          break;
        }
      }

      listModel.add(j, si);
      select(si.id());
    }
  }

  /**
   * Repopulate list of sprites
   */
  private void repopulateList() {

    entries.clear();
    listModel.clear();

    if (project != null) {

      readEntries();
      Iterator it = entries.keySet().iterator();
      while (it.hasNext()) {
        String id = (String) it.next();
        SpriteInfo ent = entryFor(id);
        ASSERT(ent != null);
        listModel.addElement(ent);
      }
      //  
      //      File[] files = project.getImageFiles();
      //
      //      //  int selIndex = -1;
      //      for (int i = 0; i < files.length; i++) {
      //        File f = files[i];
      //        if (!project.isTexture(f)) {
      //          warn("shouldn't happen!");
      //          continue;
      //        }
      //        try {
      //          SpriteInfo si = new SpriteInfo(project, f);
      //
      //          listModel.addElement(si);
      //
      //        } catch (IOException e) {
      //          AppTools.showError("repopulating sprite list", e);
      //        }
      //      }
    }
  }

  //This method is required by ListSelectionListener.
  public void valueChanged(ListSelectionEvent e) {
    final boolean db = false;

    if (e.getValueIsAdjusting() == false) {
      SpriteInfo si = getSelectedSprite();
      if (db)
        pr("ImgDirectory.valueChanged, selected=" + si + "\n" + stackTrace(20));
      if (si != null) {
        def.lastSpriteName = si.id();
        SpriteEditor.setSprite(si);
      }
    }
  }

  private static final int PAD = 2, IMG = SpriteInfo.THUMB_SIZE, IMGLBL = 12;

  //Display an icon and a string for each object in the list.
  private class MyCellRenderer extends JPanel implements ListCellRenderer {

    public MyCellRenderer() {
      this.setFont(new Font("monospaced", Font.PLAIN, 14));
    }
    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.

    public Component getListCellRendererComponent(JList list, // the list
        Object value, // value to display
        int index, // cell index
        boolean isSelected, // is the cell selected
        boolean cellHasFocus) // does the cell have focus
    {
      this.setBackground(new Color(150, 150, 150));
      this.sp = (SpriteInfo) value;
      this.selected = isSelected;
      return this;
    }

    public void paintComponent(Graphics g) {
      final int SZ = SpriteInfo.THUMB_SIZE;

      super.paintComponent(g);

      if (selected) {
        g.setColor(Color.blue);
        for (int i = 0; i < PAD; i++)
          g.drawRect(i, i, IMG + 2 * PAD - i - 1, IMG + 2 * PAD - i - 1);
      }

      g.setColor(Color.black);
      String id = sp.id();
      if (sp.isAlias())
        id = "*"+id;
      
    //  id = id.substring(1 + id.lastIndexOf('_'));
      g.drawString(id, PAD, PAD + IMG + IMGLBL);

      BufferedImage img = sp.thumbnailImage();
      if (img != null) {
        int x = PAD + SZ / 2 - img.getWidth() / 2;
        int y = PAD + SZ / 2 - img.getHeight() / 2;

        g.drawImage(img, x, y, null);
      }

    }
    @Override
    public Dimension getPreferredSize() {
      return new Dimension(IMG + PAD * 2, IMG + PAD * 2 + IMGLBL);
    }
    private boolean selected;
    private SpriteInfo sp;
  }
  public SpriteInfo getSelectedSprite() {
    SpriteInfo si = null;
    int selInd = list.getSelectedIndex();
    if (selInd >= 0)
      si = (SpriteInfo) listModel.elementAt(selInd);
    return si;
  }
  private JList list;
  private DefaultListModel listModel;
  private TexProject project;

  //  private static class Entry {
  //    public Entry(String id) {
  //      this.id = id;
  //    }
  //    public SpriteInfo si;
  //    public String id;
  //  };

  private SpriteInfo entryFor(String id) {
    return (SpriteInfo) entries.get(id);
  }

  //  /**
  //   * Add entries for any image files we find in file tree
  //   * @param f root of file tree
  //   * @throws IOException
  //   */
  //  private void addEntries(File f) {
  //    if (!f.isDirectory()) {
  //      if (project.isTexture(f)) {
  //        String id = project.extractId(f);
  //        {
  //          SpriteInfo si;
  //          try {
  //            si = new SpriteInfo(project, f);
  //            // Entry ent = new Entry(id);
  //            // ent.si = si;
  //            entries.put(id, si);
  //          } catch (IOException e) {
  //            AppTools.showError("adding image entries", e);
  //          }
  //        }
  //      }
  //    } else {
  //      if (!TexProject.isMetaInfoFolder(f)) {
  //        File[] fs = f.listFiles();
  //        for (int i = 0; i < fs.length; i++)
  //          addEntries(fs[i]);
  //      }
  //    }
  //  }

  //  /**
  //   * Add entries for any sprites that are aliased to originals.
  //   * These will not have source image files, so won't have been detected
  //   * by the previous pass.
  //   * 
  //   * @param f root of file tree
  //   * @throws IOException
  //   */
  //  private void addAliasEntries(File f) {
  //    final boolean db = false;
  //
  //    if (db)
  //      pr("addAliasEntries " + f);
  //    do {
  //      if (!f.isDirectory()) {
  //        if (!SpriteInfo.META_FILES_ONLY.accept(f))
  //          break;
  //
  //        // is there already an entry with this id?
  //        String id = project.extractId(f);
  //        SpriteInfo ent = entryFor(id);
  //        if (db)
  //          pr(" entry for id=" + id + " is " + ent);
  //
  //        // if entry exists, already added in first pass;
  //        // not an aliased entry.
  //        if (ent != null)
  //          break;
  //
  //        // SpriteInfo si;
  //        try {
  //          // construct entry, read meta file
  //          ent = new SpriteInfo(project, f);
  //
  //          // if not an alias, something funny going on
  //          File aTag = ent.getAliasTag();
  //          if (aTag == null) {
  //            warn("expected entry to be an alias: " + f);
  //            break;
  //          }
  //
  //          // find aliased entry
  //          //  File aimg = ent.imagePath();
  //          String aid = project.extractId(aTag);
  //          SpriteInfo aent = entryFor(aid);
  //          if (aent == null) {
  //            warn("can't find original " + aTag + " for alias " + f);
  //            break;
  //          }
  //
  //          ent.setAliasSprite(aent);
  //          entries.put(ent.id(), ent);
  //        } catch (IOException e) {
  //          AppTools.showError("adding alias entries", e);
  //        }
  //      } else {
  //        if (f.getName().equals(TexProject.THUMB_DIR))
  //          return;
  //
  //        File[] fs = f.listFiles();
  //        for (int i = 0; i < fs.length; i++) {
  //          addAliasEntries(fs[i]);
  //        }
  //      }
  //    } while (false);
  //  }

  private void readEntries() {
    try {
      entries = SprUtils.readSprites(project);
    } catch (IOException e) {
      AppTools.showError("reading sprites", e);
    }
    //    
    //    addEntries(project.baseDirectory());
    //    addAliasEntries(project.baseDirectory());
  }
  private TreeMap entries;

  /**
   * Select an image
   * @param id image to select; if not found, chooses following one
   */
  public void select(String id) {
    final boolean db = false;

    if (db)
      pr("ImgDirectory.select " + id);

    String key = (String) entries.ceilingKey(id);
    if (db)
      pr(" ceiling key=" + key);

    if (key == null) {
      if (entries.size() > 0)
        key = (String) entries.lastKey();
    }
    if (key != null) {
      SpriteInfo si = entryFor(key);

      list.setSelectedValue(si, true);
      if (false) {
        if (db)
          pr(" entryFor " + key + " = " + si);

        int ind = listModel.indexOf(si);
        if (db)
          pr(" indexOf " + si + "= " + ind);

        list.setSelectedIndex(ind);
      }
    }
  }

}
