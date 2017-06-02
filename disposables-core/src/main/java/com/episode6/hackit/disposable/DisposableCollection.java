package com.episode6.hackit.disposable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * A collection of {@link Disposable}s that implements {@link HasDisposables}.
 */
public class DisposableCollection implements HasDisposables {

  /**
   * Creates a {@link DisposableCollection} that can become disposed with a call
   * to {@link #flushDisposed()} if collection is empty. A flushable collection
   * is intented to be fully populated before being added to a root / unflushable
   * collection.
   * @param disposables Disposables to add to the returned collection.
   * @return A new flushable {@link DisposableCollection} with the provided disposables
   * included
   */
  public static DisposableCollection createFlushable(Disposable... disposables) {
    return createWith(true, disposables);
  }

  /**
   * Creates a {@link DisposableCollection} that will only become disposed with
   * a direct call to {@link #dispose()}, and hence will never return true from
   * {@link #flushDisposed()} (unless dispose() has already been called).
   *
   * An unflushable collection is generally intended to be the root collection
   * of a lifecycle component, where calls to {@link #flushDisposed()} are
   * used as a kind of garbage-collection to release references to objects that
   * are already disposed.
   *
   * @param disposables Disposables to add to the returned collection.
   * @return A new unflushable {@link DisposableCollection} with the provided
   * disposables included.
   */
  public static DisposableCollection createUnFlushable(Disposable... disposables) {
    return createWith(false, disposables);
  }

  private static DisposableCollection createWith(boolean disposeOnFlush, Disposable... disposables) {
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
  private DisposableCollection(boolean disposeOnFlush) {
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
  public <T extends Disposable> T add(T disposable) {
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

  public void addAll(Disposable... disposables) {
    synchronized (this) {
      if (mIsDisposed) {
        throw new IllegalStateException(
            "Tried to addAll disposables to a DisposableCollection after collection has" +
                " already been disposed");
      }
      Collections.addAll(mDisposables, disposables);
    }
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
