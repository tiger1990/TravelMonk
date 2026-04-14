package com.travelmonk.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin for the Android Application module.
 * It applies the 'com.android.application' plugin and configures common application settings.
 */
@Suppress("unused")
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                // Delegating shared configuration (SDKs, Java version, etc.) to AndroidCommon.kt
                configureAppModule(this)
                
                defaultConfig {
                    // targetSdk is specific to the application module
                    targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
                }
            }
        }
    }
}
