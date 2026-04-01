package com.travelmonk.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }
            // Centralize the BOM here so every module using this plugin
            // gets consistent Compose versions without declaring it manually.
            dependencies {
                add("implementation", platform(libs.findLibrary("androidx.compose.bom").get()))
                add("implementation", libs.findLibrary("androidx-ui-tooling-preview").get())
                add("debugImplementation", libs.findLibrary("androidx-ui-tooling").get())
            }
        }
    }
}
