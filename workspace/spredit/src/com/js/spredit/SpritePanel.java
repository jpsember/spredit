package com.js.spredit;

import static com.js.basic.Tools.ASSERT;
import static com.js.basic.Tools.d;
import static com.js.basic.Tools.pr;
import static com.js.basic.Tools.stackTrace;
import static com.js.basic.Tools.warning;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.swing.JCheckBox;

import myjogl.GLPanel;
import myopengl.BufferUtils;
import myopengl.FlMatrix44;
import myopengl.FlPoint4;
import myopengl.TextureLoader;
import tex.Atlas;
import tex.Palette;
import tex.Sprite;
import apputil.AppTools;
import apputil.IEditorView;
import apputil.MouseOper;
import apputil.MyMenuBar;

import com.js.geometry.IPoint;
import com.js.geometry.Matrix;
import com.js.geometry.MyMath;
import com.js.geometry.Point;
import com.js.geometry.Rect;

public class SpritePanel extends GLPanel implements IEditorView {

  @Override
  public Point viewToWorld(IPoint viewPt) {

    FlPoint4 in = new FlPoint4(viewPt.x, viewPt.y, 0);
    FlPoint4 out = new FlPoint4();

    cameraMatrixInverse_.apply(in, out);
    return new Point(out.x, out.y);
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

  // private void prepareProjection() {
  // gl2.glMatrixMode(GL2.GL_PROJECTION);
  // gl2.glLoadIdentity();
  //
  // // coordinate system origin at lower left with width and height same as
  // // the window
  // GLU glu = new GLU();
  // glu.gluOrtho2D(0.0f, size.x, 0.0f, size.y);
  //
  // gl2.glMatrixMode(GL2.GL_MODELVIEW);
  // gl2.glLoadIdentity();
  // }
  //

  private void prepareProjection() {
    final boolean db = true;

    IPoint currentSize = getSize();

    { // fix texture coordinate problem, so (0,0) is lower left of png
      gl2.glMatrixMode(GL2.GL_TEXTURE);
      gl2.glLoadIdentity();
      gl2.glTranslatef(0, -1, 0);
      gl2.glScalef(1, -1, 1);
    }

    gl2.glMatrixMode(GL2.GL_PROJECTION);
    gl2.glLoadIdentity();

    {
      float s = .5f / zoomFactor();
      gl2.glOrtho(-currentSize.x * s, currentSize.x * s, -currentSize.y * s,
          currentSize.y * s, -1, 1);
    }

    // now that projection has been set up,
    // switch to model/view matrix
    gl2.glMatrixMode(GL2.GL_MODELVIEW);
    gl2.glLoadIdentity();

    Point focus = getFocus();
    // translate so focus is at origin
    gl2.glTranslatef(-focus.x, -focus.y, 0);

    // store modelview matrix for retrieval by prepareModel()

    FloatBuffer params = BufferUtils.createFloatBuffer(16);

    params.position(0);

    gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, params);
    params.get(modelMatrix_.c);
    if (db)
      pr("view size " + currentSize);
    if (db)
      pr("focus " + focus);
    if (db)
      pr("zoom " + d(zoomFactor()));
    if (db)
      pr("modelMatrix: " + d(modelMatrix_.c));

    // initialize camera matrix and its inverse, for picking operations
    {
      // FlMatrix44 mat = new FlMatrix44();
      params.position(0);
      gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, params);
      float[] c = FlMatrix44.buildArray();

      params.get(c);
      if (db)
        pr(" projection_matrix=\n" + new FlMatrix44(c));

      cameraMatrix_ = new FlMatrix44();
      FlMatrix44.multiply(c, modelMatrix_.c, cameraMatrix_.c);
      if (db)
        pr(" cameraMatrix=\n" + cameraMatrix_);

      cameraMatrixInverse_ = new FlMatrix44();
      FlMatrix44.invert(cameraMatrix_.c, cameraMatrixInverse_.c);
      if (db)
        pr(" cameraMatrixInverse=\n" + cameraMatrixInverse_);

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

      IPoint viewCenter_ = new IPoint(currentSize.x / 2, currentSize.y / 2);

      c[0 + 4 * 0] = 2f / currentSize.x;
      c[0 + 4 * 1] = 0;
      c[0 + 4 * 2] = 0;
      c[0 + 4 * 3] = (-2f * viewCenter_.x) / currentSize.x;

      c[1 + 4 * 0] = 0;
      c[1 + 4 * 1] = -2f / currentSize.y;
      c[1 + 4 * 2] = 0;
      c[1 + 4 * 3] = (2f * viewCenter_.y) / currentSize.y;

      c[2 + 4 * 0] = 0;
      c[2 + 4 * 1] = 0;
      c[2 + 4 * 2] = 0;
      c[2 + 4 * 3] = -1;

      c[3 + 4 * 0] = 0;
      c[3 + 4 * 1] = 0;
      c[3 + 4 * 2] = 0;
      c[3 + 4 * 3] = 1;

      FlMatrix44.multiply(cameraMatrixInverse_.c, c, cameraMatrixInverse_.c);
    }

    // turn off backspace culling in parallel mode
    gl2.glDisable(GL2.GL_CULL_FACE);

  }

  @Override
  public void render() {

    // setZoom(SpriteEditor.zoomFactor);

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

    if (sizeHasChanged()) {
      prepareViewport();
    }
    prepareProjection();

    paintStart();
    paintContents();
    paintEnd();
  }

  private void paintContents() {

    if (spriteInfo == null)
      return;

    BufferedImage image = spriteInfo.workImage();
    if (image == null)
      return;

    spriteInfo.plotTexture(spriteInfo.centerPoint());

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
      // setColor(bgndColor());
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

  private FlMatrix44 cameraMatrix_;
  private FlMatrix44 cameraMatrixInverse_;
  // standard modelview matrix
  private FlMatrix44 modelMatrix_ = new FlMatrix44();

  private void lineWidth(float width) {
    lineWidth = width;
  }

  private float lineWidth = 1;

  /*
   * Render states. Used to avoid making unnecessary OGL state calls. Modelled
   * after the GameView.cpp class.
   */

  public static final int RENDER_UNDEFINED = 0, RENDER_RGB = 1,
      RENDER_SPRITE = 2, RENDER_TOTAL = 3;

  private int renderState;

  public void setRenderState(int state) {
    ASSERT(state > RENDER_UNDEFINED && state < RENDER_TOTAL);

    boolean changed = renderState != state;
    if (changed) {

      renderState = state;

      switch (renderState) {

      case RENDER_SPRITE: {
        mytexturesOn();
        gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
      }
        break;

      case RENDER_RGB:
        mytexturesOff();
        gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        break;
      }
    }
  }

  private boolean glInitialized;
  private int currentColorId;

  public void setRenderColor(int colorIndex) {
    if (currentColorId != colorIndex) {
      currentColorId = colorIndex;

      Color c = Palette.get(colorIndex);
      gl2.glColor4ub((byte) c.getRed(), (byte) c.getGreen(),
          (byte) c.getBlue(), (byte) c.getAlpha());
    }
  }

  /**
*/
  public void mytexturesOn() {
    if (!textureMode) {
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      textureMode = true;
    }
  }

  /**
*/
  public void mytexturesOff() {
    if (textureMode) {
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      textureMode = false;
    }
  }

  private FloatBuffer lineVertBuffer = BufferUtils.createFloatBuffer(6 * 3); // strip
                                                                             // of
                                                                             // 4
                                                                             // triangles

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
          + " nPts="
          + nPts);
    nPts = MyMath.clamp(nPts, 6, 50);

    // int nPts = 6;
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

  private FloatBuffer rectVertBuffer = BufferUtils.createFloatBuffer(4 * 2); // a
                                                                             // single
                                                                             // quad

  private FloatBuffer trisVertBuffer;

  public void fillRect(Rect r) {
    fillRect(r.x, r.y, r.width, r.height);
  }

  public void fillRect(float x, float y, float w, float h) {
    setRenderState(RENDER_RGB);

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

    // glEnableClientState(GL_VERTEX_ARRAY);
    gl2.glVertexPointer(2, GL2.GL_FLOAT, 0, v); // only 2 coords per vertex
    gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
    // glDisableClientState(GL_VERTEX_ARRAY);
  }

  private int trisBufferCap;

  public void fillTriangles(Point[] tris) {
    setRenderState(RENDER_RGB);
    // float x, float y, float w, float h) {
    // texturesOff();

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
    // glEnableClientState(GL_VERTEX_ARRAY);
    gl2.glVertexPointer(2, GL2.GL_FLOAT, 0, v); // only 2 coords per vertex
    gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, tris.length);
    // glDisableClientState(GL_VERTEX_ARRAY);
  }

  public void drawLine(float x1, float y1, float x2, float y2) {
    setRenderState(RENDER_RGB);
    // texturesOff();
    // float x1, y1, x2, y2;
    if (x1 > x2) {
      float tmp = x1;
      float tmp2 = y1;
      x1 = x2;
      y1 = y2;
      x2 = tmp;
      y2 = tmp2;
    }

    float s = lineWidth * .5f;

    if (lineVertBuffer == null)
      lineVertBuffer = BufferUtils.createFloatBuffer(6 * 2); // strip of 4
                                                             // triangles

    FloatBuffer v = lineVertBuffer;
    v.rewind();
    if (y2 >= y1) {

      // -----------------------
      v.put(x1 - s);
      v.put(y1 - s);

      v.put(x1 + s);
      v.put(y1 - s);

      v.put(x1 - s);
      v.put(y1 + s);

      // -----------------------
      v.put(x2 + s);
      v.put(y2 - s);

      v.put(x2 - s);
      v.put(y2 + s);

      v.put(x2 + s);
      v.put(y2 + s);
    } else {

      // -----------------------
      v.put(x1 - s);
      v.put(y1 + s);

      v.put(x1 - s);
      v.put(y1 - s);

      v.put(x1 + s);
      v.put(y1 + s);

      // -----------------------
      v.put(x2 - s);
      v.put(y2 - s);

      v.put(x2 + s);
      v.put(y2 + s);

      v.put(x2 + s);
      v.put(y2 - s);
    }

    // glEnableClientState(GL_VERTEX_ARRAY);
    v.rewind();

    gl2.glVertexPointer(2, GL2.GL_FLOAT, 0, v); // only 2 coords per vertex
    gl2.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 6); // 6 vertices total
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

    // initialize OGL state
    if (!glInitialized) {
      // set REPLACE mode, to ignore color
      gl2.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
      gl2.glShadeModel(GL2.GL_FLAT); // not sure this is necessary

      // textures are disabled
      gl2.glDisable(GL2.GL_TEXTURE_2D);

      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

      // glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
      // GL_NEAREST); //LINEAR);

      // initialize our state
      activeTexture = 0;
      textureMode = false;

      glInitialized = true;
    }

    // clear to background color
    // From AWTGLCanvas:
    // "Default pixel format is minimum 8 bits depth, and no alpha nor stencil."
    // Hence, alpha is not used here.
    Color c = Color.gray; // Palette.get(bgndColorIndex());
    // pr("clearing to bgnd color "+c+" (index="+bgndColorIndex()+")");

    final float TOFLOAT = 1 / 255.0f;

    gl2.glClearColor(c.getRed() * TOFLOAT, c.getGreen() * TOFLOAT, c.getBlue()
        * TOFLOAT, 1); // TOc.bgndColor[0], bgndColor[1],
                       // bgndColor[2], 1);
    gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);

    // prepareProjection();
  }

  /**
   * Finish rendering frame
   */
  private void paintEnd() {
    // while in the GL context, delete any previously removed textures
    TextureLoader.processDeleteList();
  }



  private static int textureFor(Atlas a) {
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

  private static Map atlasTextures;

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

  // public static final int COLORS = 0, TEXTURES = 1, BOTH = 2;

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

    // final boolean db = false; //sprite.id().equals("WOW");
    //
    // texturesOn();
    // selectTexture(texHandle);
    //
    // if (db)
    // pr("plotSprite texHandle#" + texHandle + ":" + sprite + " at "
    // + new IPoint2(x, y));
    //
    // err();
    // glPushMatrix();
    // glTranslatef(x, y, 0);
    //
    // IRect imgRect = new IRect(sprite.bounds());
    // FlRect aRect = new FlRect(imgRect);
    //
    // if (db)
    // pr("imgRect=" + imgRect + "\n  aRect=" + aRect + "\n compression="
    // + sprite.compressionFactor() + "\n sprite.trans="
    // + sprite.translate());
    //
    // aRect.scale(sprite.compressionFactor());
    // if (db)
    // pr(" after compression=" + aRect);
    //
    // IPoint2 tr = sprite.translate();
    // aRect.translate(tr.x, tr.y);
    // if (db)
    // pr(" after translation=" + aRect);
    //
    // ASSERT(TextureLoader.ceilingPower2(textureSize.x) == textureSize.x
    // && TextureLoader.ceilingPower2(textureSize.y) == textureSize.y);
    //
    // float sx = 1f / textureSize.x;
    // float sy = 1f / textureSize.y;
    // aRect.x *= sx;
    // aRect.y *= sy;
    // aRect.width *= sx;
    // aRect.height *= sy;
    //
    // if (db)
    // pr("texture rect= " + aRect);
    //
    // FloatBuffer v = BufferUtils.createFloatBuffer(2 * 4);
    // FloatBuffer t = BufferUtils.createFloatBuffer(2 * 4);
    //
    // v.put(imgRect.x);
    // v.put(imgRect.y);
    // v.put(imgRect.endX());
    // v.put(imgRect.y);
    // v.put(imgRect.endX());
    // v.put(imgRect.endY());
    // v.put(imgRect.x);
    // v.put(imgRect.endY());
    //
    // v.rewind();
    //
    // t.put(aRect.x);
    // t.put(aRect.y);
    // t.put(aRect.endX());
    // t.put(aRect.y);
    // t.put(aRect.endX());
    // t.put(aRect.endY());
    // t.put(aRect.x);
    // t.put(aRect.endY());
    //
    // t.rewind();
    //
    // glEnableClientState(GL_VERTEX_ARRAY);
    // glEnableClientState(GL_TEXTURE_COORD_ARRAY);
    //
    // glVertexPointer(2, 0, v);
    // err();
    // glTexCoordPointer(2, 0, t);
    // err();
    //
    // glDrawArrays(GL_QUADS, 0, 4);
    // err();
    //
    // glDisableClientState(GL_VERTEX_ARRAY);
    // glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    // err();
    //
    // glPopMatrix();
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

    final boolean db = false; // sprite.id().equals("WOW");

    setRenderState(RENDER_SPRITE);
    // texturesOn();
    selectTexture(texHandle);

    if (db)
      pr("plotSprite texHandle#" + texHandle + ":" + sprite);

    err();
    // glPushMatrix();
    // glTranslatef(x, y, 0);

    Rect imgRect = new Rect(sprite.bounds());
    Rect aRect = new Rect(imgRect);

    if (db)
      pr("imgRect=" + imgRect + "\n  aRect=" + aRect + "\n compression="
          + sprite.compressionFactor() + "\n sprite.trans="
          + sprite.translate());

    aRect.scale(sprite.compressionFactor());
    if (db)
      pr(" after compression=" + aRect);

    Point tr = sprite.translate();
    aRect.translate(tr.x, tr.y);
    if (db)
      pr(" after translation=" + aRect);

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

    // glEnableClientState(GL_VERTEX_ARRAY);
    // glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    gl2.glVertexPointer(2, GL2.GL_FLOAT, 0, v);
    err();
    gl2.glTexCoordPointer(2, GL2.GL_FLOAT, 0, t);
    err();

    gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
    err();

    // glDisableClientState(GL_VERTEX_ARRAY);
    // glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    err();

    // glPopMatrix();
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

    // setRenderState(RENDER_SPRITE);
    // texturesOn();

    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      int si = fontCharToSprite(c);
      // Sprite spr = currentFont.sprite(fontCharToSprite(c));
      // IRect r = fontCharRect(c);
      Sprite spr = plotSprite(currentFont, si, x, y);
      x += spr.bounds().width;
    }
  }

  public void setFont(Atlas font) {
    if (currentFont != font) {
      currentFont = font;
      // IRect r = fontCharRect('M');
      // fontCharSep = -Math.round(r.width * .1f);
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

  private static Atlas currentFont;

  // our OpenGL state
  private static int activeTexture; // id of last selected texture, or 0 if
  private static boolean textureMode; // true if GL_TEXTURE_2D enabled

  public void setSpriteInfo(SpriteInfo spriteInfo) {
    this.spriteInfo = spriteInfo;
  }

  public void invalidateFocus() {
    sFocusValid = false;
  }

  private boolean sFocusValid;
  private Point sFocus;
  private SpriteInfo spriteInfo;
  private JCheckBox mCpt;
  private JCheckBox mShowClip;
  private boolean hlClip, hlCP;

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

}
