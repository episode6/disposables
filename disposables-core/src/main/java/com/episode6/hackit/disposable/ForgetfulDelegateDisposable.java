package com.episode6.hackit.disposable;

import javax.annotation.Nullable;

/**
 * An implementation of {@link Disposable} that can be subclassed or used directly.
 * Takes a delegate object type V and stores a strong reference to it until this
 * object is disposed. Upon dispose, this object will release its strong reference
 * and if the delegate object itself implements {@link Disposable}, then dispose will
 * be called on the object.
 */
public class ForgetfulDelegateDisposable<V> implements Disposable {

  private transient volatile boolean mIsDisposed;
  private @Nullable V mDelegate;

  public ForgetfulDelegateDisposable(V delegate) {
    mIsDisposed = false;
    mDelegate = delegate;
  }

  /**
   * Dispose the delegate object
   */
  @Override
  public void dispose() {
    MaybeDisposables.dispose(markDisposed());
  }

  /**
   * For use by subclasses - get an instance of the delegate without
   * marking this object as disposed.
   * @return The delegate or null if we've already been disposed.
   */
  protected final @Nullable V getDelegateOrNull() {
    if (mIsDisposed) {
      return null;
    }

    synchronized (this) {
      if (mIsDisposed) {
        return null;
      }

      return mDelegate;
    }
  }

  /**
   * For use by subclasses - get an instance of the delegate without
   * marking this object as disposed, or throw an IllegalStateException
   * if the object has already been disposed.
   * @return The delegate or null if we've already been disposed.
   */
  protected final V getDelegateOrThrow() {
    final V delegate = getDelegateOrNull();
    if (delegate == null) {
      throw new IllegalStateException(
          "Attempted to interact with disposable after it's been disposed: " + toString());
    }
    return delegate;
  }

  /**
   * For use by subclasses - mark this object as disposed and return an
   * instance of the delegate. All subsequent calls to {@link #markDisposed()}
   * and {@link #getDelegateOrNull()} will return null after this method has been
   * called once.
   *
   * This method is package-protected as it should not be used in direct subclasses
   * of {@link ForgetfulDelegateDisposable}. Only subclasses of {@link ForgetfulDelegateCheckedDisposable}
   * should use this method.
   *
   * If calling this method from a subclass, its expected that you will pass the returned
   * object to {@link MaybeDisposables#dispose(Object)} when you are finished with it.
   *
   * @return The delegate or null if we've already been disposed.
   */
   @Nullable V markDisposed() {
    if (mIsDisposed) {
      return null;
    }

    synchronized (this) {
      if (mIsDisposed) {
        return null;
      }
      mIsDisposed = true;
      final V instance = mDelegate;
      mDelegate = null;
      return instance;
    }
  }
}
