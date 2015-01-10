package com.js.basicTest;

import com.js.basic.Freezable;
import com.js.testUtils.*;
import static com.js.basic.Tools.*;

public class FreezableTest extends MyTestCase {

  private static class Alpha extends Freezable.Mutable {

    public Alpha(int value) {
      mValue = value;
    }

    public int value() {
      return mValue;
    }

    public void setValue(int v) {
      mutate();
      mValue = v;
    }

    public int derived() {
      if (mDerived == 0)
        mDerived = mValue * 2;
      return mDerived;
    }

    @Override
    public Freezable getMutableCopy() {
      return new Alpha(mValue);
    }

    @Override
    public void mutate() {
      super.mutate();
      mDerived = 0;
    }

    private int mValue;
    private int mDerived;
  }

  private abstract static class Beta extends Freezable.Mutable {

    public Beta(int value) {
      mValue = value;
    }

    public int value() {
      return mValue;
    }

    public void setValue(int v) {
      mutate();
      mValue = v;
    }

    public int derived() {
      if (mDerived == 0)
        mDerived = mValue * 2;
      return mDerived;
    }

    @Override
    public void mutate() {
      super.mutate();
      mDerived = 0;
    }

    private int mValue;
    private int mDerived;
  }

  private static class Gamma extends Beta {

    public Gamma(int value) {
      super(value + 4);
    }

    @Override
    public Freezable getMutableCopy() {
      Gamma g = new Gamma(value());
      return g;
    }
  }

  public void testGetFrozenCopy() {
    Alpha a = new Alpha(7);
    a.setValue(8);
    Alpha b = frozen(a);
    assertFalse(a.isFrozen());
    assertTrue(b.isFrozen());
    assertEquals(16, b.derived());
    assertFalse(a == b);
    assertEquals(16, a.derived());
    a.setValue(9);
    assertEquals(18, a.derived());
    assertEquals(16, b.derived());
  }

  public void testGetCopyNotFrozen() {
    Alpha a = new Alpha(7);
    Alpha b = copyOf(a);
    assertFalse(a == b);
  }

  public void testIsFrozen() {
    Alpha a = new Alpha(7);
    assertFalse(a.isFrozen());
    a.freeze();
    assertTrue(a.isFrozen());
  }

  public void testGetCopyAlreadyFrozen() {
    Alpha a = freeze(new Alpha(7));
    Alpha b = copyOf(a);
    assertTrue(a == b);
  }

  public void testGetMutableCopyOfNonFrozen() {
    Alpha a = new Alpha(7);
    Alpha b = mutableCopyOf(a);
    assertFalse(a == b);
    assertFalse(b.isFrozen());
  }

  public void testGetMutableCopyOfFrozen() {
    Alpha a = freeze(new Alpha(7));
    Alpha b = mutableCopyOf(a);
    assertTrue(a.isFrozen());
    assertFalse(a == b);
    assertFalse(b.isFrozen());
    assertTrue(b.isMutable());
  }

  public void testMutateUnfrozen() {
    Alpha a = new Alpha(7);
    assertEquals(7, a.value());
    assertEquals(14, a.derived());
    a.setValue(12);
    assertEquals(24, a.derived());
  }

  public void testMutateFrozen() {
    Alpha a = new Alpha(7);
    assertEquals(7, a.value());
    assertEquals(14, a.derived());
    a.freeze();
    try {
      a.setValue(12);
      fail();
    } catch (Freezable.IllegalMutationException e) {
    }
  }

  public void testMutateMutableCopyOfFrozen() {
    Alpha a = freeze(new Alpha(7));
    Alpha b = mutableCopyOf(a);
    b.setValue(12);
    assertEquals(24, b.derived());
  }

  public void testSubclass() {
    Gamma g = new Gamma(10);
    assertEquals(14, g.value());
    g.setValue(3);
    assertEquals(3, g.value());
    assertEquals(6, g.derived());
  }

}
