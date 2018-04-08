package com.episode6.hackit.pausable.rx;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests {@link PausableScheduler}
 */
public class PausableSchedulerTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Runnable mRunnable;

  Scheduler realScheduler = Schedulers.from(new Executor() {
    @Override
    public void execute(Runnable command) {
      command.run();
    }
  });

  PausableScheduler mPausableScheduler = new PausableScheduler(realScheduler);

  @Test
  public void testUnpausedByDefault() {
    mPausableScheduler.scheduleDirect(mRunnable);

    verify(mRunnable).run();
  }

  @Test
  public void testPauseThenResume() {
    mPausableScheduler.pause();
    mPausableScheduler.scheduleDirect(mRunnable);

    verifyZeroInteractions(mRunnable);

    mPausableScheduler.resume();

    verify(mRunnable).run();
  }
}
