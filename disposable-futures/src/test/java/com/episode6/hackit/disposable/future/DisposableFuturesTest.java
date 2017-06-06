package com.episode6.hackit.disposable.future;

import com.episode6.hackit.disposable.CheckedDisposable;
import com.episode6.hackit.disposable.Disposable;
import com.episode6.hackit.disposable.HasDisposables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.fest.assertions.api.Assertions.assertThat;
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
  public void testWrapNonStandardDisposableFutureSuccessfulFlush() {
    when(mMockDisposableFuture.flushDisposed()).thenReturn(true);
    DisposableFuture<Boolean> disposableFuture = DisposableFutures.wrap(mMockDisposableFuture);

    boolean isDisposed = disposableFuture.flushDisposed();

    verify(mMockDisposableFuture).flushDisposed();
    verify(mMockDisposableFuture).dispose();
    verifyNoMoreInteractions(mMockDisposableFuture);
    assertThat(isDisposed).isTrue();
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
}
