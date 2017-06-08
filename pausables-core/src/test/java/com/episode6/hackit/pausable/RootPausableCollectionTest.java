package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.DisposableManager;
import com.episode6.hackit.disposable.Disposables;
import com.episode6.hackit.disposable.ForgetfulDisposableCollection;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests {@link RootPausableCollection}
 */
public class RootPausableCollectionTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Pausable mPausable;
  @Mock DisposablePausable mDisposablePausable;
  @Mock CheckedDisposablePausable mCheckedDisposablePausable;

  @Test
  public void testStandalonePause() {
    RootPausableCollection collection = RootPausableCollection.create(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.pause();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mPausable).pause();
    inOrder.verify(mDisposablePausable).pause();
    inOrder.verify(mCheckedDisposablePausable).pause();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testStandaloneResume() {
    RootPausableCollection collection = RootPausableCollection.create(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.resume();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mPausable).resume();
    inOrder.verify(mDisposablePausable).resume();
    inOrder.verify(mCheckedDisposablePausable).resume();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testStandaloneFlush() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposablePausable.isDisposed()).thenReturn(true);
    RootPausableCollection collection = RootPausableCollection.create(mCheckedDisposablePausable);

    assertThat(collection.flushDisposed()).isFalse();

    verify(mCheckedDisposablePausable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposablePausable);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testStandaloneDispose() throws NoSuchFieldException, IllegalAccessException {
    RootPausableCollection collection = RootPausableCollection.create(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.dispose();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).dispose();
    inOrder.verify(mDisposablePausable).dispose();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testConnectedPause() {
    DisposableManager root = Disposables.newManager();
    RootPausableCollection collection = RootPausableCollection.createConnected(root, mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.pause();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mPausable).pause();
    inOrder.verify(mDisposablePausable).pause();
    inOrder.verify(mCheckedDisposablePausable).pause();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testConnectedResume() {
    DisposableManager root = Disposables.newManager();
    RootPausableCollection collection = RootPausableCollection.createConnected(root, mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.resume();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mPausable).resume();
    inOrder.verify(mDisposablePausable).resume();
    inOrder.verify(mCheckedDisposablePausable).resume();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testConnectedFlush() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposablePausable.isDisposed()).thenReturn(true);
    DisposableManager root = Disposables.newManager();
    RootPausableCollection collection = RootPausableCollection.createConnected(root, mCheckedDisposablePausable);

    assertThat(root.flushDisposed()).isFalse();

    verify(mCheckedDisposablePausable, times(2)).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposablePausable);
    assertThat(getInternalList(root)).containsOnly(collection);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testConnectedDispose() throws NoSuchFieldException, IllegalAccessException {
    DisposableManager root = Disposables.newManager();
    RootPausableCollection collection = RootPausableCollection.createConnected(root, mPausable, mDisposablePausable, mCheckedDisposablePausable);

    root.dispose();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).dispose();
    inOrder.verify(mDisposablePausable).dispose();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    assertThat(getInternalList(root)).isEmpty();
    assertThat(getInternalList(collection)).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private static List<Object> getInternalList(Object collection)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = ForgetfulDisposableCollection.class.getDeclaredField("mList");
    field.setAccessible(true);
    return (List<Object>) field.get(collection);
  }
}
