package com.episode6.hackit.disposable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


/**
 * Tests {@link ForgetfulDelegateDisposable}
 */
public class ForgetfulDelegateDisposableTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Runnable mRandomObject;
  @Mock Disposable mDisposable;
  @Mock CheckedDisposable mCheckedDisposable;

  @Test
  public void testDisposeRandomObject() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDelegateDisposable disposable = new ForgetfulDelegateDisposable<>(mRandomObject);

    disposable.dispose();

    verifyNoMoreInteractions(mRandomObject);
    verifyDelegateIsNull(disposable);
  }

  @Test
  public void testDisposeDisposable() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDelegateDisposable disposable = new ForgetfulDelegateDisposable<>(mDisposable);

    disposable.dispose();

    verify(mDisposable).dispose();
    verifyNoMoreInteractions(mDisposable);
    verifyDelegateIsNull(disposable);
  }

  @Test
  public void testDisposeDisposableMulti() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDelegateDisposable disposable = new ForgetfulDelegateDisposable<>(mDisposable);

    disposable.dispose();
    disposable.dispose();
    disposable.dispose();
    disposable.dispose();
    disposable.dispose();

    verify(mDisposable).dispose();
    verifyNoMoreInteractions(mDisposable);
  }

  @Test
  public void testDisposeCheckedDisposable() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDelegateDisposable disposable = new ForgetfulDelegateDisposable<>(mCheckedDisposable);

    disposable.dispose();

    verify(mCheckedDisposable).dispose();
    verifyNoMoreInteractions(mCheckedDisposable);
    verifyDelegateIsNull(disposable);
  }

  // yes, this is testing implementation, but forgetting its reference is part
  // of the ForgetfulDelegateDisposable's contract, so its worth testing
  private void verifyDelegateIsNull(ForgetfulDelegateDisposable forgetfulDelegateDisposable)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = ForgetfulDelegateDisposable.class.getDeclaredField("mDelegate");
    field.setAccessible(true);
    assertThat(field.get(forgetfulDelegateDisposable)).isNull();
  }
}
