package com.episode6.hackit.disposable;

/**
 * An extension of {@link Disposer} that can also report if an object is already disposed.
 */
public interface CheckedDisposer<V> extends Disposer<V> {

  /**
   * @param instance the instance to check the disposable status of
   * @return true if instance is disposed, false otherwise
   */
  boolean isInstanceDisposed(V instance);
}
