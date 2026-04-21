package com.travelmonk.core.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.travelmonk.core.design.system.theme.TravelMonkTheme

/**
 * Reusable top app bar for TravelMonk screens.
 *
 * Wraps Material3 [TopAppBar] in a [Surface] with rounded bottom corners so the
 * bar's background color fills the transparent status bar (edge-to-edge) while
 * preserving the brand's rounded-header visual identity.
 *
 * The [Surface] owns the [containerColor] and shape; the [TopAppBar] uses
 * [Color.Transparent] and retains [TopAppBarDefaults.windowInsets] so it draws
 * correctly behind the status bar.
 *
 * @param title Composable slot for the title — supports single Text or a Column of title + subtitle.
 * @param containerColor Background color of the bar (and status bar region).
 * @param modifier Modifier applied to the outer [Surface].
 * @param navigationIcon Optional back/up icon slot.
 * @param actions Optional trailing action icons slot.
 * @param bottomContent Optional content rendered below the [TopAppBar] row but still
 *   inside the rounded [Surface] — use for tab rows, embedded search cards, etc.
 * @param scrollBehavior Optional scroll behavior for collapsing/pinned top bars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelMonkTopBar(
    title: @Composable () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(
            bottomStart = TravelMonkTheme.radius.extraLarge,
            bottomEnd = TravelMonkTheme.radius.extraLarge
        )
    ) {
        Column {
            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TravelMonkTheme.colors.onPrimary,
                    navigationIconContentColor = TravelMonkTheme.colors.onPrimary,
                    actionIconContentColor = TravelMonkTheme.colors.onPrimary
                ),
                windowInsets = TopAppBarDefaults.windowInsets,
                scrollBehavior = scrollBehavior
            )
            bottomContent?.invoke()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkTopBarPreview() {
    TravelMonkTheme {
        TravelMonkTopBar(
            title = { Text("My Bookings") },
            containerColor = TravelMonkTheme.colors.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "With Bottom Content – Light", showBackground = true)
@Preview(name = "With Bottom Content – Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkTopBarWithBottomContentPreview() {
    TravelMonkTheme {
        TravelMonkTopBar(
            title = { Text("Experiences") },
            containerColor = TravelMonkTheme.colors.tertiary,
            bottomContent = {
                Text(
                    text = "Tab row goes here",
                    color = TravelMonkTheme.colors.onPrimary
                )
            }
        )
    }
}