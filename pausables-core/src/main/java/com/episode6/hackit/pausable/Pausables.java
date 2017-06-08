package com.episode6.hackit.pausable;

import com.episode6.hackit.disposable.Disposable;
import com.episode6.hackit.disposable.DisposableManager;
import com.episode6.hackit.disposable.Disposer;
import com.episode6.hackit.disposable.MaybeDisposables;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class containing static methods to create pausables
 */
public class Pausables {

  /**
   * Create a standalone {@link PausableManager}. You will have manually call {@link PausableManager#dispose()}
   * and {@link PausableManager#flushDisposed()} on this object to manage its disposal state and memory (or explicitly
   * add it to another disposable collection).
   *
   * Using a standalone pausable manager can be problematic if also using a {@link DisposableManager}
   * in the same component. Since the two objects are completely seperate, you can't ensure all your disposables
   * are torn-down in the same order they're created (instead tearing down all of one collection, then the other).
   *
   * To deal with this issue use {@link #newConnectedManager(DisposableManager, Pausable...)} instead.
   *
   * @param pausables Any pausables you want to prefil this manager with
   * @return A new PausableManager
   */
  public static PausableManager newStandaloneManager(Pausable... pausables) {
    return new StandalonePausableManager(pausables.length == 0 ? null : Arrays.asList(pausables));
  }

  /**
   * Create a {@link PausableManager} that is connected to a {@link DisposableManager}. Any pausables
   * added to the pausable collection will also be added to disposableManager. You should NOT call
   * {@link PausableManager#dispose()} or {@link PausableManager#flushDisposed()} directly on the returned
   * manager, as those calls will propogate when you call {@link DisposableManager#dispose()} and
   * {@link DisposableManager#flushDisposed()}
   *
   * This actually creates a circular reference  where the Pausable Collection both holds a reference to the
   * disposableManager, and is also added directly to it. Both references are removed upon dispose.
   *
   * @param disposableManager The {@link DisposableManager} to connect
   * @param pausables Any pausables you want to prefil this collection with (any disposables will also be added
   *                  to disposableManager
   * @return A new {@link PausableManager} that is connected to disposableManager and included the
   * contents of pausables
   */
  public static PausableManager newConnectedManager(DisposableManager disposableManager, Pausable... pausables) {
    ConnectedPausableManager pausableManager = new ConnectedPausableManager(disposableManager);
    disposableManager.add(pausableManager);
    if (pausables.length > 0) {
      pausableManager.addAll(Arrays.asList(pausables));
    }
    return pausableManager;
  }

  public static <T> CheckedDisposablePausable weak(T instance, Pauser<T> pauser) {
    return weak(instance, pauser, null);
  }

  public static <T> CheckedDisposablePausable weak(T instance, Pauser<T> pauser, @Nullable Disposer<T> disposer) {
    return new WeakDisposablePausable<>(instance, pauser, disposer);
  }

  private static class WeakDisposablePausable<V> implements CheckedDisposablePausable {

    final WeakReference<V> mWeakReference;
    final Pauser<V> mPauser;
    final @Nullable Disposer<V> mDisposer;

    private WeakDisposablePausable(
        V instance,
        Pauser<V> pauser,
        @Nullable Disposer<V> disposer) {
      mWeakReference = new WeakReference<V>(instance);
      mPauser = pauser;
      mDisposer = disposer;
    }

    @Override
    public void pause() {
      final V instance = mWeakReference.get();
      if (!MaybeDisposables.isDisposed(instance, mDisposer)) {
        MaybePausables.pause(instance, mPauser);
      }
    }

    @Override
    public void resume() {
      final V instance = mWeakReference.get();
      if (!MaybeDisposables.isDisposed(instance, mDisposer)) {
        MaybePausables.resume(instance, mPauser);
      }
    }

    @Override
    public void dispose() {
      MaybeDisposables.dispose(mWeakReference.get(), mDisposer);
      mWeakReference.clear();
    }

    @Override
    public boolean isDisposed() {
      return MaybeDisposables.isDisposed(mWeakReference.get(), mDisposer);
    }
  }

  private static class StandalonePausableManager extends ForgetfulPausableCollection<Pausable> implements PausableManager {
    StandalonePausableManager(@Nullable Collection<Pausable> prefill) {
      super(false, prefill);
    }
  }

  private static class ConnectedPausableManager extends ForgetfulPausableCollection<Pausable> implements PausableManager {
    private @Nullable DisposableManager mDisposableManager;
    public ConnectedPausableManager(DisposableManager disposableManager) {
      super(false, null);
      mDisposableManager = disposableManager;
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
