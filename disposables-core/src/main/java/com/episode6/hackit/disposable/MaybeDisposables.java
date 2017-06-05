package com.episode6.hackit.disposable;

import javax.annotation.Nullable;

/**
 * Utility methods for dealing with objects that might be disposable.
 */
public class MaybeDisposables {

  public static void dispose(@Nullable Object maybeDisposable) {
    if (maybeDisposable == null) {
      return;
    }
    if (maybeDisposable instanceof Disposable) {
      ((Disposable) maybeDisposable).dispose();
    }
  }

  public static boolean isDisposed(@Nullable Object maybeDisposed) {
    return maybeDisposed == null ||
        maybeDisposed instanceof CheckedDisposable && ((CheckedDisposable) maybeDisposed).isDisposed();
  }

  public static boolean isFlushable(@Nullable Object maybeFlushable) {
    return isDisposed(maybeFlushable) ||
        maybeFlushable instanceof HasDisposables && ((HasDisposables) maybeFlushable).flushDisposed();
  }
}
