package apputil;

import static scanning.IBasic.*;
import java.io.*;

import com.js.geometry.IPoint;
import com.js.geometry.Point;
import com.js.geometry.Rect;

import scanning.*;
import static com.js.basic.Tools.*;

public class DefScanner extends TextScanner {

  public DefScanner(File f) throws FileNotFoundException {
    this(new FileReader(f), f.toString());
  }

  public DefScanner(Reader r, String description) {
    super(r, description, dfa(), WS);
  }

  public DefScanner(String s) {
    this(new StringReader(s), s);
  }
  private static DFA dfa;

  public static DFA dfa() {
    if (dfa == null) {
      dfa = DFA.readFromSet(DFA.class, "basic.dfa");
    }
    return dfa;
  }

  public boolean doneLine() {
    skipWS();
    return this.eof() || peek().id(CR);
  }

  /**
   * Read a line, trimming any crs.
   * @return String, or null if no characters remain in reader
   */
  public String readLine() {
    DefBuilder sb = new DefBuilder();
    while (!eof() && !peek(CR)) {
      sb.append(read().text());
    }
    return sb.toString();
  }

  public String nextDef() {
    String ret = null;
    if (!done()) {
      if (!peek(ID)) {
        this.exception("unexpected token");
      }
      ret = read().text();
    }
    return ret;
  }

  public String sLabel() {
    String s = AppTools.labelToString(read(LBL).text());
    return s;
  }

  public boolean readTag(String tag) {
    boolean read = false;
    if (peek().text().equals(tag)) {
      read = true;
      read();
    }
    return read;
  }

  public boolean done() {
    while (true) {
      skipWS();
      if (!readIf(CR))
        break;
    }
    return eof();
  }
  public void adv() {
    while (!eof() && !peek(CR))
      read();
  }
  public void skipWS() {
    skipWS(false);
  }

  public void skipWS(boolean includeCR) {
    while (!eof() && (readIf(WS) || (includeCR && readIf(CR))))
      ;
  }
  public boolean sBool() {
    skipWS();
    return read(BOOL).text().equals("T");
  }

  public String sId() {
    skipWS();
    int type = peek().id();
    if (type != BOOL)
      type = ID;
    return read(type).text();
  }
  public int sInt() {
    skipWS();
    if (peek(DBL)) {
      float f = sFloat();
      warning("expected int, got float: " + this.last().context());
      return Math.round(f);
    }

    Token t = read(INT);
    return (int) Integer.parseInt(t.text());
  }
  public float sFloat() {
    skipWS();
    Token t = peek();
    if (!(t.id(DBL) || t.id(INT))) {
      read(DBL);
    }
    read();

    return (float) Double.parseDouble(t.text());
  }

  public Point sFPt() {
    return new Point(sFloat(), sFloat());
  }

  public Rect sRect() {
    return new Rect(sFloat(), sFloat(), sFloat(), sFloat());
  }

  public IPoint sIPt() {
    return new IPoint(sInt(), sInt());
  }

  /**
   * @param base
   * @return
   */
  public File sPath(File base) {

    return new RelPath(base, sLabel()).file();
  }

}
