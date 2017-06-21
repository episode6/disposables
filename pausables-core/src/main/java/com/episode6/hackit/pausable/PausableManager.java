package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.DisposableManager;

/**
 * Interface for a manager of a collection of pausables.
 */
public interface PausableManager extends DisposableManager, Pausable {

  /**
   * Add a pausable to this manager, instructing the manager to pass down its calls
   * to {@link #pause()}, {@link #resume()} and {@link #dispose()} to the new pausable.
   * @param pausable The pausable to add,
   */
  void addPausable(Pausable pausable);
}
