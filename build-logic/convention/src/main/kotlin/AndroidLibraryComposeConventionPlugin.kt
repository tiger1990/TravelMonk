package com.travelmonk.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

@Suppress("unused") // Registered by name in build-logic/convention/build.gradle.kts gradlePlugin block
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            // Required for Compose 2.0+
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        val extension = extensions.findByType<LibraryExtension>()
            ?: extensions.findByType<ApplicationExtension>()
            ?: error("Android Library or Application extension not found")

        configureCompose(extension)
    }
}
