package com.episode6.hackit.disposable.future;

import com.episode6.hackit.disposable.CheckedDisposable;
import com.episode6.hackit.disposable.Disposable;
import com.episode6.hackit.disposable.HasDisposables;
import com.google.common.base.Function;
import com.google.common.util.concurrent.*;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class DisposableFuturesTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock FutureCallback<Boolean> mFutureCallback;
  @Mock Disposable mDisposable;
  @Mock CheckedDisposable mCheckedDisposable;
  @Mock DisposableFuture<Boolean> mMockDisposableFuture;

  SettableFuture<Boolean> mSettableFuture = SettableFuture.create();

  @Test
  public void testDisposableFutureDisposeBeforeComplete() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture);

    Futures.addCallback(disposableFuture, mFutureCallback, MoreExecutors.directExecutor());
    disposableFuture.dispose();
    mSettableFuture.set(true);

    verifyNoMoreInteractions(mFutureCallback);
  }

  @Test
  public void testDisposableFutureCompleteThenFlush() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture);

    Futures.addCallback(disposableFuture, mFutureCallback, MoreExecutors.directExecutor());
    mSettableFuture.set(true);
    boolean isDisposed = disposableFuture.flushDisposed();

    verify(mFutureCallback).onSuccess(true);
    verifyNoMoreInteractions(mFutureCallback);
    assertThat(isDisposed).isTrue();
  }

  @Test
  public void testDisposableFutureFlushThenComplete() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture);

    Futures.addCallback(disposableFuture, mFutureCallback, MoreExecutors.directExecutor());
    boolean isDisposed1 = disposableFuture.flushDisposed();
    mSettableFuture.set(true);
    boolean isDisposed2 = disposableFuture.flushDisposed();

    verify(mFutureCallback).onSuccess(true);
    verifyNoMoreInteractions(mFutureCallback);
    assertThat(isDisposed1).isFalse();
    assertThat(isDisposed2).isTrue();
  }

  @Test
  public void testEmptyDisposableFutureFlush() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture);

    boolean isDisposed = disposableFuture.flushDisposed();

    assertThat(isDisposed).isTrue();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowAfterDispose() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture);

    disposableFuture.dispose();

    disposableFuture.isDone();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowAfterSuccessfulFlush() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture);

    disposableFuture.flushDisposed();

    disposableFuture.isDone();
  }

  @Test
  public void testDisposableFutureWithExtraDisposablesDispose() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture, mDisposable, mCheckedDisposable);

    disposableFuture.dispose();

    verify(mDisposable).dispose();
    verify(mCheckedDisposable).dispose();
    verifyNoMoreInteractions(mDisposable, mCheckedDisposable);
  }

  @Test
  public void testDisposableFutureWithExtraDisposablesFlush() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture, mDisposable, mCheckedDisposable);

    boolean isDisposed = disposableFuture.flushDisposed();

    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mDisposable, mCheckedDisposable);
    assertThat(isDisposed).isFalse();
  }

  @Test
  public void testDisposableFutureWithExtraDisposablesSuccessfulFlush() {
    when(mCheckedDisposable.isDisposed()).thenReturn(true);
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mSettableFuture, mCheckedDisposable);
    Futures.addCallback(disposableFuture, mFutureCallback, MoreExecutors.directExecutor());

    mSettableFuture.set(true);
    boolean isDisposed = disposableFuture.flushDisposed();

    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable);
    assertThat(isDisposed).isTrue();
  }

  @Test
  public void testWrapNonStandardDisposableFutureFlush() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mMockDisposableFuture);

    boolean isDisposed = disposableFuture.flushDisposed();

    verify(mMockDisposableFuture).flushDisposed();
    verifyNoMoreInteractions(mMockDisposableFuture);
    assertThat(isDisposed).isFalse();
  }

  @Test
  public void testDontWrapNonStandardDisposableFuture() {
    when(mMockDisposableFuture.flushDisposed()).thenReturn(true);

    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mMockDisposableFuture);

    assertThat(disposableFuture).isEqualTo(mMockDisposableFuture);
  }

  @Test
  public void testImmediateFutureWithCallback() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.immediateFuture(true);

    Futures.addCallback(disposableFuture, mFutureCallback, MoreExecutors.directExecutor());
    boolean isDisposed = disposableFuture.flushDisposed();

    verify(mFutureCallback).onSuccess(true);
    verifyNoMoreInteractions(mFutureCallback);
    assertThat(isDisposed).isTrue();
  }

  @Test
  public void testImmediateFutureNoCallback() {
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.immediateFuture(true);

    boolean isDisposed = disposableFuture.flushDisposed();

    assertThat(isDisposed).isTrue();
  }

  @Test
  public void testAddCallbackMethod() {
    HasDisposables hasDisposables = DisposableFutures.addCallback(
        mSettableFuture,
        mFutureCallback,
        MoreExecutors.directExecutor());

    boolean isDisposed1 = hasDisposables.flushDisposed();
    mSettableFuture.set(true);
    boolean isDisposed2 = hasDisposables.flushDisposed();

    verify(mFutureCallback).onSuccess(true);
    assertThat(isDisposed1).isFalse();
    assertThat(isDisposed2).isTrue();
  }

  @Test
  public void testFutureTransform() throws ExecutionException, InterruptedException {
    DisposableFuture<Integer> intFuture = DisposableFutures.transformAndWrap(
        mSettableFuture,
        transformFunction(),
        MoreExecutors.directExecutor());

    assertThat(intFuture.flushDisposed()).isFalse();

    mSettableFuture.set(true);

    assertThat(intFuture.get()).isEqualTo(1);

    assertThat(intFuture.flushDisposed()).isTrue();
  }

  @Test
  public void testFutureAsyncTransform() throws ExecutionException, InterruptedException {
    DisposableFuture<Integer> intFuture = DisposableFutures.transformAsyncAndWrap(
        mSettableFuture,
        asyncTransformFunction(),
        MoreExecutors.directExecutor());

    assertThat(intFuture.flushDisposed()).isFalse();

    mSettableFuture.set(true);

    assertThat(intFuture.get()).isEqualTo(1);

    assertThat(intFuture.flushDisposed()).isTrue();
  }

  @Test
  public void testFutureDoesntTransformIfDisposed() {
    final DisposableFuture<Integer> intFuture = DisposableFutures.transformAndWrap(
        mSettableFuture,
        transformFunction(),
        MoreExecutors.directExecutor());
    Futures.addCallback(
        intFuture,
        failingCallback(),
        MoreExecutors.directExecutor());

    assertThat(intFuture.flushDisposed()).isFalse();
    intFuture.dispose();
    mSettableFuture.set(true);
    expectException(IllegalStateException.class, new ThrowRunnable() {
      @Override
      public void run() throws Throwable {
        intFuture.get();
      }
    });
  }

  @Test
  public void testFutureDoesntAsyncTransformIfDisposed() {
    final DisposableFuture<Integer> intFuture = DisposableFutures.transformAsyncAndWrap(
        mSettableFuture,
        asyncTransformFunction(),
        MoreExecutors.directExecutor());
    Futures.addCallback(
        intFuture,
        failingCallback(),
        MoreExecutors.directExecutor());

    assertThat(intFuture.flushDisposed()).isFalse();
    intFuture.dispose();
    mSettableFuture.set(true);
    expectException(IllegalStateException.class, new ThrowRunnable() {
      @Override
      public void run() throws Throwable {
        intFuture.get();
      }
    });
  }

  interface ThrowRunnable {
    void run() throws Throwable;
  }

  private static void expectException(Class<? extends Throwable> clazz, ThrowRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable t) {
      if (clazz.isInstance(t)) {
        return;
      }
      fail("Expected throwable: " + clazz + " but instead got: " + t);
    }
    fail("Expected throwable: " + clazz);
  }

  private static Function<Boolean, Integer> transformFunction() {
    return new Function<Boolean, Integer>() {
      @Nullable
      @Override
      public Integer apply(@Nullable Boolean input) {
        return input == null || !input ? 0 : 1;
      }
    };
  }

  private static AsyncFunction<Boolean, Integer> asyncTransformFunction() {
    return new AsyncFunction<Boolean, Integer>() {
      @Override
      public ListenableFuture<Integer> apply(@Nullable Boolean input) throws Exception {
        return Futures.immediateFuture(input == null || !input ? 0 : 1);
      }
    };
  }

  private static <T> FutureCallback<T> failingCallback() {
    return new FutureCallback<T>() {
      @Override
      public void onSuccess(@Nullable T result) {
        fail("callback success should not execute");
      }

      @Override
      public void onFailure(Throwable t) {
        fail("callback failure should not execute", t);
      }
    };
  }
}
