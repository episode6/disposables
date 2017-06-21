package com.episode6.hackit.disposable.butterknife;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.episode6.hackit.disposable.Disposable;

/**
 * Utility methods for using ButterKnife with Disposables
 */
public class DisposableButterKnife {
  public static Disposable wrapUnbinder(final Unbinder unbinder) {
    return new Disposable() {
      @Override
      public void dispose() {
        unbinder.unbind();
      }
    };
  }

  public static Disposable bind(Activity activity) {
    return wrapUnbinder(ButterKnife.bind(activity));
  }

  public static Disposable bind(View view) {
    return wrapUnbinder(ButterKnife.bind(view));
  }

  public static Disposable bind(Dialog dialog) {
    return wrapUnbinder(ButterKnife.bind(dialog));
  }

  public static Disposable bind(Object target, Activity source) {
    return wrapUnbinder(ButterKnife.bind(target, source));
  }

  public static Disposable bind(Object target, View source) {
    return wrapUnbinder(ButterKnife.bind(target, source));
  }

  public static Disposable bind(Object target, Dialog source) {
    return wrapUnbinder(ButterKnife.bind(target, source));
  }
}
