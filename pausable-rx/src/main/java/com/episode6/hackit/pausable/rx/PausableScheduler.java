package com.episode6.hackit.pausable.rx;

import com.episode6.hackit.pausable.Pausable;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * EXPERIMENTAL!
 *
 * A {@link Scheduler} that implements {@link Pausable}.
 */
public class PausableScheduler extends Scheduler implements Pausable {

  private transient volatile boolean mPaused = false;
  private final Scheduler mDelegate;
  private final List<PausableRunnable> mRunnableQueue = new LinkedList<>();

  public PausableScheduler(Scheduler delegate) {
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

      if (mRunnableQueue.isEmpty()) {
        return;
      }

      runnables = new LinkedList<>(mRunnableQueue);
      mRunnableQueue.clear();
    }

    for (PausableRunnable run : runnables) {
      mDelegate.scheduleDirect(run);
    }
  }

  @Override
  public Worker createWorker() {
    return new PausableWorker(mDelegate.createWorker());
  }

  @Override
  public long now(TimeUnit unit) {
    return mDelegate.now(unit);
  }

  @Override
  public void start() {
    mDelegate.start();
  }

  @Override
  public void shutdown() {
    mDelegate.shutdown();
  }

  @Override
  public Disposable scheduleDirect(Runnable run) {
    return mDelegate.scheduleDirect(wrapRunnable(run));
  }

  @Override
  public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
    return mDelegate.scheduleDirect(wrapRunnable(run), delay, unit);
  }

  @Override
  public Disposable schedulePeriodicallyDirect(
      Runnable run, long initialDelay, long period, TimeUnit unit) {
    return mDelegate.schedulePeriodicallyDirect(wrapRunnable(run), initialDelay, period, unit);
  }

  @Override
  public <S extends Scheduler & Disposable> S when(Function<Flowable<Flowable<Completable>>, Completable> combine) {
    return mDelegate.when(combine);
  }

  private Runnable wrapRunnable(Runnable runnable) {
    if (runnable instanceof PausableRunnable) {
      return runnable;
    }
    return new PausableRunnable(runnable);
  }

  class PausableWorker extends Worker {

    private final Worker mDelegateWorker;

    PausableWorker(Worker delegateWorker) {
      mDelegateWorker = delegateWorker;
    }

    @Override
    public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
      return mDelegateWorker.schedule(wrapRunnable(run), delay, unit);
    }

    @Override
    public Disposable schedule(Runnable run) {
      return mDelegateWorker.schedule(wrapRunnable(run));
    }

    @Override
    public Disposable schedulePeriodically(Runnable run, long initialDelay, long period, TimeUnit unit) {
      return mDelegateWorker.schedulePeriodically(wrapRunnable(run), initialDelay, period, unit);
    }

    @Override
    public long now(TimeUnit unit) {
      return mDelegateWorker.now(unit);
    }

    @Override
    public void dispose() {
      mDelegateWorker.dispose();
    }

    @Override
    public boolean isDisposed() {
      return mDelegateWorker.isDisposed();
    }
  }

  class PausableRunnable implements Runnable {

    private final Runnable mDelegate;

    PausableRunnable(Runnable delegate) {
      mDelegate = delegate;
    }

    @Override
    public void run() {
      synchronized (PausableScheduler.this) {
        if (mPaused) {
          mRunnableQueue.add(this);
          return;
        }
      }
      mDelegate.run();
    }
  }
}
