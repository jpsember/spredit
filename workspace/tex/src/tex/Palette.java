package tex;

import images.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

import base.*;
import com.js.geometry.*;
import static com.js.basic.Tools.*;

/**
 * The colors are organized into sets, where each set is derived from a particular
 * base color.
 * 
 * The first 8 colors are darker versions of the base color, 
 * the next is the base color, the next 7 are lighter versions of the base color,
 * and the final 8 are progressively transparent versions of the base color.
 * 
 * A 'base color' will be the base color number, 0..BASE_COLOR_COUNT-1.
 * 
 * A 'color index' refers to a particular variant of a base color, and will have an index
 * in the range 0..B*24, where B is the total number of base colors (BASE_COLOR_COUNT).
 * 
 */
public class Palette {

  /**
   * Get color index, given base color
   * @param baseColor base color
   * @return
   */
  public static int indexOf(int baseColor) {
    return indexOf(baseColor, 8);
  }

  /**
   * Get color index for member of a base color's set
   * @param baseColor base color
   * @param variant 0...COLOR_SET_SIZE-1
   * @return color index
   */
  public static int indexOf(int baseColor, int variant) {
    ASSERT(baseColor >= 0 && baseColor < BASE_COLOR_COUNT && variant >= 0
        && variant < COLOR_SET_SIZE);
    return baseColor * COLOR_SET_SIZE + variant;
  }

  /**
   * Get color as Color object
   * @param colorIndex color index 
   * @return Color
   */
  public static Color get(int colorIndex) {
    colorIndex = MyMath.clamp(colorIndex, 0, nColors() - 1);
    return sc()[colorIndex];
  }

  /**
   * Get color as array of four floats 0..1
   * @param colorIndex color index 
   * @return array of four floats
   */

  public static float[] getf(int colorIndex) {
    sc();
    //    colorIndex = MyMath.clamp(colorIndex, 0, nColors() - 1);
    return glColors[colorIndex];
  }

  private static Color[] sc() {
    initStdColors();
    return StandardColors;
  }

  /**
   * Get total number of color indices
   * @return 1+maximum color index
   */
  public static int nColors() {
    return sc().length;
  }

 

  /**
   *  position of base color within color set
   */
  public static final int OFFSET_STDBRIGHTNESS = 8;

  /**
   *  position of start of transparent versions within color set
   */
  public static final int OFFSET_TRANSPARENT = 16;
  /**
   *  Number of transparent variants within color set
   */
  public static final int TRANSP_COUNT = 8;

  /**
   * Number of variants within color set
   */
  public static final int COLOR_SET_SIZE = 24;

  /**
   * Number of base colors
   */
  public static final int BASE_COLOR_COUNT = 66; 

 
  /**
   *  number of columns in our palette
   */
  public static final int COLOR_COLUMNS = 3;

  private static final int COLOR_ROWS = (BASE_COLOR_COUNT + COLOR_COLUMNS - 1)
      / COLOR_COLUMNS;

  private static final int COLORS_PER_ROW = COLOR_SET_SIZE * COLOR_COLUMNS;

  //  private static final boolean TESTC = true;
  //  private static final boolean HSB = true;
  //
  //  private static final int MASK(int nBits) {
  //    return (1 << nBits) - 1;
  //  }
  //
  //  private static float nlscale(float f) {
  //    //    float g = 1-f;
  //    //    return 1-g*g;
  //    // return f*f;
  //    return f;
  //  }

  private static Color[] defaultColors = {
      //      new Color(.2f, .2f, .2f), new Color(.5f, .5f, .5f),
      //      new Color(.8f, .8f, .8f),
      //Color.BLACK, Color.WHITE, 
      Color.WHITE, new Color(0xc0, 0xc0, 0xc0), new Color(0x80, 0x80, 0x80),
      new Color(0x40, 0x40, 0x40), Color.black,
      // some browns
      new Color(0x8e, 0x39, 0x00), new Color(0x55, 0x11, 0x00), };

  //  private static char hexDig(int c) {
  //    if (c >= 10)
  //      c += 'a' - 10;
  //    else
  //      c += '0';
  //    return (char) c;
  //  }

  //  private static String toHex(int n) {
  //    StringBuilder sb = new StringBuilder("0x");
  //
  //    sb.append(hexDig(n >> 4));
  //    sb.append(hexDig(n & 0xf));
  //
  //    //    int c = n >> 4;
  //    //    if (c >= 10)
  //    //      c += 'a' - 10;
  //    //    else
  //    //      c += '0';
  //    //    sb.append((char) c);
  //    return sb.toString();
  //  }
  private static final Color[] skipC = { new Color(0x33, 0xff, 0x00),
      new Color(0x00, 0xff, 0x33), new Color(0xff, 0x20, 0xa6), };

  private static void initStdColors() {
    if (StandardColors != null)
      return;

    Set skipMap = new HashSet();
    for (int i = 0; i < skipC.length; i++)
      skipMap.add(skipC[i]);

    DArray myc = new DArray();
    for (int i = 0;; i++) {
      // float r, g, b;
      int q = i;
      Color c = null;
      do {
        final int HSB_COLORS = 30;
        if (q < HSB_COLORS) {
          float hue = q / (float) HSB_COLORS;
          float sat = 1.0f;
          float bright = 1.0f;
          int rgb = Color.HSBtoRGB(hue, sat, bright);
          c = new Color(rgb);
          break;
        }
        q -= HSB_COLORS;

        if (q < defaultColors.length) {
          c = defaultColors[q];
          break;
        }
        q -= defaultColors.length;

      } while (false);
      if (c == null)
        break;
      if (skipMap.contains(c))
        continue;
      myc.add(c);
    }
    while (myc.size() < BASE_COLOR_COUNT)
      myc.add(Color.black);
    //    int baseCount = myc.size();
    //    ASSERT(baseCount <= BASE_COLOR_COUNT);

    StandardColors = new Color[BASE_COLOR_COUNT * COLOR_SET_SIZE];
    final int HS = OFFSET_STDBRIGHTNESS;

    int k = 0;
    for (int i = 0; i < BASE_COLOR_COUNT; i++) {
      //      if (i == BASE_COLOR_COUNT)
      //        break;

      Color c = (Color) myc.get(i);
      float r = c.getRed() / 255.0f;
      float g = c.getGreen() / 255.0f;
      float b = c.getBlue() / 255.0f;

      for (int shade = 0; shade < COLOR_SET_SIZE; shade++) {

        float rs = r, gs = g, bs = b;

        float scale = 1;
        float alpha = 1;
        if (shade < HS) {
          scale = (1 + shade) / (float) (1 + HS);
          rs = r * scale;
          gs = g * scale;
          bs = b * scale;
        } else if (shade > HS && shade < OFFSET_TRANSPARENT) {
          float s2 = (shade - HS) / (float) HS;
          rs = r + (1 - r) * s2;
          gs = g + (1 - g) * s2;
          bs = b + (1 - b) * s2;
        } else if (shade >= 16) {
          alpha = 1 - (shade - 16) / (float) TRANSP_COUNT;
        }

        rs = MyMath.clamp(rs, 0, 1);
        gs = MyMath.clamp(gs, 0, 1);
        bs = MyMath.clamp(bs, 0, 1);
        Color c2 = new Color(rs, gs, bs, alpha);

        StandardColors[k] = c2;
        k++;
      }
    }

    //    while (k < BASE_COLOR_COUNT * COLOR_SET_SIZE)
    //      StandardColors[k++] = Color.black;
    // int COLOR_TOTAL = colorTbl.length / 3;
    //
    //    if (TESTC)
    //      warn("trying new color scheme");
    //
    //    if (false) {
    //      final int SIZE = 500;
    //      Color cp = null;
    //      pr("{");
    //      for (int q = 0; q < SIZE; q++) {
    //        float hue = q / (float) SIZE;
    //        float sat = 1.0f; //set / (float)(SETS-1);
    //        float bright = 1.0f; //set / (float)(SETS-1);
    //        int rgb = Color.HSBtoRGB(hue, sat, bright);
    //        Color c = new Color(rgb);
    //        boolean skip = false;
    //        if (cp != null) {
    //          int rd = Math.abs(cp.getRed() - c.getRed())
    //              + Math.abs(cp.getBlue() - c.getBlue())
    //              + Math.abs(cp.getGreen() - c.getGreen());
    //          if (rd < 16)
    //            skip = true;
    //        }
    //        if (!skip) {
    //          pr(" new Color(" + toHex(c.getRed()) + "," + toHex(c.getGreen())
    //              + "," + toHex(c.getBlue()) + "),");
    //          cp = c;
    //        }
    //      }
    //    }
    //
    //    int k = 0;
    //    for (int i = 0; i < BASE_COLOR_COUNT; i++) {
    //      int j = i * 3;
    //      float r = colorTbl[j + 0] / 255.0f;
    //      float g = colorTbl[j + 1] / 255.0f;
    //      float b = colorTbl[j + 2] / 255.0f;
    //
    //      if (TESTC) {
    //        if (HSB) {
    //          int q = i;
    //          Color c = Color.black;
    //          do {
    //            //          final int NBASE = BASE_COLOR_COUNT/4;
    //            //          int i0 = i/4;
    //            //          int i1 = i % 4;
    //
    //            final int HSB_COLORS = 30;
    //
    //            if (q < HSB_COLORS) {
    //
    //              //          final int SETS = 1;
    //              //          final int COL_PER_SET = BASE_COLOR_COUNT/SETS;
    //              //          
    //              //          int set = MyMath.clamp(i / COL_PER_SET,0,SETS-1);
    //              //          int div =  i % COL_PER_SET ;
    //              //          
    //
    //              float hue = q / (float) HSB_COLORS;
    //              float sat = 1.0f; //set / (float)(SETS-1);
    //              float bright = 1.0f; //set / (float)(SETS-1);
    //
    //              int rgb = Color.HSBtoRGB(hue, sat, bright);
    //              c = new Color(rgb);
    //              break;
    //            }
    //            q -= HSB_COLORS;
    //
    //            if (q < defaultColors.length) {
    //              c = defaultColors[q];
    //              break;
    //            }
    //            q -= defaultColors.length;
    //
    //            //  c = Color.black;
    //          } while (false);
    //          r = c.getRed() / 255.0f;
    //          g = c.getGreen() / 255.0f;
    //          b = c.getBlue() / 255.0f;
    //
    //        } else {
    //          final int RBITS = 2, GBITS = 2, BBITS = 2;
    //          if (i >= 1 << (RBITS + GBITS + BBITS)) {
    //            r = 0;
    //            g = 0;
    //            b = 0;
    //          } else {
    //            int bc;
    //            int f = i;
    //
    //            bc = RBITS;
    //            float v1 = (f & MASK(bc)) / (float) ((1 << bc) - 1);
    //            f >>= bc;
    //
    //            bc = GBITS;
    //            float v2 = (f & MASK(bc)) / (float) ((1 << bc) - 1);
    //            f >>= bc;
    //
    //            bc = BBITS;
    //            float v3 = (f & MASK(bc)) / (float) ((1 << bc) - 1);
    //            f >>= bc;
    //
    //            // try scaling r,g,b nonlinearly 
    //            r = nlscale(v1);
    //            g = nlscale(v2);
    //            b = nlscale(v3);
    //
    //            // try sorting base values by proximity.
    //            unimp("generate list of base values, then sort by distance to group close ones together.");
    //            unimp("hand-tune removal of ones deemed too close together.");
    //
    //            //          
    //            //          g = (i & MASK(GBITS)) /(float)(1 << GBITS);
    //            //          i >>= GBITS;
    //            //          
    //            //          b = (i & MASK(BBITS)) /(float)(1 << BBITS);
    //            //          i >>= GBITS;
    //            //         
    //            //            g = ((i >> 2) & 0x03) / 4.0f;
    //            //          b = ((i >> 4) & 0x03) / 4.0f;
    //          }
    //        }
    //      }
    //
    //      for (int shade = 0; shade < COLOR_SET_SIZE; shade++) {
    //
    //        float rs = r, gs = g, bs = b;
    //
    //        float scale = 1;
    //        float alpha = 1;
    //        if (shade < HS) {
    //          scale = (1 + shade) / (float) (1 + HS);
    //          rs = r * scale;
    //          gs = g * scale;
    //          bs = b * scale;
    //        } else if (shade > HS && shade < OFFSET_TRANSPARENT) {
    //          float s2 = (shade - HS) / (float) HS;
    //          rs = r + (1 - r) * s2;
    //          gs = g + (1 - g) * s2;
    //          bs = b + (1 - b) * s2;
    //        } else if (shade >= 16) {
    //          alpha = 1 - (shade - 16) / (float) TRANSP_COUNT;
    //        }
    //
    //        rs = MyMath.clamp(rs, 0, 1);
    //        gs = MyMath.clamp(gs, 0, 1);
    //        bs = MyMath.clamp(bs, 0, 1);
    //        Color c = new Color(rs, gs, bs, alpha);
    //
    //        StandardColors[k] = c;
    //        k++;
    //      }
    //    }

    glColors = new float[StandardColors.length][];

    for (int i = 0; i < glColors.length; i++) {
      Color c = StandardColors[i];
      float[] f = new float[4];
      f[0] = c.getRed() / 255.0f;
      f[1] = c.getGreen() / 255.0f;
      f[2] = c.getBlue() / 255.0f;
      f[3] = c.getAlpha() / 255.0f;
      glColors[i] = f;
    }

    //  dumpCCode();
  }
  private static float[] glColors[];

  // public static void dumpCCode() {
  // initStdColors();
  // pr("const int COLOR_TOTAL = " + BASE_COLOR_COUNT + ";");
  //
  // pr("static byte colorTbl[] = {");
  // for (int i = 0; i < BASE_COLOR_COUNT; i++) {
  // Color c = get(indexOf(i));
  //
  // System.out.print("0x" + dh(c.getRed(), "2"));
  // System.out.print(',');
  // System.out.print("0x" + toHex(c.getGreen(), 2));
  // System.out.print(',');
  // System.out.print("0x" + toHex(c.getBlue(), 2));
  // System.out.print(", ");
  // if ((i + 1) % 16 == 0)
  // System.out.println();
  // }
  // pr("};");
  // }

  public static BufferedImage build(int pixelSize) {
    final boolean db = false;

    initStdColors();
    int nColors = StandardColors.length;
    int nRows = (nColors + COLORS_PER_ROW - 1) / COLORS_PER_ROW;

    BufferedImage img = new BufferedImage(COLORS_PER_ROW * pixelSize, nRows
        * pixelSize, BufferedImage.TYPE_INT_ARGB);

    if (db)
      pr("Palette.build, nColors=" + nColors + " nRows=" + nRows + " size="
          + ImgUtil.bounds(img));

    Graphics2D g = img.createGraphics();

    int ci = 0;
    Rect pt = new Rect();
    for (int i = 0; i < StandardColors.length; i++) {
      for (int j = 0; j < COLOR_SET_SIZE; j++) {

        if (ci == StandardColors.length)
          break;

        Color c = StandardColors[ci];
        g.setColor(c);

        indexToCell(ci, pixelSize, pt);
        g.fillRect(pt.ix(), pt.iy(), pt.iWidth(), pt.iHeight());

        ci++;
      }
    }
    g.dispose();
    return img;
  }

  /**
   * Calculate the bounding rectangle of a palette pixel, given its color index
   * @param colorIndex
   * @param pixelSize size of pixels
   * @param dest where to store bounding rectangle; if null, constructs one
   * @return bounding rectangle
   */
  public static Rect indexToCell(int colorIndex, int pixelSize, Rect dest) {

    final boolean db = false;

    if (dest == null)
      dest = new Rect();

    // calculate x,y as if they were stored in one long column
    int y = colorIndex / COLOR_SET_SIZE;
    int x = colorIndex % COLOR_SET_SIZE;

    // now figure out actual column
    int column = y / COLOR_ROWS;

    dest.x = pixelSize * (column * COLOR_SET_SIZE + x);
    dest.y = pixelSize * (y % COLOR_ROWS);
    dest.width = pixelSize;
    dest.height = pixelSize;
    if (db)
      pr("indexToCell " + colorIndex + " (sz=" + pixelSize + ") ret " + dest);

    return dest;
  }

  public static int cellToIndex(int pixelSize, IPoint cell) {
    final boolean db = false;

    int index = -1;
    do {
      if (cell.x < 0 || cell.y < 0)
        break;
      int x = cell.x / pixelSize;
      int y = cell.y / pixelSize;
      if (y >= COLOR_ROWS)
        break;
      if (x >= COLOR_COLUMNS * COLOR_SET_SIZE)
        break;

      int column = x / COLOR_SET_SIZE;

      index = (y + column * COLOR_ROWS) * COLOR_SET_SIZE + (x % COLOR_SET_SIZE);
      if (db)
        pr("cellToIndex " + cell + " (ps=" + pixelSize + ")= " + index);

    } while (false);
    return index;
  }

  private static Color[] StandardColors;

  /**
   * Construct translucent version of color
   * @param colorIndex color index; we will extract the base version from it
   * @param opacity level of opacity: 1=opaque, 0=full transparent
   * @return color index
   */
  public static int translucent(int colorIndex, float opacity) {
    int base = colorIndex - (colorIndex % COLOR_SET_SIZE);
    int trans = (int) (TRANSP_COUNT * (1 - opacity));
    trans = MyMath.clamp(trans, 0, TRANSP_COUNT);

    int ret;
    if (trans == TRANSP_COUNT)
      ret = base + OFFSET_STDBRIGHTNESS;
    else
      ret = base + OFFSET_TRANSPARENT + trans;
    return ret;
  }

}
