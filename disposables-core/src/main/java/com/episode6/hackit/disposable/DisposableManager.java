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
  void addDisposable(Disposable disposable);
}
