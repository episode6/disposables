package com.episode6.hackit.disposable;

import javax.annotation.Nullable;

/**
 * An implementation of {@link CheckedDisposable} that can be subclassed or used directly.
 * Respects the same rules as {@link ForgetfulDelegateDisposable} but will also check if the delegate
 * implements {@link CheckedDisposable} when {@link #isDisposed()} is called
 */
public class ForgetfulDelegateCheckedDisposable<V> extends ForgetfulDelegateDisposable<V> implements CheckedDisposable {

  public ForgetfulDelegateCheckedDisposable(V delegate) {
    super(delegate);
  }

  /**
   * Check if this or the delegate has been disposed already
   * @return true if this object (or its delegate) has been disposed, false otherwise.
   */
  @Override
  public boolean isDisposed() {
    return isObjectDisposed(getDelegateOrNull());
  }

  @Override
  protected final @Nullable V markDisposed() {
    return super.markDisposed();
  }

  /**
   * For use by subclasses - Checks if an object should be considered disposed or not.
   * The object is disposed if it is null OR it implements {@link CheckedDisposable}
   * and {@link CheckedDisposable#isDisposed()} returns true
   * @param object The object to check the disposal status of
   * @return true if the object is considered disposed, false otherwise.
   */
  protected final static boolean isObjectDisposed(@Nullable Object object) {
    return object == null ||
        (object instanceof CheckedDisposable && ((CheckedDisposable) object).isDisposed());
  }
}
