package com.episode6.hackit.disposable;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Utility methods for dealing with objects that might be disposable.
 */
public class MaybeDisposables {

  public static void dispose(@Nullable Object maybeDisposable) {
    if (maybeDisposable == null) {
      return;
    }
    if (maybeDisposable instanceof Disposable) {
      ((Disposable) maybeDisposable).dispose();
    }
  }

  public static <T> void dispose(@Nullable T maybeDisposable, @Nullable Disposer<T> disposer) {
    if (isDisposed(maybeDisposable, disposer)) {
      return;
    }

    if (disposer != null) {
      disposer.disposeInstance(maybeDisposable);
    }
    dispose(maybeDisposable);
  }

  public static void disposeList(@Nullable List list) {
    if (list == null || list.isEmpty()) {
      return;
    }

    for (ListIterator iterator = list.listIterator(list.size()); iterator.hasPrevious();) {
      dispose(iterator.previous());
    }
    list.clear();
  }

  public static boolean isDisposed(@Nullable Object maybeDisposed) {
    return maybeDisposed == null ||
        (isCheckedDisposable(maybeDisposed) && ((CheckedDisposable) maybeDisposed).isDisposed());
  }

  public static <T> boolean isDisposed(@Nullable T maybeDisposed, @Nullable Disposer<T> disposer) {
    if (maybeDisposed == null) {
      return true;
    }

    if (disposer != null && disposer instanceof CheckedDisposer) {
      return ((CheckedDisposer<T>)disposer).isInstanceDisposed(maybeDisposed) &&
          (!isCheckedDisposable(maybeDisposed) || isDisposed(maybeDisposed));
    }
    return isDisposed(maybeDisposed);
  }

  public static boolean isFlushable(@Nullable Object maybeFlushable) {
    return isDisposed(maybeFlushable) ||
        maybeFlushable instanceof HasDisposables && ((HasDisposables) maybeFlushable).flushDisposed();
  }

  public static void flushList(@Nullable List list) {
    if (list == null || list.isEmpty()) {
      return;
    }

    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      if (isFlushable(iterator.next())) {
        iterator.remove();
      }
    }
  }

  private static boolean isCheckedDisposable(Object object) {
    return object instanceof CheckedDisposable;
  }
}
