package com.travelmonk.core.design.system.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper

/**
 * Multipreview annotation that provides Light and Dark mode previews.
 * Use with [PreviewWrapper] and [TravelMonkThemeWrapper] to apply the theme.
 */
@Preview(
    name = "Light Mode",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
annotation class TravelMonkThemePreviews

/**
 * Multipreview annotation for different device sizes.
 */
@Preview(
    name = "Phone",
    group = "Devices",
    device = "spec:width=411dp,height=891dp",
    showSystemUi = false
)
@Preview(
    name = "Tablet",
    group = "Devices",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showSystemUi = false
)
annotation class DevicePreviews

/**
 * Combined multipreview for Theme and Device variations.
 */
@TravelMonkThemePreviews
@DevicePreviews
annotation class TravelMonkComponentPreviews
