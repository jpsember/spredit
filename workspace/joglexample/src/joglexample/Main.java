package joglexample;

import javax.media.opengl.GL;
import javax.swing.JFrame;

import com.js.geometry.IPoint;

import com.js.myopengl.GLPanel;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

  private static class OurGLPanel extends GLPanel {

    @Override
    public void render() {
      super.render();

      IPoint size = getSize();

      // draw a triangle filling the window
      gl.glBegin(GL.GL_TRIANGLES);
      gl.glColor3f(1, 0, 0);
      gl.glVertex2f(0, 0);
      gl.glColor3f(0, 1, 0);
      gl.glVertex2f(size.x, 0);
      gl.glColor3f(0, 0, 1);
      gl.glVertex2f(size.x / 2, size.y);
      gl.glEnd();
    }
  }

  public static void main(String[] args) {

    OurGLPanel panel = new OurGLPanel();

    final JFrame jframe = new JFrame("One Triangle Swing GLCanvas");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowevent) {
        jframe.dispose();
        System.exit(0);
      }
    });

    jframe.getContentPane().add(panel.getComponent(), BorderLayout.CENTER);
    jframe.setSize(640, 480);
    jframe.setVisible(true);

  }
}
