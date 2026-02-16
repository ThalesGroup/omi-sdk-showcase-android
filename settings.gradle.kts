rootProject.name = "OMI SDK Showcase App"
include(":app")
include(":data")
include(":internal")
include(":core")
include (":lib:fido2ui")


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
  }
}

private fun RepositoryHandler.setupOmiSdkRepo() {
  try {
    val artifactoryUser = providers.gradleProperty("artifactory_user").get()
    val artifactoryPassword = providers.gradleProperty("artifactory_password").get()
    maven {
      url = uri("https://thalescpliam.jfrog.io/artifactory/onegini-sdk")
      credentials {
        username = artifactoryUser
        password = artifactoryPassword
      }
    }
  } catch (_: Throwable) {
    throw InvalidUserDataException(
      "You must configure the 'artifactory_user' and 'artifactory_password' properties in your project before you can build it."
    )
  }
}
