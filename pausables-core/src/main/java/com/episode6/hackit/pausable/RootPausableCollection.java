package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.Disposable;
import com.episode6.hackit.disposable.DisposableManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A root collection of pausables, optionally connected to a {@link DisposableManager}.
 * Implements {@link Pausable}, {@link Disposable} and {@link com.episode6.hackit.disposable.HasDisposables}
 *
 * While a {@link Pausable} doesn't have to implement {@link Disposable}, this collection
 * operates on the same rules as a {@link DisposableManager}. If any objects added to it
 * implement the Disposable/CheckedDisposable/HasDisposables interfaces, their respective methods
 * will be called during {@link #dispose()} and {@link #flushDisposed()}
 */
public class RootPausableCollection extends ForgetfulPausableCollection<Pausable> {

  /**
   * Create a standalone {@link RootPausableCollection}. You will have manually call {@link #dispose()}
   * and {@link #flushDisposed()} on this collection to manage its disposal state and memory (or explicitly
   * add it to another disposable collection).
   *
   * Using a standalone pausable collection can be problematic if also using a {@link DisposableManager}
   * in the same component. Since the two objects are completely seperate, you can't ensure all your disposables
   * are torn-down in the same order they're created (instead tearing down all of one collection, then the other).
   *
   * To deal with this issue use {@link #createConnected(DisposableManager, Pausable...)} instead.
   *
   * @param pausables Any pausables you want to prefil this collection with
   * @return A new RootPausableCollection
   */
  public static RootPausableCollection create(Pausable... pausables) {
    return new RootPausableCollection(pausables.length == 0 ? null : Arrays.asList(pausables));
  }

  /**
   * Create a {@link RootPausableCollection} that is connected to a {@link DisposableManager}. Any pausables
   * added to the pausable collection will also be added to disposableManager. When it's time
   * to {@link #dispose()}, you should only call it on the disposableManager, to ensure objects are torn-down
   * in the correct order. Similarly, you don't have to call {@link #flushDisposed()} on this collection directly,
   * a call to disposableManager.flushDisposed() will propagate.
   *
   * This actually creates a circular reference  where the Pausable Collection both holds a reference to the
   * disposableManager, and is also added directly to it. Both references are removed upon dispose.
   *
   * @param disposableManager The {@link DisposableManager} to connect
   * @param pausables Any pausables you want to prefil this collection with (any disposables will also be added
   *                  to disposableManager
   * @return A new {@link RootPausableCollection} that is connected to disposableManager and included the
   * contents of pausables
   */
  public static RootPausableCollection createConnected(DisposableManager disposableManager, Pausable... pausables) {
    ConnectedRootPausableCollection collection = new ConnectedRootPausableCollection(disposableManager);
    disposableManager.add(collection);
    if (pausables.length > 0) {
      collection.addAll(Arrays.asList(pausables));
    }
    return collection;
  }

  private RootPausableCollection(@Nullable Collection<Pausable> prefill) {
    super(false, prefill);
  }

  private static class ConnectedRootPausableCollection extends RootPausableCollection {

    private @Nullable DisposableManager mDisposableManager;

    private ConnectedRootPausableCollection(DisposableManager rootDisposableCollection) {
      super(null);
      mDisposableManager = rootDisposableCollection;
    }

    @Override
    public void add(Pausable obj) {
      synchronized (this) {
        getListOrThrow().add(obj);
        if (obj instanceof Disposable) {
          getRootOrThrow().add((Disposable) obj);
        }
      }
    }

    @Override
    public void addAll(Collection<Pausable> objs) {
      if (objs.isEmpty()) {
        return;
      }
      synchronized (this) {
        getListOrThrow().addAll(objs);
        getRootOrThrow().addAll(findDisposables(objs));
      }
    }

    @Override
    public void dispose() {
      List<Pausable> list = getListOrNull();
      if (list == null) {
        return;
      }
      synchronized (this) {
        list = getListOrNull();
        if (list == null) {
          return;
        }
        list.clear();
        mDisposableManager = null;
        super.dispose();
      }
    }

    private DisposableManager getRootOrThrow() {
      DisposableManager collection = mDisposableManager;
      if (collection == null) {
        throw new NullPointerException("DisposableManager should not be null");
      }
      return collection;
    }

    private static List<Disposable> findDisposables(Collection<Pausable> objs) {
      List<Disposable> disposables = new LinkedList<>();
      for (Pausable pausable : objs) {
        if (pausable instanceof Disposable) {
          disposables.add((Disposable) pausable);
        }
      }
      return disposables;
    }
  }
}
