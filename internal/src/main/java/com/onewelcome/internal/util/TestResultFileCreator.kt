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
      return """
Test run time $formattedTime
----------------------------
Android OS

${osVersionInfo.sdkVersion}
${osVersionInfo.apiLevel}
${osVersionInfo.codename}
${osVersionInfo.type}
----------------------------
OMI Showcase app

${appVersionInfo.version}
${appVersionInfo.omiSdkVersion}
----------------------------
Test results

$testResultContent
"""
    }
  }
}
