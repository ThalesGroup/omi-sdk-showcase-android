rootProject.name = "OMI SDK Showcase App"
include(":app")
include(":data")
include(":internal")
include(":core")


pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    mavenLocal()
    setupOmiSdkRepo()
    //POC-START
    setupFidoSdkRepo()
    //POC-END
  }
}

private fun RepositoryHandler.setupOmiSdkRepo() {
  try {
    val artifactoryUser = providers.gradleProperty("artifactory_user")
      .getOrElse(System.getenv("ARTIFACTORY_USER") ?: System.getenv("artifactory_user") ?: "")
    val artifactoryPassword = providers.gradleProperty("artifactory_password").getOrElse(
      System.getenv("ARTIFACTORY_PASSWORD") ?: System.getenv("artifactory_password") ?: ""
    )
    if (artifactoryUser.isBlank() || artifactoryPassword.isBlank()) {
      throw IllegalArgumentException("Artifactory credentials are blank")
    }
    maven {
      url = uri("https://thalescpliam.jfrog.io/artifactory/onegini-sdk")
      credentials {
        username = artifactoryUser
        password = artifactoryPassword
      }
      content {
        includeGroup("com.onegini.mobile.sdk.android")
      }
    }
  } catch (_: Throwable) {
    throw InvalidUserDataException(
      "You must configure the 'artifactory_user' and 'artifactory_password' properties or environment variables in your project before you can build it."
    )
  }
}

// POC - START
private fun RepositoryHandler.setupFidoSdkRepo() {
  try {
    val artifactoryUser = providers.gradleProperty("artifactory_user")
      .getOrElse(System.getenv("ARTIFACTORY_USER") ?: System.getenv("artifactory_user") ?: "")
    val artifactoryPassword = providers.gradleProperty("artifactory_password").getOrElse(
      System.getenv("ARTIFACTORY_PASSWORD") ?: System.getenv("artifactory_password") ?: ""
    )
    if (artifactoryUser.isBlank() || artifactoryPassword.isBlank()) {
      throw IllegalArgumentException("Artifactory credentials are blank")
    }
    maven {
      url = uri("https://thalescpliam.jfrog.io/artifactory/onegini-sdk/")
      credentials {
        username = artifactoryUser
        password = artifactoryPassword
      }
      content {
        includeGroup("com.thalesgroup.gemalto.fido2")
        includeGroup("fido2.android.lib")
      }
    }
  } catch (_: Throwable) {
    throw InvalidUserDataException(
      "You must configure the 'artifactory_user' and 'artifactory_password' properties or environment variables in your project before you can build it."
    )
  }
}
//POC-END
