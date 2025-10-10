package com.onewelcome.showcaseapp.feature.qrscanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.showcaseapp.feature.qrscanner.QrCodeScannerViewModel.UiEvent.RequestCameraPermissionResult.DECLINED
import com.onewelcome.showcaseapp.feature.qrscanner.QrCodeScannerViewModel.UiEvent.RequestCameraPermissionResult.GRANTED
import com.onewelcome.showcaseapp.feature.qrscanner.QrCodeScannerViewModel.UiEvent.RequestCameraPermissionResult.PERMANENTLY_DECLINED
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QrCodeScannerViewModel @Inject constructor(
  private val permissionsFacade: PermissionsFacade
) : ViewModel() {

  var uiState by mutableStateOf(UiState())
    private set

  init {
    val isCameraPermissionGranted = permissionsFacade.checkCameraPermission()
    uiState = uiState.copy(
      isCameraPermissionGranted = isCameraPermissionGranted,
      requestCameraPermission = !isCameraPermissionGranted
    )
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.DismissSettingsDialog -> uiState = uiState.copy(showSettingsDialog = false)
      is UiEvent.RequestCameraPermissionResult -> onRequestCameraPermissionResult(event)
      is UiEvent.UpdateCameraPermissionState -> uiState =
        uiState.copy(isCameraPermissionGranted = permissionsFacade.checkCameraPermission())
    }
  }

  private fun onRequestCameraPermissionResult(result: UiEvent.RequestCameraPermissionResult) {
    uiState = when (result) {
      GRANTED -> uiState.copy(isCameraPermissionGranted = true)
      DECLINED -> uiState.copy(isCameraPermissionGranted = false)
      PERMANENTLY_DECLINED -> uiState.copy(showSettingsDialog = true, isCameraPermissionGranted = false)
    }.copy(requestCameraPermission = false)
  }

  data class UiState(
    val isCameraPermissionGranted: Boolean = false,
    val requestCameraPermission: Boolean = false,
    val showSettingsDialog: Boolean = false
  )

  sealed interface UiEvent {
    enum class RequestCameraPermissionResult : UiEvent {
      GRANTED, DECLINED, PERMANENTLY_DECLINED
    }

    data object DismissSettingsDialog : UiEvent
    data object UpdateCameraPermissionState : UiEvent
  }
}
