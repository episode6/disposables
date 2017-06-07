package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.Disposer;
import com.episode6.hackit.disposable.MaybeDisposables;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * Utility class containing static methods to create pausables
 */
public class Pausables {

  public static <T> CheckedDisposablePausable weak(T instance, Pauser<T> pauser) {
    return weak(instance, pauser, null);
  }

  public static <T> CheckedDisposablePausable weak(T instance, Pauser<T> pauser, @Nullable Disposer<T> disposer) {
    return new WeakDisposablePausable<>(instance, pauser, disposer);
  }

  private static class WeakDisposablePausable<V> implements CheckedDisposablePausable {

    final WeakReference<V> mWeakReference;
    final Pauser<V> mPauser;
    final @Nullable Disposer<V> mDisposer;

    private WeakDisposablePausable(
        V instance,
        Pauser<V> pauser,
        @Nullable Disposer<V> disposer) {
      mWeakReference = new WeakReference<V>(instance);
      mPauser = pauser;
      mDisposer = disposer;
    }

    @Override
    public void pause() {
      final V instance = mWeakReference.get();
      if (!MaybeDisposables.isDisposed(instance, mDisposer)) {
        MaybePausables.pause(instance, mPauser);
      }
    }

    @Override
    public void resume() {
      final V instance = mWeakReference.get();
      if (!MaybeDisposables.isDisposed(instance, mDisposer)) {
        MaybePausables.resume(instance, mPauser);
      }
    }

    @Override
    public void dispose() {
      MaybeDisposables.dispose(mWeakReference.get(), mDisposer);
      mWeakReference.clear();
    }

    @Override
    public boolean isDisposed() {
      return MaybeDisposables.isDisposed(mWeakReference.get(), mDisposer);
    }
  }
}
