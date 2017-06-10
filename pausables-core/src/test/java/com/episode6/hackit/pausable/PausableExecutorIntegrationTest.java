package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.future.DisposableFuture;
import com.episode6.hackit.disposable.future.DisposableFutures;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests {@link PausableExecutor} + {@link DisposableFuture}
 */
public class PausableExecutorIntegrationTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  final SettableFuture<Boolean> mSettableFuture = SettableFuture.create();
  final DisposableFuture<Boolean> mDisposableFuture = DisposableFutures.wrap(mSettableFuture);
  final PausableExecutor mPausableExecutor = Pausables.queuingExecutor(MoreExecutors.directExecutor());

  @Mock FutureCallback<Boolean> mFutureCallback;

  @Test
  public void testNormalCase() {
    Futures.addCallback(mDisposableFuture, mFutureCallback, mPausableExecutor);
    mSettableFuture.set(true);

    verify(mFutureCallback).onSuccess(true);
  }

  @Test
  public void testPauseResume() {
    Futures.addCallback(mDisposableFuture, mFutureCallback, mPausableExecutor);
    mPausableExecutor.pause();
    mSettableFuture.set(true);

    verifyNoMoreInteractions(mFutureCallback);

    mPausableExecutor.resume();

    verify(mFutureCallback).onSuccess(true);
  }

  @Test
  public void testDispose() {
    Futures.addCallback(mDisposableFuture, mFutureCallback, mPausableExecutor);
    mDisposableFuture.dispose();
    mSettableFuture.set(true);

    verifyNoMoreInteractions(mFutureCallback);
  }
}
