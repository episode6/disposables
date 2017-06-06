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
public class ForgetfulDisposableCollection<V> implements HasDisposables {

  private transient volatile boolean mIsDisposed = false;
  private final List<V> mList;
  private final boolean mDisposeOnFlush;

  /**
   * @param disposeOnFlush Whether or not this collection should dispose itself after a call
   *                       to {@link #flushDisposed()} that results in this collection being
   *                       empty.
   * @param prefill A collection of objects to prefil the collection with.
   */
  public ForgetfulDisposableCollection(boolean disposeOnFlush, @Nullable Collection<V> prefill) {
    mList = prefill == null ? new LinkedList<V>() : new LinkedList<V>(prefill);
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
      getListOrThrow().add(obj);
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
      Collections.addAll(getListOrThrow(), objs);
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
    if (mIsDisposed) {
      return true;
    }

    synchronized (this) {
      if (mIsDisposed) {
        return true;
      }

      for (Iterator<V> iterator = mList.iterator(); iterator.hasNext();) {
        if (MaybeDisposables.isFlushable(iterator.next())) {
          iterator.remove();
        }
      }

      if (mDisposeOnFlush && mList.isEmpty()) {
        dispose();
        return true;
      }
      return false;
    }
  }

  @Override
  public void dispose() {
    if (mIsDisposed) {
      return;
    }
    synchronized (this) {
      if (mIsDisposed) {
        return;
      }
      mIsDisposed = true;
    }
    if (mList.isEmpty()) {
      return;
    }

    for (ListIterator<V> iterator = mList.listIterator(mList.size()); iterator.hasPrevious();) {
      MaybeDisposables.dispose(iterator.previous());
    }
    mList.clear();
  }

  protected List<V> getListOrThrow() {
    if (mIsDisposed) {
      throw new IllegalStateException(
          "Attempted to interact with disposable after it's been disposed: " + toString());
    }
    return mList;
  }

  protected @Nullable List<V> getListOrNull() {
    return mIsDisposed ? null : mList;
  }
}
