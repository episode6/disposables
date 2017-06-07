package com.episode6.hackit.pausable;

import javax.annotation.Nullable;

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
}
