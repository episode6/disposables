package com.episode6.hackit.disposable;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class containing static methods to create Disposables.
 */
public class Disposables {

  /**
   * Create a new {@link DisposableManager} to manage disposables created by your component.
   * @param prefillDisposables An disposables to prepopulate the disposable manager with
   * @return the new {@link DisposableManager}
   */
  public static DisposableManager newManager(Disposable... prefillDisposables) {
    return new BasicDisposableManager(prefillDisposables.length > 0 ? Arrays.asList(prefillDisposables) : null);
  }

  /**
   * Create a {@link CheckedDisposable} that holds a {@link WeakReference} to the supplied instance
   * instead of a strong one. Calls to {@link CheckedDisposable#isDisposed()} will check to see if
   * the reference still exists. Calls to {@link Disposable#dispose()} will clear the weak reference
   * after executing your {@link Disposer} (assuming the reference still exists)
   *
   * @param instance The object that needs disposal (will be weakly referenced)
   * @param disposer The {@link Disposer} that can perform disposal on the provided instance
   * @param <T> The type of object being disposed.
   * @return A new {@link CheckedDisposable} that hold a {@link WeakReference} to the supplied instance.
   */
  public static <T> CheckedDisposable weak(T instance, Disposer<T> disposer) {
    return new WeakDisposableComponents<T>(
        instance,
        disposer);
  }

  /**
   * Creates a {@link DisposableRunnable} out of the provided {@link Runnable}. The resulting
   * DisposableRunnable will only allow its delegate to execute once before marking itself disposed.
   * If the runnable is disposed before being executed, the delegate singleUseRunnable will not
   * execute at all.
   *
   * @param runnable The {@link Runnable} to wrap as a disposable.
   * @return A new {@link DisposableRunnable}
   */
  public static DisposableRunnable singleUseRunnable(Runnable runnable) {
    return new SingleUseRunnable(runnable);
  }

  private static class WeakDisposableComponents<V> implements CheckedDisposable {
    final WeakReference<V> instanceRef;
    final Disposer<V> disposer;

    WeakDisposableComponents(
        V instance,
        Disposer<V> disposer) {
      this.instanceRef = new WeakReference<V>(instance);
      this.disposer = disposer;
    }

    @Override
    public boolean isDisposed() {
      return MaybeDisposables.isDisposed(instanceRef.get(), disposer);
    }

    @Override
    public void dispose() {
      MaybeDisposables.dispose(instanceRef.get(), disposer);
      instanceRef.clear();
    }
  }

  private static class SingleUseRunnable extends AbstractDelegateDisposable<Runnable> implements DisposableRunnable {

    SingleUseRunnable(Runnable delegate) {
      super(delegate);
    }

    @Override
    public void run() {
      final Runnable delegate = markDisposed();
      if (delegate != null) {
        delegate.run();
        MaybeDisposables.dispose(delegate);
      }
    }

    @Override
    public void dispose() {
      MaybeDisposables.dispose(markDisposed());
    }

    @Override
    public boolean isDisposed() {
      return MaybeDisposables.isDisposed(getDelegateOrNull());
    }
  }

  private static class BasicDisposableManager extends AbstractDelegateDisposable<List<Disposable>> implements DisposableManager {

    BasicDisposableManager(@Nullable Collection<Disposable> prefill) {
      super(prefill == null ? new LinkedList<Disposable>() : new LinkedList<Disposable>(prefill));
    }

    @Override
    public void add(Disposable disposable) {
      synchronized (this) {
        getDelegateOrThrow().add(disposable);
      }
    }

    @Override
    public boolean flushDisposed() {
      if (isMarkedDisposed()) {
        return true;
      }

      synchronized (this) {
        MaybeDisposables.flushList(getDelegateOrNull());
        return isMarkedDisposed();
      }
    }

    @Override
    public void dispose() {
      MaybeDisposables.disposeList(markDisposed());
    }
  }
 }
