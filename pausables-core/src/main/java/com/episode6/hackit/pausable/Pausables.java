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

  /**
   * Create a standalone {@link PausableManager}. You will have manually call {@link PausableManager#dispose()}
   * and {@link PausableManager#flushDisposed()} on this object to manage its disposal state and memory (or explicitly
   * add it to another disposable collection).
   *
   * Using a standalone pausable manager can be problematic if also using a {@link DisposableManager}
   * in the same component. Since the two objects are completely seperate, you can't ensure all your disposables
   * are torn-down in the same order they're created (instead tearing down all of one collection, then the other).
   *
   * To deal with this issue use {@link #newConnectedManager(DisposableManager, Pausable...)} instead.
   *
   * @param pausables Any pausables you want to prefil this manager with
   * @return A new PausableManager
   */
  public static PausableManager newStandaloneManager(Pausable... pausables) {
    return new StandalonePausableManager(pausables.length == 0 ? null : Arrays.asList(pausables));
  }

  /**
   * Create a {@link PausableManager} that is connected to a {@link DisposableManager}. Any pausables
   * added to the pausable collection will also be added to disposableManager. You should NOT call
   * {@link PausableManager#dispose()} or {@link PausableManager#flushDisposed()} directly on the returned
   * manager, as those calls will propogate when you call {@link DisposableManager#dispose()} and
   * {@link DisposableManager#flushDisposed()}
   *
   * This actually creates a circular reference  where the Pausable Collection both holds a reference to the
   * disposableManager, and is also added directly to it. Both references are removed upon dispose.
   *
   * @param disposableManager The {@link DisposableManager} to connect
   * @param pausables Any pausables you want to prefil this collection with (any disposables will also be added
   *                  to disposableManager
   * @return A new {@link PausableManager} that is connected to disposableManager and included the
   * contents of pausables
   */
  public static PausableManager newConnectedManager(DisposableManager disposableManager, Pausable... pausables) {
    ConnectedPausableManager pausableManager = new ConnectedPausableManager(disposableManager);
    disposableManager.addDisposable(pausableManager);
    if (pausables.length > 0) {
      pausableManager.addAll(pausables);
    }
    return pausableManager;
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

  private static abstract class AbstractPausableManager extends AbstractDelegateDisposable<List<Pausable>> implements PausableManager {

    public AbstractPausableManager(@Nullable Collection<Pausable> prefill) {
      super(prefill == null ? new LinkedList<Pausable>() : new LinkedList<Pausable>(prefill));
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
    public boolean flushDisposed() {
      if (isMarkedDisposed()) {
        return true;
      }

      synchronized (this) {
        MaybeDisposables.flushList(getDelegateOrNull());
        return isMarkedDisposed();
      }
    }
  }

  private static class StandalonePausableManager extends AbstractPausableManager {
    StandalonePausableManager(@Nullable Collection<Pausable> prefill) {
      super(prefill);
    }

    @Override
    public void addPausable(Pausable pausable) {
      synchronized (this) {
        getDelegateOrThrow().add(pausable);
      }
    }

    @Override
    public void dispose() {
      MaybeDisposables.disposeList(markDisposed());
    }
  }

  private static class ConnectedPausableManager extends AbstractPausableManager {
    private DisposableManager mDisposableManager;
    public ConnectedPausableManager(DisposableManager disposableManager) {
      super(null);
      mDisposableManager = disposableManager;
    }

    // internal only method called when creating a ConnectedPausableManager - not syncronized because
    // it may only be used before the ConnectedPausableManager is returned.
    void addAll(Pausable[] pausables) {
      List<Pausable> list = getDelegateOrThrow();
      for (Pausable p : pausables) {
        list.add(p);
        if (p instanceof Disposable) {
          mDisposableManager.addDisposable((Disposable) p);
        }
      }
    }

    @Override
    public void addPausable(Pausable pausable) {
      synchronized (this) {
        getDelegateOrThrow().add(pausable);
        if (pausable instanceof Disposable) {
          mDisposableManager.addDisposable((Disposable) pausable);
        }
      }
    }

    @Override
    public void dispose() {
      List<Pausable> pausables = markDisposed();
      if (pausables != null) {
        pausables.clear();
        mDisposableManager = null;
      }
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
