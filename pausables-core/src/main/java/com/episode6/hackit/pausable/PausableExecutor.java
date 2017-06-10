package com.episode6.hackit.pausable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * A wrapper for {@link Executor} that implements {@link Pausable}
 */
public interface PausableExecutor extends Pausable, Executor {
}
