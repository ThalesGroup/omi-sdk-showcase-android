import com.onewelcome.buildsrc.AndroidConfig.COMPILE_SDK
import com.onewelcome.buildsrc.AndroidConfig.MIN_SDK
import com.onewelcome.buildsrc.AndroidConfig.NAMESPACE
import com.onewelcome.buildsrc.AndroidConfig.SOURCE_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TARGET_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TEST_INSTRUMENTATION_RUNNER

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.hilt.plugin)
  alias(libs.plugins.kotlin.compose)
}

android {
  defaultConfig {
    compileSdk = COMPILE_SDK
    namespace = NAMESPACE
    minSdk = MIN_SDK
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

  // Kotlin Result
  implementation(libs.kotlin.result)

  // OMI SDK
  debugApi(libs.omiSdk.developer) {
    artifact {
      type = "aar"
      isTransitive = true
    }
  }
  releaseApi(libs.omiSdk.secure) {
    artifact {
      type = "aar"
      isTransitive = true
    }
  }

  // Hilt
  implementation(libs.hilt.library)
  ksp(libs.hilt.compiler)

  // Coroutines
  implementation(libs.kotlinx.coroutines.android)

  // Compose
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.material3)

}