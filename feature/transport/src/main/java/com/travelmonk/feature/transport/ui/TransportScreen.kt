package com.travelmonk.feature.transport.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.ui.TravelMonkTopBar
import com.travelmonk.feature.transport.R
import com.travelmonk.feature.transport.di.TransportTabProviderEntryPoint
import com.travelmonk.feature.transport.mvi.*
import com.travelmonk.feature.transportapi.TransportTab
import com.travelmonk.feature.transportapi.navigator.TransportNavigator
import dagger.hilt.android.EntryPointAccessors

/**
 * Stateful entry point for the Transport Screen.
 */
@Composable
fun TransportScreen(
    navigator: TransportNavigator,
    viewModel: TransportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val providers = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TransportTabProviderEntryPoint::class.java
        ).tabProviders()
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TransportContent(
        state = state,
        onIntent = viewModel::onIntent,
        tabContent = {
            providers.firstOrNull { it.tab == state.selectedTab }?.Content()
        }
    )
}

/**
 * Stateless content for the Transport screen.
 * Follows the 'map' pattern by delegating specific UI sections to sub-composables.
 */
@Composable
fun TransportContent(
    state: TransportState,
    onIntent: (TransportIntent) -> Unit,
    modifier: Modifier = Modifier,
    tabContent: @Composable () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TransportTopBar(
                selectedTab = state.selectedTab,
                onTabSelected = { onIntent(TransportIntent.SelectTab(it)) }
            )
        },
        containerColor = TravelMonkTheme.colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TravelMonkTheme.colors.background)
        ) {
            tabContent()
        }
    }
}

/**
 * Dedicated TopBar for the Transport screen.
 * Extracted for readability and isolated preview support.
 */
@Composable
private fun TransportTopBar(
    selectedTab: TransportTab,
    onTabSelected: (TransportTab) -> Unit
) {
    val icons = listOf(
        painterResource(id = R.drawable.ic_flight),
        painterResource(id = R.drawable.ic_bus),
        painterResource(id = R.drawable.ic_transit)
    )

    TravelMonkTopBar(
        title = {
            Text(
                text = "Transport",
                color = TravelMonkTheme.colors.onPrimary,
                style = TravelMonkTheme.typography.titleLarge
            )
        },
        containerColor = TravelMonkTheme.colors.primary,
        bottomContent = {
            SecondaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = TravelMonkTheme.colors.primary,
                contentColor = TravelMonkTheme.colors.onPrimary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab.ordinal),
                        color = TravelMonkTheme.colors.onPrimary
                    )
                },
                divider = {}
            ) {
                TransportTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = TravelMonkTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                painter = icons[tab.ordinal],
                                contentDescription = null,
                                modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall)
                            )
                        },
                        selectedContentColor = TravelMonkTheme.colors.onPrimary,
                        unselectedContentColor = TravelMonkTheme.colors.onPrimary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    )
}

@Preview(name = "Transport – Light Full", showSystemUi = true)
@Preview(
    name = "Transport – Dark Full",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TransportContentPreview() {
    TravelMonkTheme {
        TransportContent(
            state = TransportState(selectedTab = TransportTab.FLIGHTS),
            onIntent = {}
        )
    }
}
