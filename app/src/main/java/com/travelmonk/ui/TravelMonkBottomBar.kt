package com.travelmonk.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.ui.navigation.BottomBarItem

/**
 * Custom floating capsule bottom navigation bar with a "glass-like" design.
 * Matches the premium design with circular indicators, translucency, and high-contrast styling.
 */
@Composable
fun TravelMonkBottomBar(
    items: List<BottomBarItem>,
    selectedItem: BottomBarItem,
    onItemSelected: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                bottom = TravelMonkTheme.spacing.medium,
                start = TravelMonkTheme.spacing.large,
                end = TravelMonkTheme.spacing.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .height(TravelMonkTheme.dimensions.bottomBarHeight)
                .fillMaxWidth(),
            color = TravelMonkTheme.colors.bottomBarBackground,
            shape = CircleShape,
            border = BorderStroke(0.5.dp, TravelMonkTheme.colors.bottomBarIndicator.copy(alpha = 0.2f)),
            shadowElevation = TravelMonkTheme.dimensions.cardElevationLarge
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = TravelMonkTheme.spacing.extraSmall),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedItem == item
                    
                    // Animate colors for a smooth transition effect
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            // Uses the dedicated semantic content token for perfect contrast/effect
                            TravelMonkTheme.colors.bottomBarIndicatorContent
                        } else {
                            // Unselected icons use the indicator content color with alpha
                            TravelMonkTheme.colors.bottomBarIndicatorContent.copy(alpha = 0.6f)
                        },
                        label = "icon_color"
                    )
                    
                    val indicatorColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            // Uses the dedicated semantic indicator token
                            TravelMonkTheme.colors.bottomBarIndicator
                        } else {
                            Color.Transparent
                        },
                        label = "indicator_color"
                    )

                    Box(
                        modifier = Modifier
                            .size(TravelMonkTheme.dimensions.fabSize)
                            .clip(CircleShape)
                            .background(indicatorColor)
                            .clickable { onItemSelected(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.title,
                            tint = contentColor,
                            modifier = Modifier.size(TravelMonkTheme.dimensions.iconMedium)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Bottom Bar – Light", showBackground = true)
@Preview(name = "Bottom Bar – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkBottomBarPreview() {
    val items = listOf(
        BottomBarItem.Home,
        BottomBarItem.Transport,
        BottomBarItem.Stays,
        BottomBarItem.Experiences,
        BottomBarItem.Services,
        BottomBarItem.Bookings
    )
    TravelMonkTheme {
        Box(modifier = Modifier.fillMaxSize().background(TravelMonkTheme.colors.background)) {
            TravelMonkBottomBar(
                items = items,
                selectedItem = BottomBarItem.Home,
                onItemSelected = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
