package com.js.basicTest;

import scanning.CmdArgs;

import com.js.testUtils.*;

public class CmdArgsTest extends MyTestCase {

  private static final String help = "Help message\n" //
      , //
      defaults = " == --help -h" + " !! " + "   ";

  private static final String[] sampleArgs = { "-h" };

  public void testConstructor() {
    CmdArgs ca = new CmdArgs(sampleArgs, defaults, help);
    assertTrue(ca.hasNext());
  }

  public void testMisc() {
    assertTrue(true);
  }

}
