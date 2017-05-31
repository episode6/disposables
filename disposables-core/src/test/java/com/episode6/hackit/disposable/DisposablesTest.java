package com.episode6.hackit.disposable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.ref.WeakReference;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Tests {@link Disposables}
 */
@PrepareForTest({WeakRefProvider.class})
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

  static class ObjDisposeChecker implements DisposeChecker<ObjWithCleanup> {

    @Override
    public boolean isInstanceDisposed(ObjWithCleanup instance) {
      return instance.isCleanedUp();
    }
  }

  @Mock ObjWithCleanup mObjWithCleanup;
  @Mock WeakReference<ObjWithCleanup> mWeakReference;

  @Before
  public void setup() {
    mockStatic(WeakRefProvider.class);
    when(WeakRefProvider.create(mObjWithCleanup)).thenReturn(mWeakReference);
  }

  @Test
  public void testSimpleDisposable() {
    CheckedDisposable disposable = Disposables.create(mObjWithCleanup, new ObjDisposer());

    disposable.dispose();

    verify(mObjWithCleanup).cleanup();
    verifyNoMoreInteractions(mObjWithCleanup);
  }

  @Test
  public void testDisposalCheck() {
    when(mObjWithCleanup.isCleanedUp()).thenReturn(true);
    CheckedDisposable disposable = Disposables.create(mObjWithCleanup, new ObjDisposer(), new ObjDisposeChecker());

    boolean isDisposed = disposable.isDisposed();

    verify(mObjWithCleanup).isCleanedUp();
    verifyNoMoreInteractions(mObjWithCleanup);
    assertThat(isDisposed).isTrue();
  }

  @Test
  public void testDisposalCheckOnDisposeWhenAlreadyDisposed() {
    when(mObjWithCleanup.isCleanedUp()).thenReturn(true);
    CheckedDisposable disposable = Disposables.create(mObjWithCleanup, new ObjDisposer(), new ObjDisposeChecker());

    disposable.dispose();

    verify(mObjWithCleanup).isCleanedUp();
    verifyNoMoreInteractions(mObjWithCleanup);
  }

  @Test
  public void testDisposalCheckOnDisposeWhenNotDisposed() {
    when(mObjWithCleanup.isCleanedUp()).thenReturn(false);
    CheckedDisposable disposable = Disposables.create(mObjWithCleanup, new ObjDisposer(), new ObjDisposeChecker());

    disposable.dispose();

    verify(mObjWithCleanup).isCleanedUp();
    verify(mObjWithCleanup).cleanup();
    verifyNoMoreInteractions(mObjWithCleanup);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDisposableOfDisposable() {
    CheckedDisposable firstDisposable = Disposables.create(mObjWithCleanup, new ObjDisposer());

    Disposer secondDisposer = mock(Disposer.class);
    CheckedDisposable secondDisposable = Disposables.create(firstDisposable, secondDisposer);

    secondDisposable.dispose();
    verify(secondDisposer).disposeInstance(firstDisposable);
    verify(mObjWithCleanup).cleanup();  // ensures our first disposable gets called through even though
                                        // our 2nd disposer is empty
    verifyNoMoreInteractions(secondDisposer, mObjWithCleanup);
  }

  @Test
  public void testWeakDisposableNoRef() {
    CheckedDisposable disposable = Disposables.createWeak(mObjWithCleanup, new ObjDisposer());

    boolean isDisposed = disposable.isDisposed();

    assertThat(isDisposed).isTrue();
  }

  @Test
  public void testWeakDisposableWithRef() {
    when(mWeakReference.get()).thenReturn(mObjWithCleanup);
    CheckedDisposable disposable = Disposables.createWeak(mObjWithCleanup, new ObjDisposer());

    boolean isDisposed = disposable.isDisposed();

    assertThat(isDisposed).isFalse();
  }

  @Test
  public void testDisposalOfWeakDisposableWithRef() {
    when(mWeakReference.get()).thenReturn(mObjWithCleanup);
    CheckedDisposable disposable = Disposables.createWeak(mObjWithCleanup, new ObjDisposer());

    disposable.dispose();

    verify(mObjWithCleanup).cleanup();
    verifyNoMoreInteractions(mObjWithCleanup);
  }

  @Test
  public void testDisposalOfWeakDisposableWNoRef() {
    CheckedDisposable disposable = Disposables.createWeak(mObjWithCleanup, new ObjDisposer());

    disposable.dispose();

    verifyNoMoreInteractions(mObjWithCleanup);
  }

  @Test
  public void testWeakCheckedDisposableNoRef() {
    CheckedDisposable disposable = Disposables.createWeak(mObjWithCleanup, new ObjDisposer(), new ObjDisposeChecker());

    boolean isDisposed = disposable.isDisposed();

    assertThat(isDisposed).isTrue();
    verifyNoMoreInteractions(mObjWithCleanup);
  }

  @Test
  public void testWeakCheckedDisposableWithRef() {
    when(mWeakReference.get()).thenReturn(mObjWithCleanup);
    CheckedDisposable disposable = Disposables.createWeak(mObjWithCleanup, new ObjDisposer(), new ObjDisposeChecker());

    boolean isDisposed = disposable.isDisposed();

    assertThat(isDisposed).isFalse();
    verify(mObjWithCleanup).isCleanedUp();
    verifyNoMoreInteractions(mObjWithCleanup);
  }

  @Test
  public void testDisposalOfWeakCheckdDisposableWithRef() {
    when(mWeakReference.get()).thenReturn(mObjWithCleanup);
    CheckedDisposable disposable = Disposables.createWeak(mObjWithCleanup, new ObjDisposer(), new ObjDisposeChecker());

    disposable.dispose();

    verify(mObjWithCleanup).isCleanedUp();
    verify(mObjWithCleanup).cleanup();
    verifyNoMoreInteractions(mObjWithCleanup);
  }

  @Test
  public void testDisposalOfWeakCheckedDisposableWNoRef() {
    CheckedDisposable disposable = Disposables.createWeak(mObjWithCleanup, new ObjDisposer(), new ObjDisposeChecker());

    disposable.dispose();

    verifyNoMoreInteractions(mObjWithCleanup);
  }
}
