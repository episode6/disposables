package com.episode6.hackit.disposable;

/**
 * An object that should be disposed (or cleaned up)
 */
public interface Disposable {

  /**
   * Perform necessary cleanup on this object.
   */
  void dispose();
}
