package com.js.spredit;

import javax.swing.*;
import apputil.*;
import tex.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class AtlasDisplay extends MyFrame {

  public static AtlasDisplay create(TexProject project) {
    final AtlasDisplay f = new AtlasDisplay(project);
    //  f.pack();

    //    f.addComponentListener(new ComponentListener() {
    //
    //      @Override
    //      public void componentHidden(ComponentEvent arg0) {
    //      }
    //
    //      @Override
    //      public void componentMoved(ComponentEvent ev) {
    //        bounds = new IRect(f.getBounds());
    //      }
    //
    //      @Override
    //      public void componentResized(ComponentEvent arg0) {
    //      }
    //
    //      @Override
    //      public void componentShown(ComponentEvent arg0) {
    //      }
    //    });

    //    if (bounds != null)
    //      f.setLocation(bounds.x, bounds.y);
    //    else
    //      f.setLocationRelativeTo(null);

    f.setVisible(true);
    //    f.setVisible(true);
    return f;
  }

  private BufferedImage image;

  /**
   * Create atlas display frame
   * @param project 
   */
  public AtlasDisplay(TexProject project) {
    super("ATLAS_" + project.name());

    Atlas atlas;
    try {
      atlas = project.atlas();
      image = atlas.image();
      setTitle(new RelPath(project.baseDirectory(), atlas.dataFile()).display());
    } catch (IOException e) {
      AppTools.showError("displaying atlas", e);
    }
    prepare();
  }

  /**
   * Create atlas display frame
   * @param project 
   */
  public AtlasDisplay(File atlasFile) {
    super("ATLAS_MISC");
    try {
      Atlas atlas = new Atlas(atlasFile);
      image = atlas.image();
      setTitle(atlasFile.toString());
    } catch (IOException e) {
      AppTools.showError("displaying atlas", e);
    }
    prepare();

  }

  private void prepare() {

    // setLayout(new BorderLayout());

    //Set up the drawing area.
    JPanel drawingPane = new DrawingPane();
    drawingPane.setBackground(Color.white);

    //Put the drawing area in a scroll pane.
    JScrollPane scroller = new JScrollPane(drawingPane);

    scroller
        .setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

    setContents(scroller);
    setResizable(false);
  }

  private class DrawingPane extends JPanel {
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      if (image != null) {
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.drawImage(image, 0, 0, null);
      }

    }
  }
  //  public void close() {
  //
  // //   bounds = new IRect(getBounds());
  //
  //    setVisible(false);
  //    dispose();
  //  }

  //  public static IConfig CONFIG = new IConfig() {
  //
  //    @Override
  //    public boolean process(DefScanner sc, String item) {
  //      if (item.equals("ATLASFRAME")) {
  //        bounds = sc.sIRect();
  //        return true;
  //      }
  //      return false;
  //    }
  //
  //    @Override
  //    public void writeTo(DefBuilder sb) {
  //      if (bounds != null) {
  //        sb.append("ATLASFRAME");
  //        sb.append(bounds);
  //        sb.addCr();
  //      }
  //    }
  //  };

  //  public static IRect bounds;
}
