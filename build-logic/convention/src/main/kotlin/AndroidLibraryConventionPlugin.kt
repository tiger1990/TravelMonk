package com.travelmonk.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused") // Registered by name in build-logic/convention/build.gradle.kts gradlePlugin block
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            extensions.configure<LibraryExtension> {
                configureLibraryModule(this)
                // resourcePrefix derived from module path to prevent R class collisions
                // e.g. :feature:home → "feature_home_"
                resourcePrefix = path
                    .split("""\W""".toRegex())
                    .drop(1)
                    .filter { it.isNotBlank() }
                    .joinToString("_")
                    .lowercase() + "_"
            }
        }
    }
}
