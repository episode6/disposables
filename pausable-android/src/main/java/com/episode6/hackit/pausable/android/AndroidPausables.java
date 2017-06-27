package com.episode6.hackit.pausable.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import com.episode6.hackit.pausable.Pausable;

/**
 * Utility methods to create android-specific pausables.
 */
public class AndroidPausables {

  public static Pausable forBroadcastReceiver(
      final Context context,
      final BroadcastReceiver broadcastReceiver,
      final IntentFilter intentFilter) {
    return new Pausable() {
      @Override
      public void pause() {
        context.unregisterReceiver(broadcastReceiver);
      }

      @Override
      public void resume() {
        context.registerReceiver(broadcastReceiver, intentFilter);
      }
    };
  }

  public static Pausable forBroadcastReceiver(
      final Context context,
      final BroadcastReceiver broadcastReceiver,
      final IntentFilter intentFilter,
      final String broadcastPermission,
      final Handler scheduler) {
    return new Pausable() {
      @Override
      public void pause() {
        context.unregisterReceiver(broadcastReceiver);
      }

      @Override
      public void resume() {
        context.registerReceiver(broadcastReceiver, intentFilter, broadcastPermission, scheduler);
      }
    };
  }
}
