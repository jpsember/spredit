package com.js.myopengl;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;

import static com.js.basic.Tools.ASSERT;
import static com.js.basic.Tools.pr;
import static com.js.basic.Tools.stackTrace;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_CULL_FACE;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static javax.media.opengl.GL.GL_REPLACE;
import static javax.media.opengl.GL.GL_SRC_ALPHA;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.GL2ES1.GL_TEXTURE_ENV;
import static javax.media.opengl.GL2ES1.GL_TEXTURE_ENV_MODE;
import static javax.media.opengl.GL2GL3.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_FLAT;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_TEXTURE_COORD_ARRAY;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import tex.Atlas;
import tex.Sprite;

import com.js.geometry.*;

import static com.js.basic.Tools.*;

/**
 * <pre>
 * 
 * Wrapper class for GLCanvas, representing a view of a 2D world.
 * 
 * 
 * The coordinate spaces involved are:
 * 
 * World space
 * ---------------
 * This is what all the inputs to GLPanel rendering should be expressed in.
 * 
 * Viewport space
 * ---------------
 * World space after translating so the world space origin is in the bottom left, 
 * then scaling by the zoom factor
 * 
 * View space
 * ---------------
 * This is viewport space but flipped so the origin is in the top left (to agree
 * with the Swing coordinate system)
 * 
 * NDC space
 * ---------------
 * This is OpenGL's normalized device coordinate space, with left/bottom/right/top 
 * equal to -1/-1/1/1 respectively
 * 
 * </pre>
 */
public class GLPanel {

  public IPoint getSize() {
    return mSize;
  }

  public Component getComponent() {
    return getCanvas();
  }

  public void repaint() {
    getComponent().repaint();
  }

  public void repaint(long tm) {
    getComponent().repaint(tm);
  }

  /**
   * Get the OpenGL context; only valid while render() is being called
   */
  public GL2 glContext() {
    return gl;
  }

  /**
   * Render view contents using OpenGL. Default implementation clears view to
   * the background color, prepares the projection matrices, and does some
   * additional maintenance
   */
  public void render() {
    clearViewport(getComponent().getBackground());
    prepareProjection();

    // while in the GL context, delete any previously removed textures
    TextureLoader.processDeleteList(gl);
    paintStart();
  }

  public Point getOrigin() {
    return mOrigin;
  }

  public void setZoom(float zoom) {
    mZoomFactor = zoom;
  }

  public float getZoom() {
    return mZoomFactor;
  }

  public void setOrigin(Point origin) {
    mOrigin.setTo(origin);
  }

  /**
   * Given a point in view space, determine the corresponding point in world
   * space (based on most recent render() operation)
   * 
   * @param viewPt
   */
  public Point viewToWorld(Point viewPt) {
    return mViewToWorldMatrix.apply(viewPt);
  }

  public void fillRect(Rect r) {
    fillRect(r.x, r.y, r.endX(), r.endY());
  }

  public void fillRect(float x, float y, float width, float height) {
    float x2 = x + width;
    float y2 = y + height;
    setRenderState(RENDER_RGB);

    gl.glBegin(GL_TRIANGLE_STRIP);
    gl.glVertex2f(x, y);
    gl.glVertex2f(x2, y);
    gl.glVertex2f(x, y2);
    gl.glVertex2f(x2, y2);
    gl.glEnd();
  }

  public void fillTriangles(Point[] vertices) {
    unimp("fillTriangles");
  }

  public void plotString(String string, Point location) {
    plotString(string, location.x, location.y);
  }

  public void plotString(String string, float x, float y) {
    unimp("plotString");
  }

  private void clearViewport(Color clearColor) {

    // clear to background color
    Color c = clearColor;

    float TOFLOAT = 1 / 255.0f;
    gl.glClearColor(c.getRed() * TOFLOAT, c.getGreen() * TOFLOAT, c.getBlue()
        * TOFLOAT, 1);
    gl.glClear(GL_COLOR_BUFFER_BIT);

    if (sizeHasChanged()) {
      IPoint size = getSize();
      gl.glViewport(0, 0, size.x, size.y);
    }

  }

  private boolean sizeHasChanged() {
    boolean hasChanged = (mSize.x != mPreviousRenderedSize.x || mSize.y != mPreviousRenderedSize.y);
    return hasChanged;
  }

  private void updateLastRenderedSize() {
    mPreviousRenderedSize = mSize;
  }

  private GLCanvas getCanvas() {
    if (mCanvas == null) {
      GLProfile glprofile = GLProfile.getDefault();
      javax.media.opengl.GLCapabilities glcapabilities = new javax.media.opengl.GLCapabilities(
          glprofile);
      mCanvas = new GLCanvas(glcapabilities);

      mCanvas.addGLEventListener(new GLEventListener() {

        @Override
        public void reshape(GLAutoDrawable glautodrawable, int x, int y,
            int width, int height) {
          setSize(new IPoint(width, height));
        }

        @Override
        public void init(GLAutoDrawable glautodrawable) {
        }

        @Override
        public void dispose(GLAutoDrawable glautodrawable) {
        }

        @Override
        public void display(GLAutoDrawable glautodrawable) {
          setSize(new IPoint(glautodrawable.getSurfaceWidth(), glautodrawable
              .getSurfaceHeight()));
          gl = mCanvas.getGL().getGL2();
          render();
          updateLastRenderedSize();
          gl = null;
        }
      });
    }
    return mCanvas;
  }

  private void setSize(IPoint size) {
    mSize = new IPoint(size);
  }

  /**
   * Prepare the transformation matrices.
   */
  private void prepareProjection() {

    IPoint size = getSize();
    float zoom = getZoom();
    {
      Matrix translate = Matrix.getTranslate(-getOrigin().x, -getOrigin().y);
      Matrix scale = Matrix.getScale(zoom);
      // We want translating to be applied first, so have it be the LAST
      // multiplicand
      Matrix worldToViewport = Matrix.multiply(scale, translate);

      // Calculate view -> world matrix, for picking operations
      // Note that View space is the same as Viewport space, except that the
      // origin is in the top left, not the bottom left
      Matrix viewportToView = Matrix.getFlipVertically(size.y);
      Matrix worldToView = Matrix.multiply(viewportToView, worldToViewport);
      worldToView.invert(mViewToWorldMatrix);

      // Construct viewport -> NDC matrix. For a description of the
      // calculations, see (ignoring the z component):
      //
      // https://developer.apple.com/library/mac/documentation/Darwin/Reference/ManPages/man3/glOrtho.3.html
      //
      Matrix viewportToNDC = new Matrix();
      viewportToNDC.a = 2.0f / size.x;
      viewportToNDC.d = 2.0f / size.y;
      viewportToNDC.tx = -1.0f;
      viewportToNDC.ty = -1.0f;

      // The OpenGL projection matrix is world -> NDC:
      Matrix projectionMatrix = Matrix.multiply(viewportToNDC, worldToViewport);

      GLTools.storeMatrix(gl, GL_PROJECTION, projectionMatrix);
    }

    // Our OpenGL ModelView matrix is just the identity matrix
    GLTools.storeMatrix(gl, GL_MODELVIEW, new Matrix());

    // Set texture matrix so (0,0) is in lower left of image
    GLTools.storeMatrix(gl, GL_TEXTURE, Matrix.getFlipVertically(1));

    // ...leave with GL_MODELVIEW as the active matrix
    gl.glMatrixMode(GL_MODELVIEW);
  }

  public void lineWidth(float width) {
    mLineWidth = width;
  }

  /*
   * Render states; used to avoid making unnecessary OGL state calls
   */
  private static final int RENDER_UNDEFINED = 0, RENDER_RGB = 1,
      RENDER_SPRITE = 2, RENDER_TOTAL = 3;

  private void setRenderState(int state) {
    ASSERT(state > RENDER_UNDEFINED && state < RENDER_TOTAL);

    boolean changed = mRenderState != state;
    if (changed) {

      mRenderState = state;

      switch (mRenderState) {

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

  public void setRenderColor(Color c) {
    if (c != mCurrentColor) {
      mCurrentColor = c;
      gl.glColor4ub((byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue(),
          (byte) c.getAlpha());
    }
  }

  private void mytexturesOn() {
    if (!mTextureEnabled) {
      gl.glEnable(GL_TEXTURE_2D);
      mTextureEnabled = true;
    }
  }

  private void mytexturesOff() {
    if (mTextureEnabled) {
      gl.glDisable(GL_TEXTURE_2D);
      mTextureEnabled = false;
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
  public void drawFrame(float x, float y, float w, float h) {
    float halfLineWidth = mLineWidth * .5f;
    drawRect(x - halfLineWidth, y - halfLineWidth, w + mLineWidth, h
        + mLineWidth);
  }

  public void drawFrame(Rect r) {
    drawFrame(r.x, r.y, r.width, r.height);
  }

  public void drawLine(Point p1, Point p2) {
    drawLine(p1.x, p1.y, p2.x, p2.y);
  }

  public void drawLine(float x1, float y1, float x2, float y2) {
    setRenderState(RENDER_RGB);

    // We want to plot the Minkowski sum of the line segment with a unit square
    // rotated to be aligned with the line segment;
    // the square has width equal to the line width

    // Calculate the unit vector from p1 to p2; if undefined, assume line is
    // horizontal
    float dist = MyMath.distanceBetween(new Point(x1, y1), new Point(x2, y2));

    // The 'radius' of the unit square representing our pen
    float radius = mLineWidth * .5f;

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

  public void drawCircle(Point point, float f) {
    unimp("drawCircle");
  }

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

    mCurrentTextureId = 0;
    mTextureEnabled = false;
    mCurrentColor = null;
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
    if (mCurrentTextureId != texHandle) {
      mCurrentTextureId = texHandle;
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
  public void plotSprite(int texHandle, IPoint textureSize, Sprite sprite,
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

  private int textureFor(Atlas a) {
    Integer iv = mAtlasToTextureIdMap.get(a);
    if (iv == null) {
      BufferedImage img = a.image();
      iv = TextureLoader.getTexture(gl, img, null);
      mAtlasToTextureIdMap.put(a, iv);
    }
    return iv.intValue();
  }

  private void err() {
    int f = gl.glGetError();
    if (f != 0) {
      pr("GL error: " + f + "   " + stackTrace());
    }
  }

  // Value returned by glContext()
  protected GL2 gl;

  private Matrix mViewToWorldMatrix = new Matrix();
  private GLCanvas mCanvas;
  private IPoint mSize;
  private IPoint mPreviousRenderedSize = new IPoint();
  private float mZoomFactor = 1;
  private Point mOrigin = new Point();
  private int mCurrentTextureId; // id of last selected texture, or 0 if none
  private boolean mTextureEnabled; // true if GL_TEXTURE_2D enabled
  private float mLineWidth = 1;
  private int mRenderState; // RENDER_xxx
  private Color mCurrentColor;
  private Map<Atlas, Integer> mAtlasToTextureIdMap = new HashMap();

}
