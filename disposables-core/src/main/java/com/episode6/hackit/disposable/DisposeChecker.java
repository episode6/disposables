package com.episode6.hackit.disposable;

/**
 * An object that can check if a specific type of object is "disposed" or not.
 * Used to convert a non-disposable object into a {@link CheckedDisposable}
 */
public interface DisposeChecker<V> {

  /**
   * @param instance the instance to check the disposable status of
   * @return true if instance is disposed, false otherwise
   */
  boolean isInstanceDisposed(V instance);
}
