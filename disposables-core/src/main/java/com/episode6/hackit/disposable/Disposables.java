package com.episode6.hackit.disposable;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * Utility class containing static methods to create Disposables from non-disposables
 */
public class Disposables {

  @SuppressWarnings("unchecked")
  public static <T> Disposable create(T instance, Disposer<T> disposer) {
    if (instance instanceof CheckedDisposable) {
      return createChecked(instance, disposer, new CheckedDisposableChecker(null));
    }
    return new DelegateDisposable<>(
        new DisposableComponents<T>(
            instance,
            maybeWrapDisposer(instance, disposer)));
  }

  public static <T> CheckedDisposable createChecked(T instance, Disposer<T> disposer, DisposeChecker<T> disposeChecker) {
    return new DelegateCheckedDisposable<>(
        new CheckedDisposableComponents<T>(
            instance,
            maybeWrapDisposer(instance, disposer),
            maybeWrapDisposeChecker(instance, disposeChecker)));
  }

  public static <T> CheckedDisposable createWeak(T instance, Disposer<T> disposer) {
    return new DelegateCheckedDisposable<>(
        new CheckedDisposableComponents<WeakReference<T>>(
            new WeakReference<T>(instance),
            new WeakDisposer<T>(maybeWrapDisposer(instance, disposer)),
            new WeakDisposeChecker<T>(null)));
  }

  public static <T> CheckedDisposable createWeak(T instance, Disposer<T> disposer, DisposeChecker<T> disposeChecker) {
    return new DelegateCheckedDisposable<>(
        new CheckedDisposableComponents<WeakReference<T>>(
            new WeakReference<T>(instance),
            new WeakDisposer<T>(maybeWrapDisposer(instance, disposer)),
            new WeakDisposeChecker<T>(maybeWrapDisposeChecker(instance, disposeChecker))));
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
      this.disposer = disposer;
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
      this.disposeChecker = disposeChecker;
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

  private static class WeakDisposer<V> implements Disposer<WeakReference<V>> {

    final Disposer<V> mDelegateDisposer;

    WeakDisposer(Disposer<V> delegateDisposer) {
      mDelegateDisposer = delegateDisposer;
    }

    @Override
    public void disposeInstance(WeakReference<V> instanceRef) {
      final V instance = instanceRef.get();
      if (instance != null) {
        mDelegateDisposer.disposeInstance(instance);
      }
    }
  }

  private static class WeakDisposeChecker<V> implements DisposeChecker<WeakReference<V>> {

    final @Nullable DisposeChecker<V> mDelegateDisposeChecker;

    WeakDisposeChecker(@Nullable DisposeChecker<V> delegateDisposeChecker) {
      mDelegateDisposeChecker = delegateDisposeChecker;
    }

    @Override
    public boolean isInstanceDisposed(WeakReference<V> instanceRef) {
      final V instance = instanceRef.get();
      return instance == null || (mDelegateDisposeChecker != null && mDelegateDisposeChecker.isInstanceDisposed(instance));
    }
  }

  private static class DisposableDisposer<V extends Disposable> implements Disposer<V> {

    final Disposer<V> mDelegateDisposer;

    private DisposableDisposer(Disposer<V> delegateDisposer) {
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

    private CheckedDisposableChecker(@Nullable DisposeChecker<V> delegateDisposeChecker) {
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
}
