package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.AbstractDelegateDisposable;
import com.episode6.hackit.disposable.Disposable;
import com.episode6.hackit.disposable.Disposer;
import com.episode6.hackit.disposable.MaybeDisposables;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Utility class containing static methods to create pausables
 */
public class Pausables {

  /**
   * Create a new {@link PausableDisposableManager} that manages both {@link Pausable}s
   * and {@link Disposable}s.
   * @return A new (and empty) {@link PausableDisposableManager}
   */
  public static PausableDisposableManager newDisposableManager() {
    return new BasicPausableDisposableManager();
  }

  public static <T> CheckedDisposablePausable weak(T instance, Pauser<T> pauser) {
    return weak(instance, pauser, null);
  }

  public static <T> CheckedDisposablePausable weak(T instance, Pauser<T> pauser, @Nullable Disposer<T> disposer) {
    return new WeakDisposablePausable<>(instance, pauser, disposer);
  }

  public static PausableExecutor queuingExecutor(Executor executor) {
    if (executor instanceof QueuingPausableExecutor) {
      return (PausableExecutor) executor;
    }
    return new QueuingPausableExecutor(executor);
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

  private static class BasicPausableDisposableManager extends AbstractDelegateDisposable<List<Object>> implements
      PausableDisposableManager {
    BasicPausableDisposableManager() {
      super(new LinkedList<Object>());
    }

    @Override
    public void pause() {
      synchronized (this) {
        MaybePausables.pauseList(getDelegateOrThrow());
      }
    }

    @Override
    public void resume() {
      synchronized (this) {
        MaybePausables.resumeList(getDelegateOrThrow());
      }
    }

    @Override
    public void addPausable(Pausable pausable) {
      synchronized (this) {
        getDelegateOrThrow().add(pausable);
      }
    }

    @Override
    public void addDisposable(Disposable disposable) {
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

  private static class QueuingPausableExecutor implements PausableExecutor {

    transient volatile boolean mPaused = false;
    final Executor mDelegate;
    final List<PausableRunnable> mRunnables = new LinkedList<>();

    QueuingPausableExecutor(Executor delegate) {
      mDelegate = delegate;
    }

    @Override
    public void pause() {
      synchronized (this) {
        mPaused = true;
      }
    }

    @Override
    public void resume() {
      List<PausableRunnable> runnables;
      synchronized (this) {
        mPaused = false;

        if (mRunnables.isEmpty()) {
          return;
        }

        runnables = new LinkedList<>(mRunnables);
        mRunnables.clear();
      }
      for (PausableRunnable runnable : runnables) {
        mDelegate.execute(runnable);
      }
    }

    @Override
    public void execute(Runnable command) {
      PausableRunnable pausableRunnable = new PausableRunnable(command);
      synchronized (this) {
        if (mPaused) {
          mRunnables.add(pausableRunnable);
          return;
        }
      }
      mDelegate.execute(pausableRunnable);
    }

    class PausableRunnable implements Runnable {

      final Runnable mRunnable;

      PausableRunnable(Runnable runnable) {
        mRunnable = runnable;
      }

      @Override
      public void run() {
        synchronized (QueuingPausableExecutor.this) {
          if (mPaused) {
            mRunnables.add(PausableRunnable.this);
          } else {
            mRunnable.run();
          }
        }
      }
    }
  }
}
