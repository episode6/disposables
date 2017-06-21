package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.AbstractDelegateDisposable;
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
 * Tests {@link PausableDisposableManager}s
 */
public class PausableDisposableManagersTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock Pausable mPausable;
  @Mock DisposablePausable mDisposablePausable;
  @Mock CheckedDisposablePausable mCheckedDisposablePausable;

  static PausableDisposableManager create(Pausable... pausables) {
    PausableDisposableManager manager = Pausables.newDisposableManager();
    for (Pausable p : pausables) {
      manager.addPausable(p);
    }
    return manager;
  }

  @Test
  public void testStandalonePause() {
    PausableDisposableManager collection = create(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.pause();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).pause();
    inOrder.verify(mDisposablePausable).pause();
    inOrder.verify(mPausable).pause();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testStandaloneResume() {
    PausableDisposableManager collection = create(mPausable, mDisposablePausable, mCheckedDisposablePausable);

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
    PausableDisposableManager collection = create(mCheckedDisposablePausable);

    assertThat(collection.flushDisposed()).isFalse();

    verify(mCheckedDisposablePausable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposablePausable);
    assertThat(getInternalList(collection)).isEmpty();
  }

  @Test
  public void testStandaloneDispose() throws NoSuchFieldException, IllegalAccessException {
    PausableDisposableManager collection = create(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.dispose();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).dispose();
    inOrder.verify(mDisposablePausable).dispose();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
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
