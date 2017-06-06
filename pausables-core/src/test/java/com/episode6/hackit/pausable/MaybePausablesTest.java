package com.episode6.hackit.pausable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link MaybePausables}
 */
public class MaybePausablesTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Pausable mPausable;

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
}
