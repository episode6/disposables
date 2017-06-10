package com.episode6.hackit.disposable;

import javax.annotation.Nullable;

/**
 * An implementation of {@link CheckedDisposable} where you can {@link #set(Disposable)} the disposable delegate
 * after-the-fact. If this object is already disposed by the time {@link #set(Disposable)} is called, then dispose()
 * will be called immediately on the provided Disposable.
 */
public class SettableDisposable implements CheckedDisposable {

  public static SettableDisposable create() {
    return new SettableDisposable();
  }

  private transient volatile boolean mIsDisposed = false;
  private boolean mIsSet = false;
  private @Nullable Disposable mDisposable;

  public void set(Disposable disposable) {
    synchronized (this) {
      if (mIsSet) {
        throw new IllegalStateException("Tried to set SettableDisposable that is already set.");
      }
      mIsSet = true;

      if (mIsDisposed) {
        disposable.dispose();
      } else {
        mDisposable = disposable;
      }
    }
  }

  @Override
  public void dispose() {
    if (mIsDisposed) {
      return;
    }

    synchronized (this) {
      if (mIsDisposed) {
        return;
      }
      mIsDisposed = true;
      if (mIsSet) {
        Disposable disposable = mDisposable;
        mDisposable = null;
        MaybeDisposables.dispose(disposable);
      }
    }
  }

  @Override
  public boolean isDisposed() {
    if (mIsDisposed) {
      return true;
    }

    synchronized (this) {
      if (mIsDisposed) {
        return true;
      }
      return mIsSet && MaybeDisposables.isFlushable(mDisposable);
    }
  }
}
