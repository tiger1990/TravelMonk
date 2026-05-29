package com.travelmonk.core.network.security

import com.travelmonk.core.common.config.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [PinSource]: pins are baked into the binary and selected by [AppConfig.environment].
 *
 * This is always the floor of trust — even when [RemotePinSource] is active, the static
 * baseline is never weakened. Remote pins are only ever additively merged on top of these.
 */
@Singleton
class StaticPinSource @Inject constructor(
    private val appConfig: AppConfig
) : PinSource {

    override fun currentConfig(): PinningConfig =
        PinningConfig.forEnvironment(appConfig.environment)
}
