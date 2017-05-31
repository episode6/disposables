package com.episode6.hackit.disposable;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * Utility class containing static methods to create Disposables from non-disposables
 */
public class Disposables {

  public static <T> CheckedDisposable create(T instance, Disposer<T> disposer) {
    return create(instance, disposer, null);
  }

  public static <T> CheckedDisposable create(T instance, Disposer<T> disposer, DisposeChecker<T> disposeChecker) {
    return new DelegateCheckedDisposable<T>(
        instance,
        maybeWrapDisposer(instance, disposer),
        maybeWrapDisposeChecker(instance, disposeChecker));
  }

  public static <T> CheckedDisposable createWeak(T instance, Disposer<T> disposer) {
    return createWeak(instance, disposer, null);
  }

  public static <T> CheckedDisposable createWeak(T instance, Disposer<T> disposer, DisposeChecker<T> disposeChecker) {
    return create(
        WeakRefProvider.create(instance),
        new WeakDisposer<T>(maybeWrapDisposer(instance, disposer)),
        new WeakDisposeChecker<T>(maybeWrapDisposeChecker(instance, disposeChecker)));
  }

  @SuppressWarnings("unchecked")
  private static <T> Disposer<T> maybeWrapDisposer(T instance, Disposer<T> disposer) {
    if (instance instanceof Disposable && !(disposer instanceof DisposableDisposer)) {
      return new DisposableDisposer(disposer);
    }
    return disposer;
  }

  @SuppressWarnings("unchecked")
  private static <T> DisposeChecker<T> maybeWrapDisposeChecker(T instance, DisposeChecker<T> disposeChecker) {
    if (instance instanceof CheckedDisposable && !(disposeChecker instanceof CheckedDisposableChecker)) {
      return new CheckedDisposableChecker(disposeChecker);
    }
    return disposeChecker;
  }

  private static class DelegateCheckedDisposable<V> implements CheckedDisposable {
    transient volatile boolean mIsDisposed;
    @Nullable V mInstance;
    @Nullable Disposer<V> mDisposer;
    @Nullable DisposeChecker<V> mDisposeChecker;

    DelegateCheckedDisposable(V instance, Disposer<V> disposer, @Nullable DisposeChecker<V> disposeChecker) {
      mIsDisposed = false;
      mInstance = instance;
      mDisposer = disposer;
      mDisposeChecker = disposeChecker;
    }

    @Override
    public boolean isDisposed() {
      if (mIsDisposed) {
        return true;
      }

      V instance;
      DisposeChecker<V> disposeChecker;
      synchronized (this) {
        if (mIsDisposed) {
          return true;
        }
        instance = mInstance;
        disposeChecker = mDisposeChecker;
      }
      return instance == null || (disposeChecker != null && disposeChecker.isInstanceDisposed(instance));
    }

    @Override
    public void dispose() {
      if (mIsDisposed) {
        return;
      }

      V instance;
      Disposer<V> disposer;
      DisposeChecker<V> disposeChecker;
      synchronized (this) {
        if (mIsDisposed) {
          return;
        }
        mIsDisposed = true;

        instance = mInstance;
        disposer = mDisposer;
        disposeChecker = mDisposeChecker;
        mInstance = null;
        mDisposer = null;
        mDisposeChecker = null;
      }

      if (instance == null ||
          disposer == null ||
          (disposeChecker != null && disposeChecker.isInstanceDisposed(instance))) {
        return;
      }

      disposer.disposeInstance(instance);
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

    final @Nullable Disposer<V> mDelegateDisposer;

    private DisposableDisposer(@Nullable Disposer<V> delegateDisposer) {
      mDelegateDisposer = delegateDisposer;
    }

    @Override
    public void disposeInstance(V instance) {
      if (mDelegateDisposer != null) {
        mDelegateDisposer.disposeInstance(instance);
      }
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
