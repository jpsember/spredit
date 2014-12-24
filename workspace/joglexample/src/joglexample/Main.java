package joglexample;

import javax.media.opengl.GL;
import javax.swing.JFrame;

import com.js.geometry.IPoint;

import com.js.myopengl.GLPanel;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

  private static class OurGLPanel extends GLPanel {

    @Override
    public void render() {
      super.render();

      IPoint size = getSize();

      // draw a triangle filling the window
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

    // Close frame automatically after several seconds
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        jframe
            .dispatchEvent(new WindowEvent(jframe, WindowEvent.WINDOW_CLOSING));
      }
    }, 30 * 1000);

  }
}
