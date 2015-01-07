package com.js.scredit;

//import images.*;
import java.awt.*;
//import java.awt.event.*;
import java.awt.image.*;
//import java.io.*;
import java.util.*;
import javax.swing.*;

import org.json.JSONObject;
//import javax.swing.event.*;

import com.js.editor.Command;
import com.js.editor.MouseOper;
import com.js.geometry.*;

import tex.*;
import static com.js.basic.Tools.*;

public class PalettePanel extends JPanel {

  // print color values clicked on in main view?
  // private
  static final boolean SHOW_COLORS = false;

  private static final int MAIN = 0,
      FAV = 1,
      PANELS = 2, //

      MAIN_CELL_SIZE = 6, //
      PAD = 4, COLUMNS = Palette.COLOR_COLUMNS, MAIN_CELLS_WIDE = 24 * COLUMNS,
      MAIN_CELLS_TALL = Palette.BASE_COLOR_COUNT / COLUMNS, FAV_CELL_SIZE = 16,
      FAV_CELLS_WIDE = 26, FAV_CELLS_TALL = 1, MAIN_X = PAD, MAIN_Y = PAD,
      MAIN_WIDTH = MAIN_CELLS_WIDE * MAIN_CELL_SIZE,
      MAIN_HEIGHT = MAIN_CELLS_TALL * MAIN_CELL_SIZE, SEP_Y = 10,
      FAV_X = MAIN_X, FAV_Y = MAIN_Y + MAIN_HEIGHT + SEP_Y,
      FAV_WIDTH = FAV_CELLS_WIDE * FAV_CELL_SIZE, FAV_HEIGHT = FAV_CELLS_TALL
          * FAV_CELL_SIZE, TOTAL_HEIGHT = FAV_Y + FAV_HEIGHT + 2 * PAD,
      TOTAL_WIDTH = MAIN_WIDTH + 2 * PAD;

  private static final IPoint nCells[] = {
      new IPoint(MAIN_CELLS_WIDE, MAIN_CELLS_TALL),
      new IPoint(FAV_CELLS_WIDE, FAV_CELLS_TALL) };
  private static final int cellSizes[] = { MAIN_CELL_SIZE, FAV_CELL_SIZE };
  private static final IRect panelBounds[] = {
      new IRect(MAIN_X, MAIN_Y, MAIN_WIDTH, MAIN_HEIGHT),
      new IRect(FAV_X, FAV_Y, FAV_WIDTH, FAV_HEIGHT) };

  // private
  static void print(int index, Color c) {
    pr(index + ": " + dh(c.getRed(), "2") + " " + dh(c.getGreen(), "2") + " "
        + dh(c.getBlue(), "2"));
  }

  // public PalettePanel() {
  // favColor(0);
  //
  // addMouseListener(new MouseListener() {
  //
  // private static final boolean db = false;
  //
  // @Override
  // public void mouseClicked(MouseEvent v) {
  // // TODO Auto-generated method stub
  //
  // }
  //
  // @Override
  // public void mouseEntered(MouseEvent v) {
  // // TODO Auto-generated method stub
  //
  // }
  //
  // @Override
  // public void mouseExited(MouseEvent v) {
  // // TODO Auto-generated method stub
  //
  // }
  //
  // @Override
  // public void mousePressed(MouseEvent v) {
  // IPoint pt = new IPoint(v.getX(), v.getY());
  // int index = mainPixelToIndex(pt);
  // if (index >= 0) {
  // if (SHOW_COLORS) {
  // warning("displaying color index");
  // Color c = Palette.get(index);
  // print(index, c);
  // }
  //
  // if (db)
  // pr("mousePressed,  pt=" + pt + " index=" + index);
  // setState(S_DRAGGING);
  // mainDownIndex = index;
  // return;
  // }
  // index = favPixelToIndex(pt);
  // if (index >= 0) {
  // favDownIndex = index;
  // setState(v.getButton() == MouseEvent.BUTTON1 ? S_FAVDRAG : S_FAVDEL);
  // return;
  // }
  //
  // }
  //
  // @Override
  // public void mouseReleased(MouseEvent v) {
  // IPoint pt = new IPoint(v.getX(), v.getY());
  // switch (state) {
  // case S_DRAGGING: {
  // int upIndex = mainPixelToIndex(pt);
  // if (db)
  // pr("mouseReleased, mainIndex=" + upIndex);
  //
  // if (upIndex >= 0) {
  // setSelectedColor(upIndex, true);
  // } else {
  // upIndex = favPixelToIndex(pt);
  // if (upIndex >= 0) {
  // setFavorite(upIndex, mainDownIndex, true);
  //
  // }
  // }
  // setState(S_NONE);
  // }
  // break;
  // case S_FAVDRAG: {
  // int index = favPixelToIndex(pt);
  // setState(S_NONE);
  // if (index != favDownIndex) {
  // if (favDownIndex == NULL_INDEX || index == NULL_INDEX)
  // break;
  //
  // int c1 = favColor(favDownIndex);
  // int c2 = favColor(index);
  // if (c1 != c2) {
  // setFavorite(favDownIndex, c2, false);
  // setFavorite(index, c1, true);
  // }
  // } else {
  // if (index == NULL_INDEX) {
  // setSelectedColor(-1, true);
  // break;
  // }
  //
  // int ci = favColor(index);
  // if (ci >= 0)
  // setSelectedColor(ci, true);
  // else {
  // // store selected color in this slot
  // setFavorite(index, selectedColor, true);
  // }
  // }
  // }
  // break;
  // case S_FAVDEL: {
  // int index = favPixelToIndex(pt);
  // if (index == favDownIndex) {
  // setFavorite(index, -1, true);
  // }
  // setState(S_NONE);
  // }
  // break;
  // }
  // }
  // });
  // }
  // private
  static final int NULL_INDEX = 0; // index of 'null' color within
                                   // favorites list

  /**
   * Change selected color, and color all selected objects
   * 
   * @param c
   *          new color, or null to use current
   */
  public void setSelectedColor(Color c, boolean colorSelectedObjects) {

    final boolean db = false;

    selectedColor = c;

    if (colorSelectedObjects) {
      if (db)
        pr(" current MouseOper=" + MouseOper.getOperation());

      {
        Command r = new ColorRev(c);
        if (db)
          pr(" constructed ColorRev, valid=" + r.shouldBeEnabled());

        if (r.shouldBeEnabled()) {
          if (db)
            pr("  performing it");

          ScriptEditor.editor().registerPush(r);
          ScriptEditor.perform(r);
        }
      }
    }
    repaint();
  }

  public Command getColorReversible(Color newColor) {
    return new ColorRev(newColor);
  }

  private class ColorRev extends ModifyObjectsReversible {
    // private static final boolean db = false;
    private Color newColor;

    public ColorRev(Color color) {

      // super(ScriptEditor.items().getSelected());

      this.newColor = color;
      setName("Color");

      // // construct colored versions of objects;
      // // we will actually store them later, by perform()
      //
      // EdObject[] m = getOrigObjects();
      // EdObject[] a = new EdObject[m.length];
      // if (db)
      // pr("ColorRev constructed, " + m.length + " origObjects");
      //
      // for (int i = 0; i < m.length; i++) {
      // EdObject obj = m[i];
      //
      // if (db)
      // pr(" applying color " + color + " to object " + obj);
      //
      // a[i] = obj.applyColor(color);
      // if (db)
      // pr("  produced " + a[i] + " (diff=" + (a[i] != obj) + ")");
      //
      // if (a[i] != obj) {
      // modCount++;
      // modObj = obj;
      // if (db)
      // pr("  item " + i + " has new color");
      //
      // }
      // }
      // modObjects = a;
    }

    // @Override
    // public String toString() {
    // StringBuilder sb = new StringBuilder();
    // sb.append("Color ");
    // if (modCount == 1) {
    // sb.append(modObj);
    // } else {
    // sb.append(modCount);
    // sb.append(" objects");
    // }
    // return sb.toString();
    // }

    @Override
    public void perform() {
      super.perform();
      setSelectedColor(newColor, false);
    }

    @Override
    public EdObject perform(EdObject orig) {
      return orig.applyColor(newColor);
      // ObjArray a = ScriptEditor.items();
      // for (int i = 0; i < this.nSlots(); i++) {
      // int slot = this.slot(i);
      // a.set(slot, modObjects[i]);
      // }
      //
      // // this.updateSelectedObjects(modObjects);
      // // EdObject[] m = this.getModifiedObjects();
      // // if (m == null)
      // // setModifiedObjects(modObjects);
      //
      // setSelectedColor(newColor, false);
      // super.perform();
    }
    // @Override
    // public boolean valid() {
    // return modCount > 0;
    // }
    // private EdObject modObj;
    // private EdObject[] modObjects;
    // private int modCount;
  }

  // private
  void setFavorite(int slot, int colorIndex, boolean refresh) {
    final boolean db = false;
    if (db)
      pr("setFavorite slot " + slot + " to " + colorIndex);
    // // read to ensure it's been initialized
    // favColor(0);

    if (favColor(slot) == colorIndex)
      return;

    // Integer val = (Integer)favColorMap.get(key);
    // if (val == null) val = new Integer(-1);
    //
    //
    // if (db)
    // pr(" existing value="+val);
    //
    // if (colorIndex == val.intValue() ) {
    // if (db)
    // pr(" returning");
    // return;
    // }

    Object key = new Integer(colorIndex);

    favColorMap.remove(key);
    if (colorIndex >= 0) {
      if (db)
        pr(" storing slot " + slot + " in color map");
      favColorMap.put(key, new Integer(slot));
    }

    if (db)
      pr(" setting color[" + slot + "] to " + colorIndex);

    favColors[slot] = colorIndex;

    // force rebuild of favorites image
    images[FAV] = null;

    if (refresh)
      repaint();
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT);
  }

  public Color getSelectedColor() {
    return selectedColor;
  }

  private static int pixelToIndex(int panel, IPoint loc) {
    final boolean db = false;

    if (db)
      pr("pixelToIndex loc=" + loc);

    int ret = -1;

    int x = loc.x - panelBounds[panel].x;
    int y = loc.y - panelBounds[panel].y;

    if (db)
      pr("relative to panel=" + new IPoint(x, y));

    if (panel == MAIN) {
      // int x = (loc.x - panelBounds[panel].x) / cellSizes[panel];
      // int y = (loc.y - panelBounds[panel].y) / cellSizes[panel];

      // if (x >= 0 && x < nCells[panel].x && y >= 0 && y < nCells[panel].y)
      // {
      ret = Palette.cellToIndex(MAIN_CELL_SIZE, new IPoint(x, y));
      // ret = x + y * nCells[panel].x;
    } else {
      x /= cellSizes[panel];
      y /= cellSizes[panel];

      if (db)
        pr(" in cell coords=" + x + "," + y);

      if (x >= 0 && x < nCells[panel].x && y >= 0 && y < nCells[panel].y) {
        ret = x + y * nCells[panel].x;
      }
    }
    if (db)
      pr(" returning " + ret);

    return ret;
  }

  // private
  static int mainPixelToIndex(IPoint loc) {
    return pixelToIndex(MAIN, loc);
  }

  // private
  static int favPixelToIndex(IPoint loc) {
    return pixelToIndex(FAV, loc);
  }

  private static IRect calcPixel(int panel, int index, IRect r) {

    if (panel == MAIN) {
      r = Palette.indexToCell(index, MAIN_CELL_SIZE, r);
      r.translate(MAIN_X, MAIN_Y);
    } else {
      if (r == null)
        r = new IRect();

      int cx = index % nCells[panel].x, cy = index / nCells[panel].x;

      int cs = cellSizes[panel];

      r.x = cx * cs + panelBounds[panel].x;
      r.y = cy * cs + panelBounds[panel].y;
      r.width = cs;
      r.height = cs;
    }
    return r;
  }

  // private
  static IRect calcMainPixel(int index, IRect r) {
    return calcPixel(MAIN, index, r);
  }

  // private
  static IRect calcFavPixel(int index, IRect r) {
    return calcPixel(FAV, index, r);
  }

  @Override
  public void paintComponent(Graphics g1) {
    unimp("paintComponent");
    // super.paintComponent(g1);
    // Graphics2D g = (Graphics2D) g1;
    //
    // g.drawImage(getImage(MAIN), MAIN_X, MAIN_Y, null);
    //
    // g.drawImage(getImage(FAV), FAV_X, FAV_Y, null);
    //
    // if (selectedColor != null) {
    // IRect r = calcMainPixel(selectedColor, null);
    // hlPix(g, r);
    // }
    // int favPos = NULL_INDEX;
    // if (selectedColor >= 0)
    // favPos = findColorInFav(selectedColor);
    //
    // if (favPos >= 0) {
    // hlPix(g, calcFavPixel(favPos, null));
    // }
    //
  }

  // private
  static void hlPix(Graphics2D g, IRect r) {
    g.setColor(Color.BLACK);
    g.drawRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
    g.setColor(Color.WHITE);
    g.drawRect(r.x, r.y, r.width, r.height);
  }

  /**
   * Build image containing main palette
   * 
   * @return
   */
  // private
  BufferedImage getImage(int panel) {

    if (images[panel] == null) {

      if (panel == MAIN) {
        images[panel] = Palette.build(MAIN_CELL_SIZE);
      } else {

        BufferedImage img = new BufferedImage(panelBounds[panel].width,
            panelBounds[panel].height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = img.createGraphics();

        final int cw = nCells[panel].x;
        final int ch = nCells[panel].y;
        final int cs = cellSizes[panel];

        for (int i = 0; i < cw * ch; i++) {
          int x = (i % cw) * cs;
          int y = (i / cw) * cs;

          int j = favColor(i);
          if (j < 0 || i == 0)
            g.setColor(Color.WHITE);
          else {
            Color c = Palette.get(j);
            g.setColor(c);
          }
          g.fillRect(x, y, cs, cs);
          g.setColor(Color.GRAY);
          if (j < 0) {
            g.drawLine(x + cs / 2, y + cs / 2, x + cs / 2 + 1, y + cs / 2);
          }
          g.drawRect(x, y, cs, cs);

          if (i == 0) {
            g.setColor(Color.BLACK);
            g.drawLine(x, y + cs, x + cs, y);
          }

        }

        g.dispose();
        images[panel] = img;
      }
    }
    return images[panel];
  }

  private int favColor(int index) {
    if (favColors == null) {
      favColors = new int[FAV_CELLS_WIDE * FAV_CELLS_TALL];
      for (int i = 0; i < favColors.length; i++)
        favColors[i] = -1;
    }
    return favColors[index];
  }

  // private
  int findColorInFav(int colorIndex) {
    Object key = new Integer(colorIndex);

    Integer pos = (Integer) favColorMap.get(key);
    return (pos == null) ? -1 : pos.intValue();
  }

  public JSONObject encodeDefaults() {
    return new JSONObject();
    //
    // DefBuilder sb = new DefBuilder();
    //
    // sb.append(selectedColor);
    // sb.append(favColors.length);
    // for (int i = 0; i < favColors.length; i++)
    // sb.append(favColors[i]);
    //
    // return sb.toString();
  }

  public void decodeDefaults(JSONObject map) {
    if (map == null)
      return;
    // DefScanner sc = new DefScanner(str);
    // setSelectedColor(sc.sInt(), true);
    // int len = sc.sInt();
    // for (int i = 0; i < len; i++) {
    // int c = sc.sInt();
    // if (i < favColors.length)
    // setFavorite(i, c, false);
    // }
  }

  // private
  static final int S_NONE = 0, S_DRAGGING = 1, S_FAVDRAG = 2, S_FAVDEL = 3;

  // private
  void setState(int s) {
    state = s;
  }

  // public int favoriteSlot() {
  // return findColorInFav(selectedColor);
  // }

  public int[] getFavoriteColors() {
    return favColors;
  }

  // private
  int favDownIndex;
  // private
  int mainDownIndex;
  // private
  int state;
  private Map favColorMap = new HashMap();
  private BufferedImage images[] = new BufferedImage[PANELS];
  private Color selectedColor = Color.blue;
  private int[] favColors;
}
