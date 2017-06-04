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
   * If the DisposableRunnable is disposed before being executed, the delegate runnable will not
   * execute at all.
   *
   * @param runnable The {@link Runnable} to wrap as a disposable.
   * @return A new {@link DisposableRunnable}
   */
  public static DisposableRunnable runnable(Runnable runnable) {
    if (runnable instanceof DisposableRunnable) {
      return (DisposableRunnable) runnable;
    }
    return new DelegateDisposableRunnable(runnable);
  }

  @SuppressWarnings("unchecked")
  private static <T> Disposer<T> maybeWrapDisposer(T instance, Disposer<T> disposer) {
    if (instance instanceof CheckedDisposable && !(disposer instanceof CheckedDisposableDisposer)) {
      return new CheckedDisposableDisposer(disposer);
    }
    if (instance instanceof Disposable && !(disposer instanceof DisposableDisposer)) {
      return new DisposableDisposer(disposer);
    }
    return disposer;
  }

  private static class WeakDisposableComponents<V> implements CheckedDisposable {
    final WeakReference<V> instanceRef;
    final Disposer<V> disposer;

    WeakDisposableComponents(
        V instance,
        Disposer<V> disposer) {
      this.instanceRef = new WeakReference<V>(instance);
      this.disposer = maybeWrapDisposer(instance, disposer);
    }

    @Override
    public boolean isDisposed() {
      return isInstanceDisposed(instanceRef.get());
    }

    @Override
    public void dispose() {
      final V instance = instanceRef.get();
      if (isInstanceDisposed(instance)) {
        return;
      }
      disposer.disposeInstance(instance);
      instanceRef.clear();
    }

    boolean isInstanceDisposed(V instance) {
      return instance == null ||
          (disposer instanceof CheckedDisposer && ((CheckedDisposer<V>) disposer).isInstanceDisposed(instance));
    }
  }

  private static class DisposableDisposer<V extends Disposable> implements Disposer<V> {

    final Disposer<V> mDelegateDisposer;

    DisposableDisposer(Disposer<V> delegateDisposer) {
      mDelegateDisposer = delegateDisposer;
    }

    @Override
    public void disposeInstance(V instance) {
      mDelegateDisposer.disposeInstance(instance);
      instance.dispose();
    }
  }

  private static class CheckedDisposableDisposer<V extends CheckedDisposable> extends DisposableDisposer<V> implements CheckedDisposer<V> {

    CheckedDisposableDisposer(Disposer<V> delegateDisposer) {
      super(delegateDisposer);
    }

    @Override
    public boolean isInstanceDisposed(V instance) {
      if (mDelegateDisposer instanceof CheckedDisposer) {
        return ((CheckedDisposer<V>) mDelegateDisposer).isInstanceDisposed(instance) && instance.isDisposed();
      }
      return instance.isDisposed();
    }
  }

  private static class DelegateDisposableRunnable extends ForgetfulDelegateCheckedDisposable<Runnable> implements DisposableRunnable {

    DelegateDisposableRunnable(Runnable delegate) {
      super(delegate);
    }

    @Override
    public void run() {
      final Runnable delegate = markDisposed();
      if (delegate != null) {
        delegate.run();
        disposeObjectIfNeeded(delegate);
      }
    }
  }
}
