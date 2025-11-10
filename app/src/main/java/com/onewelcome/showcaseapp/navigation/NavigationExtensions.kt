package com.onewelcome.showcaseapp.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.navigation.NavController


fun <V> NavController.popBackStackWithResult(key: String, value: V) {
  previousBackStackEntry?.savedStateHandle?.set(key, value)
  popBackStack()
}

fun <V> NavController.getResult(key: String): V? {
  return currentBackStackEntry?.savedStateHandle?.remove(key)
}

fun Activity.openAppSettings() {
  Intent(
    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
    Uri.fromParts("package", packageName, null)
  ).also(::startActivity)
}
