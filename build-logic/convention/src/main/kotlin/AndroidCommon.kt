package com.travelmonk.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Properties shared by all Android modules (app + libraries).
 * compileSdk is set here via version catalog — single source of truth in libs.versions.toml.
 */
internal fun Project.configureAndroidBase(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()
        buildFeatures.buildConfig = true

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
                freeCompilerArgs.addAll(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
                    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                )
            }
        }
    }
}

internal fun Project.configureAppModule(extension: ApplicationExtension) {
    extension.apply {
        configureAndroidBase(extension)
        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
            testInstrumentationRunner = "com.enterprise.app.testing.HiltTestRunner"
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        testOptions {
            unitTests.isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
        }
        buildFeatures {
            buildConfig = true
        }
    }
}

internal fun Project.configureLibraryModule(extension: LibraryExtension) {
    extension.apply {
        configureAndroidBase(extension)
        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
            // Each library module ships its own R8 rules to the consuming app.
            val proguardFile = "consumer-rules.pro"
            if (file(proguardFile).exists()) {
                consumerProguardFiles(proguardFile)
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        testOptions {
            unitTests.isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
        }
        buildFeatures {
            buildConfig = true
        }
    }
}
