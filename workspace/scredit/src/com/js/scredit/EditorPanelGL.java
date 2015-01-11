package com.js.scredit;

import static com.js.basic.Tools.*;

import java.awt.Color;
import java.io.*;

import com.js.myopengl.*;
import tex.*;

import com.js.basic.Files;
import com.js.editor.IEditorView;
import com.js.editor.MouseOper;
import com.js.geometry.*;

public class EditorPanelGL extends GLPanel implements IEditorView {

  public EditorPanelGL(InfoPanel infoPanel) {
    mInfoPanel = infoPanel;
  }

  private static Atlas sOurFont;
  private InfoPanel mInfoPanel;

  // private
  static Atlas getOurFont() {
    if (sOurFont == null) {
      System.out.println("attempting to open workfont.atl");
      InputStream s;
      try {
        s = Files.openResource(ScrMain.class, "workfont.atl");
        sOurFont = new Atlas(s);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return sOurFont;
  }

  @Override
  public void render() {
    unimp("set font");
    // setFont(ourFont);
    setZoom(ScriptEditor.zoomFactor());
    setOrigin(ScriptEditor.focus().toPoint());
    super.render();
    paintContents();
  }

  private void paintContents() {
    if (!ScriptEditor.isProjectOpen())
      return;

    // clear background (or plot layers)
    final int MAX_FADED = 3;
    int[] fadeLayers = new int[MAX_FADED];

    // int fadeLayer = -1;
    {
      ScriptSet layers = ScriptEditor.layers();

      if (mInfoPanel.isFaded()) {

        int fStart = layers.getForegroundLayer();
        int fTotal = layers.size() - layers.getForegroundLayer();

        for (int i = 0; i < MAX_FADED; i++) {
          if (fTotal <= i + 1)
            continue;
          int q = MyMath.myMod((layers.getCursor() - 1 - i - fStart), fTotal)
              + fStart;
          fadeLayers[i] = 1 + q;
        }
      }

      // Render any background layers that appear at or before current slot
      for (int i = 0; i < Math.min(layers.getCursor(),
          layers.getForegroundLayer()); i++) {
        ScriptEditor editor = layers.get(i);
        editor.render(this, true);
      }

      for (int j = fadeLayers.length - 1; j >= 0; j--) {
        int fadeLayer = fadeLayers[j] - 1;

        if (fadeLayer >= 0) {
          ScriptEditor editor = layers.get(fadeLayer);
          editor.render(this, true);

          // fill screen with translucent bgnd color

          // int bg = this.bgndColorIndex();

          // float alpha = (j+1) / (float)(fadeLayers.length + 1);

          float alpha = 1 / (float) (fadeLayers.length + 1);
          alpha = .5f + (alpha / 2);

          unimp("set bgnd color to translucent perhaps");
          setRenderColor(Color.gray);

          // setRenderColor(Palette.translucent(bgndColorIndex(), alpha));

          // fill actual bounds of screen; need to calc using inv tfm?
          Rect r = new Rect(viewToWorld(Point.ZERO), viewToWorld(getSize()
              .toPoint()));

          fillRect(r);
          setRenderColor(Color.WHITE);
        }
      }
    }

    ScriptEditor.editor().render(this, false);

    MouseOper op = MouseOper.getOperation();
    if (op != null)
      op.paint();

    if (mInfoPanel.isOriginShowing()) {
      final float W = 80;

      lineWidth(3.2f / getZoom());
      setRenderColor(Color.GRAY);
      drawLine(-W, 0, W, 0);
      drawLine(0, -W, 0, W);
      lineWidth(1.2f / getZoom());
      setRenderColor(Color.BLACK); //
      drawLine(-W, 0, W, 0);
      drawLine(0, -W, 0, W);
    }

  }
}
