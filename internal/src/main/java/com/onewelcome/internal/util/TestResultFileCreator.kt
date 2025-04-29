package com.onewelcome.internal.util

import android.annotation.SuppressLint
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestResultFileCreator {
  companion object {
    @SuppressLint("ConstantLocale")
    val formattedTime: String = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault()).format(Date(currentTimeMillis()))
    fun getFileContent(appVersionInfo: AppVersionInfo, osVersionInfo: OSVersionInfo, testResultContent: String?): String {
      return StringBuilder().apply {
        append("Test run time ").append(formattedTime).append("\n")
        append("----------------------------\n")
        append("Android OS\n\n")
        append(osVersionInfo.sdkVersion).append("\n")
        append(osVersionInfo.apiLevel).append("\n")
        append(osVersionInfo.codename).append("\n")
        append(osVersionInfo.type).append("\n")
        append("----------------------------\n")
        append("OMI Showcase app\n\n")
        append(appVersionInfo.version).append("\n")
        append(appVersionInfo.omiSdkVersion).append("\n")
        append("----------------------------\n")
        append("Test results\n\n")
        append(testResultContent).append("\n")
      }.toString()
    }
  }
}
