package joglexample;

import javax.swing.JFrame;

import myjogl.GLPanel;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

  private static class OurGLPanel extends GLPanel {
    @Override
    public void render() {
      if (sizeHasChanged()) {
        OneTriangle.setup(getSize());
      }
      OneTriangle.render(getSize());
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
