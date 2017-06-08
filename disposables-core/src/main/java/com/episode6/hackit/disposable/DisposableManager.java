package com.episode6.hackit.disposable;

import java.util.Collection;

/**
 * Interface for a manager of a collection of disposables.
 */
public interface DisposableManager extends HasDisposables {

  /**
   * Add a disposable to this manager, instructing the manager to pass down its calls to {@link #dispose()} to
   * the new disposable.
   * @param disposable The disposable to add.
   */
  void add(Disposable disposable);

  /**
   * Add a collection of disposables to this manager, instructing the manager to pass down its calls
   * to {@link #dispose()} to all disposables in the provided collection.
   * @param disposables a collection containing disposable to add.
   */
  void addAll(Collection<Disposable> disposables);
}
