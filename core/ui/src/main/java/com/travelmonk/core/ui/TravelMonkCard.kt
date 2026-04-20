package com.travelmonk.core.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.travelmonk.core.design.system.theme.TravelMonkTheme

/**
 * Standard surface card for TravelMonk.
 *
 * Uses design system tokens for shape ([Radius.medium] = 16dp),
 * elevation ([Dimensions.cardElevation] = 2dp), and colors ([TravelMonkColors.surface]).
 * Exposes a [ColumnScope] content slot for flexible composition.
 */
@Composable
fun TravelMonkCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = TravelMonkTheme.colors
    val dimensions = TravelMonkTheme.dimensions
    val radius = TravelMonkTheme.radius
    val spacing = TravelMonkTheme.spacing

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.medium),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            content()
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkCardPreview() {
    TravelMonkTheme {
        TravelMonkCard {
            Text(text = "Card title", style = TravelMonkTheme.typography.titleLarge)
            Text(text = "Card body text goes here.", style = TravelMonkTheme.typography.bodyLarge)
        }
    }
}
