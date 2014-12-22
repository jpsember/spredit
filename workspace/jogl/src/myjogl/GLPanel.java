package myjogl;

import java.awt.Component;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import com.js.geometry.IPoint;
import com.js.geometry.Point;

//import static base.MyTools.*;
////import static org.lwjgl.opengl.GL11.*;
//import java.awt.*;
//import java.awt.image.*;
//import java.nio.*;
//import java.util.*;
//import javax.swing.*;
//import base.*;
//import tex.*;

/**
 * Wrapper class for GLCanvas
 */
public class GLPanel {

  public IPoint getSize() {
    return mSize;
  }

  public Component getComponent() {
    return getCanvas();
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
          MyJOGL.setContext(null);
        }

        @Override
        public void display(GLAutoDrawable glautodrawable) {
          setSize(new IPoint(glautodrawable.getSurfaceWidth(), glautodrawable
              .getSurfaceHeight()));
          MyJOGL.setContext(mCanvas.getGL());
          gl2 = MyJOGL.context().getGL2();
          render();
          gl2 = null;
          updateLastRenderedSize();
          MyJOGL.setContext(null);
        }
      });
    }
    return mCanvas;
  }

  protected GL2 gl2;

  private void setSize(IPoint size) {
    mSize = new IPoint(size);
  }

  public void repaint() {
    getComponent().repaint();
  }

  public void repaint(long tm) {
    getComponent().repaint(tm);
  }
  
  public void render() {
  }

  public boolean sizeHasChanged() {
    boolean hasChanged = (mSize.x != mPreviousRenderedSize.x || mSize.y != mPreviousRenderedSize.y);
    return hasChanged;
  }

  private void updateLastRenderedSize() {
    mPreviousRenderedSize = mSize;
  }

  public float zoomFactor() {
    return zoomFactor;
  }

  private float zoomFactor = 1;
  private Point focus = new Point();

  public void setZoom(float zoom) {
    zoomFactor = zoom;
  }

  public void setFocus(Point focus) {
    this.focus = new Point(focus);
  }

  private GLCanvas mCanvas;
  private IPoint mSize;
  private IPoint mPreviousRenderedSize = new IPoint();
}
// final GLCanvas glcanvas = new GLCanvas(glcapabilities);
//

// /*
// [] figure out how to use precompiled buffer objects (?) to speed up sprites,
// text
// */
// public class GLPanel extends AWTGLCanvas {
//
// private static final boolean DBSTATE = false;
// public static boolean glActive() {
// return glActive;
// }
//
// /*
// * Render states.
// * Used to avoid making unnecessary OGL state calls.
// * Modelled after the GameView.cpp class.
// */
//
// public static final int RENDER_UNDEFINED = 0, RENDER_RGB = 1,
// RENDER_SPRITE = 2, RENDER_TOTAL = 3;
//
// private static int renderState;
// public static void setRenderState(int state) {
// ASSERT(state > RENDER_UNDEFINED && state < RENDER_TOTAL);
//
// ASSERT(glActive());
//
// boolean changed = renderState != state;
// if (changed) {
//
// renderState = state;
//
// switch (renderState) {
//
// case RENDER_SPRITE:
// {
// mytexturesOn();
// glEnableClientState(GL_TEXTURE_COORD_ARRAY);
// glEnableClientState(GL_VERTEX_ARRAY);
// }
// break;
//
// case RENDER_RGB:
// mytexturesOff();
// glDisableClientState(GL_TEXTURE_COORD_ARRAY);
// glEnableClientState(GL_VERTEX_ARRAY);
// break;
// }
// }
// }
// private static boolean tintMode;
// private static boolean glInitialized;
//
// public static void setTintMode(boolean f) {
// ASSERT(glActive());
// if (tintMode != f || !glInitialized) {
// tintMode = f;
// if (!tintMode) {
// glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
// } else {
// if (false) {
// warn("trying blend");
// glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
// } else {
// glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
// }
//
// }
// }
// }
// private static int currentColorId;
//
// public static void setRenderColor(int colorIndex) {
// final boolean db = false;
// ASSERT(glActive());
// if (currentColorId != colorIndex) {
//
// if (db)
// pr("setRenderColor index=" + colorIndex);
//
// currentColorId = colorIndex;
//
// Color c = Palette.get(colorIndex);
// if (db)
// pr(" Color=" + c + "\n red=" + (byte) c.getRed());
//
// glColor4ub((byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue(),
// (byte) c.getAlpha());
// }
// }
//
// /**
// */
// public static void mytexturesOn() {
// if (!textureMode) {
// glEnable(GL_TEXTURE_2D);
// textureMode = true;
// }
// }
//
// /**
// */
// public static void mytexturesOff() {
// if (textureMode) {
// glDisable(GL_TEXTURE_2D);
// textureMode = false;
// }
// }
//
// public GLPanel() throws LWJGLException {
// bgndColorIndex = GRAY; //Palette.indexOf(MyColors.GRAY);
// // bgndColor = new float[4];
// // bgndColor[0] = .6f;
// // bgndColor[1] = .6f;
// // bgndColor[2] = .6f;
// // bgndColor[3] = 1;
// }
// /**
// * Select perspective vs parallel mode
// * Not yet implemented.
// * @param f true for perspective
// */
// public void setPerspective(boolean f) {
// if (f)
// throw new UnsupportedOperationException();
// perspective = f;
// }
//
// public float zoomFactor() {
// return zoomFactor;
// }
//
// private static FloatBuffer lineVertBuffer = BufferUtils
// .createFloatBuffer(6 * 3); // strip of 4 triangles
//
// public static void lineWidth(float width) {
// lineWidth = width;
// }
// private static float lineWidth = 1;
//
// public static void drawLine(FlPoint2 pt1, FlPoint2 pt2) {
// drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
// }
//
// public static void drawRect(FlRect r) {
// drawRect(r.x, r.y, r.width, r.height);
// }
// public static void drawRect(float x, float y, float w, float h) {
// drawLine(x, y, x + w, y);
// drawLine(x, y + h, x + w, y + h);
// drawLine(x, y, x, y + h);
// drawLine(x + w, y, x + w, y + h);
// }
//
// /**
// * Draw frame around a rectangle, adjusting for line width
// * @param x
// * @param y
// * @param w
// * @param h
// */
// public static void drawFrame(float x, float y, float w, float h) {
// float halfLineWidth = lineWidth * .5f;
// drawRect(x - halfLineWidth, y - halfLineWidth, w + lineWidth, h + lineWidth);
// }
// public static void drawFrame(IRect r) {
// drawFrame(r.x, r.y, r.width, r.height);
// }
// public static void drawFrame(FlRect r) {
// drawFrame(r.x, r.y, r.width, r.height);
// }
// public static void drawCircle(FlPoint2 origin, float radius) {
// final boolean db = false;
//
// int nPts = (int) (radius * zoomFactor / 2);
// if (db)
// pr("before clamping, radius=" + radius + " zoom=" + zoomFactor + " nPts="
// + nPts);
// nPts = MyMath.clamp(nPts, 6, 50);
//
// //int nPts = 6;
// FlPoint2 prev = null;
// float angle = 0;
// for (int i = 0; i <= nPts; i++) {
// FlPoint2 curr = MyMath.ptOnCircle(origin, angle, radius);
// if (prev != null)
// drawLine(prev, curr);
// prev = curr;
// angle += (2 * MyMath.PI) / nPts;
// }
// }
//
// public static void drawCircle(float x, float y, float radius) {
// drawCircle(new FlPoint2(x, y), radius);
// }
//
// private static FloatBuffer rectVertBuffer = BufferUtils
// .createFloatBuffer(4 * 2); // a single quad
//
// private static FloatBuffer trisVertBuffer;
//
// public static void fillRect(FlRect r) {
// fillRect(r.x, r.y, r.width, r.height);
// }
//
// public static void fillRect(float x, float y, float w, float h) {
// setRenderState(RENDER_RGB);
//
// FloatBuffer v = rectVertBuffer;
// v.rewind();
// v.put(x);
// v.put(y);
// v.put(x + w);
// v.put(y);
// v.put(x + w);
// v.put(y + h);
// v.put(x);
// v.put(y + h);
// v.rewind();
//
// // glEnableClientState(GL_VERTEX_ARRAY);
// glVertexPointer(2, 0, v); // only 2 coords per vertex
// glDrawArrays(GL_QUADS, 0, 4);
// // glDisableClientState(GL_VERTEX_ARRAY);
// }
// private static int trisBufferCap;
//
// public static void fillTriangles(FlPoint2[] tris) {
// setRenderState(RENDER_RGB);
// //float x, float y, float w, float h) {
// // texturesOff();
//
// if (trisVertBuffer == null || trisBufferCap < tris.length) {
// trisVertBuffer = BufferUtils.createFloatBuffer(2 * tris.length);
// trisBufferCap = tris.length;
// }
//
// FloatBuffer v = trisVertBuffer;
// v.rewind();
// for (int i = 0; i < tris.length; i++) {
// FlPoint2 pt = tris[i];
// v.put(pt.x);
// v.put(pt.y);
// }
// v.rewind();
//
// // glEnableClientState(GL_VERTEX_ARRAY);
// glVertexPointer(2, 0, v); // only 2 coords per vertex
// glDrawArrays(GL_TRIANGLES, 0, tris.length);
// // glDisableClientState(GL_VERTEX_ARRAY);
// }
//
// public static void drawLine(float x1, float y1, float x2, float y2) {
// setRenderState(RENDER_RGB);
// // texturesOff();
// //float x1, y1, x2, y2;
// if (x1 > x2) {
// float tmp = x1;
// float tmp2 = y1;
// x1 = x2;
// y1 = y2;
// x2 = tmp;
// y2 = tmp2;
// }
//
// float s = lineWidth * .5f;
//
// if (lineVertBuffer == null)
// lineVertBuffer = BufferUtils.createFloatBuffer(6 * 2); // strip of 4
// triangles
//
// FloatBuffer v = lineVertBuffer;
// v.rewind();
// if (y2 >= y1) {
//
// //-----------------------
// v.put(x1 - s);
// v.put(y1 - s);
//
// v.put(x1 + s);
// v.put(y1 - s);
//
// v.put(x1 - s);
// v.put(y1 + s);
//
// //-----------------------
// v.put(x2 + s);
// v.put(y2 - s);
//
// v.put(x2 - s);
// v.put(y2 + s);
//
// v.put(x2 + s);
// v.put(y2 + s);
// } else {
//
// //-----------------------
// v.put(x1 - s);
// v.put(y1 + s);
//
// v.put(x1 - s);
// v.put(y1 - s);
//
// v.put(x1 + s);
// v.put(y1 + s);
//
// //-----------------------
// v.put(x2 - s);
// v.put(y2 - s);
//
// v.put(x2 + s);
// v.put(y2 + s);
//
// v.put(x2 + s);
// v.put(y2 - s);
// }
//
// // glEnableClientState(GL_VERTEX_ARRAY);
// v.rewind();
// glVertexPointer(2, 0, v); // only 2 coords per vertex
// glDrawArrays(GL_TRIANGLE_STRIP, 0, 6); // 6 vertices total
// // glDisableClientState(GL_VERTEX_ARRAY);
// }
//
// private boolean perspective;
// private IPoint2 currentSize;
// private static float zoomFactor = 1;
// private FlPoint3 focus = new FlPoint3();
// public void setZoom(float zoom) {
// zoomFactor = zoom;
// }
// public void setFocus(IPoint2 focus) {
// ASSERT(!perspective);
// this.focus = new FlPoint3(focus.x, focus.y, 0);
// }
// public void setFocus(FlPoint2 focus) {
// ASSERT(!perspective);
// this.focus = new FlPoint3(focus.x, focus.y, 0);
// }
//
// private void prepareProjection() {
// final boolean db = false;
//
// currentSize = new IPoint2(getWidth(), getHeight());
//
// glViewport(0, 0, currentSize.x, currentSize.y);
//
// { // fix texture coordinate problem, so (0,0) is lower left of png
// glMatrixMode(GL_TEXTURE);
// glLoadIdentity();
// glTranslatef(0, -1, 0);
// glScalef(1, -1, 1);
// }
//
// glMatrixMode(GL11.GL_PROJECTION);
// glLoadIdentity();
//
// {
// float s = .5f / zoomFactor;
// glOrtho(-currentSize.x * s, currentSize.x * s, -currentSize.y * s,
// currentSize.y * s, -1, 1);
// }
//
// // now that projection has been set up,
// // switch to model/view matrix
// glMatrixMode(GL_MODELVIEW);
// glLoadIdentity();
//
// // translate so focus is at origin
// glTranslatef(-focus.x, -focus.y, -focus.z);
//
// // store modelview matrix for retrieval by prepareModel()
//
// FloatBuffer params = BufferUtils.createFloatBuffer(16);
//
// params.position(0);
// glGetFloat(GL11.GL_MODELVIEW_MATRIX, params);
// params.get(modelMatrix_.c);
// if (db)
// pr("prepareCamera, modelview_matrix=\n" + modelMatrix_);
//
// // initialize camera matrix and its inverse, for picking operations
// {
// // FlMatrix44 mat = new FlMatrix44();
// params.position(0);
// GL11.glGetFloat(GL_PROJECTION_MATRIX, params);
// float[] c = FlMatrix44.buildArray();
//
// params.get(c);
// if (db)
// pr(" projection_matrix=\n" + new FlMatrix44(c));
//
// cameraMatrix_ = new FlMatrix44();
// FlMatrix44.multiply(c, modelMatrix_.c, cameraMatrix_.c);
// if (db)
// pr(" cameraMatrix=\n" + cameraMatrix_);
//
// cameraMatrixInverse_ = new FlMatrix44();
// FlMatrix44.invert(cameraMatrix_.c, cameraMatrixInverse_.c);
// if (db)
// pr(" cameraMatrixInverse=\n" + cameraMatrixInverse_);
//
// // construct matrix to convert from window coordinates to
// // normalized device coordinates (UIView to NDC).
//
// /* This converts UIView coordinates to NDC as follows:
//
// NDCx = (Vx - Cx) * 2 / W
// NDCy = (Vy - Cy) * 2 / H
// NDCz = -1
// NDCw = 1
//
// where
//
// Vx,Vy = UIView coordinates
// Cx,Cy = Trans.viewCenter_, the center of the OpenGL view (in UIView
// coordinates)
// W,H = size of OpenGL view
// */
//
// IPoint2 viewCenter_ = new IPoint2(currentSize.x / 2, currentSize.y / 2);
//
// c[0 + 4 * 0] = 2f / currentSize.x;
// c[0 + 4 * 1] = 0;
// c[0 + 4 * 2] = 0;
// c[0 + 4 * 3] = (-2f * viewCenter_.x) / currentSize.x;
//
// if (db)
// pr("viewCenter=" + viewCenter_);
//
// c[1 + 4 * 0] = 0;
// c[1 + 4 * 1] = -2f / currentSize.y;
// c[1 + 4 * 2] = 0;
// c[1 + 4 * 3] = (2f * viewCenter_.y) / currentSize.y;
//
// c[2 + 4 * 0] = 0;
// c[2 + 4 * 1] = 0;
// c[2 + 4 * 2] = 0;
// c[2 + 4 * 3] = -1;
//
// c[3 + 4 * 0] = 0;
// c[3 + 4 * 1] = 0;
// c[3 + 4 * 2] = 0;
// c[3 + 4 * 3] = 1;
//
// FlMatrix44.multiply(cameraMatrixInverse_.c, c, cameraMatrixInverse_.c);
// }
//
// // turn off backspace culling in parallel mode
// glDisable(GL_CULL_FACE);
//
// }
//
// private float angle;
//
// /**
// * Override this to draw into the view.
// */
// public void paintContents() {
// angle += 1;
// glPushMatrix();
// setRenderColor(42); // no idea what 42 is.
// //setColor(.5f, .2f, .2f);
// glTranslatef(getWidth() / 2.0f, getHeight() / 2.0f, 0.0f);
// glRotatef(angle, 0f, 0f, 1.0f);
// glRectf(-50.0f, -50.0f, 50.0f, 50.0f);
// glPopMatrix();
// }
//
// // // private static final boolean dbt = false;
// // public float[] bgndColor() {
// // return bgndColor;
// // }
// public int bgndColorIndex() {
// return bgndColorIndex;
// }
// // private float[] bgndColor;
// private int bgndColorIndex;
//
// public static final int BLACK = 776;//Palette.indexOf(MyColors.BLACK);
// public static final int RED = 8;//Palette.indexOf(MyColors.RED);
// public static final int BLUE = 416;//Palette.indexOf(MyColors.BLUE);
// public static final int YELLOW = 129;//Palette.indexOf(MyColors.YELLOW);
// public static final int WHITE = 680;//Palette.indexOf(MyColors.WHITE);
// public static final int GREEN = 224;//Palette.indexOf(MyColors.GREEN);
// public static final int GRAY = 704;//Palette.indexOf(MyColors.GRAY);
// public static final int DARKGRAY = 728;//Palette.indexOf(MyColors.DARKGRAY);
//
// // public static final float[] BLACK = { 0, 0, 0 };
// // public static final float[] RED = { 1, 0, 0 };
// // public static final float[] BLUE = { 0, 0, 1 };
// // public static final float[] YELLOW = { 1, 1, 0 };
// // public static final float[] WHITE = { 1, 1, 1 };
// // public static final float[] GREEN = { 0, 1, 0 };
//
// /**
// * @deprecated use setRenderColor()
// * @param r
// * @param g
// * @param b
// */
// public static void setColor(float r, float g, float b) {
// setColor(r, g, b, 1);
// }
//
// /**
// * @deprecated use setRenderColor()
// */
// public static void setColor(float r, float g, float b, float a) {
// if (DBSTATE)
// pr("glColor4f " + f(r) + f(g) + f(b) + f(a));
// glColor4f(r, g, b, a);
// }
//
// /**
// * @deprecated use setRenderColor()
// * @param r
// * @param g
// * @param b
// */
// public static void setColor(float[] c) {
// if (c.length >= 4)
// setColor(c[0], c[1], c[2], c[3]);
// else
// setColor(c[0], c[1], c[2]);
// }
//
// /**
// * Prepare projection, clear view, etc
// */
// public void paintStart() {
//
// // initialize OGL state
// if (!glInitialized) {
// // set REPLACE mode, to ignore color
// glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
// glShadeModel(GL_FLAT); // not sure this is necessary
//
// // textures are disabled
// glDisable(GL_TEXTURE_2D);
//
// glEnable(GL_BLEND);
// glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//
// // glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
// // GL_NEAREST); //LINEAR);
//
// // initialize our state
// activeTexture = 0;
// textureMode = false;
//
// glInitialized = true;
// }
//
// // clear to background color
// // From AWTGLCanvas:
// "Default pixel format is minimum 8 bits depth, and no alpha nor stencil."
// // Hence, alpha is not used here.
// Color c = Palette.get(bgndColorIndex());
// //pr("clearing to bgnd color "+c+" (index="+bgndColorIndex()+")");
//
// final float TOFLOAT = 1 / 255.0f;
//
// glClearColor(c.getRed() * TOFLOAT, c.getGreen() * TOFLOAT, c.getBlue()
// * TOFLOAT, 1); //TOc.bgndColor[0], bgndColor[1], bgndColor[2], 1);
// glClear(GL_COLOR_BUFFER_BIT);
//
// prepareProjection();
// }
//
// /**
// * Finish rendering frame
// */
// public void paintEnd() {
// // while in the GL context, delete any previously removed textures
// TextureLoader.processDeleteList();
//
// try {
// swapBuffers();
// } catch (LWJGLException e) {
// throw new RuntimeException(e);
// }
//
// }
//
// private static boolean glActive;
//
// @Override
// public void paintGL() {
// glActive = true;
// paintStart();
// paintContents();
// paintEnd();
// glActive = false;
// }
//
// /**
// * Unproject point from view space to world space;
// * parallel projection only.
// * @param viewLoc
// * @return
// */
// public FlPoint2 viewToWorld(IPoint2 viewLoc) {
// if (perspective)
// throw new IllegalStateException();
//
// final boolean db = false;
//
// if (db)
// pr("viewToWorld  viewLoc=" + viewLoc);
//
// FlPoint4 in = new FlPoint4(viewLoc.x, viewLoc.y, 0);
// FlPoint4 out = new FlPoint4();
//
// cameraMatrixInverse_.apply(in, out);
// if (db)
// pr("applied cameraMatrixInverse:\n" + cameraMatrixInverse_ + "\n result="
// + out);
// return new FlPoint2(out.x, out.y);
// }
//
// /**
// * Unproject point from view space to world space
// * @param viewLoc view space location
// * @param cameraLoc if not null, camera location returned here
// * @param ray if not null, where to store normalized ray
// * @return normalized ray from camera
// */
// public FlPoint3 viewToWorld(FlPoint2 viewLoc, FlPoint3 cameraLoc, FlPoint3
// ray) {
//
// final boolean db = false;
//
// if (db)
// pr("viewToWorld  viewLoc=" + viewLoc);
//
// if (ray == null)
// ray = new FlPoint3();
//
// FlPoint4 in = new FlPoint4(viewLoc.x, viewLoc.y, 0);
// FlPoint4 out = new FlPoint4();
//
// cameraMatrixInverse_.apply(in, out);
// if (db)
// pr("applied cameraMatrixInverse:\n" + cameraMatrixInverse_ + "\n result="
// + out);
//
// // the w component should equal |zNear|.
// MyTools.ASSERT(Math.abs(out.w) > 1e-3);
//
// FlPoint3 eye = focus; //= cam.eye();
//
// ray.x = out.x / out.w - eye.x;
// ray.y = out.y / out.w - eye.y;
// ray.z = out.z / out.w - eye.z;
//
// if (db)
// pr("before normalize: " + ray);
//
// ray.normalize();
//
// if (cameraLoc != null)
// cameraLoc.setTo(eye);
//
// if (db)
// pr("returning:        " + ray);
//
// return ray;
//
// }
//
// private static int textureFor(Atlas a) {
// final boolean db = false;
//
// if (atlasTextures == null)
// atlasTextures = new HashMap();
// Integer iv = (Integer) atlasTextures.get(a);
// if (iv == null) {
// if (db)
// pr("installing atlas texture: " + a);
//
// BufferedImage img = a.image();
//
// int texHandle;
//
// texHandle = TextureLoader.getTexture(img, null);
//
// iv = new Integer(texHandle);
//
// atlasTextures.put(a, iv);
// if (db)
// pr(" handle=" + iv);
// }
// return iv.intValue();
// }
//
// private static Map atlasTextures;
//
// /**
// * Plot sprite
// * @param atlas atlas containing sprite
// * @param spriteIndex index within atlas
// * @param x position
// * @param y
// * @return sprite plotted
// */
// public static Sprite plotSprite(Atlas atlas, int spriteIndex, float x, float
// y) {
// Matrix tfm = Matrix.getTranslate(new FlPoint2(x, y), false);
// return plotSprite(atlas, spriteIndex, tfm);
// }
//
// /**
// * Plot sprite
// * @param atlas atlas containing sprite
// * @param spriteIndex index within atlas
// * @param tfm transformation matrix to apply to vertices
// * @return sprite plotted
// */
// public static Sprite plotSprite(Atlas atlas, int spriteIndex, Matrix tfm) {
// int texHandle = textureFor(atlas);
// Sprite sprite = atlas.sprite(spriteIndex);
// plotSprite(texHandle, atlas.imageSize(), sprite, tfm); //x, y);
// return sprite;
// }
//
// // public static final int COLORS = 0, TEXTURES = 1, BOTH = 2;
//
// public static void selectTexture(int texHandle) {
// if (activeTexture != texHandle) {
// activeTexture = texHandle;
// if (DBSTATE)
// pr("glBindTexture " + texHandle);
// glBindTexture(GL_TEXTURE_2D, texHandle);
// err();
// }
// }
//
// /**
// * Plot sprite
// * @param texHandle openGL texture
// * @param textureSize size of texture, in pixels
// * @param sprite sprite
// * @param x location
// * @param y
// */
// public static void plotSprite(int texHandle, IPoint2 textureSize,
// Sprite sprite, float x, float y) {
//
// Matrix tfm = Matrix.getTranslate(new FlPoint2(x, y), false);
// plotSprite(texHandle, textureSize, sprite, tfm);
//
// // final boolean db = false; //sprite.id().equals("WOW");
// //
// // texturesOn();
// // selectTexture(texHandle);
// //
// // if (db)
// // pr("plotSprite texHandle#" + texHandle + ":" + sprite + " at "
// // + new IPoint2(x, y));
// //
// // err();
// // glPushMatrix();
// // glTranslatef(x, y, 0);
// //
// // IRect imgRect = new IRect(sprite.bounds());
// // FlRect aRect = new FlRect(imgRect);
// //
// // if (db)
// // pr("imgRect=" + imgRect + "\n  aRect=" + aRect + "\n compression="
// // + sprite.compressionFactor() + "\n sprite.trans="
// // + sprite.translate());
// //
// // aRect.scale(sprite.compressionFactor());
// // if (db)
// // pr(" after compression=" + aRect);
// //
// // IPoint2 tr = sprite.translate();
// // aRect.translate(tr.x, tr.y);
// // if (db)
// // pr(" after translation=" + aRect);
// //
// // ASSERT(TextureLoader.ceilingPower2(textureSize.x) == textureSize.x
// // && TextureLoader.ceilingPower2(textureSize.y) == textureSize.y);
// //
// // float sx = 1f / textureSize.x;
// // float sy = 1f / textureSize.y;
// // aRect.x *= sx;
// // aRect.y *= sy;
// // aRect.width *= sx;
// // aRect.height *= sy;
// //
// // if (db)
// // pr("texture rect= " + aRect);
// //
// // FloatBuffer v = BufferUtils.createFloatBuffer(2 * 4);
// // FloatBuffer t = BufferUtils.createFloatBuffer(2 * 4);
// //
// // v.put(imgRect.x);
// // v.put(imgRect.y);
// // v.put(imgRect.endX());
// // v.put(imgRect.y);
// // v.put(imgRect.endX());
// // v.put(imgRect.endY());
// // v.put(imgRect.x);
// // v.put(imgRect.endY());
// //
// // v.rewind();
// //
// // t.put(aRect.x);
// // t.put(aRect.y);
// // t.put(aRect.endX());
// // t.put(aRect.y);
// // t.put(aRect.endX());
// // t.put(aRect.endY());
// // t.put(aRect.x);
// // t.put(aRect.endY());
// //
// // t.rewind();
// //
// // glEnableClientState(GL_VERTEX_ARRAY);
// // glEnableClientState(GL_TEXTURE_COORD_ARRAY);
// //
// // glVertexPointer(2, 0, v);
// // err();
// // glTexCoordPointer(2, 0, t);
// // err();
// //
// // glDrawArrays(GL_QUADS, 0, 4);
// // err();
// //
// // glDisableClientState(GL_VERTEX_ARRAY);
// // glDisableClientState(GL_TEXTURE_COORD_ARRAY);
// // err();
// //
// // glPopMatrix();
// }
//
// /**
// * Plot sprite
// * @param texHandle openGL texture
// * @param textureSize size of texture, in pixels
// * @param sprite sprite
// * @param x location
// * @param y
// */
// public static void plotSprite(int texHandle, IPoint2 textureSize,
// Sprite sprite, Matrix tfm) {
//
// final boolean db = false; //sprite.id().equals("WOW");
//
// setRenderState(RENDER_SPRITE);
// //texturesOn();
// selectTexture(texHandle);
//
// if (db)
// pr("plotSprite texHandle#" + texHandle + ":" + sprite);
//
// err();
// // glPushMatrix();
// // glTranslatef(x, y, 0);
//
// FlRect imgRect = new FlRect(sprite.bounds());
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
// FlPoint2 tr = sprite.translate();
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
// FlPoint2 tp = new FlPoint2();
// tfm.apply(imgRect.x, imgRect.y, tp);
// v.put(tp.x);
// v.put(tp.y);
//
// tfm.apply(imgRect.endX(), imgRect.y, tp);
// v.put(tp.x);
// v.put(tp.y);
//
// tfm.apply(imgRect.endX(), imgRect.endY(), tp);
// v.put(tp.x);
// v.put(tp.y);
//
// tfm.apply(imgRect.x, imgRect.endY(), tp);
// v.put(tp.x);
// v.put(tp.y);
//
// // v.put(imgRect.x);
// // v.put(imgRect.y);
// // v.put(imgRect.endX());
// // v.put(imgRect.y);
// // v.put(imgRect.endX());
// // v.put(imgRect.endY());
// // v.put(imgRect.x);
// // v.put(imgRect.endY());
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
// // glEnableClientState(GL_VERTEX_ARRAY);
// // glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//
// glVertexPointer(2, 0, v);
// err();
// glTexCoordPointer(2, 0, t);
// err();
//
// glDrawArrays(GL_QUADS, 0, 4);
// err();
//
// // glDisableClientState(GL_VERTEX_ARRAY);
// // glDisableClientState(GL_TEXTURE_COORD_ARRAY);
// err();
//
// // glPopMatrix();
// }
//
// public static void plotString(String str, FlPoint2 pt) {
// plotString(str, (int) pt.x, (int) pt.y);
// }
// public static void plotString(String str, float x, float y) {
// plotString(str, (int) x, (int) y);
// }
//
// public static void plotString(String str, int x, int y) {
//
// if (currentFont == null)
// throw new IllegalStateException();
//
// // setRenderState(RENDER_SPRITE);
// // texturesOn();
//
// for (int i = 0; i < str.length(); i++) {
// char c = str.charAt(i);
// int si = fontCharToSprite(c);
// //Sprite spr = currentFont.sprite(fontCharToSprite(c));
// //IRect r = fontCharRect(c);
// Sprite spr = plotSprite(currentFont, si, x, y);
// x += spr.bounds().width;
// }
// }
//
// public static void setFont(Atlas font) {
// if (currentFont != font) {
// currentFont = font;
// // IRect r = fontCharRect('M');
// // fontCharSep = -Math.round(r.width * .1f);
// }
// }
//
// public static void err() {
// int f = glGetError();
// if (f != 0) {
// pr("GL error: " + f + "   " + stackTrace());
// }
// }
//
// private static int fontCharToSprite(char c) {
// if (c < ' ' || c > 0x7f)
// c = '_';
// warn("figure out where first character is using better method");
// return c - ' '+1;
// }
//
// private static Atlas currentFont;
//
// private FlMatrix44 cameraMatrix_;
// private FlMatrix44 cameraMatrixInverse_;
// // standard modelview matrix
// private FlMatrix44 modelMatrix_ = new FlMatrix44();
//
// // our OpenGL state
// private static int activeTexture; // id of last selected texture, or 0 if
// none
// private static boolean textureMode; // true if GL_TEXTURE_2D enabled
// }
