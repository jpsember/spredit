package apputil;

import java.io.*;
import java.util.ArrayList;

import org.json.*;

import streams.*;
import base.*;
import static com.js.basic.Tools.*;

public class ConfigSet {

  private static final boolean db = false;
  private static final boolean SHOW = false;

  public ConfigSet(IConfig c) {
    if (c != null)
      add(c);
  }

  public void add(IConfig c) {
    configs.add(c);
  }

  public String writeTo(File f) throws IOException {
    String s = write();
    if (SHOW)
      warning("showing defaults:\n" + s);

    Streams.writeIfChanged(f, s);
    return s;
  }

  public String write() {
    JSONObject map = new JSONObject();
    try {
      for (IConfig ic : configs)
        ic.writeTo(map);
    } catch (JSONException e) {
      die(e);
    }
    return map.toString();
  }

  public void readFrom(File f) throws IOException {
    String s = "{}";
    if (f.exists()) {
      s = Streams.readTextFile(f.getPath());
    }
    unimp("this checked exception nonsense is really annoying");
    try {
      readFrom(new JSONObject(s));
    } catch (JSONException e) {
      die(e);
    }
  }

  public void readFrom(JSONObject map) throws JSONException {
    for (IConfig ic : configs)
      ic.readFrom(map);
  }

  @Deprecated
  public void readFrom(DefScanner sc) {
    throw new UnsupportedOperationException();
  }

  private ArrayList<IConfig> configs = new ArrayList();
}
