package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.DisposableManager;

/**
 * Interface for a manager of a collection of pausables and disposables. Any pausables
 * added to a PausableDisposableManager should also be disposed of / flushed if
 * they happen to implement {@link com.episode6.hackit.disposable.Disposable} or
 * {@link com.episode6.hackit.disposable.CheckedDisposable}. Similarly, any Disposables
 * added to this manager should be paused/resumed if they happen to implement pausable.
 */
public interface PausableDisposableManager extends DisposableManager, PausableManager {
}
