package com.js.spredit;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;

import com.js.myopengl.GLPanel;

import com.js.editor.MouseOper;
import com.js.editor.UserEventSource;
import com.js.geometry.*;

public class SpritePanel extends GLPanel implements UserEventSource {

  public SpritePanel() {
    getComponent().setBackground(Color.white.darker());
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
      setRenderColor(hlClip ? Color.RED : Color.BLUE);
      lineWidth(10f / getZoom());
      drawFrame(spriteInfo.cropRect().toRect());
    }

    if (mCpt.isSelected()) {
      Point t0 = new Point(spriteInfo.centerpoint());

      gl.glPushMatrix();
      gl.glTranslatef(t0.x, t0.y, 0);

      lineWidth(3.2f / getZoom());
      setRenderColor(Color.YELLOW);

      float W = 20 / getZoom();

      drawLine(-W, 0, W, 0);
      drawLine(0, -W, 0, W);
      lineWidth(1.2f / getZoom());
      setRenderColor(hlCP ? Color.YELLOW : Color.BLACK);

      drawLine(-W, 0, W, 0);
      drawLine(0, -W, 0, W);
      gl.glPopMatrix();
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

  @Override
  public MouseOper getDefaultOperation() {
    throw new UnsupportedOperationException("finish refactoring");
  }

  private boolean sFocusValid;
  private Point sFocus;
  private SpriteInfo spriteInfo;
  private JCheckBox mCpt;
  private JCheckBox mShowClip;
  private boolean hlClip, hlCP;
}
