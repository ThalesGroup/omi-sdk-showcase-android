import com.onewelcome.buildsrc.AndroidConfig.COMPILE_SDK
import com.onewelcome.buildsrc.AndroidConfig.CORE_MODULE
import com.onewelcome.buildsrc.AndroidConfig.ENVIRONMENT_FLAVOR_DIMENSION
import com.onewelcome.buildsrc.AndroidConfig.IS_INTERNAL_VARIANT
import com.onewelcome.buildsrc.AndroidConfig.MIN_SDK
import com.onewelcome.buildsrc.AndroidConfig.NAMESPACE
import com.onewelcome.buildsrc.AndroidConfig.SOURCE_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TARGET_COMPATIBILITY
import com.onewelcome.buildsrc.AndroidConfig.TEST_INSTRUMENTATION_RUNNER
import com.onewelcome.buildsrc.AndroidConfig.VERSION_NAME

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
    buildConfigField("String", "VERSION_NAME", "\"${VERSION_NAME}\"")
    buildConfigField("String", "OMI_SDK_VERSION", "\"${libs.versions.omiSdk.get()}\"")
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  flavorDimensions += ENVIRONMENT_FLAVOR_DIMENSION
  productFlavors {
    create("internal") {
      dimension = ENVIRONMENT_FLAVOR_DIMENSION
      buildConfigField("Boolean", IS_INTERNAL_VARIANT, "true")
      buildConfigField("String", "VERSION_NAME", "\"${VERSION_NAME}\"")
      buildConfigField("String", "OMI_SDK_VERSION", "\"${libs.versions.omiSdk.get()}\"")
    }
    create("developer") {
      isDefault = true
      dimension = ENVIRONMENT_FLAVOR_DIMENSION
      buildConfigField("Boolean", IS_INTERNAL_VARIANT, "false")
    }
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  compileOptions {
    sourceCompatibility = SOURCE_COMPATIBILITY
    targetCompatibility = TARGET_COMPATIBILITY
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
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.runtime.livedata)

  // Hilt
  implementation(libs.hilt.library)
  implementation(libs.hilt.navigation.compose)
  ksp(libs.hilt.compiler)

  // Kotlin Result
  implementation(libs.kotlin.result)

  // Test
  testImplementation(libs.androidx.junit)
}
