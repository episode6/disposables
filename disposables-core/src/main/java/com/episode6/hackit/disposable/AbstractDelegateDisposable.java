package com.episode6.hackit.disposable;

import javax.annotation.Nullable;

/**
 * An abstract implementation of {@link Disposable}.
 * Takes a delegate object type V and stores a strong reference to it until {@link #markDisposed()}
 * is called. It's the subclasser's responsibility to implement {@link #dispose()} and call {@link #markDisposed()}
 */
public abstract class AbstractDelegateDisposable<V> implements Disposable {

  private transient volatile boolean mIsDisposed;
  private @Nullable V mDelegate;

  public AbstractDelegateDisposable(V delegate) {
    mIsDisposed = false;
    mDelegate = delegate;
  }

  /**
   * For use by subclasses - provides an unsyncronized read of our volatile
   * mIsDisposed boolean. Useful for fast returns at the top of methods when you
   * want to no-op if disposed.
   * @return true if mIsDisposed is true, false otherwise
   */
  protected final boolean isMarkedDisposed() {
    return mIsDisposed;
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
   * @return The delegate or null if we've already been disposed.
   */
   protected final @Nullable V markDisposed() {
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
