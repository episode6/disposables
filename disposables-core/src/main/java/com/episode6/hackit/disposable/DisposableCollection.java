package com.episode6.hackit.disposable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * A collection of {@link Disposable}s that implements {@link HasDisposables}.
 */
public class DisposableCollection implements HasDisposables {

  public static DisposableCollection createWith(Disposable... disposables) {
    return createWith(true, disposables);
  }

  public static DisposableCollection createWith(boolean disposeOnFlush, Disposable... disposables) {
    DisposableCollection collection = new DisposableCollection(disposeOnFlush);
    Collections.addAll(collection.mDisposables, disposables);
    return collection;
  }

  private final Collection<Disposable> mDisposables = new ArrayList<>();
  private final boolean mDisposeOnFlush;
  private transient volatile boolean mIsDisposed = false;

  /**
   * Create a new DisposableCollection
   * @param disposeOnFlush Whether this collection should be considered disposed
   *                       after a call to flushDisposed() results in the collection
   *                       being empty.
   */
  public DisposableCollection(boolean disposeOnFlush) {
    mDisposeOnFlush = disposeOnFlush;
  }

  /**
   * Add a new disposable to this object's collection of disposables. For conveinence,
   * the provided disposable is returned back to the caller so that this method may be
   * chained inside other calls.
   * @param disposable The disposable to add
   * @param <T> The type of the disposable being added
   * @return the provided disposable
   *
   * throws an {@link IllegalStateException} if this collection is already disposed
   */
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
  public boolean flushDisposed() {
    if (mIsDisposed) {
      return true;
    }

    synchronized (this)  {
      if (mIsDisposed) {
        return true;
      }

      for (Iterator<Disposable> iterator = mDisposables.iterator(); iterator.hasNext();) {
        if (shouldFlushDisposable(iterator.next())) {
          iterator.remove();
        }
      }

      if (mDisposeOnFlush && mDisposables.isEmpty()) {
        mIsDisposed = true;
        return true;
      }
    }
    return false;
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

  private static boolean shouldFlushDisposable(Disposable disposable) {
    return (disposable instanceof HasDisposables && ((HasDisposables) disposable).flushDisposed()) ||
        (disposable instanceof CheckedDisposable && ((CheckedDisposable) disposable).isDisposed());
  }
}
