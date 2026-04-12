package com.travelmonk.buildlogic

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

@Suppress("unused") // Registered by name in build-logic/convention/build.gradle.kts gradlePlugin block
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.google.dagger.hilt.android")
            apply("com.google.devtools.ksp")
        }

        // Inside your apply block
        val androidComponents = extensions.findByType<AndroidComponentsExtension<*, *, *>>()
        androidComponents?.onVariants { variant ->
            // 1. Define the path where KSP puts its generated files for this variant
            val kspGeneratedDir = layout.buildDirectory.dir("generated/ksp/${variant.name}/kotlin")

            // 2. Register the directory as a static source for this specific variant
            // This covers 'debug', 'release', and 'benchmarkRelease' automatically
            variant.sources.kotlin?.addStaticSourceDirectory(
                kspGeneratedDir.map { it.asFile.absolutePath }.toString()
            )
        }
        
        dependencies {
            add("implementation", libs.findLibrary("hilt-android").get())
            add("ksp", libs.findLibrary("hilt-compiler").get())

            // --- ADD THESE FOR Instrumented Tests ---
            // Allows HiltTestRunner to find HiltTestApplication
            add("androidTestImplementation", libs.findLibrary("hilt-android-testing").get())
            // Allows KSP to generate Hilt components for your tests
            add("kspAndroidTest", libs.findLibrary("hilt-compiler").get())

            // Unit Tests (Optional, for Robolectric, etc.)
            add("testImplementation", libs.findLibrary("hilt-android-testing").get())
            add("kspTest", libs.findLibrary("hilt-compiler").get())
        }
    }
}
