package com.episode6.hackit.disposable;

import javax.annotation.Nullable;

/**
 * An implementation of {@link Disposable} that can be subclassed or used directly.
 * Takes a delegate object type V and stores a strong reference to it until this
 * object is disposed. Upon dispose, this object will release its strong reference
 * and if the delegate object itself implements {@link Disposable}, then dispose will
 * be called on the object.
 */
public class DelegateDisposable<V> implements Disposable {
  private transient volatile boolean mIsDisposed;
  private @Nullable V mDelegate;

  public DelegateDisposable(V delegate) {
    mIsDisposed = false;
    mDelegate = delegate;
  }

  /**
   * Dispose the delegate object
   */
  @Override
  public void dispose() {
    disposeObjectIfNeeded(markDisposed());
  }

  /**
   * For use by subclasses - get an instance of the delegate without
   * marking this object as disposed.
   * @return The delegate or null if we've already been disposed.
   */
  protected final @Nullable V getDelegate() {
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
   * For use by subclasses - mark this object as disposed and return an
   * instance of the delegate. All subsequent calls to {@link #markDisposed()}
   * and {@link #getDelegate()} will return null after this method has been
   * called once.
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

  /**
   * For use by subclasses - checks if object implements {@link Disposable}
   * and disposes it if it does.
   * @param object The object to dispose.
   */
  protected final static void disposeObjectIfNeeded(@Nullable Object object) {
    if (object != null && object instanceof Disposable) {
      ((Disposable) object).dispose();
    }
  }
}
