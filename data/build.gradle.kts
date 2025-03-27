import com.onewelcome.buildsrc.AndroidConfig.COMPILE_SDK
import com.onewelcome.buildsrc.AndroidConfig.MIN_SDK
import com.onewelcome.buildsrc.AndroidConfig.NAMESPACE

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.hilt.plugin)
}

android {
  namespace = NAMESPACE
  compileSdk = COMPILE_SDK

  defaultConfig {
    minSdk = MIN_SDK
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }
}

dependencies {
  // DataStore
  implementation(libs.androidx.datastore)

  // Hilt
  implementation(libs.hilt.library)
  kapt(libs.hilt.compiler)

  // Test
  testImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.junit)

}
