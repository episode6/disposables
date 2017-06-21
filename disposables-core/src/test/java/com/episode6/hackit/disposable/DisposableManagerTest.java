package com.episode6.hackit.disposable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests {@link DisposableManager}
 */
public class DisposableManagerTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  interface TestObj {}

  @Mock Disposable mDisposable1;
  @Mock CheckedDisposable mCheckedDisposable1;
  @Mock Disposable mDisposable2;
  @Mock CheckedDisposable mCheckedDisposable2;
  @Mock HasDisposables mHasDisposables;


  private static DisposableManager create(Disposable... disposables) {
    return Disposables.newManager(disposables);
  }

  @Test
  public void testSimpleDispose() throws NoSuchFieldException, IllegalAccessException {
    DisposableManager collection = create(mDisposable1, mCheckedDisposable1, mHasDisposables);

    collection.dispose();

    verify(mDisposable1).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mHasDisposables).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables);
    assertThat(getInternalList(collection)).isNull();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsWhenAddAfterDispose() {
    DisposableManager collection = create(mDisposable1);

    collection.dispose();
    collection.addDisposable(mCheckedDisposable2);
  }

  @Test
  public void testSimpleFlushNotDisposed() throws NoSuchFieldException, IllegalAccessException {
    DisposableManager collection = create(mDisposable1, mCheckedDisposable1, mHasDisposables);

    collection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mHasDisposables).flushDisposed();
    assertThat(getInternalList(collection)).contains(mDisposable1, mCheckedDisposable1, mHasDisposables);

    collection.dispose();

    verify(mDisposable1).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mHasDisposables).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables);
    assertThat(getInternalList(collection)).isNull();
  }

  @Test
  public void testSimpleFlushDisposed() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    DisposableManager collection = create(mDisposable1, mCheckedDisposable1, mHasDisposables);

    collection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mHasDisposables).flushDisposed();
    assertThat(getInternalList(collection)).containsOnly(mDisposable1);

    collection.dispose();

    verify(mDisposable1).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables);
    assertThat(getInternalList(collection)).isNull();
  }

  @Test
  public void testEmptyAfterFlushCantDispose() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    when(mCheckedDisposable2.isDisposed()).thenReturn(true);
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    DisposableManager collection = create(
        mCheckedDisposable1,
        mCheckedDisposable2,
        mHasDisposables);

    boolean result = collection.flushDisposed();

    assertThat(result).isFalse();
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testFlushReturnValueEmptyCollection() {
    DisposableManager collectionDoesntDispose = create();

    boolean doesntDisposeResult = collectionDoesntDispose.flushDisposed();

    assertThat(doesntDisposeResult).isFalse();
  }

  @Test
  public void testDisposeInverseOrder() {
    DisposableManager collection = create();
    collection.addDisposable(mDisposable1);
    collection.addDisposable(mDisposable2);
    collection.addDisposable(mCheckedDisposable1);
    collection.addDisposable(mCheckedDisposable2);

    collection.dispose();

    InOrder inOrder = inOrder(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
    inOrder.verify(mCheckedDisposable2).dispose();
    inOrder.verify(mCheckedDisposable1).dispose();
    inOrder.verify(mDisposable2).dispose();
    inOrder.verify(mDisposable1).dispose();
    verifyNoMoreInteractions(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
  }

  @SuppressWarnings("unchecked")
  private static List<Disposable> getInternalList(DisposableManager collection)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = AbstractDelegateDisposable.class.getDeclaredField("mDelegate");
    field.setAccessible(true);
    return (List<Disposable>) field.get(collection);
  }
}
