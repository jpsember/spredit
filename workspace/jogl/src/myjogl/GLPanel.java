package myjogl;

import java.awt.Component;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import com.js.geometry.IPoint;
import com.js.geometry.Point;

//import static com.js.basic.Tools.*;

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

  public Point getFocus() {
    return mFocus;
  }

  private float zoomFactor = 1;
  private Point mFocus = new Point();

  public void setZoom(float zoom) {
    zoomFactor = zoom;
  }

  public float getZoom() {
    return zoomFactor;
  }

  public void setFocus(Point focus) {
    mFocus.setTo(focus);
  }

  private GLCanvas mCanvas;
  private IPoint mSize;
  private IPoint mPreviousRenderedSize = new IPoint();
}
