package com.episode6.hackit.disposable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * A root collection of disposables.
 */
public class RootDisposableCollection extends ForgetfulDisposableCollection<Disposable> {

  public static RootDisposableCollection create(Disposable... disposables) {
    return new RootDisposableCollection(disposables.length == 0 ? null : Arrays.asList(disposables));
  }

  private RootDisposableCollection(@Nullable Collection<Disposable> delegate) {
    super(false, delegate);
  }
}
