package com.episode6.hackit.pausable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests {@link PausableExecutor}
 */
public class PausableExecutorTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Runnable mRunnable;
  @Mock Executor mExecutor;

  PausableExecutor mPausableExecutor;

  @Before
  public void setup() {
    mPausableExecutor = PausableExecutor.wrap(mExecutor);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((Runnable)invocation.getArgument(0)).run();
        return null;
      }
    }).when(mExecutor).execute(any(Runnable.class));
  }

  @Test
  public void testExecByDefault() {
    mPausableExecutor.execute(mRunnable);

    verify(mExecutor).execute(any(Runnable.class));
    verify(mRunnable).run();
    verifyNoMoreInteractions(mExecutor, mRunnable);
  }

  @Test
  public void testPauseThenResume() {
    mPausableExecutor.pause();
    mPausableExecutor.execute(mRunnable);

    verifyNoMoreInteractions(mExecutor, mRunnable);

    mPausableExecutor.resume();

    verify(mExecutor).execute(any(Runnable.class));
    verify(mRunnable).run();
    verifyNoMoreInteractions(mExecutor, mRunnable);
  }
}
