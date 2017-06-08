package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.CheckedDisposable;

/**
 * Interface the extends both {@link CheckedDisposable} and {@link Pausable}
 */
public interface CheckedDisposablePausable extends DisposablePausable, CheckedDisposable {
}
