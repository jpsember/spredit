package com.js.scredit;

import static com.js.basic.Tools.*;

import java.awt.Color;
import java.io.*;
import apputil.*;

import com.js.basic.Streams;
import com.js.myopengl.*;
import tex.*;
import com.js.geometry.*;

public class EditorPanelGL extends GLPanel implements IEditorView {

  public EditorPanelGL() {
    MouseOper.setView(this);
    if (!AppTools.isMac())
      MyMenuBar.addRepaintComponent(this.getComponent());
  }

  private static Atlas sOurFont;

  // private
  static Atlas getOurFont() {
    if (sOurFont == null) {
      System.out.println("attempting to open workfont.atl");
      InputStream s;
      try {
        s = Streams.openResource(ScrMain.class, "workfont.atl");
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
    warning("fig bug: see text file on desktop");
    setZoom(ScriptEditor.zoomFactor());
    setOrigin(ScriptEditor.focus().toPoint());
    super.render();
    paintContents();
  }

  private void paintContents() {
    final boolean db = false;

    // clear background (or plot layers)
    final int MAX_FADED = 3;
    int[] fadeLayers = new int[MAX_FADED];

    // int fadeLayer = -1;
    {
      LayerSet layers = ScriptEditor.layers();

      // final boolean db = false;

      if (ScriptEditor.faded()) {

        int fStart = layers.foregroundStart();
        int fTotal = layers.size() - layers.foregroundStart();

        int slot = layers.currentSlot();

        for (int i = 0; i < MAX_FADED; i++) {
          if (fTotal <= i + 1)
            continue;
          int q = MyMath.myMod((slot - 1 - i - fStart), fTotal) + fStart;
          fadeLayers[i] = 1 + q;

          // if (fTotal > 1+i && slot+i >= fStart) {
          // fadeLayer = MyMath.mod(slot - fStart - 1, fTotal) + fStart;
          // }
          // if (db)
          // pr("faded, fStart=" + fStart + " fTotal=" + fTotal + " slot="
          // + slot + " fadeLayer=" + fadeLayer);
        }

      }
      for (int i = 0; i < Math.min(layers.currentSlot(),
          layers.foregroundStart()); i++) {
        ScriptEditor ed2 = layers.layer(i);
        if (db)
          pr("  plotting layer " + i);
        ed2.render(this, true);
      }

      for (int j = fadeLayers.length - 1; j >= 0; j--) {
        int fadeLayer = fadeLayers[j] - 1;

        if (fadeLayer >= 0) {
          ScriptEditor ed2 = layers.layer(fadeLayer);
          if (db)
            pr("rendering fade layer " + ed2);

          ed2.render(this, true);

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

    if (ScriptEditor.editor() == null)
      return;

    ScriptEditor.editor().render(this, false);

    MouseOper op = MouseOper.getOperation();
    if (op != null)
      op.paint();

    if (ScriptEditor.showOrigin()) {
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
