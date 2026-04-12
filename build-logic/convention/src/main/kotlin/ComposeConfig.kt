package com.travelmonk.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Shared Compose configuration for all modules that need UI.
 * Enables compose build feature and adds the BOM + core dependencies.
 */
internal fun Project.configureCompose(
    commonExtension: CommonExtension
) {
    commonExtension.apply {
        buildFeatures.compose = true
    }

    /**
     * @Deprecated In AGP 9.0+ and Kotlin 2.0+,
     * the composeOptions block and the kotlinCompilerExtensionVersion property are deprecated.
     * The Compose compiler is now managed by a dedicated Gradle plugin that
     * automatically matches your Kotlin version.
     * // Configure Compose options
     * //        composeOptions {
     * //            // Retrieve the Compose compiler version from the version catalog
     * //            // Make sure to define 'androidx-compose-compiler' version in libs.versions.toml
     * //            val composeCompilerVersion = extensions.getByType(
     * //                org.gradle.api.artifacts.VersionCatalogsExtension::class
     * //            ).named("libs").findVersion("androidx-compose-compiler").get().requiredVersion
     * //            kotlinCompilerExtensionVersion = composeCompilerVersion
     * //        }
     */

    // Alternative approach to configure Compose Compiler options
    // The new way to configure Compose Compiler options in Kotlin 2.0+
    // Note: You must apply the "org.jetbrains.kotlin.plugin.compose" plugin for this block to work
    extensions.configure<ComposeCompilerGradlePluginExtension> {
        // Enable strong skipping mode or other optimizations if desired

        /**
         * @Deprecated  enableStrongSkippingMode.set(true)
         * You can safely remove the enableStrongSkippingMode.set(true) line entirely.
         * If you are using a version between 2.0.0 and 2.0.20,
         * the property still exists but is unnecessary. For Kotlin 2.0.20+,
         * use the featureFlags DSL if you need to explicitly manage it or other new features.
         */
        // Strong skipping is now enabled by default in Kotlin 2.0.20+
        // StrongSkipping is now enabled by default; no flag needed.
        // You can use featureFlags to enable/disable specific upcoming features
        // featureFlags.add(ComposeFeatureFlag.StrongSkipping) // Optional: Explicitly opt-in if needed

        // Optional: Reference a stability configuration file if you use one
        // stabilityConfigurationFile.set(rootProject.layout.projectDirectory.file("compose-stability.conf"))

        // Reports and metrics (optional)
        metricsDestination.set(layout.buildDirectory.dir("compose_metrics"))
        reportsDestination.set(layout.buildDirectory.dir("compose_reports"))
    }

    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libs.findLibrary("androidx-compose-ui").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-preview").get())
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("implementation", libs.findLibrary("androidx-compose-animation").get())
        add("implementation", libs.findLibrary("androidx-compose-animation-core").get())

        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
    }
}