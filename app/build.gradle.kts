import com.onewelcome.buildsrc.AndroidConfig.APPLICATION_ID
import com.onewelcome.buildsrc.AndroidConfig.COMPILE_SDK
import com.onewelcome.buildsrc.AndroidConfig.CORE_MODULE
import com.onewelcome.buildsrc.AndroidConfig.ENVIRONMENT_FLAVOR_DIMENSION
import com.onewelcome.buildsrc.AndroidConfig.INTERNAL_MODULE
import com.onewelcome.buildsrc.AndroidConfig.IS_INTERNAL_VARIANT
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

  buildFeatures {
    buildConfig = true
    compose = true
  }

  flavorDimensions += ENVIRONMENT_FLAVOR_DIMENSION
  productFlavors {
    create("internal") {
      dimension = ENVIRONMENT_FLAVOR_DIMENSION
      applicationIdSuffix = ".internal"
      versionNameSuffix = "-internal"
      buildConfigField("Boolean", IS_INTERNAL_VARIANT, "true")
    }
    create("developer") {
      dimension = ENVIRONMENT_FLAVOR_DIMENSION
      applicationIdSuffix = ".developer"
      versionNameSuffix = "-developer"
      buildConfigField("Boolean", IS_INTERNAL_VARIANT, "false")
    }
  }

  compileOptions {
    sourceCompatibility = SOURCE_COMPATIBILITY
    targetCompatibility = TARGET_COMPATIBILITY
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

dependencies {
  // Project modules
  implementation(project(CORE_MODULE))
  implementation(project(INTERNAL_MODULE))

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
  implementation(libs.hilt.navigation.compose)
  ksp(libs.hilt.compiler)

  // DataStore
  implementation(libs.androidx.datastore)

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

  //Kotlin Result
  implementation(libs.kotlin.result)

  // Test
  testImplementation(libs.androidx.junit)
  testImplementation(libs.robolectric)
  testImplementation(libs.hilt.testing)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.assertj)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
