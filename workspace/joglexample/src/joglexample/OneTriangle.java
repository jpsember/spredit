package joglexample;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.js.geometry.IPoint;

import myjogl.MyJOGL;

public class OneTriangle {
  protected static void setup(IPoint size) {
    GL2 gl2 = MyJOGL.context().getGL2();
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();

		// coordinate system origin at lower left with width and height same as
		// the window
		GLU glu = new GLU();
    glu.gluOrtho2D(0.0f, size.x, 0.0f, size.y);

		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();

    gl2.glViewport(0, 0, size.x, size.y);
	}

  protected static void render(IPoint size) {
    GL2 gl2 = MyJOGL.context().getGL2();
		gl2.glClear(GL.GL_COLOR_BUFFER_BIT);

		// draw a triangle filling the window
		gl2.glLoadIdentity();
		gl2.glBegin(GL.GL_TRIANGLES);
		gl2.glColor3f(1, 0, 0);
		gl2.glVertex2f(0, 0);
		gl2.glColor3f(0, 1, 0);
    gl2.glVertex2f(size.x, 0);
		gl2.glColor3f(0, 0, 1);
    gl2.glVertex2f(size.x / 2, size.y);
		gl2.glEnd();
	}
}
