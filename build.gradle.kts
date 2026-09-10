// Top-level build file where you can add configuration options common to all sub-projects/modules.

//POC-START

// Fix transitive dependency issue: fido2ui pulls in fido2.android.lib:fido2 (unspecified)
// which doesn't exist; substitute it with the correct artifact.
subprojects {
  configurations.all {
    resolutionStrategy {
      dependencySubstitution {
        substitute(module("fido2.android.lib:fido2")).using(module("com.thalesgroup.gemalto.fido2:fido2:4.1.0"))
      }
    }
  }
}


//POC-END
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.jetbrains.kotlin.jvm) apply false
  alias(libs.plugins.hilt.plugin) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.google.services) apply false
}
