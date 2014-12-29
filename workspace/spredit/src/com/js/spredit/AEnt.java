package com.js.spredit;

import com.js.geometry.IPoint;

public class AEnt {

  public AEnt(SpriteInfo si, String id) {
    this.si = si;
    this.id = id;
  }

  public void setLocation(IPoint loc) {
    this.loc = loc;
  }

  private void lazy() {
    si.getCompiledImage();
  }

  public SpriteInfo si() {
    lazy();
    return si;
  }

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
  private String id;
}
