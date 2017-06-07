package com.episode6.hackit.pausable;

/**
 * An object that can pause another object that it does not directly hold a reference to.
 */
public interface Pauser<V> {

  /**
   * Perform pause on an instance of V
   * @param instance the instance to pause
   */
  void pauseInstance(V instance);

  /**
   * Perform resume on an instance of V
   * @param instance the instance to resume
   */
  void resumeInstance(V instance);
}
