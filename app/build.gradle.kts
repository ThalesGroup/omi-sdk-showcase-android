import com.onewelcome.buildsrc.AndroidConfig.APPLICATION_ID
import com.onewelcome.buildsrc.AndroidConfig.COMPILE_SDK
import com.onewelcome.buildsrc.AndroidConfig.MIN_SDK
import com.onewelcome.buildsrc.AndroidConfig.NAMESPACE
import com.onewelcome.buildsrc.AndroidConfig.SOURCE_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TARGET_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TARGET_SDK
import com.onewelcome.buildsrc.AndroidConfig.TEST_INSTRUMENTATION_RUNNER
import com.onewelcome.buildsrc.AndroidConfig.VERSION_CODE
import com.onewelcome.buildsrc.AndroidConfig.VERSION_NAME

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.hilt.plugin)
}

android {
  defaultConfig {
    compileSdk = COMPILE_SDK
    namespace = NAMESPACE
    applicationId = APPLICATION_ID
    minSdk = MIN_SDK
    targetSdk = TARGET_SDK
    versionCode = VERSION_CODE
    versionName = VERSION_NAME
    testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = SOURCE_COMPATIBILITY
    targetCompatibility = TARGET_COMPATIBILITY
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
  }
}

dependencies {
  // Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.ui)

  // Lifecycle
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.livedata.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)

  // Coroutines
  implementation(libs.kotlinx.coroutines.android)

  // Compose
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.ui.test.manifest)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)
  implementation(libs.androidx.compose.runtime.livedata)
  implementation(libs.androidx.navigation)

  // Retrofit
  implementation(libs.squareup.okhttp3)
  implementation(libs.squareup.retrofit2)
  implementation(libs.squareup.retrofit2.converter.gson)

  // Hilt
  implementation(libs.hilt.library)
  ksp(libs.hilt.compiler)

  // DataStore
  implementation(libs.androidx.datastore)

  // OMI SDK
  debugApi("com.onegini.mobile.sdk.android:onegini-sdk-developer:13.0.0@aar") {
    isTransitive = true
  }
  releaseApi("com.onegini.mobile.sdk.android:onegini-sdk:13.0.0@aar") {
    isTransitive = true
  }

  // Test
  testImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
