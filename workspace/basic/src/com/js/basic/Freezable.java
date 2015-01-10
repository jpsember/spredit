package com.js.basic;

/**
 * <pre>
 * 
 * Interface representing objects that can be frozen to an immutable state
 * 
 * </pre>
 */
public interface Freezable {

  /**
   * Get a copy of this object; if already frozen, returns this
   */
  public <T extends Freezable> T getCopy();

  /**
   * Get a mutable copy of this object
   */
  public <T extends Freezable> T getMutableCopy();

  /**
   * Get a frozen copy of this object; if already frozen, returns this
   */
  public <T extends Freezable> T getFrozenCopy();

  /**
   * Make this object frozen (if not already)
   */
  public void freeze();

  /**
   * Determine if this object is frozen
   */
  public boolean isFrozen();

  /**
   * Prepare for mutating this object; should throw IllegalMutationException if
   * object is frozen
   */
  public void mutate();

  public class IllegalMutationException extends UnsupportedOperationException {
  }

  /**
   * Concrete implementation of the Freezable interface, for objects that can be
   * mutable
   */
  public abstract class Mutable implements Freezable {

    @Override
    public abstract <T extends Freezable> T getMutableCopy();

    @Override
    public <T extends Freezable> T getFrozenCopy() {
      if (mFrozenCopy == null) {
        mFrozenCopy = getMutableCopy();
        mFrozenCopy.freeze();
      }
      return (T) mFrozenCopy;
    }

    @Override
    public void freeze() {
      if (!mFrozen) {
        mFrozen = true;
        mFrozenCopy = this;
      }
    }

    @Override
    public boolean isFrozen() {
      return mFrozen;
    }

    @Override
    public <T extends Freezable> T getCopy() {
      if (isFrozen())
        return (T) this;
      return getMutableCopy();
    }

    @Override
    public void mutate() {
      if (isFrozen())
        throw new IllegalMutationException();
      // Throw out any frozen version of this object prior to mutation
      mFrozenCopy = null;
    }

    public boolean isMutable() {
      return !mFrozen;
    }

    private boolean mFrozen;
    // Frozen version of this object (an optimization)
    private Mutable mFrozenCopy;
  }
}
