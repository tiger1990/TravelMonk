pluginManagement {
    // Convention plugins live here
    includeBuild("build-logic")
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
        // Uncomment and change the build ID if you need to use snapshot artifacts.
        // See androidx.dev for full instructions.
        /*maven {
            url = uri("https://androidx.dev/snapshots/builds/<build_id>/artifacts/repository")
            eg:  url = uri("https://androidx.dev/snapshots/builds/13508953/artifacts/repository")
        }*/
        // for absolute latest alpha or rc
        // Only needed if using bleeding-edge dev builds:
        // maven { url = uri("https://androidx.dev/storage/compose-staging/repository") }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Uncomment and change the build ID if you need to use snapshot artifacts.
        // See androidx.dev for full instructions.
        /*maven {
            url = uri("https://androidx.dev/snapshots/builds/<build_id>/artifacts/repository")
        }*/

        // Add any other custom repositories here, e.g.:
        // maven { url "https://jitpack.io" }
    }
}

rootProject.name = "TravelMonk"
// App Shell
include(":app")

// Core Modules
include(":core:tokens")
include(":core:navigation")
include(":core:design-system")
include(":core:ui")
include(":core:common")
include(":core:model")
include(":core:network")
include(":core:database")

// Feature API Modules (contracts: nav keys + navigator interfaces)
include(":feature:transport-api")
include(":feature:flights-api")
include(":feature:stays-api")
include(":feature:experiences-api")
include(":feature:services-api")
include(":feature:bookings-api")
include(":feature:home-api")

// Feature Implementation Modules
include(":feature:transport")
include(":feature:flights")
include(":feature:stays")
include(":feature:experiences")
include(":feature:services")
include(":feature:bookings")
include(":feature:home")
