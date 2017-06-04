package com.episode6.hackit.disposable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ForgetfulDelegateCheckedDisposable}
 */
public class ForgetfulDelegateCheckedDisposableTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Runnable mRandomObject;
  @Mock Disposable mDisposable;
  @Mock CheckedDisposable mCheckedDisposable;

  @Test
  public void testIsDisposedRandomObject() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDelegateCheckedDisposable disposable = new ForgetfulDelegateCheckedDisposable<>(mRandomObject);

    boolean isDisposedBefore = disposable.isDisposed();
    disposable.dispose();
    boolean isDisposedAfter = disposable.isDisposed();

    verifyNoMoreInteractions(mRandomObject);
    assertThat(isDisposedBefore).isFalse();
    assertThat(isDisposedAfter).isTrue();
    verifyDelegateIsNull(disposable);
  }

  @Test
  public void testIsDisposedDisposable() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDelegateCheckedDisposable disposable = new ForgetfulDelegateCheckedDisposable<>(mDisposable);

    boolean isDisposedBefore = disposable.isDisposed();
    disposable.dispose();
    boolean isDisposedAfter = disposable.isDisposed();

    verify(mDisposable).dispose();
    verifyNoMoreInteractions(mDisposable);
    assertThat(isDisposedBefore).isFalse();
    assertThat(isDisposedAfter).isTrue();
    verifyDelegateIsNull(disposable);
  }

  @Test
  public void testIsDisposedCheckedDisposable() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDelegateCheckedDisposable disposable = new ForgetfulDelegateCheckedDisposable<>(mCheckedDisposable);

    boolean isDisposedBefore = disposable.isDisposed();
    disposable.dispose();
    boolean isDisposedAfter = disposable.isDisposed();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposable);
    inOrder.verify(mCheckedDisposable).isDisposed();
    inOrder.verify(mCheckedDisposable).dispose();
    verifyNoMoreInteractions(mCheckedDisposable);
    assertThat(isDisposedBefore).isFalse();
    assertThat(isDisposedAfter).isTrue();
    verifyDelegateIsNull(disposable);
  }

  @Test
  public void testIsDisposedPreDisposedCheckedDisposable() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposable.isDisposed()).thenReturn(true);
    ForgetfulDelegateCheckedDisposable disposable = new ForgetfulDelegateCheckedDisposable<>(mCheckedDisposable);

    boolean isDisposedBefore = disposable.isDisposed();
    disposable.dispose();
    boolean isDisposedAfter = disposable.isDisposed();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposable);
    inOrder.verify(mCheckedDisposable).isDisposed();
    inOrder.verify(mCheckedDisposable).dispose(); // dispose still gets called here
    verifyNoMoreInteractions(mCheckedDisposable);
    assertThat(isDisposedBefore).isTrue();
    assertThat(isDisposedAfter).isTrue();
    verifyDelegateIsNull(disposable);
  }

  private void verifyDelegateIsNull(ForgetfulDelegateCheckedDisposable delegateDisposable)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = ForgetfulDelegateDisposable.class.getDeclaredField("mDelegate");
    field.setAccessible(true);
    assertThat(field.get(delegateDisposable)).isNull();
  }
}
