package com.episode6.hackit.pausable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;

import static org.mockito.Mockito.verify;

/**
 * Tests {@link ForgetfulPausableCollection}
 */
public class ForgetfulPausableCollectionTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Pausable mPausable;

  static ForgetfulPausableCollection<Object> createFlushable(Object... objects) {
    return new ForgetfulPausableCollection<>(true, Arrays.asList(objects));
  }

  static ForgetfulPausableCollection<Object> createUnflushable(Object... objects) {
    return new ForgetfulPausableCollection<>(false, Arrays.asList(objects));
  }

  @Test
  public void testPause() {
    ForgetfulPausableCollection<Object> collection = createFlushable(mPausable);

    collection.pause();

    verify(mPausable).pause();
  }

  @Test
  public void testResume() {
    ForgetfulPausableCollection<Object> collection = createFlushable(mPausable);

    collection.resume();

    verify(mPausable).resume();
  }
}
