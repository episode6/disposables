package com.episode6.hackit.disposable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.ref.WeakReference;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Tests {@link Disposables}
 */
@SuppressWarnings("unchecked")
@PrepareForTest({Disposables.class}) // creates weak refs
@RunWith(PowerMockRunner.class)
public class DisposablesTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  interface ObjWithCleanup {
    boolean isCleanedUp();
    void cleanup();
  }

  static class ObjDisposer implements Disposer<ObjWithCleanup> {

    @Override
    public void disposeInstance(ObjWithCleanup instance) {
      instance.cleanup();
    }
  }

  static class CheckedObjDisposer extends ObjDisposer implements CheckedDisposer<ObjWithCleanup> {

    @Override
    public boolean isInstanceDisposed(ObjWithCleanup instance) {
      return instance.isCleanedUp();
    }
  }

  interface RunnableWithDispose extends CheckedDisposable, Runnable {}

  @Mock ObjWithCleanup mObjWithCleanup;
  @Mock WeakReference<ObjWithCleanup> mWeakObjWithCleanup;
  @Mock Runnable mRunnable;
  @Mock RunnableWithDispose mRunnableWithDispose;

  @Mock WeakReference<Disposable> mWeakDisposable;
  @Mock WeakReference<CheckedDisposable> mWeakCheckedDisposable;
  @Mock Disposable mMockDisposable;
  @Mock CheckedDisposable mMockCheckedDisposable;

  @Before
  public void setup() throws Exception {
    whenNew(WeakReference.class).withArguments(mObjWithCleanup).thenReturn(mWeakObjWithCleanup);
    whenNew(WeakReference.class).withArguments(mMockDisposable).thenReturn(mWeakDisposable);
    whenNew(WeakReference.class).withArguments(mMockCheckedDisposable).thenReturn(mWeakCheckedDisposable);
  }

  @Test
  public void testWrapRawDisposable() {
    when(mWeakDisposable.get()).thenReturn(mMockDisposable);
    Disposer mockDisposer = mock(Disposer.class);
    Disposable wrapper = Disposables.weak(mMockDisposable, mockDisposer);

    wrapper.dispose();

    verify(mWeakDisposable).get();
    verify(mockDisposer).disposeInstance(mMockDisposable);
    verify(mMockDisposable).dispose();
    verify(mWeakDisposable).clear();
    verifyNoMoreInteractions(mMockDisposable, mockDisposer, mWeakDisposable);
  }

  @Test
  public void testWrapRawCheckedDisposableIsDisposedCheckBoth() {
    when(mWeakCheckedDisposable.get()).thenReturn(mMockCheckedDisposable);
    CheckedDisposer mockDisposer = mock(CheckedDisposer.class);
    when(mockDisposer.isInstanceDisposed(mMockCheckedDisposable)).thenReturn(true);
    CheckedDisposable wrapper = Disposables.weak(mMockCheckedDisposable, mockDisposer);

    boolean isDisposed = wrapper.isDisposed();

    verify(mWeakCheckedDisposable).get();
    verify(mMockCheckedDisposable).isDisposed();
    verify(mockDisposer).isInstanceDisposed(mMockCheckedDisposable);
    verifyNoMoreInteractions(mMockCheckedDisposable, mockDisposer, mWeakCheckedDisposable);
    assertThat(isDisposed).isFalse();
  }

  @Test
  public void testWrapRawCheckedDisposableIsDisposedTrue() {
    when(mWeakCheckedDisposable.get()).thenReturn(mMockCheckedDisposable);
    CheckedDisposer mockDisposer = mock(CheckedDisposer.class);
    when(mockDisposer.isInstanceDisposed(mMockCheckedDisposable)).thenReturn(true);
    when(mMockCheckedDisposable.isDisposed()).thenReturn(true);
    CheckedDisposable wrapper = Disposables.weak(mMockCheckedDisposable, mockDisposer);

    boolean isDisposed = wrapper.isDisposed();

    verify(mWeakCheckedDisposable).get();
    verify(mMockCheckedDisposable).isDisposed();
    verify(mockDisposer).isInstanceDisposed(mMockCheckedDisposable);
    verifyNoMoreInteractions(mMockCheckedDisposable, mockDisposer, mWeakCheckedDisposable);
    assertThat(isDisposed).isTrue();
  }

  @Test
  public void testWrapRawCheckedDisposableDisposed() {
    when(mWeakCheckedDisposable.get()).thenReturn(mMockCheckedDisposable);
    CheckedDisposer mockDisposer = mock(CheckedDisposer.class);
    CheckedDisposable wrapper = Disposables.weak(mMockCheckedDisposable, mockDisposer);

    wrapper.dispose();

    verify(mWeakCheckedDisposable).get();
    verify(mockDisposer).isInstanceDisposed(mMockCheckedDisposable);
    verify(mockDisposer).disposeInstance(mMockCheckedDisposable);
    verify(mMockCheckedDisposable).dispose();
    verify(mWeakCheckedDisposable).clear();
    verifyNoMoreInteractions(mockDisposer, mMockCheckedDisposable, mWeakCheckedDisposable);
  }

  @Test
  public void testWeakDisposableNoRef() {
    CheckedDisposable disposable = Disposables.weak(mObjWithCleanup, new ObjDisposer());

    boolean isDisposed = disposable.isDisposed();

    assertThat(isDisposed).isTrue();
    verify(mWeakObjWithCleanup).get();
    verifyNoMoreInteractions(mWeakObjWithCleanup);
  }

  @Test
  public void testWeakDisposableWithRef() {
    when(mWeakObjWithCleanup.get()).thenReturn(mObjWithCleanup);
    CheckedDisposable disposable = Disposables.weak(mObjWithCleanup, new ObjDisposer());

    boolean isDisposed = disposable.isDisposed();

    assertThat(isDisposed).isFalse();
    verify(mWeakObjWithCleanup).get();
    verifyNoMoreInteractions(mWeakObjWithCleanup);
  }

  @Test
  public void testDisposalOfWeakDisposableWithRef() {
    when(mWeakObjWithCleanup.get()).thenReturn(mObjWithCleanup);
    CheckedDisposable disposable = Disposables.weak(mObjWithCleanup, new ObjDisposer());

    disposable.dispose();

    verify(mWeakObjWithCleanup).get();
    verify(mObjWithCleanup).cleanup();
    verify(mWeakObjWithCleanup).clear();
    verifyNoMoreInteractions(mObjWithCleanup, mWeakObjWithCleanup);
  }

  @Test
  public void testDisposalOfWeakDisposableNoRef() {
    CheckedDisposable disposable = Disposables.weak(mObjWithCleanup, new ObjDisposer());

    disposable.dispose();

    verify(mWeakObjWithCleanup).get();
    verify(mWeakObjWithCleanup).clear();
    verifyNoMoreInteractions(mObjWithCleanup, mWeakObjWithCleanup);
  }

  @Test
  public void testWeakCheckedDisposableNoRef() {
    CheckedDisposable disposable = Disposables.weak(mObjWithCleanup, new CheckedObjDisposer());

    boolean isDisposed = disposable.isDisposed();

    assertThat(isDisposed).isTrue();
    verify(mWeakObjWithCleanup).get();
    verifyNoMoreInteractions(mObjWithCleanup, mWeakObjWithCleanup);
  }

  @Test
  public void testWeakCheckedDisposableWithRef() {
    when(mWeakObjWithCleanup.get()).thenReturn(mObjWithCleanup);
    CheckedDisposable disposable = Disposables.weak(mObjWithCleanup, new CheckedObjDisposer());

    boolean isDisposed = disposable.isDisposed();

    assertThat(isDisposed).isFalse();
    verify(mWeakObjWithCleanup).get();
    verify(mObjWithCleanup).isCleanedUp();
    verifyNoMoreInteractions(mObjWithCleanup, mWeakObjWithCleanup);
  }

  @Test
  public void testDisposalOfWeakCheckdDisposableWithRef() {
    when(mWeakObjWithCleanup.get()).thenReturn(mObjWithCleanup);
    CheckedDisposable disposable = Disposables.weak(mObjWithCleanup, new CheckedObjDisposer());

    disposable.dispose();

    verify(mWeakObjWithCleanup).get();
    verify(mObjWithCleanup).isCleanedUp();
    verify(mObjWithCleanup).cleanup();
    verify(mWeakObjWithCleanup).clear();
    verifyNoMoreInteractions(mObjWithCleanup, mWeakObjWithCleanup);
  }

  @Test
  public void testDisposalOfWeakCheckedDisposableNoRef() {
    CheckedDisposable disposable = Disposables.weak(mObjWithCleanup, new CheckedObjDisposer());

    disposable.dispose();

    verify(mWeakObjWithCleanup).get();
    verify(mWeakObjWithCleanup).clear();
    verifyNoMoreInteractions(mObjWithCleanup, mWeakObjWithCleanup);
  }

  @Test
  public void testRunnableDispose() {
    DisposableRunnable disposableRunnable = Disposables.singleUseRunnable(mRunnable);

    disposableRunnable.dispose();
    disposableRunnable.run();
    disposableRunnable.run();
    disposableRunnable.run();

    verifyNoMoreInteractions(mRunnable);
  }

  @Test
  public void testRunnableRun() {
    DisposableRunnable disposableRunnable = Disposables.singleUseRunnable(mRunnable);

    disposableRunnable.run();
    disposableRunnable.run();
    disposableRunnable.run();

    verify(mRunnable).run();
    verifyNoMoreInteractions(mRunnable);
  }

  @Test
  public void testDisposableRunnableDispose() {
    DisposableRunnable disposableRunnable = Disposables.singleUseRunnable(mRunnableWithDispose);

    disposableRunnable.dispose();
    disposableRunnable.run();
    disposableRunnable.run();
    disposableRunnable.run();

    verify(mRunnableWithDispose).dispose();
    verifyNoMoreInteractions(mRunnableWithDispose);
  }

  @Test
  public void testDisposableRunnableRun() {
    DisposableRunnable disposableRunnable = Disposables.singleUseRunnable(mRunnableWithDispose);

    disposableRunnable.run();
    disposableRunnable.run();
    disposableRunnable.run();

    InOrder inOrder = inOrder(mRunnableWithDispose);
    inOrder.verify(mRunnableWithDispose).run();
    inOrder.verify(mRunnableWithDispose).dispose();
    verifyNoMoreInteractions(mRunnableWithDispose);
  }

  @Test
  public void testDisposableRunnableCheck() {
    DisposableRunnable disposableRunnable = Disposables.singleUseRunnable(mRunnableWithDispose);

    disposableRunnable.isDisposed();

    verify(mRunnableWithDispose).isDisposed();
    verifyNoMoreInteractions(mRunnableWithDispose);
  }

  @Test
  public void testForgetfulWrapIgnored() {
    CheckedDisposable originalDisposable = new ForgetfulDelegateCheckedDisposable<>(mObjWithCleanup);

    CheckedDisposable wrapper = Disposables.forgetful(originalDisposable);

    assertThat(wrapper).isEqualTo(originalDisposable);
  }

  @Test
  public void testForgetfulWrapNotIgnored() {
    CheckedDisposable originalDisposable = new ForgetfulDelegateCheckedDisposable<Object>(mMockCheckedDisposable) {};

    CheckedDisposable wrapper = Disposables.forgetful(originalDisposable);

    assertThat(wrapper).isNotEqualTo(originalDisposable);

    boolean isDisposed = wrapper.isDisposed();

    verify(mMockCheckedDisposable).isDisposed();

    wrapper.dispose();

    verify(mMockCheckedDisposable).isDisposed();
  }
}
