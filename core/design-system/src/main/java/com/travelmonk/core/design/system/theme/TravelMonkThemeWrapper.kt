package com.travelmonk.core.design.system.theme

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

/**
 * Modern Compose Preview Wrapper.
 * Automatically applies the TravelMonk Theme and a Surface to all linked previews.
 */
class TravelMonkThemeWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        TravelMonkTheme {
            Surface(color = TravelMonkTheme.colors.background) {
                content()
            }
        }
    }
}
