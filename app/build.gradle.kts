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

/**
 * Compose Compiler Configuration (Kotlin 2.0+)
 * Using stabilityConfigurationFiles (plural) which replaces the deprecated singular version.
 * This allows marking non-Compose module classes (like FeatureFlags) as stable.
 */
composeCompiler {
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_stability_config.conf"))
}

android {
    namespace = "com.travelmonk"

    defaultConfig {
        applicationId = "com.travelmonk"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.travelmonk.testing.HiltTestRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "password"
            keyAlias = "alias"
            keyPassword = "password"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "environment"

    /**
     * Just for example how free and paid product flavor are build
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

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL",            "\"https://dev-api.travelmonk.com/\"")
            buildConfigField("String", "ENVIRONMENT",         "\"DEV\"")
            buildConfigField("int",    "API_TIMEOUT_SECONDS", "30")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "BASE_URL",            "\"https://staging-api.travelmonk.com/\"")
            buildConfigField("String", "ENVIRONMENT",         "\"STAGING\"")
            buildConfigField("int",    "API_TIMEOUT_SECONDS", "30")
            // Readable symbols on stagingRelease so QA can parse crash logs
            proguardFiles("proguard-staging.pro")
        }
        create("production") {
            dimension = "environment"
            // No applicationIdSuffix — production is the canonical Play Store ID
            buildConfigField("String", "BASE_URL",            "\"https://api.travelmonk.com/\"")
            buildConfigField("String", "ENVIRONMENT",         "\"PRODUCTION\"")
            buildConfigField("int",    "API_TIMEOUT_SECONDS", "15")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix   = "-DEBUG"
            isDebuggable        = true
        }
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            signingConfig     = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        // staging BUILD TYPE removed — environment is now the "environment" product flavor
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

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
    implementation(project(":feature:transport-api"))
    implementation(project(":feature:flights-api"))
    implementation(project(":feature:stays-api"))
    implementation(project(":feature:experiences-api"))
    implementation(project(":feature:services-api"))
    implementation(project(":feature:bookings-api"))
    implementation(project(":feature:home-api"))

    // --- FEATURE IMPLEMENTATIONS ---
    implementation(project(":feature:transport"))
    implementation(project(":feature:flights"))
    implementation(project(":feature:stays"))
    implementation(project(":feature:experiences"))
    implementation(project(":feature:services"))
    implementation(project(":feature:bookings"))
    implementation(project(":feature:home"))
    implementation(project(":feature:onboarding-api"))
    implementation(project(":feature:onboarding"))

    // --- LIBRARIES ---
    implementation(libs.androidx.core.splashscreen)
    // Retained for a future Lottie-based splash; current splash is code-driven (no asset).
    implementation(libs.lottie.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
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
