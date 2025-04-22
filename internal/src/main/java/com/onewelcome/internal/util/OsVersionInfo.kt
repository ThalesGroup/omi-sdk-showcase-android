package com.onewelcome.internal.util

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.onewelcome.showcaseapp.R

data class OSVersionInfo(
  val sdkVersion: String,
  val apiLevel: String,
  val codename: String,
  val type: String
)

private const val releaseCodename = "REL"

@Composable
fun osVersionInfo(): OSVersionInfo {
  val sdkVersion = stringResource(R.string.sdk_version, Build.VERSION.RELEASE)
  val apiLevel = stringResource(R.string.api_level, Build.VERSION.SDK_INT)
  val codename = if (Build.VERSION.CODENAME == releaseCodename) stringResource(R.string.codename_release) else stringResource(R.string.codename, Build.VERSION.CODENAME)
  val previewSdkVersion = Build.VERSION.PREVIEW_SDK_INT
  val isPreview = previewSdkVersion > 0 || Build.VERSION.CODENAME != releaseCodename
  val type = if (isPreview) stringResource(R.string.type_beta_preview_version, previewSdkVersion) else stringResource(R.string.type_stable_release)

  return remember {
    OSVersionInfo(
      sdkVersion = sdkVersion,
      apiLevel = apiLevel,
      codename = codename,
      type = type
    )
  }
}
