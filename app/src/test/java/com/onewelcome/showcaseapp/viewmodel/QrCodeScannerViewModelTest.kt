package com.onewelcome.showcaseapp.viewmodel

import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.showcaseapp.fakes.PermissionsFacadeFake
import com.onewelcome.showcaseapp.feature.qrscanner.QrCodeScannerViewModel
import com.onewelcome.showcaseapp.feature.qrscanner.QrCodeScannerViewModel.UiEvent
import com.onewelcome.showcaseapp.feature.qrscanner.QrCodeScannerViewModel.UiState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class QrCodeScannerViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var permissionsFacadeFake: PermissionsFacadeFake

  private lateinit var viewModel: QrCodeScannerViewModel

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @Test
  fun `Given camera permission is given, When view model initialize, Then should have correct state`() {
    permissionsFacadeFake.cameraPermissionGranted = true
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(
      UiState(
        isCameraPermissionGranted = true,
        requestCameraPermission = false,
        showSettingsDialog = false
      )
    )
  }

  @Test
  fun `Given camera permission is not given, When view model initialize, Then should have correct state`() {
    permissionsFacadeFake.cameraPermissionGranted = false
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(
      UiState(
        isCameraPermissionGranted = false,
        requestCameraPermission = true,
        showSettingsDialog = false
      )
    )
  }

  @Test
  fun `Given camera permission is permanently declined, When DismissSettingsDialog event is sent, Then should not show settings dialog`() {
    permissionsFacadeFake.cameraPermissionGranted = false
    initializeViewModel()

    viewModel.onEvent(UiEvent.RequestCameraPermissionResult.PERMANENTLY_DECLINED)
    assertThat(viewModel.uiState.showSettingsDialog).isTrue
    viewModel.onEvent(UiEvent.DismissSettingsDialog)

    assertThat(viewModel.uiState.showSettingsDialog).isFalse
  }

  @Test
  fun `When UpdateCameraPermissionState event is sent, Then should check camera permission`() {
    val permissionsFacadeSpy = spy(permissionsFacadeFake)
    initializeViewModel(permissionsFacadeSpy)

    viewModel.onEvent(UiEvent.UpdateCameraPermissionState)

    verify(permissionsFacadeSpy, times(2)).checkCameraPermission()
  }

  @Test
  fun `Given camera permission was requested, When RequestPostNotificationsPermissionResult GRANTED event is sent, Then should update the state`() {
    permissionsFacadeFake.cameraPermissionGranted = false
    initializeViewModel()

    viewModel.onEvent(UiEvent.RequestCameraPermissionResult.GRANTED)

    assertThat(viewModel.uiState).isEqualTo(
      UiState(
        isCameraPermissionGranted = true,
        requestCameraPermission = false,
        showSettingsDialog = false
      )
    )
  }

  @Test
  fun `Given camera permission was requested, When RequestPostNotificationsPermissionResult DECLINED event is sent, Then should update the state`() {
    permissionsFacadeFake.cameraPermissionGranted = false
    initializeViewModel()

    viewModel.onEvent(UiEvent.RequestCameraPermissionResult.DECLINED)

    assertThat(viewModel.uiState).isEqualTo(
      UiState(
        isCameraPermissionGranted = false,
        requestCameraPermission = false,
        showSettingsDialog = false
      )
    )
  }

  @Test
  fun `Given camera permission was requested, When RequestPostNotificationsPermissionResult PERMANENTLY_DECLINED event is sent, Then should update the state`() {
    permissionsFacadeFake.cameraPermissionGranted = false
    initializeViewModel()

    viewModel.onEvent(UiEvent.RequestCameraPermissionResult.PERMANENTLY_DECLINED)

    assertThat(viewModel.uiState).isEqualTo(
      UiState(
        isCameraPermissionGranted = false,
        requestCameraPermission = false,
        showSettingsDialog = true
      )
    )
  }

  private fun initializeViewModel(permissionsFacade: PermissionsFacade = permissionsFacadeFake) {
    viewModel = QrCodeScannerViewModel(permissionsFacade)
  }
}
