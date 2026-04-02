pluginManagement {
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
    }
}

rootProject.name = "TravelMonk"
include(":app")

// Core Modules
include(":core:tokens")
include(":core:navigation")
include(":core:designsystem")
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
