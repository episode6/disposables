package com.episode6.hackit.disposable;

import java.lang.ref.WeakReference;

/**
 * static utility class to provide new WeakReferences.
 * Only exists so we can power-mock the weak references
 * in tests.
 */
class WeakRefProvider {
  static <T> WeakReference<T> create(T instance) {
    return new WeakReference<T>(instance);
  }
}
