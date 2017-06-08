package com.episode6.hackit.disposable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests {@link MaybeDisposables}
 */
@SuppressWarnings("unchecked")
public class MaybeDisposablesTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  interface TestObj {}

  @Mock Disposable mDisposable;
  @Mock CheckedDisposable mCheckedDisposable;
  @Mock HasDisposables mHasDisposables;
  @Mock Disposer mDisposer;
  @Mock CheckedDisposer mCheckedDisposer;
  @Mock TestObj mTestObj;

  @Test
  public void testDisposeNull() {
    MaybeDisposables.dispose(null);
    MaybeDisposables.dispose(mTestObj);
    MaybeDisposables.dispose(null, null);
    MaybeDisposables.dispose(mTestObj, null);
    MaybeDisposables.dispose(null, mDisposer);
    MaybeDisposables.dispose(null, mCheckedDisposer);
    MaybeDisposables.disposeList(null);
    MaybeDisposables.disposeList(new LinkedList());

    verifyNoMoreInteractions(mTestObj, mDisposer, mCheckedDisposer);
  }

  @Test
  public void testIsDisposedNull() {
    assertThat(MaybeDisposables.isDisposed(null)).isTrue();
    assertThat(MaybeDisposables.isDisposed(null, null)).isTrue();
    assertThat(MaybeDisposables.isDisposed(null, mDisposer)).isTrue();
    assertThat(MaybeDisposables.isDisposed(null, mCheckedDisposer)).isTrue();
    verifyNoMoreInteractions(mDisposer, mCheckedDisposer);
  }

  @Test
  public void testIsDisposedUnknown() {
    assertThat(MaybeDisposables.isDisposed(mTestObj)).isFalse();
    assertThat(MaybeDisposables.isDisposed(mTestObj, null)).isFalse();
    assertThat(MaybeDisposables.isDisposed(mTestObj, mDisposer)).isFalse();
    verifyNoMoreInteractions(mTestObj, mDisposer);
  }

  @Test
  public void testIsFlushableDefaults() {
    assertThat(MaybeDisposables.isFlushable(null)).isTrue();
    assertThat(MaybeDisposables.isFlushable(mTestObj)).isFalse();
    MaybeDisposables.flushList(null);
    MaybeDisposables.flushList(new LinkedList());
    verifyNoMoreInteractions(mTestObj);
  }

  @Test
  public void testDisposeDisposable() {
    MaybeDisposables.dispose(mDisposable);
    MaybeDisposables.dispose(mCheckedDisposable);
    MaybeDisposables.dispose(mHasDisposables);

    verify(mDisposable).dispose();
    verify(mCheckedDisposable).dispose();
    verify(mHasDisposables).dispose();
    verifyNoMoreInteractions(mDisposable, mCheckedDisposable, mHasDisposables);
  }

  @Test
  public void testDisposeWithDisposer() {
    MaybeDisposables.dispose(mTestObj, mDisposer);
    MaybeDisposables.dispose(mTestObj, mCheckedDisposer);

    verify(mDisposer).disposeInstance(mTestObj);
    verify(mCheckedDisposer).isInstanceDisposed(mTestObj);
    verify(mCheckedDisposer).disposeInstance(mTestObj);
    verifyNoMoreInteractions(mDisposer, mCheckedDisposer, mTestObj);
  }

  @Test
  public void testDisposeDisposableWithDisposer() {
    MaybeDisposables.dispose(mDisposable, mDisposer);
    MaybeDisposables.dispose(mCheckedDisposable, mCheckedDisposer);
    MaybeDisposables.dispose(mHasDisposables, null);

    InOrder inOrder = Mockito.inOrder(mDisposable, mDisposer, mCheckedDisposable, mCheckedDisposer, mHasDisposables);
    inOrder.verify(mDisposer).disposeInstance(mDisposable);
    inOrder.verify(mDisposable).dispose();
    inOrder.verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposable);
    inOrder.verify(mCheckedDisposer).disposeInstance(mCheckedDisposable);
    inOrder.verify(mCheckedDisposable).dispose();
    inOrder.verify(mHasDisposables).dispose();
    verifyNoMoreInteractions(mDisposable, mDisposer, mCheckedDisposable, mCheckedDisposer, mHasDisposables);
  }

  @Test
  public void testIsDisposedWithDisposer() {
    assertThat(MaybeDisposables.isDisposed(mTestObj, mCheckedDisposer)).isFalse();

    verify(mCheckedDisposer).isInstanceDisposed(mTestObj);
    verifyNoMoreInteractions(mTestObj, mDisposer, mCheckedDisposer);
  }

  @Test
  public void testIsDisposedWithDisposerTrue() {
    when(mCheckedDisposer.isInstanceDisposed(mTestObj)).thenReturn(true);

    assertThat(MaybeDisposables.isDisposed(mTestObj, mCheckedDisposer)).isTrue();

    verify(mCheckedDisposer).isInstanceDisposed(mTestObj);
    verifyNoMoreInteractions(mTestObj, mDisposer, mCheckedDisposer);
  }

  @Test
  public void testIsCheckedDisposed() {
    assertThat(MaybeDisposables.isDisposed(mCheckedDisposable)).isFalse();

    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable);
  }

  @Test
  public void testIsCheckedDisposedTrue() {
    when(mCheckedDisposable.isDisposed()).thenReturn(true);
    
    assertThat(MaybeDisposables.isDisposed(mCheckedDisposable)).isTrue();

    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable);
  }

  @Test
  public void testIsCheckedDisposedWithDisposer() {
    when(mCheckedDisposer.isInstanceDisposed(mCheckedDisposable)).thenReturn(true);

    assertThat(MaybeDisposables.isDisposed(mCheckedDisposable, mCheckedDisposer)).isFalse();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposer, mCheckedDisposable);
    inOrder.verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposable);
    inOrder.verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable, mDisposer, mCheckedDisposer);
  }

  @Test
  public void testIsCheckedDisposedWithDisposerTrue() {
    when(mCheckedDisposable.isDisposed()).thenReturn(true);
    when(mCheckedDisposer.isInstanceDisposed(mCheckedDisposable)).thenReturn(true);

    assertThat(MaybeDisposables.isDisposed(mCheckedDisposable, mCheckedDisposer)).isTrue();

    InOrder inOrder = Mockito.inOrder(mCheckedDisposer, mCheckedDisposable);
    inOrder.verify(mCheckedDisposer).isInstanceDisposed(mCheckedDisposable);
    inOrder.verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable, mDisposer, mCheckedDisposer);
  }

  @Test
  public void testIsCheckedFlushable() {
    assertThat(MaybeDisposables.isFlushable(mCheckedDisposable)).isFalse();

    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable);
  }

  @Test
  public void testIsCheckedFlushableTrue() {
    when(mCheckedDisposable.isDisposed()).thenReturn(true);

    assertThat(MaybeDisposables.isFlushable(mCheckedDisposable)).isTrue();

    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposable);
  }

  @Test
  public void testIsHasDisposablesFlushable() {
    assertThat(MaybeDisposables.isFlushable(mHasDisposables)).isFalse();

    verify(mHasDisposables).flushDisposed();
    verifyNoMoreInteractions(mHasDisposables);
  }

  @Test
  public void testIsHasDisposablesFlushableTrue() {
    when(mHasDisposables.flushDisposed()).thenReturn(true);

    assertThat(MaybeDisposables.isFlushable(mHasDisposables)).isTrue();

    verify(mHasDisposables).flushDisposed();
    verifyNoMoreInteractions(mHasDisposables);
  }

  @Test
  public void disposeList() {
    List<Object> list = asList(mDisposable, mCheckedDisposable, mHasDisposables, mTestObj);

    MaybeDisposables.disposeList(list);

    InOrder inOrder = Mockito.inOrder(mDisposable, mCheckedDisposable, mHasDisposables, mTestObj);
    inOrder.verify(mHasDisposables).dispose();
    inOrder.verify(mCheckedDisposable).dispose();
    inOrder.verify(mDisposable).dispose();
    verifyNoMoreInteractions(mDisposable, mCheckedDisposable, mHasDisposables, mTestObj);
    assertThat(list).isEmpty();
  }

  @Test
  public void flushList() {
    when(mCheckedDisposable.isDisposed()).thenReturn(true);
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    List<Object> list = asList(mDisposable, mCheckedDisposable, mHasDisposables, mTestObj);

    MaybeDisposables.flushList(list);

    verify(mHasDisposables).flushDisposed();
    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mDisposable, mCheckedDisposable, mHasDisposables, mTestObj);
    assertThat(list).containsOnly(mDisposable, mTestObj);
  }

  @Test
  public void flushListDontRemoveHasDisposables() {
    when(mCheckedDisposable.isDisposed()).thenReturn(true);
    List<Object> list = asList(mDisposable, mCheckedDisposable, mHasDisposables, mTestObj);

    MaybeDisposables.flushList(list);

    verify(mHasDisposables).flushDisposed();
    verify(mCheckedDisposable).isDisposed();
    verifyNoMoreInteractions(mDisposable, mCheckedDisposable, mHasDisposables, mTestObj);
    assertThat(list).containsOnly(mDisposable, mHasDisposables, mTestObj);
  }

  static <T> List<T> asList(T... a) {
    return new LinkedList<>(Arrays.asList(a));
  }
}
