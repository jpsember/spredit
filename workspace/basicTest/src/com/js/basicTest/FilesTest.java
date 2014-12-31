package com.js.basicTest;

import java.io.File;

import com.js.basic.Files;
import com.js.testUtils.*;
import static com.js.basic.Tools.*;

public class FilesTest extends MyTestCase {

  public void testHasExtension() {

    doNothing(); // Get rid of unused import warning

    String[] a = { "alpha.txt",//
        "com/alpha.txt",//
        "!com.txt/alpha",//
        "!", //
        "!Volumes/Macintosh HD",//
    };
    for (String s : a) {
      boolean hasExt = true;
      if (s.startsWith("!")) {
        s = s.substring(1);
        hasExt = false;
      }
      assertEquals(hasExt, Files.hasExtension(new File(s)));
    }
  }

  public void testGetExtension() {
    String[] a = { "alpha.txt", "txt",//
        "com/alpha.txt", "txt",//
        "com.txt/alpha", "",//
        "", "", //
        "Volumes/Macintosh HD", "",//
    };
    for (int i = 0; i < a.length; i += 2) {
      String s = a[i];
      String ext = a[i + 1];
      assertEquals(ext, Files.getExtension(new File(s)));
    }
  }

  public void testRemoveExtension() {
    String[] a = {//
    "", "", //
        "foo.txt", "foo",//
        "a/b/c.jpg", "a/b/c",//
        "a/b/c", "a/b/c",//
        "a.b/c", "a.b/c",//
    };
    for (int i = 0; i < a.length; i += 2) {
      String before = a[i];
      String after = a[i + 1];
      assertEquals(new File(after), Files.removeExtension(new File(before)));
    }
  }
}
