package com.episode6.hackit.disposable;

import javax.annotation.Nullable;
import java.util.*;

/**
 * An implementation of a (potentially) disposable collection of objects. The type of V does not have to
 * implement {@link Disposable} directly, ForgetfulDisposableCollection will scan the objects in its list
 * in {@link #flushDisposed()} and {@link #dispose()}, and do instanceof checks for {@link CheckedDisposable} and
 * {@link Disposable} (respectively).
 *
 * Note: the collection is actually a List since we need to ensure proper disposal ordering, but since callers
 * of this class have no access to its contents, the term Collection seemed more appropriate.
 */
public class ForgetfulDisposableCollection<V>
    extends ForgetfulDelegateDisposable<List<V>>
    implements HasDisposables {

  private final boolean mDisposeOnFlush;

  /**
   * @param disposeOnFlush Whether or not this collection should dispose itself after a call
   *                       to {@link #flushDisposed()} that results in this collection being
   *                       empty.
   * @param prefill A collection of objects to prefil the collection with.
   */
  public ForgetfulDisposableCollection(boolean disposeOnFlush, @Nullable Collection<V> prefill) {
    super(prefill == null ? new LinkedList<V>() : new LinkedList<V>(prefill));
    mDisposeOnFlush = disposeOnFlush;
  }

  /**
   * Add an object to the collection.
   * @param obj The object to add
   * @param <T> The type of object being added.
   * @return obj as a convenience so this method may be in-lined.
   */
  public <T extends V> T add(T obj) {
    synchronized (this) {
      List<V> list = getDelegateOrThrow();
      list.add(obj);
    }
    return obj;
  }

  /**
   * Add multiple objects to the collection.
   * @param objs
   */
  @SuppressWarnings("unchecked")
  public void addAll(V... objs) {
    synchronized (this) {
      List<V> list = getDelegateOrThrow();
      Collections.addAll(list, objs);
    }
  }

  /**
   * Flushes this collection of any {@link CheckedDisposable}s where {@link CheckedDisposable#isDisposed()} is true
   * and {@link HasDisposables} where {@link HasDisposables#flushDisposed()} returns true
   *
   * @return true if this collection is disposed, false otherwise.
   */
  @Override
  public boolean flushDisposed() {
    synchronized (this) {
      List<V> list = getDelegateOrNull();
      if (list == null) {
        return true;
      }

      for (Iterator<V> iterator = list.iterator(); iterator.hasNext();) {
        if (shouldFlushDisposable(iterator.next())) {
          iterator.remove();
        }
      }

      if (mDisposeOnFlush && list.isEmpty()) {
        dispose();
        return true;
      }
      return false;
    }
  }

  @Override
  public void dispose() {
    final List<V> disposables = markDisposed();
    if (disposables == null || disposables.isEmpty()) {
      return;
    }

    for (ListIterator<V> iterator = disposables.listIterator(disposables.size()); iterator.hasPrevious();) {
      disposeObjectIfNeeded(iterator.previous());
    }
    disposables.clear();
  }

  protected static boolean shouldFlushDisposable(Object disposable) {
    return (disposable instanceof HasDisposables && ((HasDisposables) disposable).flushDisposed()) ||
        (disposable instanceof CheckedDisposable && ((CheckedDisposable) disposable).isDisposed());
  }
}
