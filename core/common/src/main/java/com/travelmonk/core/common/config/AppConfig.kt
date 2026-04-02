package com.travelmonk.core.common.config

/**
 * Global application configuration.
 * Decouples feature modules from build-time constants like BuildConfig.DEBUG.
 */
interface AppConfig {
    val isDebug: Boolean
}
