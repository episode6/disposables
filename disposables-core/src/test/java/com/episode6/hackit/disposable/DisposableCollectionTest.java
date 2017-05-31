package com.episode6.hackit.disposable;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.episode6.hackit.disposable.DisposableCollection.createWith;
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

  @Test
  public void testSimpleDispose() {
    DisposableCollection collection = createWith(mDisposable1, mCheckedDisposable1);

    collection.dispose();

    verify(mDisposable1).dispose();
    verify(mCheckedDisposable1).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1);
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsWhenAddAfterDispose() {
    DisposableCollection collection = createWith(mDisposable1);

    collection.dispose();
    collection.addDisposable(mCheckedDisposable2);
  }

  @Test
  public void testSimpleFlushNotDisposed() {
    DisposableCollection collection = createWith(mDisposable1, mCheckedDisposable1);

    collection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();

    collection.dispose();

    verify(mDisposable1).dispose();
    verify(mCheckedDisposable1).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1);
  }

  @Test
  public void testSimpleFlushDisposed() {
    when(mCheckedDisposable1.isDisposed()).thenReturn(true);
    DisposableCollection collection = createWith(mDisposable1, mCheckedDisposable1);

    collection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();

    collection.dispose();

    verify(mDisposable1).dispose();
    verifyNoMoreInteractions(mDisposable1, mCheckedDisposable1);
  }

  @Test
  public void testParentChildDispose() {
    DisposableCollection childCollection = createWith(mDisposable2, mCheckedDisposable2);
    DisposableCollection parentCollection = createWith(mDisposable1, mCheckedDisposable1, childCollection);

    parentCollection.dispose();

    verify(mDisposable1).dispose();
    verify(mDisposable2).dispose();
    verify(mCheckedDisposable1).dispose();
    verify(mCheckedDisposable2).dispose();
    verifyNoMoreInteractions(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
  }

  @Test
  public void testParentChildFlushNotDisposed() {
    DisposableCollection childCollection = createWith(mDisposable2, mCheckedDisposable2);
    DisposableCollection parentCollection = createWith(mDisposable1, mCheckedDisposable1, childCollection);

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
    DisposableCollection childCollection = createWith(mDisposable2, mCheckedDisposable2);
    DisposableCollection parentCollection = createWith(mDisposable1, mCheckedDisposable1, childCollection);

    parentCollection.flushDisposed();

    verify(mCheckedDisposable1).isDisposed();
    verify(mCheckedDisposable2).isDisposed();

    parentCollection.dispose();

    verify(mDisposable1).dispose();
    verify(mDisposable2).dispose();
    verifyNoMoreInteractions(mDisposable1, mDisposable2, mCheckedDisposable1, mCheckedDisposable2);
  }
}
