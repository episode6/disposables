package com.episode6.hackit.pausable;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Utility methods for dealing with objects that might be pausable.
 */
public class MaybePausables {

  public static void pause(@Nullable Object object) {
    if (object == null) {
      return;
    }
    if (object instanceof Pausable) {
      ((Pausable) object).pause();
    }
  }

  public static <T> void pause(@Nullable T object, @Nullable Pauser<T> pauser) {
    if (object == null) {
      return;
    }
    if (pauser != null) {
      pauser.pauseInstance(object);
    }
    pause(object);
  }

  public static void pauseList(@Nullable List list) {
    if (list == null || list.isEmpty()) {
      return;
    }

    for (ListIterator iterator = list.listIterator(list.size()); iterator.hasPrevious();) {
      pause(iterator.previous());
    }
  }

  public static void resume(@Nullable Object object) {
    if (object == null) {
      return;
    }
    if (object instanceof Pausable) {
      ((Pausable) object).resume();
    }
  }

  public static <T> void resume(@Nullable T object, @Nullable Pauser<T> pauser) {
    if (object == null) {
      return;
    }
    if (pauser != null) {
      pauser.resumeInstance(object);
    }
    resume(object);
  }

  public static void resumeList(@Nullable List list) {
    if (list == null || list.isEmpty()) {
      return;
    }

    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      resume(iterator.next());
    }
  }
}
