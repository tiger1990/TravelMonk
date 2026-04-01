package com.travelmonk.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }
            extensions.configure<LibraryExtension> {
                compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()
                defaultConfig {
                    minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
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
