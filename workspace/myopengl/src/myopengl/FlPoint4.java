package myopengl;

import base.*;

@Deprecated
public class FlPoint4 {
  public float x, y, z, w;

  public FlPoint4(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = 1;
  }

  public FlPoint4() {
    this.w = 1;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('(');
    sb.append(MyTools.f(x));
    sb.append(MyTools.f(y));
    sb.append(MyTools.f(z));
    sb.append(MyTools.f(w));
    sb.append(')');
    return sb.toString();
  }

}
