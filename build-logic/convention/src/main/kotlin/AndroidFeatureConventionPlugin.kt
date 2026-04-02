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
 *  - kotlinx serialization plugin (for @Serializable data/model classes in feature modules)
 *  - Core module dependencies: model, navigation, design-system, tokens, ui, common, network
 *  - Full lifecycle, navigation3, coroutines, serialization, and Hilt Compose stack
 */
@Suppress("unused") // Registered by name in build-logic/convention/build.gradle.kts gradlePlugin block
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("travelmonk.android.library")
                apply("travelmonk.android.library.compose")
                apply("travelmonk.android.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }
            dependencies {
                add("implementation", project(":core:model"))
                add("implementation", project(":core:navigation"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:tokens"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:common"))
                add("implementation", project(":core:network"))

                // Compose UI primitives — every feature screen needs basic Compose + Material3
                add("implementation", libs.findLibrary("androidx-ui").get())
                add("implementation", libs.findLibrary("androidx-material3").get())

                // ViewModel scoped to a Compose NavEntry; provides viewModel() in Composables
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())

                // collectAsStateWithLifecycle() — lifecycle-aware Flow collection in Composables
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())

                // SavedStateHandle support in ViewModel — survives process death and back-stack restore
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-savedstate").get())

                // Navigation3 ViewModel integration — scopes ViewModels to Nav3 back-stack entries
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())

                // Navigation3 runtime — NavDisplay, NavBackStack, back-stack state management
                add("implementation", libs.findLibrary("androidx-navigation3-runtime").get())

                // Navigation3 UI utilities — predictive-back gesture, system-back integration
                add("implementation", libs.findLibrary("androidx-navigation3-ui").get())

                // hiltViewModel() scoped to Nav3 entries; bridges Hilt DI with Navigation3
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())

                // Android-specific coroutine dispatcher (Main); needed for ViewModel + Flow work
                add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())

                // kotlinx.serialization JSON — for serializing data models and nav-key back-stack persistence
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}
