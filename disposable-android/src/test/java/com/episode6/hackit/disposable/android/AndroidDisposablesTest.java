package com.episode6.hackit.disposable.android;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;
import com.episode6.hackit.disposable.CheckedDisposable;
import com.episode6.hackit.disposable.Disposable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Build.VERSION.class, HandlerThread.class})
public class AndroidDisposablesTest {

  @Mock HandlerThread mHandlerThread;
  @Mock Looper mLooper;
  @Mock Dialog mDialog;
  @Mock Context mContext;
  @Mock BroadcastReceiver mBroadcastReceiver;

  @Test
  public void testHandlerThreadNotDisposed() {
    CheckedDisposable disposable = AndroidDisposables.forHandlerThread(mHandlerThread);
    when(mHandlerThread.getLooper()).thenReturn(mLooper);

    assertThat(disposable.isDisposed()).isFalse();
    verify(mHandlerThread).getLooper();
  }

  @Test
  public void testHandlerThreadDisposed() {
    CheckedDisposable disposable = AndroidDisposables.forHandlerThread(mHandlerThread);
    when(mHandlerThread.getLooper()).thenReturn(null);

    assertThat(disposable.isDisposed()).isTrue();
    verify(mHandlerThread).getLooper();
  }

  @Test
  public void testHandlerThreadBelow18() {
    setSdkVersion(17);
    CheckedDisposable disposable = AndroidDisposables.forHandlerThread(mHandlerThread);

    disposable.dispose();
    verify(mHandlerThread).quit();
  }

  @Test
  public void testHandlerThreadAbove18() {
    setSdkVersion(18);
    CheckedDisposable disposable = AndroidDisposables.forHandlerThread(mHandlerThread);

    disposable.dispose();
    verify(mHandlerThread).quitSafely();
  }

  @Test
  public void testDialogIsDisposed() {
    CheckedDisposable disposable = AndroidDisposables.forDialog(mDialog);
    when(mDialog.isShowing()).thenReturn(false);

    assertThat(disposable.isDisposed()).isTrue();
    verify(mDialog).isShowing();
  }

  @Test
  public void testDialogNotDisposed() {
    CheckedDisposable disposable = AndroidDisposables.forDialog(mDialog);
    when(mDialog.isShowing()).thenReturn(true);

    assertThat(disposable.isDisposed()).isFalse();
    verify(mDialog).isShowing();
  }

  @Test
  public void testDisposeDialog() {
    CheckedDisposable disposable = AndroidDisposables.forDialog(mDialog);
    when(mDialog.isShowing()).thenReturn(true);

    disposable.dispose();

    verify(mDialog).isShowing();
    verify(mDialog).dismiss();
  }

  @Test
  public void testBroadcastReceiver() {
    Disposable disposable = AndroidDisposables.forBroadcastReceiver(mContext, mBroadcastReceiver);

    disposable.dispose();

    verify(mContext).unregisterReceiver(mBroadcastReceiver);
  }

  private static void setSdkVersion(int sdkVersion) {
    Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", sdkVersion);
  }
}
