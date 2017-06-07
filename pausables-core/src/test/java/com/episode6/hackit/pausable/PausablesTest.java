package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.CheckedDisposer;
import com.episode6.hackit.disposable.Disposables;
import com.episode6.hackit.disposable.Disposer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.ref.WeakReference;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Tests {@link Pausables}
 */
@SuppressWarnings("unchecked")
@PrepareForTest({Pausables.class}) // creates weak refs
@RunWith(PowerMockRunner.class)
public class PausablesTest {

  interface TestObj {}

  @Mock TestObj mTestObj;
  @Mock Pausable mPausable;
  @Mock Pauser mPauser;
  @Mock Disposer mDisposer;
  @Mock CheckedDisposer mCheckedDisposer;
  @Mock WeakReference mWeakReference;
  @Mock CheckedDisposablePausable mCheckedDisposablePausable;

  @Before
  public void setup() throws Exception {
    whenNew(WeakReference.class).withAnyArguments().thenReturn(mWeakReference);
  }

  @Test
  public void testGenericWeakPausablePause() {
    when(mWeakReference.get()).thenReturn(mTestObj);
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser);

    pausable.pause();

    verify(mPauser).pauseInstance(mTestObj);
    verifyNoMoreInteractions(mTestObj, mPauser);
  }

  @Test
  public void testGenericWeakPausableResume() {
    when(mWeakReference.get()).thenReturn(mTestObj);
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser);

    pausable.resume();

    verify(mPauser).resumeInstance(mTestObj);
    verifyNoMoreInteractions(mTestObj, mPauser);
  }

  @Test
  public void testGenericWeakPausablePauseNullRef() {
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser);

    pausable.pause();

    verifyNoMoreInteractions(mTestObj, mPauser);
  }

  @Test
  public void testGenericWeakPausableResumeNullRef() {
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser);

    pausable.resume();

    verifyNoMoreInteractions(mTestObj, mPauser);
  }

  @Test
  public void testWeakPausablePause() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser);

    pausable.pause();

    verify(mPauser).pauseInstance(mPausable);
    verify(mPausable).pause();
    verifyNoMoreInteractions(mPausable, mPauser);
  }

  @Test
  public void testWeakPausableResume() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser);

    pausable.resume();

    verify(mPauser).resumeInstance(mPausable);
    verify(mPausable).resume();
    verifyNoMoreInteractions(mPausable, mPauser);
  }

  @Test
  public void testWeakPausablePauseNullRef() {
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser);

    pausable.pause();

    verifyNoMoreInteractions(mPausable, mPauser);
  }

  @Test
  public void testWeakPausableResumeNullRef() {
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser);

    pausable.resume();

    verifyNoMoreInteractions(mPausable, mPauser);
  }

  @Test
  public void testGenericWeakPausablePauseWithDisposer() {
    when(mWeakReference.get()).thenReturn(mTestObj);
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mDisposer);

    pausable.pause();

    verify(mPauser).pauseInstance(mTestObj);
    verifyNoMoreInteractions(mTestObj, mPauser, mDisposer);
  }

  @Test
  public void testGenericWeakPausableResumeWithDisposer() {
    when(mWeakReference.get()).thenReturn(mTestObj);
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mDisposer);

    pausable.resume();

    verify(mPauser).resumeInstance(mTestObj);
    verifyNoMoreInteractions(mTestObj, mPauser, mDisposer);
  }

  @Test
  public void testGenericWeakPausablePauseNullRefWithDisposer() {
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mDisposer);

    pausable.pause();

    verifyNoMoreInteractions(mTestObj, mPauser, mDisposer);
  }

  @Test
  public void testGenericWeakPausableResumeNullRefWithDisposer() {
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mDisposer);

    pausable.resume();

    verifyNoMoreInteractions(mTestObj, mPauser, mDisposer);
  }

  @Test
  public void testWeakPausablePauseWithDisposer() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mDisposer);

    pausable.pause();

    verify(mPauser).pauseInstance(mPausable);
    verify(mPausable).pause();
    verifyNoMoreInteractions(mPausable, mPauser, mDisposer);
  }

  @Test
  public void testWeakPausableResumeWithDisposer() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mDisposer);

    pausable.resume();

    verify(mPauser).resumeInstance(mPausable);
    verify(mPausable).resume();
    verifyNoMoreInteractions(mPausable, mPauser, mDisposer);
  }

  @Test
  public void testWeakPausablePauseNullRefWithDisposer() {
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mDisposer);

    pausable.pause();

    verifyNoMoreInteractions(mPausable, mPauser, mDisposer);
  }

  @Test
  public void testWeakPausableResumeNullRefWithDisposer() {
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mDisposer);

    pausable.resume();

    verifyNoMoreInteractions(mPausable, mPauser, mDisposer);
  }

  @Test
  public void testGenericWeakPausablePauseWithCheckedDisposer() {
    when(mWeakReference.get()).thenReturn(mTestObj);
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mCheckedDisposer);

    pausable.pause();

    verify(mPauser).pauseInstance(mTestObj);
    verifyNoMoreInteractions(mTestObj, mPauser, mCheckedDisposer);
  }

  @Test
  public void testGenericWeakPausableResumeWithCheckedDisposer() {
    when(mWeakReference.get()).thenReturn(mTestObj);
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mCheckedDisposer);

    pausable.resume();

    verify(mPauser).resumeInstance(mTestObj);
    verifyNoMoreInteractions(mTestObj, mPauser, mCheckedDisposer);
  }

  @Test
  public void testGenericWeakPausablePauseNullRefWithCheckedDisposer() {
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mCheckedDisposer);

    pausable.pause();

    verifyNoMoreInteractions(mTestObj, mPauser, mCheckedDisposer);
  }

  @Test
  public void testGenericWeakPausableResumeNullRefWithCheckedDisposer() {
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mCheckedDisposer);

    pausable.resume();

    verifyNoMoreInteractions(mTestObj, mPauser, mCheckedDisposer);
  }

  @Test
  public void testWeakPausablePauseWithCheckedDisposer() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mCheckedDisposer);

    pausable.pause();

    verify(mPauser).pauseInstance(mPausable);
    verify(mPausable).pause();
    verifyNoMoreInteractions(mPausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testWeakPausableResumeWithCheckedDisposer() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mCheckedDisposer);

    pausable.resume();

    verify(mPauser).resumeInstance(mPausable);
    verify(mPausable).resume();
    verifyNoMoreInteractions(mPausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testWeakPausablePauseNullRefWithCheckedDisposer() {
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mCheckedDisposer);

    pausable.pause();

    verifyNoMoreInteractions(mPausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testWeakPausableResumeNullRefWithCheckedDisposer() {
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mCheckedDisposer);

    pausable.resume();

    verifyNoMoreInteractions(mPausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testDisposePausable() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser);

    pausable.dispose();

    verify(mCheckedDisposablePausable).isDisposed();
    verify(mCheckedDisposablePausable).dispose();
    verifyNoMoreInteractions(mPauser, mCheckedDisposablePausable);
  }

  @Test
  public void isPausableDisposed() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser);

    assertThat(pausable.isDisposed()).isFalse();

    verify(mCheckedDisposablePausable).isDisposed();
    verifyNoMoreInteractions(mPauser, mCheckedDisposablePausable);
  }

  @Test
  public void testDisposePausableWithDisposer() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser, mDisposer);

    pausable.dispose();

    verify(mCheckedDisposablePausable).isDisposed();
    verify(mDisposer).disposeInstance(mCheckedDisposablePausable);
    verify(mCheckedDisposablePausable).dispose();
    verifyNoMoreInteractions(mPauser, mCheckedDisposablePausable, mDisposer);
  }

  @Test
  public void isPausableDisposedWithCheckedDisposer() {
    when(mCheckedDisposer.isInstanceDisposed(mCheckedDisposablePausable)).thenReturn(true);
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser, mCheckedDisposer);

    assertThat(pausable.isDisposed()).isFalse();

    verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposablePausable);
    verify(mCheckedDisposablePausable).isDisposed();
    verifyNoMoreInteractions(mPauser, mCheckedDisposablePausable, mCheckedDisposer);
  }
}
