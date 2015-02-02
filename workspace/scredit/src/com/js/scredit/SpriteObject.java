package com.js.scredit;

import static com.js.scredit.ScriptEditor.zoomFactor;

import java.awt.Color;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.opengl.GL2;

import org.json.JSONException;
import org.json.JSONObject;

import tex.Atlas;
import tex.Sprite;
import apputil.AppTools;

import com.js.basic.Files;
import com.js.basic.Freezable;
import com.js.geometry.*;
import com.js.myopengl.GLPanel;

import static com.js.basic.Tools.*;

public class SpriteObject extends EdObject {
  public static final int CODE = 1; // code for sprite object

  @Override
  public EdObject flip(boolean horz, Point newLocation) {
    unimp("include option to flip sprites");
    return super.flip(horz, newLocation);
  }

  @Override
  public Freezable getMutableCopy() {
    SpriteObject e = new SpriteObject(atlas, spriteIndex);
    e.tfm = new ObjTransform(tfm);
    return e;
  }

  public EdObject snapToGrid() {
    Point loc = location();

    Point loc2 = Grid.snapToGrid(loc, false);
    if (loc == loc2)
      return this;
    SpriteObject s = mutableCopyOf(this);
    s.setLocation(loc2);
    return s;
  }

  public EdObject applyColor(Color color) {
    EdObject ret = this;
    if (tintColor != color) {
      SpriteObject so = mutableCopyOf(this);
      so.tintColor = color;
      ret = so;
    }
    return ret;
  }

  public Color getColor() {
    return tintColor;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Sprite ");
    // sb.append("(S:");
    int si = spriteIndex();
    if (si < 0) {
      if (missingId != null)
        sb.append("!!! missing: " + missingId);
      else
        sb.append("???");
    } else
      sb.append(atlas.sprite(si).id());
    // sb.append(' ');
    // sb.append(tfm.location());
    // sb.append(")");
    return sb.toString();
  }

  private Atlas atlas;
  private int spriteIndex;
  private Color tintColor;

  private ObjTransform tfm = new ObjTransform();

  private static final int DEF_SIZE = 20;

  @Override
  public boolean contains(Point pt) {
    if (spriteIndex >= 0) {
      Rect r;
      Point spt = tfm.inverse().apply(pt);
      {
        Sprite s = atlas.sprite(spriteIndex);
        r = new Rect(s.bounds());
        return r.contains(spt);

      }
    } else
      return boundingRect().contains(pt);
  }

  private Rect bounds;
  private ArrayList<Point> corners;

  @Override
  public Rect boundingRect() {
    if (bounds == null) {
      corners = null;
      if (spriteIndex < 0) {
        bounds = new Rect(tfm.location().x - DEF_SIZE, tfm.location().y
            - DEF_SIZE, 2 * DEF_SIZE, 2 * DEF_SIZE);
      } else {
        corners = new ArrayList();
        Sprite s = atlas.sprite(spriteIndex);
        Rect bn = s.bounds().toRect();
        Matrix m = tfm.matrix();
        corners.add(m.apply(bn.bottomLeft()));
        corners.add(m.apply(bn.bottomRight()));
        corners.add(m.apply(bn.topRight()));
        corners.add(m.apply(bn.topLeft()));
        bounds = Rect.rectContainingPoints(corners);
      }
    }
    return bounds;
  }

  @Override
  public EdObjectFactory getFactory() {
    return FACTORY;
  }

  @Override
  public void render(GLPanel panel, boolean isSelected, boolean isEditable) {
    int si = spriteIndex();

    if (si >= 0) {
      unimp("tint");
      // panel.setTintMode(tintColor != null);
      // if (tintColor >= 0)
      // setRenderColor(tintColor);
      panel.plotSprite(atlas, si, tfm.matrix());
    }

    if (isSelected || missingId != null) {
      Rect b = boundingRect();

      String id = missingId; // "???";
      if (si >= 0)
        id = atlas.sprite(si).id();

      unimp("plot tint here");
      // // to plot text in a particular color,
      // // turn textures on THEN set color.
      // // If textures are turned on by plotString(), the
      // // color is reset to white.
      // panel.setTintMode(true);
      // // texturesOn();
      // panel.setRenderColor(Color.YELLOW);

      Point pt = (si >= 0) ? b.topLeft() : new Point(b.x + 5, b.midY());
      GL2 gl = panel.glContext();
      // plot text at constant size, independent of zoom;
      // to do this, translate to origin then scale
      gl.glPushMatrix();
      gl.glTranslatef(pt.x, pt.y, 0);
      float sc = 1 / zoomFactor();
      gl.glScalef(sc, sc, sc);
      panel.plotString(id, 0, 0);
      gl.glPopMatrix();

      panel.setRenderColor(isSelected ? Color.YELLOW : Color.BLUE);
      panel.lineWidth(1 / ScriptEditor.zoomFactor());
      if (corners != null && corners.size() > 1) {
        for (int i = 0; i < corners.size(); i++) {
          panel.drawLine(getMod(corners, i - 1), corners.get(i));
        }
      }
      if (missingId != null) {
        panel.drawFrame(b);
      }
    }
  }

  public SpriteObject(EdObject source) {
    super(source);
  }

  /**
   * Constructor for object defining sprite within atlas, to be used for message
   * passing
   * 
   * @param atlas
   * @param sprite
   */
  public SpriteObject(Atlas atlas, int spriteIndex) {
    this(null);
    this.atlas = atlas;
    this.spriteIndex = spriteIndex;
  }

  public static EdObjectFactory FACTORY = new SpriteFactory();

  private static class SpriteFactory extends EdObjectFactory {

    @Override
    public String getTag() {
      return "S";
    }

    private static final String ATLAS = "A", ID = "I";

    // , TINT = "T";

    @Override
    public EdObject parse(Script script, JSONObject map) throws JSONException {

      SpriteObject so = new SpriteObject(null);
      String atlasStr = map.optString(ATLAS);
      if (atlasStr != null) {
        File atlasPath = new File(script.project().directory(), atlasStr);
        Atlas a;
        try {
          a = script.project().getAtlas(atlasPath);
        } catch (IOException e) {
          AppTools.showError("reading atlas " + atlasPath, e);
          return null;
        }
        script.setAtlas(a);
      }
      String id = map.getString(ID);

      so.atlas = script.lastAtlas();
      if (so.atlas != null)
        // so.atlas.indexOf(id);
        so.spriteIndex = so.atlas.indexOf(id);
      else {
        warning("atlas null for id=" + id);
      }
      if (so.spriteIndex < 0)
        so.setMissingId(id);

      unimp("read tint");
      // if (s.readTag(TINT)) {
      // so.tintColor = s.sInt();
      // }

      so.tfm.setLocation(Point.opt(map, "loc"));
      float scale = (float) map.optDouble("scale", 1);
      so.tfm.setScale(scale);
      float rot = (float) map.optDouble("rot", 0) / MyMath.M_DEG;
      so.tfm.setRotation(rot);
      return so;
    }

    @Override
    public void write(Script script, JSONObject map, EdObject obj)
        throws JSONException {

      SpriteObject so = (SpriteObject) obj;

      if (!(so.spriteIndex >= 0 || so.missingId != null))
        return;
      {
        // [ATLAS] [ID]
        if (so.atlas != null && so.atlas != script.lastAtlas()) {
          map.put(
              ATLAS,
              Files.fileWithinDirectory(so.atlas.dataFile(),
                  script.project().directory()).toString());
          script.setAtlas(so.atlas);
        }
        map.put(
            ID,
            so.missingId != null ? so.missingId : so.atlas.sprite(
                so.spriteIndex).id());

        unimp("tint color");
        /*
         * if (so.tintColor >= 0) { sb.append(TINT); sb.append(so.tintColor); }
         */
      }
      so.tfm.location().put(map, "loc");
      if (so.tfm.scale() != 1 || so.tfm.rotation() != 0) {
        map.put("scale", so.tfm.scale());
        if (so.tfm.rotation() != 0)
          map.put("rot", MyMath.M_DEG * so.tfm.rotation());
      }
    }

    /**
     * Write SpriteObject to ScriptsFile. Format: [1] type (SpriteObj.CODE) [2]
     * tintColor (-1 if no tinting) [n] mapped string: name of atlas, without
     * extension [n] mapped string: id of sprite [n] ObjTransform
     * 
     * @param sf
     *          ScriptsFile to write to
     * @param obj
     *          SpriteObject
     */
    public void write(ScriptsFile sf, EdObject obj) throws IOException {

      SpriteObject so = (SpriteObject) obj;
      DataOutput dw = sf.outputStream();
      if (so.spriteIndex() < 0) {
        return;
      }

      ASSERT(so.spriteIndex() >= 0);
      dw.writeByte(getCode());

      unimp("write tint color");
      // dw.writeShort(so.tintColor);
      Atlas at = so.getAtlas();

      sf.writeString(at.atlasTag());

      sf.writeString(at.sprite(so.spriteIndex).id());

      so.tfm.write(dw);
    }

    @Override
    public int getCode() {
      return CODE;
    }

  }

  @Override
  public void mutate() {
    super.mutate();
    bounds = null;
  }

  @Override
  public void setLocation(Point pt) {
    mutate();
    tfm.setLocation(new Point(pt));
  }

  public void setMissingId(String id) {
    this.missingId = id;
  }

  private String missingId;

  @Override
  public Point location() {
    return new Point(tfm.location());
  }

  public int spriteIndex() {
    return spriteIndex;
  }

  public void setSprite(SpriteObject src) {
    this.atlas = src.atlas;
    this.spriteIndex = src.spriteIndex;
  }

  @Override
  public Atlas getAtlas() {
    return atlas;
  }

}
