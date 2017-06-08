package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.Disposable;
import com.episode6.hackit.disposable.HasDisposables;

import java.util.Collection;

/**
 * Interface for a manager of a collection of pausables.
 */
public interface PausableManager extends HasDisposables, Pausable {

  /**
   * Add a pausable to this manager, instructing the manager to pass down its calls
   * to {@link #pause()}, {@link #resume()} and {@link #dispose()} to the new pausable.
   * @param pausable The pausable to add,
   */
  void add(Pausable pausable);

  /**
   * Add a collection of pausables to this manager, instructing the manager to pass down
   * its calls to {@link #pause()}, {@link #resume()} and {@link #dispose()} to all
   * disposables in the provided collection.
   * @param pausables a collection containing pausables to add.
   */
  void addAll(Collection<Pausable> pausables);
}
