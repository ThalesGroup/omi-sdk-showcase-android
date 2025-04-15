rootProject.name = "OMI Showcase App"
include(":app")
include(":data")
include(":internal")

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
      url = uri("https://repo.onewelcome.com/artifactory/public")
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
include(":core")
