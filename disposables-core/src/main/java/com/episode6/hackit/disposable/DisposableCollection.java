package com.episode6.hackit.disposable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * A collection of {@link Disposable}s that implements {@link HasDisposables}.
 */
public class DisposableCollection extends ForgetfulDisposableCollection<Disposable> {

  /**
   * Creates a {@link DisposableCollection} that can become disposed with a call
   * to {@link #flushDisposed()} if the collection is empty. A flushable collection
   * is intented to be fully populated before being added to a root / unflushable
   * collection.
   *
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
    return new DisposableCollection(disposeOnFlush, disposables.length == 0 ? null : Arrays.asList(disposables));
  }

  private DisposableCollection(boolean disposeOnFlush, @Nullable Collection<Disposable> delegate) {
    super(disposeOnFlush, delegate);
  }
}
