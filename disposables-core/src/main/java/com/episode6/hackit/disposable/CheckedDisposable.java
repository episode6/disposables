package com.episode6.hackit.disposable;

/**
 * A type of {@link Disposable} that can also report when it's been disposed
 */
public interface CheckedDisposable extends Disposable {

  /**
   * @return true if this object is already disposed, false otherwise
   */
  boolean isDisposed();
}
