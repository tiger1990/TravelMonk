package com.travelmonk.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.ui.navigation.BottomBarItem

/**
 * Stateless bottom navigation bar for the app.
 *
 * Receives stable values derived from [NavigationState] at the call site — no direct
 * dependency on navigation state, making it independently previewable and skippable
 * by the Compose compiler when [selectedItem] and [onItemSelected] haven't changed.
 */
@Composable
fun TravelMonkBottomBar(
    items: List<BottomBarItem>,
    selectedItem: BottomBarItem,
    onItemSelected: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selectedItem == item,
                onClick = { onItemSelected(item) }
            )
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
        TravelMonkBottomBar(
            items = items,
            selectedItem = BottomBarItem.Home,
            onItemSelected = {}
        )
    }
}
