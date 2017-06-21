package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.*;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Utility class containing static methods to create pausables
 */
public class Pausables {

  public static PausableManager newManager(Pausable... pausables) {
    return new BasicPausableManager(pausables.length == 0 ? null : Arrays.asList(pausables));
  }

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

  private static class BasicPausableManager extends AbstractDelegateDisposable<List<Pausable>> implements PausableManager, HasDisposables {

    public BasicPausableManager(@Nullable Collection<Pausable> prefill) {
      super(prefill == null ? new LinkedList<Pausable>() : new LinkedList<Pausable>(prefill));
    }

    @Override
    public synchronized void addPausable(Pausable pausable) {
      getDelegateOrThrow().add(pausable);
    }

    @Override
    public synchronized void pause() {
      MaybePausables.pauseList(getDelegateOrThrow());
    }

    @Override
    public synchronized void resume() {
      MaybePausables.resumeList(getDelegateOrThrow());
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
      markDisposed();
    }
  }

  private static class BasicPausableDisposableManager implements PausableDisposableManager {
    private final DisposableManager mDisposableManager = Disposables.newManager();
    private final BasicPausableManager mPausableManager = new BasicPausableManager(null);

    @Override
    public synchronized void addDisposable(Disposable disposable) {
      mDisposableManager.addDisposable(disposable);
      if (disposable instanceof Pausable) {
        mPausableManager.addPausable((Pausable) disposable);
      }
    }

    @Override
    public synchronized void addPausable(Pausable pausable) {
      mPausableManager.addPausable(pausable);
      if (pausable instanceof Disposable) {
        mDisposableManager.addDisposable((Disposable) pausable);
      }
    }

    @Override
    public synchronized void pause() {
      mPausableManager.pause();
    }

    @Override
    public synchronized void resume() {
      mPausableManager.resume();
    }

    @Override
    public synchronized boolean flushDisposed() {
      return mPausableManager.flushDisposed() && mDisposableManager.flushDisposed();
    }

    @Override
    public synchronized void dispose() {
      mPausableManager.dispose();
      mDisposableManager.dispose();
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
