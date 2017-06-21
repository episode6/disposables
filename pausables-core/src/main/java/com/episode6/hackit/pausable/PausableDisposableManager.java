package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.DisposableManager;

/**
 * Interface for a manager of a collection of pausables & disposables. Any pausables
 * added to a PausableDisposableManager should also be disposed of / flushed if
 * they happen to implement {@link com.episode6.hackit.disposable.Disposable} or
 * {@link com.episode6.hackit.disposable.CheckedDisposable}. The inverse should
 * also be true.
 */
public interface PausableDisposableManager extends DisposableManager, Pausable {

  /**
   * Add a pausable to this manager, instructing the manager to pass down its calls
   * to {@link #pause()}, {@link #resume()} and {@link #dispose()} (if applicable)
   * to the new pausable.
   * @param pausable The pausable to add,
   */
  void addPausable(Pausable pausable);
}
