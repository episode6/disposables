package com.episode6.hackit.disposable.rx;

import com.episode6.hackit.disposable.CheckedDisposable;

public class RxDisposables {

  public static CheckedDisposable convertFromRx(final io.reactivex.disposables.Disposable rxDisposable) {
    return new CheckedDisposable() {
      @Override
      public boolean isDisposed() {
        return rxDisposable.isDisposed();
      }

      @Override
      public void dispose() {
        rxDisposable.dispose();
      }
    };
  }

  public static io.reactivex.disposables.Disposable convertToRx(final CheckedDisposable checkedDisposable) {
    return new io.reactivex.disposables.Disposable() {
      @Override
      public void dispose() {
        checkedDisposable.dispose();
      }

      @Override
      public boolean isDisposed() {
        return checkedDisposable.isDisposed();
      }
    };
  }
}
