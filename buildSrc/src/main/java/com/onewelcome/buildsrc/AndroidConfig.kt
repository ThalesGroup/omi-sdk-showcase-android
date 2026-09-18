package com.onewelcome.buildsrc

import org.gradle.api.JavaVersion

object AndroidConfig {
  // Use MIN_SDK as 26 for FIDO POC, as Passkey authentication will not work below 26
  const val MIN_SDK = 28
  const val COMPILE_SDK = 37
  const val TARGET_SDK = 37
  const val VERSION_CODE = 1
  const val VERSION_NAME = "13.3.0"
  const val APPLICATION_ID = "com.onewelcome.showcaseapp"
  const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
  const val CORE_MODULE = ":core"
  const val DATA_MODULE = ":data"
  const val INTERNAL_MODULE = ":internal"
  // POC - START

  // FIDO2_MODULE was introduced in feature/fido-poc branch for the local :lib:fido2ui library.
  // const val FIDO2_MODULE = ":lib:fido2ui"

  // POC - END
  const val ENVIRONMENT_FLAVOR_DIMENSION = "environment"
  const val IS_INTERNAL_VARIANT = "IS_INTERNAL_VARIANT"
  val SOURCE_COMPATIBILITY = JavaVersion.VERSION_21
  val TARGET_COMPATIBILITY = JavaVersion.VERSION_21
}
