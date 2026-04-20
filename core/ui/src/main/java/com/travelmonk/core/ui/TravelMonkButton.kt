package com.travelmonk.core.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travelmonk.core.design.system.shapes.Radius
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Standard CTA button for TravelMonk.
 *
 * Uses design system tokens for height ([Dimensions.buttonHeight] = 56dp),
 * shape ([Radius.medium] = 16dp), and colors ([TravelMonkColors.primary]).
 * Shows a [CircularProgressIndicator] when [isLoading] is true.
 */
@Composable
fun TravelMonkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colors = TravelMonkTheme.colors
    val dimensions = TravelMonkTheme.dimensions
    val radius = TravelMonkTheme.radius
    val typography = TravelMonkTheme.typography

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.buttonHeight),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(radius.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.primary.copy(alpha = 0.38f),
            disabledContentColor = colors.onPrimary.copy(alpha = 0.38f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = text, style = typography.titleLarge)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkButtonPreview() {
    TravelMonkTheme {
        TravelMonkButton(text = "Search Flights", onClick = {})
    }
}

@Preview(name = "Loading Light", showBackground = true)
@Preview(name = "Loading Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkButtonLoadingPreview() {
    TravelMonkTheme {
        TravelMonkButton(text = "Search Flights", onClick = {}, isLoading = true)
    }
}
