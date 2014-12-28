package com.js.spredit;

import com.js.geometry.IPoint;

//import tex.*;

public class AEnt {

  public AEnt(SpriteInfo si, String id) {
    this.si = si;
    this.id = id;
    // this.resolution = 1;
    // this.origRes = si.sprite().compressionFactor();
  }

  // public void setResolution(float r) {
  // if (r >= 0) {
  // // resolution = r;
  // Sprite s = si.sprite();
  // // s.setCompression(origRes * resolution);
  // flushLazy();
  // }
  //
  // }
  // private void flushLazy() {
  // si.releaseImage();
  // // compImageSize = null;
  // // compCP = null;
  // }

  public void setLocation(IPoint loc) {
    this.loc = loc;
  }
  private void lazy() {
    // BufferedImage img =
    si.getCompiledImage();
    // this.compImageSize = new Point(ImgUtil.size(img));
    // this.compCP = si.compressedCenterPoint();
  }
  public SpriteInfo si() {
    lazy();
    return si;
  }

  // public Point compImageSize() {
  // lazy();
  // return compImageSize;
  // }
  //
  // public Point compCP() {
  // lazy();
  // return compCP;
  // }

  public IPoint loc() {
    lazy();
    return loc;
  }

  public IPoint size() {
    lazy();
    return si().cropRect().size();
  }

  public String id() {
    return id;
  }

  private IPoint loc;
  private SpriteInfo si;
  // private Point compImageSize;
  // private Point compCP;
  private String id;
  // private float resolution;
  // private float origRes;
}
