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
 * Tests {@link DelegateDisposable}
 */
public class DelegateDisposableTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Runnable mRandomObject;
  @Mock Disposable mDisposable;
  @Mock CheckedDisposable mCheckedDisposable;

  @Test
  public void testDisposeRandomObject() throws NoSuchFieldException, IllegalAccessException {
    DelegateDisposable disposable = new DelegateDisposable<>(mRandomObject);

    disposable.dispose();

    verifyNoMoreInteractions(mRandomObject);
    verifyDelegateIsNull(disposable);
  }

  @Test
  public void testDisposeDisposable() throws NoSuchFieldException, IllegalAccessException {
    DelegateDisposable disposable = new DelegateDisposable<>(mDisposable);

    disposable.dispose();

    verify(mDisposable).dispose();
    verifyNoMoreInteractions(mDisposable);
    verifyDelegateIsNull(disposable);
  }

  @Test
  public void testDisposeDisposableMulti() throws NoSuchFieldException, IllegalAccessException {
    DelegateDisposable disposable = new DelegateDisposable<>(mDisposable);

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
    DelegateDisposable disposable = new DelegateDisposable<>(mCheckedDisposable);

    disposable.dispose();

    verify(mCheckedDisposable).dispose();
    verifyNoMoreInteractions(mCheckedDisposable);
    verifyDelegateIsNull(disposable);
  }

  // yes, this is testing implementation, but forgetting its reference is part
  // of the DelegateDisposable's contract, so its worth testing
  private void verifyDelegateIsNull(DelegateDisposable delegateDisposable)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = DelegateDisposable.class.getDeclaredField("mDelegate");
    field.setAccessible(true);
    assertThat(field.get(delegateDisposable)).isNull();
  }
}
