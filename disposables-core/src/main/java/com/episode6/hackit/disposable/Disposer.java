package com.episode6.hackit.disposable;

/**
 * An object that can "dispose" a specific type of object. This interface is used
 * to convert a non-disposable object into a disposable one.
 */
public interface Disposer<V> {

  /**
   * Performs necessary clean up of instance
   * @param instance The instance of V to clean up/dispose of
   */
  void disposeInstance(V instance);
}
