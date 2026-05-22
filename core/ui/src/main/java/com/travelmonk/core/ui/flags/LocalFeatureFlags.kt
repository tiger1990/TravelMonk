package com.travelmonk.core.ui.flags

import androidx.compose.runtime.staticCompositionLocalOf
import com.travelmonk.core.common.config.FeatureFlagsData

/**
 * CompositionLocal to provide feature flag snapshots down the UI tree.
 * Using staticCompositionLocalOf as flags update infrequently and
 * usually trigger major UI tree reconstructions anyway.
 */
val LocalFeatureFlags = staticCompositionLocalOf { FeatureFlagsData.DEFAULT }
