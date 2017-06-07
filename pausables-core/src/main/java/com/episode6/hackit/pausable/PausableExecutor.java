package com.episode6.hackit.pausable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * A wrapper for {@link Executor} that implements {@link Pausable}
 */
public class PausableExecutor implements Pausable, Executor {

  public static PausableExecutor wrap(Executor executor) {
    if (executor instanceof PausableExecutor) {
      return (PausableExecutor) executor;
    }
    return new PausableExecutor(executor);
  }

  private transient volatile boolean mPaused = false;
  private final Executor mDelegate;
  private final List<PausableRunnable> mRunnables = new LinkedList<>();

  private PausableExecutor(Executor delegate) {
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

  private class PausableRunnable implements Runnable {

    final Runnable mRunnable;

    private PausableRunnable(Runnable runnable) {
      mRunnable = runnable;
    }

    @Override
    public void run() {
      synchronized (PausableExecutor.this) {
        if (mPaused) {
          mRunnables.add(PausableRunnable.this);
        } else {
          mRunnable.run();
        }
      }
    }
  }
}
