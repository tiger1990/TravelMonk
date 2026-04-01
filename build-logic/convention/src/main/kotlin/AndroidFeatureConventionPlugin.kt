package com.travelmonk.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for all feature modules.
 * Applying this single plugin provides:
 *  - Android library setup (compileSdk, minSdk, resourcePrefix, compileOptions)
 *  - Compose support + BOM (via travelmonk.android.library.compose)
 *  - Hilt + KSP (via travelmonk.android.hilt)
 *  - Common feature dependencies: core:model, core:navigation, core:designsystem,
 *    core:ui, core:common, core:network
 *  - Compose UI: material3, hilt-navigation-compose, lifecycle-viewmodel-compose,
 *    lifecycle-runtime-compose
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("travelmonk.android.library")
                apply("travelmonk.android.library.compose")
                apply("travelmonk.android.hilt")
            }
            dependencies {
                add("implementation", project(":core:model"))
                add("implementation", project(":core:navigation"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:tokens"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:common"))
                add("implementation", project(":core:network"))

                add("implementation", libs.findLibrary("androidx.ui").get())
                add("implementation", libs.findLibrary("androidx.material3").get())
                add("implementation", libs.findLibrary("hilt.navigation.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtime.compose").get())
            }
        }
    }
}
