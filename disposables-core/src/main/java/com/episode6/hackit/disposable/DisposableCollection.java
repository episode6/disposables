package com.episode6.hackit.disposable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * A collection of {@link Disposable}s that implements {@link HasDisposables} and {@link Disposable}
 */
public class DisposableCollection implements HasDisposables, Disposable {

  private final Collection<Disposable> mDisposables = new ArrayList<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Disposable> T addDisposable(T disposable) {
    synchronized (this) {
      mDisposables.add(disposable);
    }
    return disposable;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flushDisposed() {
    Collection<Disposable> disposables;
    synchronized (this)  {
      disposables = new ArrayList<>(mDisposables);
    }

    Collection<CheckedDisposable> alreadyDisposed = new LinkedList<>();
    for (Disposable d : disposables) {
      if (d instanceof CheckedDisposable && ((CheckedDisposable) d).isDisposed()) {
        alreadyDisposed.add((CheckedDisposable) d);
      } else if (d instanceof HasDisposables) {
        ((HasDisposables) d).flushDisposed();
      }
    }

    if (alreadyDisposed.isEmpty()) {
      return;
    }

    synchronized (this) {
      mDisposables.removeAll(alreadyDisposed);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    Collection<Disposable> disposables;
    synchronized (this)  {
      disposables = new ArrayList<>(mDisposables);
      mDisposables.clear();
    }

    for (Disposable d : disposables) {
      d.dispose();
    }
  }
}
