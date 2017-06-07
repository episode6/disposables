package com.episode6.hackit.disposable.future;

import com.episode6.hackit.disposable.*;
import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
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
   * @return a {@link DisposableFuture} with the included disposables attached
   */
  public static <T> DisposableFuture<T> wrap(ListenableFuture<T> future, Disposable... disposables) {
    if (future instanceof DelegateDisposableFuture) {
      ((DelegateDisposableFuture<T>) future).addAll(disposables);
      return (DisposableFuture<T>) future;
    }
    if (future instanceof DisposableFuture && disposables.length == 0) {
      return (DisposableFuture<T>) future;
    }
    return new DelegateDisposableFuture<T>(
        future,
        disposables.length == 0 ? null : Arrays.asList(disposables));
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

  /**
   * Wraps the input ListenableFuture in a {@link DisposableFuture} (if it does not already implement disposable),
   * then applies the transform function and wraps the result in a new {@link DisposableFuture} (attaching the
   * disposable input future in the process).
   * @param input The {@link ListenableFuture} to transform
   * @param transform The {@link Function} that transforms input
   * @param executor The executor to execute the transformation on.
   * @param <I> The input type
   * @param <O> The output type
   * @return A new {@link DisposableFuture} wrapping the transformed future, and with the input disposable attached.
   */
  public static <I, O> DisposableFuture<O> transformAndWrap(
      ListenableFuture<I> input,
      Function<I, O> transform,
      Executor executor) {
    if (input instanceof Disposable) {
      return wrap(
          Futures.transform(input, transform, executor),
          (Disposable)input);
    }
    return transformAndWrap(wrap(input), transform, executor);
  }

  /**
   * Wraps the input ListenableFuture in a {@link DisposableFuture} (if it does not already implement disposable),
   * then applies the transform function and wraps the result in a new {@link DisposableFuture} (attaching the
   * disposable input future in the process).
   * @param input The {@link ListenableFuture} to transform
   * @param transform The {@link AsyncFunction} that transforms input
   * @param executor The executor to execute the transformation on.
   * @param <I> The input type
   * @param <O> The output type
   * @return A new {@link DisposableFuture} wrapping the transformed future, and with the input disposable attached.
   */
  public static <I, O> DisposableFuture<O> transformAsyncAndWrap(
      ListenableFuture<I> input,
      AsyncFunction<I, O> transform,
      Executor executor) {
    if (input instanceof Disposable) {
      return wrap(
          Futures.transformAsync(input, transform, executor),
          (Disposable)input);
    }
    return transformAsyncAndWrap(wrap(input), transform, executor);
  }

  private static class DelegateDisposableFuture<V> extends ForgetfulDisposableCollection<Disposable> implements DisposableFuture<V> {

    private final ListenableFuture<V> mDelegate;

    DelegateDisposableFuture(ListenableFuture<V> delegate, @Nullable Collection<Disposable> disposables) {
      super(true, disposables);
      mDelegate = delegate;
      if (delegate instanceof Disposable) {
        getListOrThrow().add(0, (Disposable) delegate);
      }
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
      DisposableRunnable runnable = Disposables.runnable(listener);
      synchronized (this) {
        getListOrThrow().add(runnable);
        mDelegate.addListener(runnable, executor);
      }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return mDelegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
      return mDelegate.isCancelled();
    }

    @Override
    public boolean isDone() {
      return mDelegate.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
      return mDelegate.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return mDelegate.get(timeout, unit);
    }
  }
}
