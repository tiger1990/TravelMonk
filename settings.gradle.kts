pluginManagement {
    // Convention plugins live here
    includeBuild("build-logic")

    /**
     * The pluginManagement.repositories block configures the
     * repositories Gradle uses to search or download the Gradle plugins and
     * their transitive dependencies. Gradle pre-configures support for remote
     * repositories such as JCenter, Maven Central, and Ivy. You can also use
     * local repositories or define your own remote repositories. Here we
     * define the Gradle Plugin Portal, Google's Maven repository,
     * and the Maven Central Repository as the repositories Gradle should use to look for its
     * dependencies.
     */
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
    /**
     * The dependencyResolutionManagement.repositories
     * block is where you configure the repositories and dependencies used by
     * all modules in your project, such as libraries that you are using to
     * create your application. However, you should configure module-specific
     * dependencies in each module-level build.gradle file. For new projects,
     * Android Studio includes Google's Maven repository and the Maven Central
     * Repository by default, but it does not configure any dependencies (unless
     * you select a template that requires some).
     */
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
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
