package com.episode6.hackit.disposable;

/**
 * Applies to an object that holds references to other disposables
 */
public interface HasDisposables {

  /**
   * Add a new disposable to this object's collection of disposables. For conveinence,
   * the provided disposable is returned back to the caller so that this method may be
   * chained inside other calls.
   * @param disposable The disposable to add
   * @param <T> The type of the disposable being added
   * @return the provided disposable
   */
  <T extends Disposable> T addDisposable(T disposable);

  /**
   * Flush any {@link CheckedDisposable}s that are already disposed from memory.
   */
  void flushDisposed();
}
