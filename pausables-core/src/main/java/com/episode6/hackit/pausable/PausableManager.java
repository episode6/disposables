package com.episode6.hackit.pausable;

/**
 * Interface for a manager of a collection of pausables.
 */
public interface PausableManager extends Pausable {

  /**
   * Add a pausable to this manager so that calls to pause and resume
   * may be propogated to it.
   * @param pausable The pausable to add.
   */
  void addPausable(Pausable pausable);
}
