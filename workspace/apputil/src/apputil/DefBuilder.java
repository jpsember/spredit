package apputil;

import com.js.geometry.IPoint;
import com.js.geometry.Point;
import com.js.geometry.Rect;
import static com.js.basic.Tools.*;

public class DefBuilder {

  public String toString() {
    int i = sb.length();
    if (i > 0 && sb.charAt(i - 1) == ' ')
      i--;
    return sb.substring(0, i);
  }

  private void sp() {
    sb.append(' ');
  }

  public void append(RelPath p) {
    appendLabel(p.toString()); // p.path());
  }

  public void append(boolean f) {
    sb.append(f ? "T" : "F");
    sp();
  }

  public void append(Object obj) {
    sb.append(obj);
    sp();
  }

  public void append(IPoint p) {
    sb.append(p.x);
    sp();
    sb.append(p.y);
    sp();
  }

  public void append(int i) {
    sb.append(i);
    sp();
  }

  public void append(float f) {
    String s = d(f);
    if (s.charAt(0) == '*')
      throw new IllegalArgumentException("overflow: " + f);
    s = s.trim();
    sb.append(s);
    sp();
  }

  public void append(Rect r) {
    append(r.x);
    append(r.y);
    append(r.width);
    append(r.height);
  }

  public void append(Point p) {
    append(p.x);
    append(p.y);
  }

  private StringBuilder sb = new StringBuilder();

  public void appendLabel(Object obj) {
    AppTools.stringToLabel(obj.toString(), sb);
    sp();
  }

  public void addCr() {
    if (sb.length() == 0 || sb.charAt(sb.length() - 1) != '\n')
      sb.append('\n');
  }
}
