package com.episode6.hackit.disposable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SettableDisposable}
 */
public class SettableDisposableTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Disposable mDisposable;
  @Mock CheckedDisposable mCheckedDisposable;
  @Mock HasDisposables mHasDisposables;

  SettableDisposable mSettableDisposable = SettableDisposable.create();

  @Test
  public void testDisposableDispose() {
    mSettableDisposable.set(mDisposable);
    mSettableDisposable.dispose();

    verify(mDisposable).dispose();
  }

  @Test
  public void testDisposableDisposeLate() {
    mSettableDisposable.dispose();
    mSettableDisposable.set(mDisposable);

    verify(mDisposable).dispose();
  }

  @Test
  public void testEmptyIsDisposed() {
    assertThat(mSettableDisposable.isDisposed()).isFalse();
  }

  @Test
  public void testDisposableIsDisposed() {
    mSettableDisposable.set(mDisposable);

    assertThat(mSettableDisposable.isDisposed()).isFalse();
    verifyNoMoreInteractions(mDisposable);
  }

  @Test
  public void testCheckedDisposableIsDisposed() {
    mSettableDisposable.set(mCheckedDisposable);

    assertThat(mSettableDisposable.isDisposed()).isFalse();
    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable);
  }

  @Test
  public void testCheckedDisposableIsDisposedTrue() {
    when(mCheckedDisposable.isDisposed()).thenReturn(true);
    mSettableDisposable.set(mCheckedDisposable);

    assertThat(mSettableDisposable.isDisposed()).isTrue();
    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable);
  }

  @Test
  public void testHasDisposablesIsDisposed() {
    mSettableDisposable.set(mHasDisposables);

    assertThat(mSettableDisposable.isDisposed()).isFalse();
    verify(mHasDisposables).flushDisposed();
    verifyNoMoreInteractions(mHasDisposables);
  }

  @Test
  public void testHasDisposablesIsDisposedTrue() {
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    mSettableDisposable.set(mHasDisposables);

    assertThat(mSettableDisposable.isDisposed()).isTrue();
    verify(mHasDisposables).flushDisposed();
    verifyNoMoreInteractions(mHasDisposables);
  }
}
