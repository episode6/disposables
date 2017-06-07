package com.episode6.hackit.pausable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests {@link MaybePausables}
 */
@SuppressWarnings("unchecked")
public class MaybePausablesTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  interface TestObj {};

  @Mock Pausable mPausable;
  @Mock Pauser mPauser;
  @Mock TestObj mTestObj;

  @Test
  public void testNulls() {
    MaybePausables.pause(null);
    MaybePausables.pause(null, null);
    MaybePausables.pause(null, mPauser);
    MaybePausables.pause(mTestObj, null);

    MaybePausables.resume(null);
    MaybePausables.resume(null, null);
    MaybePausables.resume(null, mPauser);
    MaybePausables.resume(mTestObj, null);

    verifyNoMoreInteractions(mTestObj, mPauser);
  }

  @Test
  public void testPause() {
    MaybePausables.pause(mPausable);
    verify(mPausable).pause();
  }

  @Test
  public void testResume() {
    MaybePausables.resume(mPausable);
    verify(mPausable).resume();
  }

  @Test
  public void testPausePauser() {
    MaybePausables.pause(mTestObj, mPauser);
    verify(mPauser).pauseInstance(mTestObj);
  }

  @Test
  public void testResumePauser() {
    MaybePausables.resume(mTestObj, mPauser);
    verify(mPauser).resumeInstance(mTestObj);
  }

  @Test
  public void testPausePauserPausable() {
    MaybePausables.pause(mPausable, mPauser);

    InOrder inOrder = Mockito.inOrder(mPauser, mPausable);
    inOrder.verify(mPauser).pauseInstance(mPausable);
    inOrder.verify(mPausable).pause();
    verifyNoMoreInteractions(mPauser, mPausable);
  }

  @Test
  public void testResumePauserPausable() {
    MaybePausables.resume(mPausable, mPauser);

    InOrder inOrder = Mockito.inOrder(mPauser, mPausable);
    inOrder.verify(mPauser).resumeInstance(mPausable);
    inOrder.verify(mPausable).resume();
    verifyNoMoreInteractions(mPauser, mPausable);
  }
}
