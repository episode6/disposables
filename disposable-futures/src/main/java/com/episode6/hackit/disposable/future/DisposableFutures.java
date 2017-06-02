package com.episode6.hackit.disposable.future;

import com.episode6.hackit.disposable.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility methods for dealing with {@link DisposableFuture}s
 */
public class DisposableFutures {

  /**
   * Wrap the supplied future in a {@link DisposableFuture}. Any included disposables are
   * added to the DisposableFuture's internal collection of disposables.
   * @param future The future to wrap
   * @param disposables {@link Disposable}s to be included in the DisposableFuture
   * @param <T> The type of future being wrapped
   * @return a new {@link DisposableFuture}
   */
  public static <T> DisposableFuture<T> wrap(ListenableFuture<T> future, Disposable... disposables) {
    if (future instanceof DelegateDisposableFuture) {
      if (disposables.length > 0) {
        ((DelegateDisposableFuture) future).mDisposables.addAll(disposables);
      }
      return (DisposableFuture<T>) future;
    }
    return new DelegateDisposableFuture<T>(
        future,
        DisposableCollection.createFlushable(disposables));
  }

  /**
   * Convenience method: Creates a {@link DisposableFuture} with its value set immediately
   * upon construction
   * @param instance The instance to set on the resulting future
   * @param <T> The type of future to create
   * @return A new {@link DisposableFuture} with its value set
   */
  public static <T> DisposableFuture<T> immediateFuture(T instance) {
    return wrap(Futures.immediateFuture(instance));
  }

  /**
   * Convenience method: wraps the supplied future in a DisposableFuture (if necessary),
   * adds the supplied callback to the DisposableFuture, and returns the future cast as
   * {@link HasDisposables}. This can simplify the case of adding a single (disposable)
   * callback to the future, and adding the disposable to a collection in a single code-block
   * @param future The future you want to add a callback to
   * @param callback The callback to add
   * @param executor The executor on which the callback should be called
   * @param <T> The type of future.
   * @return An instance of {@link HasDisposables} that represents the future and its callbacks
   */
  public static <T> HasDisposables addCallback(ListenableFuture<T> future, FutureCallback<T> callback, Executor executor) {
    DisposableFuture<T> disposableFuture = wrap(future);
    Futures.addCallback(disposableFuture, callback, executor);
    return disposableFuture;
  }

  private static class DelegateDisposableFuture<V> extends DelegateDisposable<ListenableFuture<V>> implements DisposableFuture<V> {

    private final DisposableCollection mDisposables;

    DelegateDisposableFuture(ListenableFuture<V> delegate, DisposableCollection collection) {
      super(delegate);
      mDisposables = collection;
    }

    @Override
    public boolean flushDisposed() {
      if (mDisposables.flushDisposed() && flushObjectIfNeeded(getDelegate())) {
        dispose();
        return true;
      }
      return false;
    }

    @Override
    public void dispose() {
      super.dispose();
      mDisposables.dispose();
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
      getDelegateOrThrow().addListener(
          mDisposables.add(Disposables.createRunnable(listener)),
          executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return getDelegateOrThrow().cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
      return getDelegateOrThrow().isCancelled();
    }

    @Override
    public boolean isDone() {
      return getDelegateOrThrow().isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
      return getDelegateOrThrow().get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return getDelegateOrThrow().get(timeout, unit);
    }

    ListenableFuture<V> getDelegateOrThrow() {
      ListenableFuture<V> delegate = getDelegate();
      if (delegate == null) {
        throw new NullPointerException("Attempting to interact with DisposableFuture after its been disposed.");
      }
      return delegate;
    }

    private static boolean flushObjectIfNeeded(@Nullable Object object) {
      if (object instanceof HasDisposables) {
        return ((HasDisposables) object).flushDisposed();
      }
      return true;
    }
  }
}
