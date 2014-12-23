package com.js.spredit;

import static com.js.basic.Tools.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.swing.JCheckBox;

import myjogl.GLPanel;
import myopengl.BufferUtils;
import myopengl.GLTools;
import myopengl.TextureLoader;
import tex.Atlas;
import tex.Palette;
import tex.Sprite;
import apputil.AppTools;
import apputil.IEditorView;
import apputil.MouseOper;
import apputil.MyMenuBar;

import com.js.geometry.*;

public class SpritePanel extends GLPanel implements IEditorView {

  @Override
  public Point viewToWorld(IPoint viewPt) {
    Point out = mViewToWorldMatrix.apply(viewPt.x, viewPt.y, null);
    return out;
  }

  public SpritePanel() {
    getComponent().setBackground(Color.white.darker());
    MouseOper.setView(this);
    if (!AppTools.isMac())
      MyMenuBar.addRepaintComponent(this.getComponent());
  }

  private void prepareViewport() {
    IPoint size = getSize();
    gl2.glViewport(0, 0, size.x, size.y);
  }

  private void prepareProjection() {

    { // Set texture matrix so (0,0) is in lower left of image
      gl2.glMatrixMode(GL2.GL_TEXTURE);
      gl2.glLoadIdentity();
      gl2.glTranslatef(0, -1, 0);
      gl2.glScalef(1, -1, 1);
    }

    IPoint size = getSize();
    float zoom = zoomFactor();
    Point focus = getFocus();

    Matrix projectionMatrix = new Matrix();
    {
      projectionMatrix.a = (2 * zoom) / size.x;
      projectionMatrix.d = (2 * zoom) / size.y;
      GLTools.storeMatrix(gl2, GL2.GL_PROJECTION, projectionMatrix);
    }

    Matrix modelViewMatrix = new Matrix();
    {
      modelViewMatrix.tx = -focus.x;
      modelViewMatrix.ty = -focus.y;
      GLTools.storeMatrix(gl2, GL2.GL_MODELVIEW, modelViewMatrix);
    }

    Matrix worldToNDCMatrix = new Matrix();
    Matrix.multiply(projectionMatrix, modelViewMatrix, worldToNDCMatrix);
    {
      // TODO: clarify the different coordinate spaces:
      // world, view, model, NDC; possibly eliminate requirement to calculate
      // inverse

      Matrix NDCToWorldMatrix = worldToNDCMatrix.invert(null);

      // construct matrix to convert from window coordinates to
      // normalized device coordinates (UIView to NDC).

      /*
       * This converts UIView coordinates to NDC as follows:
       * 
       * NDCx = (Vx - Cx) * 2 / W NDCy = (Vy - Cy) * 2 / H NDCz = -1 NDCw = 1
       * 
       * where
       * 
       * Vx,Vy = UIView coordinates Cx,Cy = Trans.viewCenter_, the center of the
       * OpenGL view (in UIView coordinates) W,H = size of OpenGL view
       */

      IPoint viewCenter_ = new IPoint(size.x / 2, size.y / 2);

      Matrix c = new Matrix();
      c.a = 2f / size.x;
      c.tx = (-2f * viewCenter_.x) / size.x;

      c.d = -2f / size.y;
      c.ty = (2f * viewCenter_.y) / size.y;

      Matrix.multiply(NDCToWorldMatrix, c, mViewToWorldMatrix);
    }
    gl2.glMatrixMode(GL2.GL_MODELVIEW);
  }

  @Override
  public void render() {

    do {
      if (sFocusValid)
        break;
      sFocus = new Point();
      sFocusValid = true;
      if (spriteInfo == null)
        break;
      if (spriteInfo.workImage() == null)
        break;
      sFocus.setTo(spriteInfo.workImageSize().x / 2,
          spriteInfo.workImageSize().y / 2);
    } while (false);
    setFocus(sFocus);

    paintStart();
    prepareProjection();
    paintContents();
    paintEnd();
  }

  private void paintContents() {

    if (spriteInfo == null)
      return;

    BufferedImage image = spriteInfo.workImage();
    if (image == null)
      return;

    spriteInfo.plotTexture(spriteInfo.centerPoint(), this);

    if (mShowClip.isSelected()) {
      setRenderColor(hlClip ? RED : BLUE);
      lineWidth(10f / zoomFactor());
      drawFrame(spriteInfo.cropRect());
    }

    if (mCpt.isSelected()) {
      Point t0 = spriteInfo.centerPoint();

      gl2.glPushMatrix();
      gl2.glTranslatef(t0.x, t0.y, 0);

      lineWidth(3.2f / zoomFactor());
      setRenderColor(704);

      float W = 20 / zoomFactor();

      drawLine(-W, 0, W, 0);
      drawLine(0, -W, 0, W);
      lineWidth(1.2f / zoomFactor());
      setRenderColor(hlCP ? YELLOW : BLACK);

      drawLine(-W, 0, W, 0);
      drawLine(0, -W, 0, W);
      gl2.glPopMatrix();
    }

  }

  private void lineWidth(float width) {
    lineWidth = width;
  }

  /*
   * Render states; used to avoid making unnecessary OGL state calls
   */
  public static final int RENDER_UNDEFINED = 0, RENDER_RGB = 1,
      RENDER_SPRITE = 2, RENDER_TOTAL = 3;

  public void setRenderState(int state) {
    ASSERT(state > RENDER_UNDEFINED && state < RENDER_TOTAL);

    boolean changed = renderState != state;
    if (changed) {

      renderState = state;

      switch (renderState) {

      case RENDER_SPRITE:
        mytexturesOn();
        gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        break;

      case RENDER_RGB:
        mytexturesOff();
        gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        break;
      }
    }
  }

  public void setRenderColor(int colorIndex) {
    if (currentColorId != colorIndex) {
      currentColorId = colorIndex;

      Color c = Palette.get(colorIndex);
      gl2.glColor4ub((byte) c.getRed(), (byte) c.getGreen(),
          (byte) c.getBlue(), (byte) c.getAlpha());
    }
  }

  public void mytexturesOn() {
    if (!textureMode) {
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      textureMode = true;
    }
  }

  public void mytexturesOff() {
    if (textureMode) {
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      textureMode = false;
    }
  }

  public void drawLine(Point pt1, Point pt2) {
    drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
  }

  public void drawRect(Rect r) {
    drawRect(r.x, r.y, r.width, r.height);
  }

  public void drawRect(float x, float y, float w, float h) {
    drawLine(x, y, x + w, y);
    drawLine(x, y + h, x + w, y + h);
    drawLine(x, y, x, y + h);
    drawLine(x + w, y, x + w, y + h);
  }

  /**
   * Draw frame around a rectangle, adjusting for line width
   * 
   * @param x
   * @param y
   * @param w
   * @param h
   */
  public void drawFrame(float x, float y, float w, float h) {
    float halfLineWidth = lineWidth * .5f;
    drawRect(x - halfLineWidth, y - halfLineWidth, w + lineWidth, h + lineWidth);
  }

  public void drawFrame(Rect r) {
    drawFrame(r.x, r.y, r.width, r.height);
  }

  public void drawCircle(Point origin, float radius) {
    final boolean db = false;

    int nPts = (int) (radius * zoomFactor() / 2);
    if (db)
      pr("before clamping, radius=" + radius + " zoom=" + zoomFactor()
          + " nPts=" + nPts);
    nPts = MyMath.clamp(nPts, 6, 50);

    Point prev = null;
    float angle = 0;
    for (int i = 0; i <= nPts; i++) {
      Point curr = MyMath.pointOnCircle(origin, angle, radius);
      if (prev != null)
        drawLine(prev, curr);
      prev = curr;
      angle += (2 * MyMath.PI) / nPts;
    }
  }

  public void drawCircle(float x, float y, float radius) {
    drawCircle(new Point(x, y), radius);
  }

  public void fillRect(Rect r) {
    fillRect(r.x, r.y, r.width, r.height);
  }

  public void fillRect(float x, float y, float w, float h) {
    setRenderState(RENDER_RGB);
    FloatBuffer rectVertBuffer = BufferUtils.createFloatBuffer(4 * 2);

    FloatBuffer v = rectVertBuffer;
    v.rewind();
    v.put(x);
    v.put(y);
    v.put(x + w);
    v.put(y);
    v.put(x + w);
    v.put(y + h);
    v.put(x);
    v.put(y + h);
    v.rewind();
    gl2.glVertexPointer(2, GL2.GL_FLOAT, 0, v); // only 2 coords per vertex
    gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
  }

  private int trisBufferCap;

  public void fillTriangles(Point[] tris) {
    FloatBuffer trisVertBuffer = null;

    setRenderState(RENDER_RGB);

    if (trisVertBuffer == null || trisBufferCap < tris.length) {
      trisVertBuffer = BufferUtils.createFloatBuffer(2 * tris.length);
      trisBufferCap = tris.length;
    }

    FloatBuffer v = trisVertBuffer;
    v.rewind();
    for (int i = 0; i < tris.length; i++) {
      Point pt = tris[i];
      v.put(pt.x);
      v.put(pt.y);
    }
    v.rewind();
    gl2.glVertexPointer(2, GL2.GL_FLOAT, 0, v); // only 2 coords per vertex
    gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, tris.length);
  }

  public void drawLine(float x1, float y1, float x2, float y2) {
    setRenderState(RENDER_RGB);

    float s = lineWidth * .5f;

    // Calculate the unit vector rotated 90 degrees from ray p1->p2
    float dist = MyMath.distanceBetween(new Point(x1, y1), new Point(x2, y2));
    float nx = 0;
    float ny = s;
    if (dist > 0) {
      float r = s / dist;
      ny = (x2 - x1) * r;
      nx = -(y2 - y1) * r;
    }

    gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
    gl2.glVertex3f(x1 - nx, y1 - ny, 0);
    gl2.glVertex3f(x2 - nx, y2 - ny, 0);
    gl2.glVertex3f(x1 + nx, y1 + ny, 0);
    gl2.glVertex3f(x2 + nx, y2 + ny, 0);
    gl2.glEnd();
  }

  public static final int BLACK = 776;// Palette.indexOf(MyColors.BLACK);
  public static final int RED = 8;// Palette.indexOf(MyColors.RED);
  public static final int BLUE = 416;// Palette.indexOf(MyColors.BLUE);
  public static final int YELLOW = 129;// Palette.indexOf(MyColors.YELLOW);
  public static final int WHITE = 680;// Palette.indexOf(MyColors.WHITE);
  public static final int GREEN = 224;// Palette.indexOf(MyColors.GREEN);
  public static final int GRAY = 704;// Palette.indexOf(MyColors.GRAY);
  public static final int DARKGRAY = 728;// Palette.indexOf(MyColors.DARKGRAY);

  // public static final float[] BLACK = { 0, 0, 0 };
  // public static final float[] RED = { 1, 0, 0 };
  // public static final float[] BLUE = { 0, 0, 1 };
  // public static final float[] YELLOW = { 1, 1, 0 };
  // public static final float[] WHITE = { 1, 1, 1 };
  // public static final float[] GREEN = { 0, 1, 0 };

  private void paintStart() {
    // clear to background color
    Color c = Color.gray;

    float TOFLOAT = 1 / 255.0f;
    gl2.glClearColor(c.getRed() * TOFLOAT, c.getGreen() * TOFLOAT, c.getBlue()
        * TOFLOAT, 1);
    gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);

    if (sizeHasChanged()) {
      prepareViewport();
    }

    // set REPLACE mode, to ignore color
    gl2.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
    gl2.glShadeModel(GL2.GL_FLAT); // not sure this is necessary

    // textures are disabled
    gl2.glDisable(GL2.GL_TEXTURE_2D);

    gl2.glEnable(GL2.GL_BLEND);
    gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

    // turn off backspace culling
    gl2.glDisable(GL2.GL_CULL_FACE);

    activeTexture = 0;
    textureMode = false;
  }

  /**
   * Finish rendering frame
   */
  private void paintEnd() {
    // while in the GL context, delete any previously removed textures
    TextureLoader.processDeleteList();
  }

  private int textureFor(Atlas a) {
    final boolean db = false;

    if (atlasTextures == null)
      atlasTextures = new HashMap();
    Integer iv = (Integer) atlasTextures.get(a);
    if (iv == null) {
      if (db)
        pr("installing atlas texture: " + a);

      BufferedImage img = a.image();

      int texHandle;

      texHandle = TextureLoader.getTexture(img, null);

      iv = new Integer(texHandle);

      atlasTextures.put(a, iv);
      if (db)
        pr(" handle=" + iv);
    }
    return iv.intValue();
  }

  /**
   * Plot sprite
   * 
   * @param atlas
   *          atlas containing sprite
   * @param spriteIndex
   *          index within atlas
   * @param x
   *          position
   * @param y
   * @return sprite plotted
   */
  public Sprite plotSprite(Atlas atlas, int spriteIndex, float x, float y) {
    Matrix tfm = Matrix.getTranslate(new Point(x, y));
    return plotSprite(atlas, spriteIndex, tfm);
  }

  /**
   * Plot sprite
   * 
   * @param atlas
   *          atlas containing sprite
   * @param spriteIndex
   *          index within atlas
   * @param tfm
   *          transformation matrix to apply to vertices
   * @return sprite plotted
   */
  public Sprite plotSprite(Atlas atlas, int spriteIndex, Matrix tfm) {
    int texHandle = textureFor(atlas);
    Sprite sprite = atlas.sprite(spriteIndex);
    plotSprite(texHandle, atlas.imageSize(), sprite, tfm); // x, y);
    return sprite;
  }

  public void selectTexture(int texHandle) {
    if (activeTexture != texHandle) {
      activeTexture = texHandle;
      gl2.glBindTexture(GL2.GL_TEXTURE_2D, texHandle);
      err();
    }
  }

  /**
   * Plot sprite
   * 
   * @param texHandle
   *          openGL texture
   * @param textureSize
   *          size of texture, in pixels
   * @param sprite
   *          sprite
   * @param x
   *          location
   * @param y
   */
  public void plotSprite(int texHandle, IPoint textureSize, Sprite sprite,
      float x, float y) {

    Matrix tfm = Matrix.getTranslate(new Point(x, y));
    plotSprite(texHandle, textureSize, sprite, tfm);
  }

  /**
   * Plot sprite
   * 
   * @param texHandle
   *          openGL texture
   * @param textureSize
   *          size of texture, in pixels
   * @param sprite
   *          sprite
   * @param x
   *          location
   * @param y
   */
  public void plotSprite(int texHandle, IPoint textureSize, Sprite sprite,
      Matrix tfm) {

    setRenderState(RENDER_SPRITE);
    selectTexture(texHandle);

    err();

    Rect imgRect = new Rect(sprite.bounds());
    Rect aRect = new Rect(imgRect);

    aRect.scale(sprite.compressionFactor());

    Point tr = sprite.translate();
    aRect.translate(tr.x, tr.y);

    ASSERT(TextureLoader.ceilingPower2(textureSize.x) == textureSize.x
        && TextureLoader.ceilingPower2(textureSize.y) == textureSize.y);

    float sx = 1f / textureSize.x;
    float sy = 1f / textureSize.y;
    aRect.x *= sx;
    aRect.y *= sy;
    aRect.width *= sx;
    aRect.height *= sy;

    if (db)
      pr("texture rect= " + aRect);

    FloatBuffer v = BufferUtils.createFloatBuffer(2 * 4);
    FloatBuffer t = BufferUtils.createFloatBuffer(2 * 4);

    Point tp = new Point();
    tfm.apply(imgRect.x, imgRect.y, tp);
    v.put(tp.x);
    v.put(tp.y);

    tfm.apply(imgRect.endX(), imgRect.y, tp);
    v.put(tp.x);
    v.put(tp.y);

    tfm.apply(imgRect.endX(), imgRect.endY(), tp);
    v.put(tp.x);
    v.put(tp.y);

    tfm.apply(imgRect.x, imgRect.endY(), tp);
    v.put(tp.x);
    v.put(tp.y);

    v.rewind();

    t.put(aRect.x);
    t.put(aRect.y);
    t.put(aRect.endX());
    t.put(aRect.y);
    t.put(aRect.endX());
    t.put(aRect.endY());
    t.put(aRect.x);
    t.put(aRect.endY());

    t.rewind();

    gl2.glVertexPointer(2, GL2.GL_FLOAT, 0, v);
    err();
    gl2.glTexCoordPointer(2, GL2.GL_FLOAT, 0, t);
    err();

    gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
    err();

    err();
  }

  public void plotString(String str, Point pt) {
    plotString(str, (int) pt.x, (int) pt.y);
  }

  public void plotString(String str, float x, float y) {
    plotString(str, (int) x, (int) y);
  }

  public void plotString(String str, int x, int y) {

    if (currentFont == null)
      throw new IllegalStateException();

    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      int si = fontCharToSprite(c);
      Sprite spr = plotSprite(currentFont, si, x, y);
      x += spr.bounds().width;
    }
  }

  public void setFont(Atlas font) {
    if (currentFont != font) {
      currentFont = font;
    }
  }

  public void err() {
    int f = gl2.glGetError();
    if (f != 0) {
      pr("GL error: " + f + "   " + stackTrace());
    }
  }

  private static int fontCharToSprite(char c) {
    if (c < ' ' || c > 0x7f)
      c = '_';
    warning("figure out where first character is using better method");
    return c - ' ' + 1;
  }

  public void setSpriteInfo(SpriteInfo spriteInfo) {
    this.spriteInfo = spriteInfo;
  }

  public void invalidateFocus() {
    sFocusValid = false;
  }

  public void setHighlightClip(boolean f) {
    hlClip = f;
  }

  public void setHighlightCenterpoint(boolean f) {
    hlCP = f;
  }

  public void setCenterPointCheckBox(JCheckBox cpt) {
    mCpt = cpt;
  }

  public void setShowClipCheckBox(JCheckBox showClip) {
    mShowClip = showClip;
  }

  private Atlas currentFont;
  private int activeTexture; // id of last selected texture, or 0 if none
  private boolean textureMode; // true if GL_TEXTURE_2D enabled
  private boolean sFocusValid;
  private Point sFocus;
  private SpriteInfo spriteInfo;
  private JCheckBox mCpt;
  private JCheckBox mShowClip;
  private boolean hlClip, hlCP;
  private float lineWidth = 1;
  private Matrix mViewToWorldMatrix = new Matrix();
  private int renderState;
  private int currentColorId;
  private Map atlasTextures;
}
