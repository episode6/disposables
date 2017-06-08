package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.CheckedDisposer;
import com.episode6.hackit.disposable.Disposables;
import com.episode6.hackit.disposable.Disposer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    verifyNoMoreInteractions(mPausable, mPauser);
  }

  @Test
  public void testWeakPausableResume() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser);

    pausable.resume();

    verify(mPauser).resumeInstance(mPausable);
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
    verifyNoMoreInteractions(mPausable, mPauser, mDisposer);
  }

  @Test
  public void testWeakPausableResumeWithDisposer() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mDisposer);

    pausable.resume();

    verify(mPauser).resumeInstance(mPausable);
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

    verify(mCheckedDisposer).isInstanceDisposed(mTestObj);
    verify(mPauser).pauseInstance(mTestObj);
    verifyNoMoreInteractions(mTestObj, mPauser, mCheckedDisposer);
  }

  @Test
  public void testGenericWeakPausableResumeWithCheckedDisposer() {
    when(mWeakReference.get()).thenReturn(mTestObj);
    CheckedDisposablePausable pausable = Pausables.weak(mTestObj, mPauser, mCheckedDisposer);

    pausable.resume();

    verify(mCheckedDisposer).isInstanceDisposed(mTestObj);
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

    verify(mCheckedDisposer).isInstanceDisposed(mPausable);
    verify(mPauser).pauseInstance(mPausable);
    verifyNoMoreInteractions(mPausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testWeakPausablePauseWithCheckedDisposerDisposed() {
    when(mWeakReference.get()).thenReturn(mPausable);
    when(mCheckedDisposer.isInstanceDisposed(mPausable)).thenReturn(true);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mCheckedDisposer);

    pausable.pause();

    verify(mCheckedDisposer).isInstanceDisposed(mPausable);
    verifyNoMoreInteractions(mPausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testWeakPausableResumeWithCheckedDisposer() {
    when(mWeakReference.get()).thenReturn(mPausable);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mCheckedDisposer);

    pausable.resume();

    verify(mCheckedDisposer).isInstanceDisposed(mPausable);
    verify(mPauser).resumeInstance(mPausable);
    verifyNoMoreInteractions(mPausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testWeakPausableResumeWithCheckedDisposerDisposed() {
    when(mWeakReference.get()).thenReturn(mPausable);
    when(mCheckedDisposer.isInstanceDisposed(mPausable)).thenReturn(true);
    CheckedDisposablePausable pausable = Pausables.weak(mPausable, mPauser, mCheckedDisposer);

    pausable.resume();

    verify(mCheckedDisposer).isInstanceDisposed(mPausable);
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

    verifyNoMoreInteractions(mPauser, mCheckedDisposablePausable);
  }

  @Test
  public void isPausableDisposed() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser);

    assertThat(pausable.isDisposed()).isFalse();

    verifyNoMoreInteractions(mPauser, mCheckedDisposablePausable);
  }

  @Test
  public void testDisposePausableWithDisposer() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser, mDisposer);

    pausable.dispose();

    verify(mDisposer).disposeInstance(mCheckedDisposablePausable);
    verifyNoMoreInteractions(mPauser, mCheckedDisposablePausable, mDisposer);
  }

  @Test
  public void isPausableDisposedWithCheckedDisposer() {
    when(mCheckedDisposer.isInstanceDisposed(mCheckedDisposablePausable)).thenReturn(true);
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser, mCheckedDisposer);

    assertThat(pausable.isDisposed()).isTrue();

    verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposablePausable);
    verifyNoMoreInteractions(mPauser, mCheckedDisposablePausable, mCheckedDisposer);
  }

  @Test
  public void testDisposablePausablePause() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser);

    pausable.pause();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposablePausable, mPauser);
    inOrder.verify(mPauser).pauseInstance(mCheckedDisposablePausable);
    verifyNoMoreInteractions(mCheckedDisposablePausable, mPauser);
  }

  @Test
  public void testDisposablePausableResume() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser);

    pausable.resume();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposablePausable, mPauser);
    inOrder.verify(mPauser).resumeInstance(mCheckedDisposablePausable);
    verifyNoMoreInteractions(mCheckedDisposablePausable, mPauser);
  }
  
  @Test
  public void testDisposablePausableWithDisposerPause() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser, mCheckedDisposer);

    pausable.pause();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposablePausable, mPauser, mCheckedDisposer);
    inOrder.verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposablePausable);
    inOrder.verify(mPauser).pauseInstance(mCheckedDisposablePausable);
    verifyNoMoreInteractions(mCheckedDisposablePausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testDisposablePausableWithDisposerPauseDisposed() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    when(mCheckedDisposer.isInstanceDisposed(mCheckedDisposablePausable)).thenReturn(true);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser, mCheckedDisposer);

    pausable.pause();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposablePausable, mPauser, mCheckedDisposer);
    inOrder.verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposablePausable);
    verifyNoMoreInteractions(mCheckedDisposablePausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testDisposablePausableWithDisposerResume() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser, mCheckedDisposer);

    pausable.resume();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposablePausable, mPauser, mCheckedDisposer);
    inOrder.verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposablePausable);
    inOrder.verify(mPauser).resumeInstance(mCheckedDisposablePausable);
    verifyNoMoreInteractions(mCheckedDisposablePausable, mPauser, mCheckedDisposer);
  }

  @Test
  public void testDisposablePausableWithDisposerResumeDisposed() {
    when(mWeakReference.get()).thenReturn(mCheckedDisposablePausable);
    when(mCheckedDisposer.isInstanceDisposed(mCheckedDisposablePausable)).thenReturn(true);
    CheckedDisposablePausable pausable = Pausables.weak(mCheckedDisposablePausable, mPauser, mCheckedDisposer);

    pausable.resume();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposablePausable, mPauser, mCheckedDisposer);
    inOrder.verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposablePausable);
    verifyNoMoreInteractions(mCheckedDisposablePausable, mPauser, mCheckedDisposer);
  }
}
