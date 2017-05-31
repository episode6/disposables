package com.episode6.hackit.disposable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * A collection of {@link Disposable}s that implements {@link HasDisposables} and {@link Disposable}
 */
public class DisposableCollection implements HasDisposables, Disposable {

  public static DisposableCollection createWith(Disposable... disposables) {
    DisposableCollection collection = new DisposableCollection();
    Collections.addAll(collection.mDisposables, disposables);
    return collection;
  }

  private final Collection<Disposable> mDisposables = new ArrayList<>();
  private transient volatile boolean mIsDisposed = false;

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Disposable> T addDisposable(T disposable) {
    synchronized (this) {
      if (mIsDisposed) {
        throw new IllegalStateException(
            "Tried to add a disposable to a DisposableCollection after collection has" +
                " already been disposed. Disposable added: " + disposable.toString());
      }
      mDisposables.add(disposable);
    }
    return disposable;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flushDisposed() {
    if (mIsDisposed) {
      return;
    }

    synchronized (this)  {
      if (mIsDisposed) {
        return;
      }

      for (Iterator<Disposable> iterator = mDisposables.iterator(); iterator.hasNext();) {
        Disposable disposable = iterator.next();
        if (disposable instanceof HasDisposables) {
          ((HasDisposables) disposable).flushDisposed();
          continue;
        }
        if (disposable instanceof CheckedDisposable && ((CheckedDisposable) disposable).isDisposed()) {
          iterator.remove();
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    if (mIsDisposed) {
      return;
    }

    Collection<Disposable> disposables;
    synchronized (this)  {
      if (mIsDisposed) {
        return;
      }
      mIsDisposed = true;

      disposables = new ArrayList<>(mDisposables);
      mDisposables.clear();
    }

    for (Disposable d : disposables) {
      d.dispose();
    }
  }
}
