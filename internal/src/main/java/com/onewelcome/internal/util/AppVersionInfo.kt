package com.onewelcome.internal.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.onewelcome.showcaseapp.BuildConfig
import com.onewelcome.showcaseapp.R

data class AppVersionInfo(
  val version: String,
  val omiSdkVersion: String
)

@Composable
fun appVersionInfo(): AppVersionInfo {
  val version = stringResource(R.string.version, BuildConfig.VERSION_NAME)
  val omiSdkVersion = stringResource(R.string.omi_sdk_version, BuildConfig.OMI_SDK_VERSION)
  return remember {
    AppVersionInfo(
      version = version,
      omiSdkVersion = omiSdkVersion,
    )
  }
}
