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

  static PausableManager create(Pausable... pausables) {
    return Pausables.newManager(pausables);
  }

  static PausableDisposableManager createDisposable(Pausable... pausables) {
    PausableDisposableManager manager = Pausables.newDisposableManager();
    for (Pausable p : pausables) {
      manager.addPausable(p);
    }
    return manager;
  }

  @Test
  public void testStandalonePause() {
    PausableManager collection = create(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.pause();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).pause();
    inOrder.verify(mDisposablePausable).pause();
    inOrder.verify(mPausable).pause();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testStandaloneResume() {
    PausableManager collection = create(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.resume();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mPausable).resume();
    inOrder.verify(mDisposablePausable).resume();
    inOrder.verify(mCheckedDisposablePausable).resume();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testDisposablePause() {
    PausableDisposableManager collection = createDisposable(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.pause();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).pause();
    inOrder.verify(mDisposablePausable).pause();
    inOrder.verify(mPausable).pause();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testDisposableResume() {
    PausableDisposableManager collection = createDisposable(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.resume();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mPausable).resume();
    inOrder.verify(mDisposablePausable).resume();
    inOrder.verify(mCheckedDisposablePausable).resume();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
  }

  @Test
  public void testDisposableFlush() throws NoSuchFieldException, IllegalAccessException {
    when(mCheckedDisposablePausable.isDisposed()).thenReturn(true);
    PausableDisposableManager collection = createDisposable(mCheckedDisposablePausable);

    assertThat(collection.flushDisposed()).isFalse();

    verify(mCheckedDisposablePausable).isDisposed();
    verifyNoMoreInteractions(mCheckedDisposablePausable);
    assertThat(getPausableList(collection)).isEmpty();
  }

  @Test
  public void testDisposableDispose() throws NoSuchFieldException, IllegalAccessException {
    PausableDisposableManager collection = createDisposable(mPausable, mDisposablePausable, mCheckedDisposablePausable);

    collection.dispose();

    InOrder inOrder = Mockito.inOrder(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    inOrder.verify(mCheckedDisposablePausable).dispose();
    inOrder.verify(mDisposablePausable).dispose();
    verifyNoMoreInteractions(mPausable, mDisposablePausable, mCheckedDisposablePausable);
    assertThat(getPausableList(collection)).isNull();
  }

  @SuppressWarnings("unchecked")
  private static List<Pausable> getPausableList(PausableDisposableManager collection)
      throws NoSuchFieldException, IllegalAccessException {
    Field pausableManagerField = collection.getClass().getDeclaredField("mPausableManager");
    pausableManagerField.setAccessible(true);
    PausableManager pausableManager = (PausableManager) pausableManagerField.get(collection);
    return getInternalList(pausableManager);
  }

  @SuppressWarnings("unchecked")
  private static List<Pausable> getInternalList(PausableManager pausableManager)
      throws NoSuchFieldException, IllegalAccessException {
    Field listField = AbstractDelegateDisposable.class.getDeclaredField("mDelegate");
    listField.setAccessible(true);
    return (List<Pausable>) listField.get(pausableManager);
  }
}
