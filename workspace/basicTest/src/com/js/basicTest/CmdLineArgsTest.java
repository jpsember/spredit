package com.js.basicTest;

import com.js.basic.CmdLineArgs;
import com.js.testUtils.*;

public class CmdLineArgsTest extends MyTestCase {

  @Override
  protected void setUp() {
    super.setUp();
    mArgs = new CmdLineArgs();
  }

  private CmdLineArgs mArgs;

  private static String[] split(String argString) {
    String[] result = argString.split("\\s");
    return result;
  }

  public void testStringDefault() {
    mArgs.add("title").def("XXX");
    mArgs.parse(split(""));
    assertEquals("XXX", mArgs.getString("title"));
  }

  public void testStringDefaultOverridden() {
    mArgs.add("title").def("XXX");
    mArgs.parse(split("--title YYY"));
    assertEquals("YYY", mArgs.getString("title"));
  }

  public void testTitleMissing() {
    mArgs.add("title").setString();
    try {
      mArgs.parse(split("--title"));
      fail();
    } catch (CmdLineArgs.Exception e) {
    }
  }

  public void testZeroStrings() {
    mArgs.add("speed").setInt();
    mArgs.add("titles").setString().setArray();
    mArgs.parse(split("--speed 14 --titles"));
    assertEquals(mArgs.getStrings("titles").length, 0);
  }

  public void testMultipleStrings() {
    mArgs.add("speed").setInt();
    mArgs.add("titles").setString().setArray();
    mArgs.parse(split("--titles XXX YYY ZZZ --speed 3"));
    assertEquals(mArgs.getStrings("titles").length, 3);
  }

  public void testIntDefault() {
    mArgs.add("speed").def(42);
    mArgs.parse(split(""));
    assertEquals(42, mArgs.getInt("speed"));
  }

  public void testIntDefaultOverridden() {
    mArgs.add("speed").def(42);
    mArgs.parse(split("--speed 17"));
    assertEquals(17, mArgs.getInt("speed"));
  }

  public void testIntTypeMismatch() {
    mArgs.add("speed").def(42);
    try {
      mArgs.parse(split("--speed fast"));
      fail();
    } catch (CmdLineArgs.Exception e) {
    }
  }

  public void testIntMissing() {
    mArgs.add("speed").def(42);
    mArgs.add("title").def("Moby Dick");
    try {
      mArgs.parse(split("--speed --title XXX"));
      fail();
    } catch (CmdLineArgs.Exception e) {
    }
  }

  public void testZeroInts() {
    mArgs.add("speeds").setInt().setArray();
    mArgs.add("title").def("Moby Dick");
    mArgs.parse(split("--speeds --title XXX"));
    assertEquals(mArgs.getInts("speeds").length, 0);
  }

  public void testMultipleInts() {
    mArgs.add("speeds").setInt().setArray();
    mArgs.add("title").def("Moby Dick");
    mArgs.parse(split("--speeds 3 1 -41 592 6 --title XXX"));
    assertEquals(mArgs.getInts("speeds").length, 5);
  }

  public void testDoubleDefault() {
    mArgs.add("speed").def(42.0);
    mArgs.parse(split(""));
    assertEqualsFloat(42, mArgs.getDouble("speed"));
  }

  public void testDoubleDefaultOverridden() {
    mArgs.add("speed").def(42.0);
    mArgs.parse(split("--speed 17"));
    assertEqualsFloat(17, mArgs.getDouble("speed"));
  }

  public void testDoubleTypeMismatch() {
    mArgs.add("speed").def(42.0);
    try {
      mArgs.parse(split("--speed fast"));
      fail();
    } catch (CmdLineArgs.Exception e) {
    }
  }

  public void testDoubleMissing() {
    mArgs.add("speed").def(42.0);
    mArgs.add("title").def("Moby Dick");
    try {
      mArgs.parse(split("--speed --title XXX"));
      fail();
    } catch (CmdLineArgs.Exception e) {
    }
  }

  public void testZeroDoubles() {
    mArgs.add("speeds").setDouble().setArray();
    mArgs.add("title").def("Moby Dick");
    mArgs.parse(split("--speeds --title XXX"));
    assertEquals(mArgs.getDoubles("speeds").length, 0);
  }

  public void testMultipleDoubles() {
    mArgs.add("speeds").setDouble().setArray();
    mArgs.add("title").def("Moby Dick");
    mArgs.parse(split("--speeds 3 1 -41 592.3 -6e-2 --title XXX"));
    assertEquals(mArgs.getDoubles("speeds").length, 5);
    assertEqualsFloat(-6e-2, mArgs.getDoubles("speeds")[4]);
  }

  public void testExtras() {
    mArgs.add("gravity").setDouble();
    mArgs.add("speeds").setInt().setArray();
    mArgs.add("title").def("Moby Dick");
    mArgs
        .parse(split("16 alpha --speeds 4 12 -3 6 --gravity 32.0 16.3 --title XXX YYY"));
    String[] extras = mArgs.getExtras();
    assertEquals(4, extras.length);
    assertEquals("YYY", extras[3]);
    assertEquals("16", extras[0]);
  }

  public void testNoExtras() {
    mArgs.add("gravity").setDouble();
    mArgs.add("speeds").setInt().setArray();
    mArgs.add("title").def("Moby Dick");
    mArgs.parse(split("--speeds 4 12 -3 6"));
    String[] extras = mArgs.getExtras();
    assertEquals(0, extras.length);
  }

  public void testNoValueSupplied() {
    mArgs.add("speed").def(42.0);
    mArgs.parse(split(""));
    assertFalse(mArgs.hasValue("speed"));
  }

  public void testValueSupplied() {
    mArgs.add("speed").def(42.0);
    mArgs.parse(split("--speed 17"));
    assertTrue(mArgs.hasValue("speed"));
  }

  public void testHelp() {
    mArgs.add("gravity").setDouble();
    mArgs.add("speeds").setInt().setArray();
    mArgs.add("title").def("Moby Dick");
    try {
      mArgs.parse(split("--speeds 4 12 --help -3 6"));
      fail();
    } catch (CmdLineArgs.Exception e) {
    }
  }

  public void testHelpSmall() {
    mArgs.banner("CmdLineArgsTest.java");
    mArgs.add("gravity").setDouble();
    mArgs.add("speeds").setInt().setArray();
    mArgs.add("title").def("Moby Dick");
    try {
      mArgs.parse(split("--speeds 4 12 -h -3 6"));
      fail();
    } catch (CmdLineArgs.Exception e) {
      // System.out.println(e.getMessage());
    }
  }

  public void testShortConflicts() {
    mArgs.banner("CmdLineArgsTest.java");
    mArgs.add("gravity").setDouble();
    mArgs.add("goose");
    mArgs.add("gray");
    mArgs.add("glad");
    mArgs.add("greenwhich");

    // These all have variants of 'g','r' or whatnot
    mArgs.parse(split("-g 17 -G -r -R"));
    assertTrue(mArgs.getExtras().length == 0);
  }

}
