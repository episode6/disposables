package com.episode6.hackit.disposable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;
import java.util.Collection;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests {@link DisposableCollection}
 */
public class DisposableCollectionTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Disposable mDisposable1;
  @Mock CheckedDisposable mCheckedDisposable1;
  @Mock Disposable mDisposable2;
  @Mock CheckedDisposable mCheckedDisposable2;
  @Mock HasDisposables mHasDisposables;

  @Test
  public void testSimpleDispose() {
    DisposableCollection collection = DisposableCollection.createFlushable(mDisposable1, mCheckedDisposable1, mHasDisposables);

    collection.dispose();

    verify(mDisposable1).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mHasDisposables).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables);
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsWhenAddAfterDispose() {
    DisposableCollection collection = DisposableCollection.createFlushable(mDisposable1);

    collection.dispose();
    collection.add(mCheckedDisposable2);
  }

  @Test
  public void testSimpleFlushNotDisposed() {
    DisposableCollection collection = DisposableCollection.createFlushable(mDisposable1, mCheckedDisposable1, mHasDisposables);

    collection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mHasDisposables).flushDisposed();

    collection.dispose();

    verify(mDisposable1).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mHasDisposables).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables);
  }

  @Test
  public void testSimpleFlushDisposed() {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    DisposableCollection collection = DisposableCollection.createFlushable(mDisposable1, mCheckedDisposable1, mHasDisposables);

    collection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mHasDisposables).flushDisposed();

    collection.dispose();

    verify(mDisposable1).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables);
  }

  @Test
  public void testParentChildDispose() {
    DisposableCollection childCollection = DisposableCollection.createFlushable(mDisposable2, mCheckedDisposable2);
    DisposableCollection parentCollection = DisposableCollection.createFlushable(mDisposable1, mCheckedDisposable1, childCollection);

    parentCollection.dispose();

    verify(mDisposable1).dispose();
    verify(mDisposable2).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mCheckedDisposable2).dispose();
    verifyNoMoreInteractions(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
  }

  @Test
  public void testParentChildFlushNotDisposed() {
    DisposableCollection childCollection = DisposableCollection.createFlushable(mDisposable2, mCheckedDisposable2);
    DisposableCollection parentCollection = DisposableCollection.createFlushable(mDisposable1, mCheckedDisposable1, childCollection);

    parentCollection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mCheckedDisposable2).isDisposed();

    parentCollection.dispose();

    verify(mDisposable1).dispose();
    verify(mDisposable2).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mCheckedDisposable2).dispose();
    verifyNoMoreInteractions(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
  }

  @Test
  public void testParentChildFlushDisposed() {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    when(mCheckedDisposable2.isDisposed()).thenReturn(true);
    DisposableCollection childCollection = DisposableCollection.createFlushable(mDisposable2, mCheckedDisposable2);
    DisposableCollection parentCollection = DisposableCollection.createFlushable(mDisposable1, mCheckedDisposable1, childCollection);

    parentCollection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mCheckedDisposable2).isDisposed();

    parentCollection.dispose();

    verify(mDisposable1).dispose();
    verify(mDisposable2).dispose();
    verifyNoMoreInteractions(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
  }

  @Test
  public void testEmptyAfterFlushCanDispose() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    when(mCheckedDisposable2.isDisposed()).thenReturn(true);
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    DisposableCollection collection = DisposableCollection.createFlushable(
        mCheckedDisposable1,
        mCheckedDisposable2,
        mHasDisposables);

    boolean result = collection.flushDisposed();

    assertThat(result).isTrue();
    assertInternalCollectionEmpty(collection);
  }

  @Test
  public void testEmptyAfterFlushCantDispose() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    when(mCheckedDisposable2.isDisposed()).thenReturn(true);
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    DisposableCollection collection = DisposableCollection.createUnFlushable(
        mCheckedDisposable1,
        mCheckedDisposable2,
        mHasDisposables);

    boolean result = collection.flushDisposed();

    assertThat(result).isFalse();
    assertInternalCollectionEmpty(collection);
  }

  @Test
  public void testFlushReturnValueEmptyCollection() {
    DisposableCollection collectionDisposes = DisposableCollection.createFlushable();
    DisposableCollection collectionDoesntDispose = DisposableCollection.createUnFlushable();

    boolean disposesResult = collectionDisposes.flushDisposed();
    boolean doesntDisposeResult = collectionDoesntDispose.flushDisposed();

    assertThat(disposesResult).isTrue();
    assertThat(doesntDisposeResult).isFalse();
  }

  @Test
  public void testDisposeInverseOrder() {
    DisposableCollection collection = DisposableCollection.createUnFlushable();
    collection.add(mDisposable1);
    collection.add(mDisposable2);
    collection.add(mCheckedDisposable1);
    collection.add(mCheckedDisposable2);

    collection.dispose();

    InOrder inOrder = inOrder(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
    inOrder.verify(mCheckedDisposable2).dispose();
    inOrder.verify(mCheckedDisposable1).dispose();
    inOrder.verify(mDisposable2).dispose();
    inOrder.verify(mDisposable1).dispose();
    verifyNoMoreInteractions(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
  }

  @SuppressWarnings("unchecked")
  private static void assertInternalCollectionEmpty(DisposableCollection collection)
      throws NoSuchFieldException, IllegalAccessException {
    Field collectionField = DisposableCollection.class.getDeclaredField("mDisposables");
    collectionField.setAccessible(true);
    assertThat((Collection)collectionField.get(collection)).isEmpty();
  }
}
