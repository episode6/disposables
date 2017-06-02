package com.episode6.hackit.disposable;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * Utility class containing static methods to create Disposables from non-disposables
 */
public class Disposables {

  /**
   * Creates a {@link Disposable} out of a generic object and a {@link Disposer} for that
   * object.
   *
   * If wrapping an object that already implements {@link Disposable}, it is not necessary to
   * call dispose() from your Disposer, dispose() will be called automatically after your Disposer
   * executes.
   *
   * If the provided instance implements {@link CheckedDisposable}, this method will also
   * return a {@link CheckedDisposable}
   *
   * @param instance The object that needs disposal
   * @param disposer The {@link Disposer} that can perform disposal on the provided instance
   * @param <T> The type of object being disposed.
   * @return A new {@link Disposable} that will pass the provided instance
   * to {@link Disposer#disposeInstance(Object)} when {@link Disposable#dispose()} is called
   */
  @SuppressWarnings("unchecked")
  public static <T> Disposable create(T instance, Disposer<T> disposer) {
    if (instance instanceof CheckedDisposable) {
      return createChecked(instance, disposer, new CheckedDisposableChecker(null));
    }
    return new DelegateDisposable<>(
        new DisposableComponents<T>(
            instance,
            disposer));
  }

  /**
   * Creates a {@link CheckedDisposable} out of a generic object, and a {@link Disposer}
   * and {@link DisposeChecker} for that object.
   *
   * In this case it's guaranteed that {@link DisposeChecker#isInstanceDisposed(Object)}
   * will be called before {@link Disposer#disposeInstance(Object)}
   *
   * If wrapping an object that already implements {@link Disposable} or {@link CheckedDisposable},
   * it is not necessary to call dispose() or isDisposed() from your Disposer/DisposeChecker. They
   * will be called automatically after your Disposer/DisposeChecker has executed.
   *
   * @param instance The object that needs disposal
   * @param disposer The {@link Disposer} that can perform disposal on the provided instance
   * @param disposeChecker The {@link DisposeChecker} that can check if the object is already disposed.
   * @param <T> The type of object being disposed.
   * @return A new {@link CheckedDisposable} that will pass the provided instance to
   * {@link DisposeChecker#isInstanceDisposed(Object)} and {@link Disposer#disposeInstance(Object)}
   */
  public static <T> CheckedDisposable createChecked(T instance, Disposer<T> disposer, DisposeChecker<T> disposeChecker) {
    return new DelegateCheckedDisposable<>(
        new CheckedDisposableComponents<T>(
            instance,
            disposer,
            disposeChecker));
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
  public static <T> CheckedDisposable createWeak(T instance, Disposer<T> disposer) {
    return new DelegateCheckedDisposable<>(
        new WeakDisposableComponents<T>(
            instance,
            disposer,
            null));
  }

  /**
   * Create a {@link CheckedDisposable} that holds a {@link WeakReference} to the supplied instance
   * instead of a strong one. Calls to {@link CheckedDisposable#isDisposed()} will check to see if
   * the reference still exists and only call your {@link DisposeChecker} if it does. Calls
   * to {@link Disposable#dispose()} will clear the weak reference after executing your {@link Disposer}
   * (again, assuming the reference still exists)
   *
   * @param instance The object that needs disposal (will be weakly referenced)
   * @param disposer The {@link Disposer} that can perform disposal on the provided instance
   * @param disposeChecker The {@link DisposeChecker} that can check if the object is already disposed.
   * @param <T> The type of object being disposed.
   * @return A new {@link CheckedDisposable} that hold a {@link WeakReference} to the supplied instance.
   */
  public static <T> CheckedDisposable createWeak(T instance, Disposer<T> disposer, DisposeChecker<T> disposeChecker) {
    return new DelegateCheckedDisposable<>(
        new WeakDisposableComponents<T>(
            instance,
            disposer,
            disposeChecker));
  }

  public static DisposableRunnable createRunnable(Runnable runnable) {
    if (runnable instanceof DisposableRunnable) {
      return (DisposableRunnable) runnable;
    }
    return new DelegateDisposableRunnable(runnable);
  }

  @SuppressWarnings("unchecked")
  private static <T> Disposer<T> maybeWrapDisposer(T instance, Disposer<T> disposer) {
    if (instance instanceof Disposable && !(disposer instanceof DisposableDisposer)) {
      return new DisposableDisposer(disposer);
    }
    return disposer;
  }

  @SuppressWarnings("unchecked")
  private static <T> DisposeChecker<T> maybeWrapDisposeChecker(T instance, @Nullable DisposeChecker<T> disposeChecker) {
    if (instance instanceof CheckedDisposable && !(disposeChecker instanceof CheckedDisposableChecker)) {
      return new CheckedDisposableChecker(disposeChecker);
    }
    return disposeChecker;
  }

  private static class DisposableComponents<V> implements Disposable {
    final V instance;
    final Disposer<V> disposer;

    DisposableComponents(
        V instance,
        Disposer<V> disposer) {
      this.instance = instance;
      this.disposer = maybeWrapDisposer(instance, disposer);
    }

    @Override
    public void dispose() {
      disposer.disposeInstance(instance);
    }
  }

  private static class CheckedDisposableComponents<V> extends DisposableComponents<V> implements CheckedDisposable {
    final DisposeChecker<V> disposeChecker;

    CheckedDisposableComponents(
        V instance,
        Disposer<V> disposer,
        DisposeChecker<V> disposeChecker) {
      super(instance, disposer);
      this.disposeChecker = maybeWrapDisposeChecker(instance, disposeChecker);
    }

    @Override
    public void dispose() {
      if (isDisposed()) {
        return;
      }
      super.dispose();
    }

    @Override
    public boolean isDisposed() {
      return disposeChecker.isInstanceDisposed(instance);
    }
  }

  private static class WeakDisposableComponents<V> implements CheckedDisposable {
    final WeakReference<V> instanceRef;
    final Disposer<V> disposer;
    @Nullable final DisposeChecker<V> disposeChecker;

    WeakDisposableComponents(
        V instance,
        Disposer<V> disposer,
        DisposeChecker<V> disposeChecker) {
      this.instanceRef = new WeakReference<V>(instance);
      this.disposer = maybeWrapDisposer(instance, disposer);
      this.disposeChecker = maybeWrapDisposeChecker(instance, disposeChecker);
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
      return instance == null || (disposeChecker != null && disposeChecker.isInstanceDisposed(instance));
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

  private static class CheckedDisposableChecker<V extends CheckedDisposable> implements DisposeChecker<V> {

    final @Nullable DisposeChecker<V> mDelegateDisposeChecker;

    CheckedDisposableChecker(@Nullable DisposeChecker<V> delegateDisposeChecker) {
      mDelegateDisposeChecker = delegateDisposeChecker;
    }

    @Override
    public boolean isInstanceDisposed(V instance) {
      if (mDelegateDisposeChecker != null) {
        return mDelegateDisposeChecker.isInstanceDisposed(instance) && instance.isDisposed();
      }
      return instance.isDisposed();
    }
  }

  private static class DelegateDisposableRunnable extends DelegateCheckedDisposable<Runnable> implements DisposableRunnable {

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
