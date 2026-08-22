pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My-MB-Challenge"

include(":core:common")
include(":core:network")
include(":core:ui")
include(":domain")
include(":data")
include(":feature:exchangelist")
include(":feature:exchangedetail")
