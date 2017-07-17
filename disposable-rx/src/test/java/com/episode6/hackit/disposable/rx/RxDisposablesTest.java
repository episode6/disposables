package com.episode6.hackit.disposable.rx;

import com.episode6.hackit.disposable.CheckedDisposable;
import io.reactivex.disposables.Disposable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RxDisposablesTest {

  @Mock CheckedDisposable mCheckedDisposable;
  @Mock Disposable mRxDisposable;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testWrapRx() {
    CheckedDisposable checkedDisposable = RxDisposables.convertFromRx(mRxDisposable);

    assertThat(checkedDisposable.isDisposed()).isFalse();
    verify(mRxDisposable).isDisposed();

    checkedDisposable.dispose();
    verify(mRxDisposable).dispose();

    verifyNoMoreInteractions(mRxDisposable);
  }

  @Test
  public void testWrapChecked() {
    Disposable rxDisposable = RxDisposables.convertToRx(mCheckedDisposable);

    assertThat(rxDisposable.isDisposed()).isFalse();
    verify(mCheckedDisposable).isDisposed();

    rxDisposable.dispose();
    verify(mCheckedDisposable).dispose();

    verifyNoMoreInteractions(mCheckedDisposable);
  }
}
