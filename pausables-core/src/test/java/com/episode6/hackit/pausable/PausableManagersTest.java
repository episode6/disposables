package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.AbstractDelegateDisposable;
import com.episode6.hackit.disposable.DisposableManager;
import com.episode6.hackit.disposable.Disposables;
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
 * Tests {@link PausableManager}s
 */
public class PausableManagersTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Pausable mPausable;
  @Mock DisposablePausable mDisposablePausable;
  @Mock CheckedDisposablePausable mCheckedDisposablePausable;

  @Test
  public void testStandalonePause() {
    PausableManager collection = Pausables.newStandaloneManager(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.pause();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).pause();
    inOrder.verify(mDisposablePausable).pause();
    inOrder.verify(mPausable).pause();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testStandaloneResume() {
    PausableManager collection = Pausables.newStandaloneManager(mPausable, mDisposablePausable, mCheckedDisposablePausable);

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
    PausableManager collection = Pausables.newStandaloneManager(mCheckedDisposablePausable);

    assertThat(collection.flushDisposed()).isFalse();

    verify(mCheckedDisposablePausable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposablePausable);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testStandaloneDispose() throws NoSuchFieldException, IllegalAccessException {
    PausableManager collection = Pausables.newStandaloneManager(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.dispose();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).dispose();
    inOrder.verify(mDisposablePausable).dispose();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    assertThat(getInternalList(collection)).isNull();
  }

  @Test
  public void testConnectedPause() {
    DisposableManager root = Disposables.newManager();
    PausableManager collection = Pausables.newConnectedManager(root, mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.pause();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).pause();
    inOrder.verify(mDisposablePausable).pause();
    inOrder.verify(mPausable).pause();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testConnectedResume() {
    DisposableManager root = Disposables.newManager();
    PausableManager collection = Pausables.newConnectedManager(root, mPausable, mDisposablePausable, mCheckedDisposablePausable);

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
    PausableManager collection = Pausables.newConnectedManager(root, mCheckedDisposablePausable);

    assertThat(root.flushDisposed()).isFalse();

    verify(mCheckedDisposablePausable, times(2)).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposablePausable);
    assertThat(getInternalList(root)).containsOnly(collection);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testConnectedDispose() throws NoSuchFieldException, IllegalAccessException {
    DisposableManager root = Disposables.newManager();
    PausableManager collection = Pausables.newConnectedManager(root, mPausable, mDisposablePausable, mCheckedDisposablePausable);

    root.dispose();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).dispose();
    inOrder.verify(mDisposablePausable).dispose();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    assertThat(getInternalList(root)).isNull();
    assertThat(getInternalList(collection)).isNull();
  }

  @SuppressWarnings("unchecked")
  private static List<Pausable> getInternalList(Object collection)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = AbstractDelegateDisposable.class.getDeclaredField("mDelegate");
    field.setAccessible(true);
    return (List<Pausable>) field.get(collection);
  }
}
