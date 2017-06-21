package com.episode6.hackit.disposable.butterknife;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.episode6.hackit.disposable.Disposable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Tests {@link DisposableButterKnife}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ButterKnife.class)
public class DisposableButterKnifeTest {

  @Mock Unbinder mUnbinder;
  @Mock View mView;
  @Mock Activity mActivity;
  @Mock Dialog mDialog;

  final Object mTarget = new Object();

  @Before
  public void setup() {
    mockStatic(ButterKnife.class);
    when(ButterKnife.bind(any(View.class))).thenReturn(mUnbinder);
    when(ButterKnife.bind(any(Activity.class))).thenReturn(mUnbinder);
    when(ButterKnife.bind(any(Dialog.class))).thenReturn(mUnbinder);
    when(ButterKnife.bind(any(Object.class), any(View.class))).thenReturn(mUnbinder);
    when(ButterKnife.bind(any(Object.class), any(Activity.class))).thenReturn(mUnbinder);
    when(ButterKnife.bind(any(Object.class), any(Dialog.class))).thenReturn(mUnbinder);
  }

  @Test
  public void testWrapUnbinder() {
    Disposable disposable = DisposableButterKnife.wrapUnbinder(mUnbinder);
    verifyNoMoreInteractions(mUnbinder);

    disposable.dispose();

    verify(mUnbinder).unbind();
  }

  @Test
  public void testBindView() {
    Disposable disposable = DisposableButterKnife.bind(mView);

    verifyStatic();
    ButterKnife.bind(mView);
    verifyNoMoreInteractions(mUnbinder);

    disposable.dispose();
    verify(mUnbinder).unbind();
  }

  @Test
  public void testBindActivity() {
    Disposable disposable = DisposableButterKnife.bind(mActivity);

    verifyStatic();
    ButterKnife.bind(mActivity);
    verifyNoMoreInteractions(mUnbinder);

    disposable.dispose();
    verify(mUnbinder).unbind();
  }

  @Test
  public void testBindDialog() {
    Disposable disposable = DisposableButterKnife.bind(mDialog);

    verifyStatic();
    ButterKnife.bind(mDialog);
    verifyNoMoreInteractions(mUnbinder);

    disposable.dispose();
    verify(mUnbinder).unbind();
  }

  @Test
  public void testBindTargetAndView() {
    Disposable disposable = DisposableButterKnife.bind(mTarget, mView);

    verifyStatic();
    ButterKnife.bind(mTarget, mView);
    verifyNoMoreInteractions(mUnbinder);

    disposable.dispose();
    verify(mUnbinder).unbind();
  }

  @Test
  public void testBindTargetAndActivity() {
    Disposable disposable = DisposableButterKnife.bind(mTarget, mActivity);

    verifyStatic();
    ButterKnife.bind(mTarget, mActivity);
    verifyNoMoreInteractions(mUnbinder);

    disposable.dispose();
    verify(mUnbinder).unbind();
  }

  @Test
  public void testBindTargetAndDialog() {
    Disposable disposable = DisposableButterKnife.bind(mTarget, mDialog);

    verifyStatic();
    ButterKnife.bind(mTarget, mDialog);
    verifyNoMoreInteractions(mUnbinder);

    disposable.dispose();
    verify(mUnbinder).unbind();
  }
}
