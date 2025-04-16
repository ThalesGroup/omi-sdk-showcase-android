import com.onewelcome.buildsrc.AndroidConfig.COMPILE_SDK
import com.onewelcome.buildsrc.AndroidConfig.CORE_MODULE
import com.onewelcome.buildsrc.AndroidConfig.MIN_SDK
import com.onewelcome.buildsrc.AndroidConfig.NAMESPACE
import com.onewelcome.buildsrc.AndroidConfig.SOURCE_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TARGET_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TEST_INSTRUMENTATION_RUNNER
import org.gradle.kotlin.dsl.implementation

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.hilt.plugin)
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
  // Project modules
  implementation(project(CORE_MODULE))

  // Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)

  // Compose
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.navigation)

  // Hilt
  implementation(libs.hilt.library)
  ksp(libs.hilt.compiler)

  // Kotlin Result
  implementation(libs.kotlin.result)

  // Test
  testImplementation(libs.junit)
}
