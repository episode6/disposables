package com.episode6.hackit.disposable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests {@link ForgetfulDisposableCollection}
 */
public class ForgetfulDisposableCollectionTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  interface TestObj {}

  @Mock Disposable mDisposable1;
  @Mock CheckedDisposable mCheckedDisposable1;
  @Mock Disposable mDisposable2;
  @Mock CheckedDisposable mCheckedDisposable2;
  @Mock HasDisposables mHasDisposables;
  @Mock TestObj mTestObj;

  private static ForgetfulDisposableCollection<Object> createFlushable(Object... objects) {
    return new ForgetfulDisposableCollection<Object>(true, Arrays.asList(objects));
  }

  private static ForgetfulDisposableCollection<Object> createUnFlushable(Object... objects) {
    return new ForgetfulDisposableCollection<Object>(false, Arrays.asList(objects));
  }

  @Test
  public void testSimpleDispose() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDisposableCollection<Object> collection = createFlushable(mDisposable1, mCheckedDisposable1, mHasDisposables, mTestObj);

    collection.dispose();

    verify(mDisposable1).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mHasDisposables).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables, mTestObj);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsWhenAddAfterDispose() {
    ForgetfulDisposableCollection<Object> collection = createFlushable(mDisposable1);

    collection.dispose();
    collection.add(mCheckedDisposable2);
  }

  @Test
  public void testSimpleFlushNotDisposed() throws NoSuchFieldException, IllegalAccessException {
    ForgetfulDisposableCollection<Object> collection = createFlushable(mDisposable1, mCheckedDisposable1, mHasDisposables, mTestObj);

    collection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mHasDisposables).flushDisposed();
    assertThat(getInternalList(collection)).contains(mDisposable1, mCheckedDisposable1, mHasDisposables, mTestObj);

    collection.dispose();

    verify(mDisposable1).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mHasDisposables).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables, mTestObj);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testSimpleFlushDisposed() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    ForgetfulDisposableCollection<Object> collection = createFlushable(mDisposable1, mCheckedDisposable1, mHasDisposables, mTestObj);

    collection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mHasDisposables).flushDisposed();
    assertThat(getInternalList(collection)).containsOnly(mDisposable1, mTestObj);

    collection.dispose();

    verify(mDisposable1).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1, mHasDisposables, mTestObj);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testParentChildDispose() {
    ForgetfulDisposableCollection<Object> childCollection = createFlushable(mDisposable2, mCheckedDisposable2);
    ForgetfulDisposableCollection<Object> parentCollection = createFlushable(mDisposable1, mCheckedDisposable1, childCollection);

    parentCollection.dispose();

    verify(mDisposable1).dispose();
    verify(mDisposable2).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mCheckedDisposable2).dispose();
    verifyNoMoreInteractions(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
  }

  @Test
  public void testParentChildFlushNotDisposed() {
    ForgetfulDisposableCollection<Object> childCollection = createFlushable(mDisposable2, mCheckedDisposable2);
    ForgetfulDisposableCollection<Object> parentCollection = createFlushable(mDisposable1, mCheckedDisposable1, childCollection);

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
    ForgetfulDisposableCollection<Object> childCollection = createFlushable(mDisposable2, mCheckedDisposable2);
    ForgetfulDisposableCollection<Object> parentCollection = createFlushable(mDisposable1, mCheckedDisposable1, childCollection);

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
    ForgetfulDisposableCollection<Object> collection = createFlushable(
        mCheckedDisposable1,
        mCheckedDisposable2,
        mHasDisposables);

    boolean result = collection.flushDisposed();

    assertThat(result).isTrue();
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testEmptyAfterFlushCantDispose() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    when(mCheckedDisposable2.isDisposed()).thenReturn(true);
    when(mHasDisposables.flushDisposed()).thenReturn(true);
    ForgetfulDisposableCollection<Object> collection = createUnFlushable(
        mCheckedDisposable1,
        mCheckedDisposable2,
        mHasDisposables);

    boolean result = collection.flushDisposed();

    assertThat(result).isFalse();
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testFlushReturnValueEmptyCollection() {
    ForgetfulDisposableCollection<Object> collectionDisposes = createFlushable();
    ForgetfulDisposableCollection<Object> collectionDoesntDispose = createUnFlushable();

    boolean disposesResult = collectionDisposes.flushDisposed();
    boolean doesntDisposeResult = collectionDoesntDispose.flushDisposed();

    assertThat(disposesResult).isTrue();
    assertThat(doesntDisposeResult).isFalse();
  }

  @Test
  public void testDisposeInverseOrder() {
    ForgetfulDisposableCollection<Object> collection = createUnFlushable();
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
  private static List<Object> getInternalList(ForgetfulDisposableCollection collection)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = ForgetfulDisposableCollection.class.getDeclaredField("mList");
    field.setAccessible(true);
    return (List<Object>) field.get(collection);
  }
}
