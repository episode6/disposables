package com.episode6.hackit.disposable.future;

import com.episode6.hackit.disposable.Disposable;
import com.episode6.hackit.disposable.HasDisposables;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * An implementation of {@link ListenableFuture} that also implements {@link Disposable}
 * and {@link HasDisposables}. Any runnable added as a listener to this future will
 * be automatically wrapped in a {@link com.episode6.hackit.disposable.DisposableRunnable}
 * and added to its collection of Disposables.
 *
 * A disposable future will mark itself disposed when {@link #flushDisposed()} is called
 * if there are no disposables in its internal collection. For this reason it's vital
 * that you only add it to a {@link com.episode6.hackit.disposable.RootDisposableCollection}
 * AFTER you've finished adding listeners / callbacks to it.
 */
public interface DisposableFuture<V> extends ListenableFuture<V>, HasDisposables {
}
