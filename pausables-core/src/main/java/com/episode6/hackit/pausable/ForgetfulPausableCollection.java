package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.ForgetfulDisposableCollection;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class ForgetfulPausableCollection<V> extends ForgetfulDisposableCollection<V> implements Pausable {

  /**
   * @param disposeOnFlush Whether or not this collection should dispose itself after a call
   *                       to {@link #flushDisposed()} that results in this collection being
   *                       empty.
   * @param prefill        A collection of objects to prefil the collection with.
   */
  public ForgetfulPausableCollection(boolean disposeOnFlush, @Nullable Collection<V> prefill) {
    super(disposeOnFlush, prefill);
  }

  @Override
  public void pause() {
    synchronized (this) {
      List<V> list = getListOrThrow();
      if (list.isEmpty()) {
        return;
      }
      for (Iterator<V> iterator = list.iterator(); iterator.hasNext();) {
        MaybePausables.pause(iterator.next());
      }
    }
  }

  @Override
  public void resume() {
    synchronized (this) {
      List<V> list = getListOrThrow();
      if (list.isEmpty()) {
        return;
      }
      for (Iterator<V> iterator = list.iterator(); iterator.hasNext();) {
        MaybePausables.resume(iterator.next());
      }
    }
  }
}
