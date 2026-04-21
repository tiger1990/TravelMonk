plugins {
    // Custom convention plugin for Android Applications
    alias(libs.plugins.travelmonk.android.application)
    // Common Compose configuration for both libraries and apps
    alias(libs.plugins.travelmonk.android.library.compose)
    // Hilt dependency injection configuration
    alias(libs.plugins.travelmonk.android.hilt)
    // Kotlin serialization plugin for JSON/Protobuf
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.travelmonk"

    defaultConfig {
        // Unique ID for the app in the Google Play Store
        applicationId = "com.travelmonk"

        // version number for internal tracking
        versionCode = 1
        // User-visible version string
        versionName = "1.0"

        // Custom test runner for Hilt compatibility (recommended for production)
        testInstrumentationRunner = "com.travelmonk.testing.HiltTestRunner"
    }
    
    /**
     *  // Define flavor dimensions to group your flavors
     *     flavorDimensions += "tier"
     *     // Build type will have tier:
     *     // free and paid freeDebug, freeRelease || paidDebug, paidRelease
     *     productFlavors {
     *         create("free") {
     *             dimension = "tier"
     *             // Different package name for the free version
     *             applicationIdSuffix = ".free"
     *             versionNameSuffix = "-free"
     *         }
     *
     *         create("paid") {
     *             dimension = "tier"
     *             // Different package name for the paid version
     *             applicationIdSuffix = ".paid"
     *             versionNameSuffix = "-paid"
     *         }
     *     }
     */

    signingConfigs {
        // Production apps define signing configs here (usually via local.properties for security)
        create("release") {
            // Placeholder for production keystore settings
            storeFile = file("release.keystore")
            storePassword = "password"
            keyAlias = "alias"
            keyPassword = "password"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // Allows installing debug and release versions on the same device
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("String", "BASE_URL", "\"https://dev-api.travelmonk.com/\"")
        }

        create("staging") {
            // Inherits release settings but with a different suffix for testing environments
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            matchingFallbacks += listOf("release")
            buildConfigField("String", "BASE_URL", "\"https://staging-api.travelmonk.com/\"")
        }

        release {
            // Enables R8 code shrinking and obfuscation
            isMinifyEnabled = true
            // Removes unused resources to reduce APK size
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            // Standard ProGuard optimization rules
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://api.travelmonk.com/\"")
        }
    }

    packaging {
        resources {
            // Exclude duplicate license files from the final APK
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Modern apps use this to control Bundle behavior
    @Suppress("UnstableApiUsage")
    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }
}

dependencies {
    // --- CORE LAYOUT & NAVIGATION ---
    implementation(project(":core:navigation"))
    implementation(project(":core:design-system"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:tokens"))
    implementation(project(":core:logger"))

    // --- FEATURE INTERFACES ---
    // The app module wires these to implement a loosely coupled architecture
    implementation(project(":feature:transport-api"))
    implementation(project(":feature:flights-api"))
    implementation(project(":feature:stays-api"))
    implementation(project(":feature:experiences-api"))
    implementation(project(":feature:services-api"))
    implementation(project(":feature:bookings-api"))
    implementation(project(":feature:home-api"))

    // --- FEATURE IMPLEMENTATIONS ---
    // Injected at runtime or defined via TravelEntryProvider
    implementation(project(":feature:transport"))
    implementation(project(":feature:flights"))
    implementation(project(":feature:stays"))
    implementation(project(":feature:experiences"))
    implementation(project(":feature:services"))
    implementation(project(":feature:bookings"))
    implementation(project(":feature:home"))

    // --- SPLASH SCREEN & ANIMATION ---
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.lottie.compose)

    // --- ANDROIDX & KOTLIN EXTENSIONS ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Navigation 3 (Modern Navigation for Compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // Hilt / DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Serialization & Networking
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)

    // --- TESTING ---
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.konsist)
}
