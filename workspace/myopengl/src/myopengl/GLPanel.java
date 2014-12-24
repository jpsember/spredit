package myopengl;

import java.awt.Color;
import java.awt.Component;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import myjogl.MyJOGL;

import com.js.geometry.*;

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
   * Render view contents using OpenGL. Default implementation clears view to
   * the background color, prepares the projection matrices, and does some
   * additional maintenance
   */
  public void render() {
    clearViewport(getComponent().getBackground());
    prepareProjection();

    // while in the GL context, delete any previously removed textures
    TextureLoader.processDeleteList();
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

  private void clearViewport(Color clearColor) {

    // clear to background color
    Color c = clearColor;

    float TOFLOAT = 1 / 255.0f;
    gl2.glClearColor(c.getRed() * TOFLOAT, c.getGreen() * TOFLOAT, c.getBlue()
        * TOFLOAT, 1);
    gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);

    if (sizeHasChanged()) {
      IPoint size = getSize();
      gl2.glViewport(0, 0, size.x, size.y);
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

      GLTools.storeMatrix(gl2, GL2.GL_PROJECTION, projectionMatrix);
    }

    // Our OpenGL ModelView matrix is just the identity matrix
    GLTools.storeMatrix(gl2, GL2.GL_MODELVIEW, new Matrix());

    // Set texture matrix so (0,0) is in lower left of image
    GLTools.storeMatrix(gl2, GL2.GL_TEXTURE, Matrix.getFlipVertically(1));

    // ...leave with GL_MODELVIEW as the active matrix
    gl2.glMatrixMode(GL2.GL_MODELVIEW);
  }

  protected GL2 gl2;

  private Matrix mViewToWorldMatrix = new Matrix();
  private GLCanvas mCanvas;
  private IPoint mSize;
  private IPoint mPreviousRenderedSize = new IPoint();
  private float mZoomFactor = 1;
  private Point mOrigin = new Point();
}
