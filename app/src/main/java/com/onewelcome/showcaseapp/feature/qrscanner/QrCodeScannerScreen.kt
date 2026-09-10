package com.onewelcome.showcaseapp.feature.qrscanner

import android.Manifest
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.qrscanner.QrCodeScannerViewModel.UiEvent
import com.onewelcome.showcaseapp.feature.qrscanner.QrCodeScannerViewModel.UiState
import com.onewelcome.showcaseapp.navigation.openAppSettings
import com.onewelcome.showcaseapp.navigation.popBackStackWithResult

@Composable
fun QrCodeScannerScreen(
  navController: NavController,
  viewModel: QrCodeScannerViewModel = hiltViewModel()
) {
  QrCodeScannerContent(
    uiState = viewModel.uiState,
    onEvent = viewModel::onEvent,
    onNavigateBack = { result -> navController.popBackStackWithResult(Constants.QR_CODE_RESULT_KEY, result) }
  )
}

@Composable
private fun QrCodeScannerContent(uiState: UiState, onEvent: (UiEvent) -> Unit, onNavigateBack: (String) -> Unit) {
  when {
    uiState.isCameraPermissionGranted -> QrCodeScanner(onNavigateBack)
    uiState.requestCameraPermission -> RequestCameraPermission(onEvent)
    uiState.showSettingsDialog -> ShowPermissionSettingsAlertDialog(onEvent)
    else -> onNavigateBack("")
  }
}

@Composable
private fun RequestCameraPermission(onEvent: (UiEvent) -> Unit) {
  val activity = LocalActivity.current
  val permissionResultLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { handleCameraPermissionResult(it, activity, onEvent) }
  )
  SideEffect {
    permissionResultLauncher.launch(Manifest.permission.CAMERA)
  }
}

private fun handleCameraPermissionResult(result: Boolean, activity: Activity?, onEvent: (UiEvent) -> Unit) {
  when {
    result -> onEvent(UiEvent.RequestCameraPermissionResult.GRANTED)
    activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == false ->
      onEvent(UiEvent.RequestCameraPermissionResult.PERMANENTLY_DECLINED)

    else -> onEvent(UiEvent.RequestCameraPermissionResult.DECLINED)
  }
}

@Composable
private fun QrCodeScanner(onNavigateBack: (String) -> Unit) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val cameraController = remember { LifecycleCameraController(context) }
  var hasScanned by remember { mutableStateOf(false) }

  AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context ->
      val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
      val barcodeScanner = BarcodeScanning.getClient(options)
      val executor = ContextCompat.getMainExecutor(context)
      val mlKitAnalyzer = MlKitAnalyzer(
        listOf(barcodeScanner),
        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
        executor
      ) { result: MlKitAnalyzer.Result ->
        if (hasScanned) return@MlKitAnalyzer
        result.getValue(barcodeScanner)
          ?.firstOrNull()
          ?.rawValue
          ?.let { value ->
            hasScanned = true
            onNavigateBack(value)
          }
      }
      PreviewView(context).apply {
        cameraController.setImageAnalysisAnalyzer(executor, mlKitAnalyzer)
        cameraController.bindToLifecycle(lifecycleOwner)
        controller = cameraController
      }
    }
  )
}

@Composable
private fun ShowPermissionSettingsAlertDialog(onEvent: (UiEvent) -> Unit) {
  val activity = LocalActivity.current
  AlertDialog(
    onDismissRequest = { onEvent(UiEvent.DismissSettingsDialog) },
    title = { Text(stringResource(R.string.camera_permission_dialog_title)) },
    text = { Text(stringResource(R.string.camera_permission_dialog_description)) },
    confirmButton = {
      TextButton(
        onClick = {
          onEvent(UiEvent.DismissSettingsDialog)
          activity?.openAppSettings()
        }) {
        Text(stringResource(R.string.navigate_to_settings))
      }
    },
    dismissButton = {
      TextButton(onClick = {
        onEvent(UiEvent.DismissSettingsDialog)
      }) {
        Text(stringResource(R.string.cancel))
      }
    }

  )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  QrCodeScannerContent(
    uiState = UiState(),
    onEvent = {},
    onNavigateBack = {},
  )
}
