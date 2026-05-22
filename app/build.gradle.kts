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

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("String", "BASE_URL", "\"https://dev-api.travelmonk.com/\"")
        }

        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            matchingFallbacks += listOf("release")
            buildConfigField("String", "BASE_URL", "\"https://staging-api.travelmonk.com/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-staging.pro"
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://api.travelmonk.com/\"")
        }
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
