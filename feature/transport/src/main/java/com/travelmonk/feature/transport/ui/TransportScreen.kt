package com.travelmonk.feature.transport.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.feature.transport.mvi.*
import com.travelmonk.feature.transportapi.TransportTabContentProvider
import com.travelmonk.feature.transportapi.navigator.TransportNavigator
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.travelmonk.feature.transport.R
import com.travelmonk.feature.transportapi.TransportTab

// Hilt EntryPoint for tab providers (must be top-level)
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface TransportTabProviderEntryPoint {
    fun tabProviders(): Set<@JvmSuppressWildcards TransportTabContentProvider>
}

@Composable
fun TransportScreen(
    navigator: TransportNavigator,
    viewModel: TransportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val providers = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TransportTabProviderEntryPoint::class.java
        )
        entryPoint.tabProviders()
    }
    val state by viewModel.uiState.collectAsState()
    val icons = listOf(
        painterResource(id = R.drawable.ic_flight),
        painterResource(id = R.drawable.ic_bus),
        painterResource(id = R.drawable.ic_transit)
    )

    Column(modifier = Modifier.fillMaxSize().background(TravelMonkTheme.colors.background)) {
        TabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            containerColor = TravelMonkTheme.colors.surface,
            contentColor = TravelMonkTheme.colors.primary,
            indicator = { tabPositions ->
                val selectedIndex = state.selectedTab.ordinal
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = TravelMonkTheme.colors.primary
                    )
                }
            }
        ) {
            TransportTab.entries.forEach { tab ->
                val isSelected = state.selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.onIntent(TransportIntent.SelectTab(tab)) },
                    text = { 
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = TravelMonkTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    icon = { Icon(icons[tab.ordinal], contentDescription = null, modifier = Modifier.size(20.dp)) },
                    selectedContentColor = TravelMonkTheme.colors.primary,
                    unselectedContentColor = TravelMonkTheme.colors.grayText
                )
            }
        }
        // Find the provider for the selected tab and render its content
        providers.firstOrNull { it.tab == state.selectedTab }?.Content()
    }
}

// BusSearchScreen, TrainSearchScreen, TransportSearchCard, and SearchField are now provided via TransportTabContentProvider implementations only.
