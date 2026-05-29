package com.travelmonk.core.common.config

/**
 * Global application configuration.
 * Decouples feature modules from build-time constants like BuildConfig.DEBUG.
 * Values are bound by AppModule using BuildConfig fields generated per flavor × build type.
 */
interface AppConfig {
    val isDebug: Boolean
    val baseUrl: String
    val environment: Environment
    val apiTimeoutSeconds: Int
}

enum class Environment { DEV, STAGING, PRODUCTION }
