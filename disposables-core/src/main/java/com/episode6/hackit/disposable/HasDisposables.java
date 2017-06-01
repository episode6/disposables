package com.episode6.hackit.disposable;

/**
 * Applies to an object that holds references to other disposables
 */
public interface HasDisposables extends Disposable {

  /**
   * Flush any disposables from memory that are already disposed.
   * @return true if this collection is now disposed, false otherwise
   */
  boolean flushDisposed();
}
