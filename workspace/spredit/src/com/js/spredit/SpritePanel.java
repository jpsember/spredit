package com.js.spredit;

import static com.js.basic.Tools.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static javax.media.opengl.GL2.*;
import javax.swing.JCheckBox;

import com.js.myopengl.BufferUtils;
import com.js.myopengl.GLPanel;
import com.js.myopengl.TextureLoader;
import tex.Atlas;
import tex.Palette;
import tex.Sprite;
import apputil.AppTools;
import apputil.IEditorView;
import apputil.MouseOper;
import apputil.MyMenuBar;

import com.js.geometry.*;

public class SpritePanel extends GLPanel implements IEditorView {

  public SpritePanel() {
    getComponent().setBackground(Color.white.darker());
    MouseOper.setView(this);
    if (!AppTools.isMac())
      MyMenuBar.addRepaintComponent(this.getComponent());
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
      if (spriteInfo.getSourceImage() == null)
        break;
      sFocus.setTo(spriteInfo.workImageSize().x / 2,
          spriteInfo.workImageSize().y / 2);
    } while (false);

    IPoint size = getSize();
    float zoom = getZoom();

    // Calculate the origin from the focus and the view size
    // We want the (possibly zoomed) sprite pixel at sFocus to appear in the
    // center of the view.
    //
    setOrigin(new Point(sFocus.x - size.x / (2 * zoom), sFocus.y - size.y
        / (2 * zoom)));

    super.render();

    paintStart();
    paintContents();
  }

  private void paintContents() {

    if (spriteInfo == null)
      return;

    BufferedImage image = spriteInfo.getSourceImage();
    if (image == null)
      return;

    spriteInfo.plotTexture(this);

    if (mShowClip.isSelected()) {
      setRenderColor(hlClip ? RED : BLUE);
      lineWidth(10f / getZoom());
      drawFrame(spriteInfo.cropRect());
    }

    if (mCpt.isSelected()) {
      Point t0 = new Point(spriteInfo.centerpoint());

      gl.glPushMatrix();
      gl.glTranslatef(t0.x, t0.y, 0);

      lineWidth(3.2f / getZoom());
      setRenderColor(704);

      float W = 20 / getZoom();

      drawLine(-W, 0, W, 0);
      drawLine(0, -W, 0, W);
      lineWidth(1.2f / getZoom());
      setRenderColor(hlCP ? YELLOW : BLACK);

      drawLine(-W, 0, W, 0);
      drawLine(0, -W, 0, W);
      gl.glPopMatrix();
    }
  }

  private void lineWidth(float width) {
    lineWidth = width;
  }

  /*
   * Render states; used to avoid making unnecessary OGL state calls
   */
  private static final int RENDER_UNDEFINED = 0, RENDER_RGB = 1,
      RENDER_SPRITE = 2, RENDER_TOTAL = 3;

  private void setRenderState(int state) {
    ASSERT(state > RENDER_UNDEFINED && state < RENDER_TOTAL);

    boolean changed = renderState != state;
    if (changed) {

      renderState = state;

      switch (renderState) {

      case RENDER_SPRITE:
        mytexturesOn();
        gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL_VERTEX_ARRAY);
        break;

      case RENDER_RGB:
        mytexturesOff();
        gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL_VERTEX_ARRAY);
        break;
      }
    }
  }

  private void setRenderColor(int colorIndex) {
    if (currentColorId != colorIndex) {
      currentColorId = colorIndex;

      Color c = Palette.get(colorIndex);
      gl.glColor4ub((byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue(),
          (byte) c.getAlpha());
    }
  }

  private void mytexturesOn() {
    if (!textureMode) {
      gl.glEnable(GL_TEXTURE_2D);
      textureMode = true;
    }
  }

  private void mytexturesOff() {
    if (textureMode) {
      gl.glDisable(GL_TEXTURE_2D);
      textureMode = false;
    }
  }

  private void drawRect(float x, float y, float w, float h) {
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
  private void drawFrame(float x, float y, float w, float h) {
    float halfLineWidth = lineWidth * .5f;
    drawRect(x - halfLineWidth, y - halfLineWidth, w + lineWidth, h + lineWidth);
  }

  private void drawFrame(IRect r) {
    drawFrame(r.x, r.y, r.width, r.height);
  }

  private void drawLine(float x1, float y1, float x2, float y2) {
    setRenderState(RENDER_RGB);

    // We want to plot the Minkowski sum of the line segment with a unit square
    // rotated to be aligned with the line segment;
    // the square has width equal to the line width

    // Calculate the unit vector from p1 to p2; if undefined, assume line is
    // horizontal
    float dist = MyMath.distanceBetween(new Point(x1, y1), new Point(x2, y2));

    // The 'radius' of the unit square representing our pen
    float radius = lineWidth * .5f;

    float u = radius;
    float v = 0;
    if (dist > 0) {
      float r = radius / dist;
      u = (x2 - x1) * r;
      v = -(y2 - y1) * r;
    }

    // The top left corner of the (rotated) unit square has coordinates
    // (x,y) = (u-v, u+v), and each successive (ccw) corner is found
    // by applying the substitution (x',y') = (-y, x)

    gl.glBegin(GL_TRIANGLE_STRIP);
    // bottom left
    gl.glVertex2f(x1 - u + v, y1 - u - v);
    // bottom right
    gl.glVertex2f(x2 + u + v, y2 - u + v);
    // top left
    gl.glVertex2f(x1 - u - v, y1 + u - v);
    // top right
    gl.glVertex2f(x2 + u - v, y2 + u + v);

    gl.glEnd();
  }

  private static final int BLACK = 776;// Palette.indexOf(MyColors.BLACK);
  private static final int RED = 8;// Palette.indexOf(MyColors.RED);
  private static final int BLUE = 416;// Palette.indexOf(MyColors.BLUE);
  private static final int YELLOW = 129;// Palette.indexOf(MyColors.YELLOW);

  private void paintStart() {

    // set REPLACE mode, to ignore color
    gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
    gl.glShadeModel(GL_FLAT); // not sure this is necessary

    // textures are disabled
    gl.glDisable(GL_TEXTURE_2D);

    gl.glEnable(GL_BLEND);
    gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // turn off backspace culling
    gl.glDisable(GL_CULL_FACE);

    activeTexture = 0;
    textureMode = false;
  }

  private int textureFor(Atlas a) {
    if (atlasTextures == null)
      atlasTextures = new HashMap();
    Integer iv = (Integer) atlasTextures.get(a);
    if (iv == null) {
      BufferedImage img = a.image();

      int texHandle;

      texHandle = TextureLoader.getTexture(gl, img, null);

      iv = new Integer(texHandle);

      atlasTextures.put(a, iv);
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
      gl.glBindTexture(GL_TEXTURE_2D, texHandle);
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
      Point location) {
    Matrix tfm = Matrix.getTranslate(location);
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
  private void plotSprite(int texHandle, IPoint textureSize, Sprite sprite,
      Matrix tfm) {

    setRenderState(RENDER_SPRITE);
    selectTexture(texHandle);

    err();

    IRect imgRect = new IRect(sprite.bounds());
    Rect aRect = new Rect(imgRect);

    IPoint tr = sprite.translate();
    aRect.translate(tr.x, tr.y);

    ASSERT(TextureLoader.ceilingPower2(textureSize.x) == textureSize.x
        && TextureLoader.ceilingPower2(textureSize.y) == textureSize.y);

    float sx = 1f / textureSize.x;
    float sy = 1f / textureSize.y;
    aRect.x *= sx;
    aRect.y *= sy;
    aRect.width *= sx;
    aRect.height *= sy;

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

    gl.glVertexPointer(2, GL_FLOAT, 0, v);
    err();
    gl.glTexCoordPointer(2, GL_FLOAT, 0, t);
    err();

    gl.glDrawArrays(GL_QUADS, 0, 4);
    err();

    err();
  }

  private void err() {
    int f = gl.glGetError();
    if (f != 0) {
      pr("GL error: " + f + "   " + stackTrace());
    }
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

  private int activeTexture; // id of last selected texture, or 0 if none
  private boolean textureMode; // true if GL_TEXTURE_2D enabled
  private boolean sFocusValid;
  private Point sFocus;
  private SpriteInfo spriteInfo;
  private JCheckBox mCpt;
  private JCheckBox mShowClip;
  private boolean hlClip, hlCP;
  private float lineWidth = 1;
  private int renderState;
  private int currentColorId;
  private Map atlasTextures;

}
