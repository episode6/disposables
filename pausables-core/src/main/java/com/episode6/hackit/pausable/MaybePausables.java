package com.episode6.hackit.pausable;

import javax.annotation.Nullable;

/**
 *
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

  public static void resume(@Nullable Object object) {
    if (object == null) {
      return;
    }
    if (object instanceof Pausable) {
      ((Pausable) object).resume();
    }
  }
}
