package com.js.spredit;

import images.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.*;

import com.js.geometry.IPoint;
import com.js.geometry.IRect;
import com.js.geometry.Point;
import com.js.geometry.Rect;

import static com.js.basic.Tools.*;

public class FontExtractor {

  public void setHorizontalSpacing(int n) {
    charXSep = n;
  }

  public void setCropping(boolean f) {
    fontCropping = f;
  }

  public FontExtractor(Font f) {
    imageSize = new IPoint(1000, 800);
    workImg = new BufferedImage(imageSize.x, imageSize.y,
        BufferedImage.TYPE_INT_ARGB);
    g = (Graphics2D) workImg.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setFont(f);
    g.setColor(Color.white);
    metrics = g.getFontMetrics();

    fontAscent = metrics.getAscent();
    fontDescent = metrics.getDescent();
    fontLeading = metrics.getLeading();
    charHeight = fontAscent + fontDescent;

    cursor = new IPoint(0, imageSize.y);

    // there's no harm in having a large horizontal spacing, since we crop the
    // letters to remove any slack.
    setHorizontalSpacing(8);
    setCropping(true);

    // warn("turning off cropping");setCropping(false);
  }

  /**
   * Render a character into the image
   * 
   * @param c
   *          character to render
   */
  public void render(char c) {
    if (advanceRequired)
      advanceCursor();

    int charWidth = metrics.charWidth(c) + charXSep;

    g.drawString(Character.toString(c), cursor.x, cursor.y - fontDescent);
    IRect subRect = new IRect(cursor.x, cursor.y - charHeight, charWidth,
        charHeight);

    cimg = workImg.getSubimage(subRect.x, subRect.y, subRect.width,
        subRect.height);

    // crop vertically (we need width for spacing)
    IRect cropped = ImgUtil.calcUsedBounds(cimg, 0);

    int cropTop = 0, cropBottom = 0;
    warning("clean up font stuff at some point");

    // if (true) warn("not cropping");else
    if (fontCropping) {
      cropTop = cropped.y;
      cropBottom = cimg.getHeight() - (int) cropped.endY();

      // if cropped image is empty, pretend its baseline has some pixels
      if (cropped.height == 0) {
        cropTop = fontAscent;
        cropBottom = fontDescent - 1;
      }
    }

    clip = new Rect(0, cropBottom, cimg.getWidth(), cimg.getHeight() - cropTop
        - cropBottom);
    cp = new Point(0, fontDescent);

    advanceRequired = true;

  }

  private Rect clip;
  private Point cp;
  private BufferedImage cimg;

  public Point getCP() {
    return cp;
  }

  public Rect getClip() {
    return clip;
  }

  public BufferedImage getImage() {
    return cimg;
  }

  private void advanceCursor() {
    advanceRequired = false;
    cursor.x += clip.width + charXSep / 2;
    if (cursor.x + metrics.getMaxAdvance() + charXSep > imageSize.x) {
      cursor.x = 0;
      cursor.y -= charHeight;
      if (cursor.y - charHeight < 0)
        throw new IllegalStateException("image buffer is full!");
    }
    cimg = null;
    cp = null;
    clip = null;
  }

  public int[] getFontInfo() {
    int[] fd = new int[3];
    fd[0] = fontAscent;
    fd[1] = fontDescent;
    fd[2] = fontLeading;
    return fd;
  }

  private boolean fontCropping;
  private FontMetrics metrics;

  private BufferedImage workImg;

  private IPoint imageSize;
  private Graphics2D g;
  private int fontAscent, fontDescent, fontLeading;

  private int charXSep;
  private int charHeight;
  private IPoint cursor;
  private boolean advanceRequired;

}
