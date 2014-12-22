package apputil;

import java.io.*;
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
    DefBuilder sb = new DefBuilder();
    for (int i = 0; i < configs.size(); i++) {
      IConfig ic = (IConfig) configs.get(i);
      if (db)
        pr("ConfigSet.write: " + ic);

      ic.writeTo(sb);
      sb.addCr();
    }
    return sb.toString();
  }

  public void readFrom(File f) throws IOException {
    if (db)
      pr("ConfigSeg.readFrom file:" + f);

    String s = "";
    if (f.exists()) {
      s = Streams.readTextFile(f.getPath());
      if (SHOW)
        warning("showing defaults:\n" + s);
    }

    readFrom(new DefScanner(s));
  }

  public void readFrom(DefScanner sc) {
    // final boolean db = false;

    if (db)
      pr("processConfigs");

    while (!sc.done()) {
      String item = sc.nextDef();

      boolean processed = false;

      for (int i = 0; i < configs.size(); i++) {
        IConfig ic = (IConfig) configs.get(i);
        if (db)
          pr("ConfigSet.readFrom: " + ic);
        processed = ic.process(sc, item);
        if (db)
          pr(" processed=" + processed);
        if (processed)
          break;
      }
      if (!processed) {
        pr("*** ignoring unrecognized ConfigSet argument: " + item //+"\n (called from "+stackTrace(3)+")"
        );
        sc.adv();
      }
    }
  }

  private DArray configs = new DArray();
}
