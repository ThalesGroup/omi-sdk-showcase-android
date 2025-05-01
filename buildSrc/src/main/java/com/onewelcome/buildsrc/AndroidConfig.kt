package com.onewelcome.buildsrc

import org.gradle.api.JavaVersion

object AndroidConfig {
  const val MIN_SDK = 23
  const val COMPILE_SDK = 35
  const val TARGET_SDK = 35
  const val VERSION_CODE = 1
  const val VERSION_NAME = "1.0"
  const val NAMESPACE = "com.onewelcome.showcaseapp"
  const val APPLICATION_ID = "com.onewelcome.showcaseapp"
  const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
  const val CORE_MODULE = ":core"
  const val INTERNAL_MODULE = ":internal"
  const val ENVIRONMENT_FLAVOR_DIMENSION = "environment"
  const val IS_INTERNAL_VARIANT = "IS_INTERNAL_VARIANT"
  val SOURCE_COMPATIBILITY = JavaVersion.VERSION_21
  val TARGET_COMPATIBILITY = JavaVersion.VERSION_21
}
