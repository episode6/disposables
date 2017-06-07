package com.episode6.hackit.disposable;

import java.lang.ref.WeakReference;

/**
 * Utility class containing static methods to create Disposables from non-disposables
 */
public class Disposables {

  public static <T> CheckedDisposable forgetful(T obj) {
    // subclasses of ForgetfulDelegateCheckedDisposable might be adding variables
    // that explicitly need to be forgotten, so check obj.getClass() instead of using
    // obj instanceof.
    return obj.getClass() == ForgetfulDelegateCheckedDisposable.class ?
        (CheckedDisposable) obj :
        new ForgetfulDelegateCheckedDisposable<T>(obj);
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

  private static class SingleUseRunnable extends ForgetfulDelegateCheckedDisposable<Runnable> implements DisposableRunnable {

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
  }
}
