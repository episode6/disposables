package com.episode6.hackit.pausable.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import com.episode6.hackit.pausable.Pausable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link AndroidPausables}
 */
public class AndroidPausablesTest {

  @Mock Context mContext;
  @Mock BroadcastReceiver mBroadcastReceiver;
  @Mock IntentFilter mIntentFilter;
  @Mock Handler mHandler;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testBroadcastReceiverSimple() {
    Pausable pausable = AndroidPausables.forBroadcastReceiver(mContext, mBroadcastReceiver, mIntentFilter);

    pausable.resume();
    verify(mContext).registerReceiver(mBroadcastReceiver, mIntentFilter);

    pausable.pause();
    verify(mContext).unregisterReceiver(mBroadcastReceiver);
  }

  @Test
  public void testBroadcastReceiverComplex() {
    Pausable pausable = AndroidPausables.forBroadcastReceiver(
        mContext,
        mBroadcastReceiver,
        mIntentFilter,
        "fake_permission",
        mHandler);

    pausable.resume();
    verify(mContext).registerReceiver(mBroadcastReceiver, mIntentFilter, "fake_permission", mHandler);

    pausable.pause();
    verify(mContext).unregisterReceiver(mBroadcastReceiver);
  }
}
