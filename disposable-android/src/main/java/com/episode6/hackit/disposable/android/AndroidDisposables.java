package com.episode6.hackit.disposable.android;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.os.HandlerThread;
import com.episode6.hackit.disposable.CheckedDisposable;
import com.episode6.hackit.disposable.CheckedDisposer;
import com.episode6.hackit.disposable.Disposable;
import com.episode6.hackit.disposable.Disposables;

public class AndroidDisposables {

  @SuppressWarnings("deprecation")
  public static CheckedDisposable forHandlerThread(final HandlerThread thread) {
    return new CheckedDisposable() {
      @Override
      public boolean isDisposed() {
        return thread.getLooper() == null;
      }

      @Override
      public void dispose() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
          thread.quitSafely();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
          thread.quit();
        } else {
          thread.stop();
        }
      }
    };
  }

  public static CheckedDisposable forDialog(Dialog dialog) {
    return Disposables.weak(dialog, new CheckedDisposer<Dialog>() {
      @Override
      public boolean isInstanceDisposed(Dialog instance) {
        return !instance.isShowing();
      }

      @Override
      public void disposeInstance(Dialog instance) {
        instance.dismiss();
      }
    });
  }

  public static Disposable forBroadcastReceiver(final Context context, final BroadcastReceiver broadcastReceiver) {
    return new Disposable() {
      @Override
      public void dispose() {
        context.unregisterReceiver(broadcastReceiver);
      }
    };
  }
}
