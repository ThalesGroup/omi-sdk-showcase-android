import com.onewelcome.buildsrc.AndroidConfig.COMPILE_SDK
import com.onewelcome.buildsrc.AndroidConfig.JVM_TARGET
import com.onewelcome.buildsrc.AndroidConfig.MIN_SDK
import com.onewelcome.buildsrc.AndroidConfig.NAMESPACE
import com.onewelcome.buildsrc.AndroidConfig.SOURCE_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TARGET_COMPATIBILITY

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.hilt.plugin)
  alias(libs.plugins.google.devtools.ksp)
}

android {
  namespace = NAMESPACE
  compileSdk = COMPILE_SDK

  defaultConfig {
    minSdk = MIN_SDK
  }
  compileOptions {
    sourceCompatibility = SOURCE_COMPATIBILITY
    targetCompatibility = TARGET_COMPATIBILITY
  }
}

dependencies {
  // DataStore
  implementation(libs.androidx.datastore)

  // Hilt
  implementation(libs.hilt.library)
  ksp(libs.hilt.compiler)

  // Test
  testImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.junit)
}
