package com.travelmonk.feature.transport.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.feature.transport.mvi.*
import com.travelmonk.feature.transportapi.navigator.TransportNavigator
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.travelmonk.feature.transport.R
import com.travelmonk.feature.transport.di.TransportTabProviderEntryPoint
import com.travelmonk.feature.transportapi.TransportTab

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
    val icons = listOf(
        painterResource(id = R.drawable.ic_flight),
        painterResource(id = R.drawable.ic_bus),
        painterResource(id = R.drawable.ic_transit)
    )

    Column(modifier = Modifier.fillMaxSize().background(TravelMonkTheme.colors.background)) {
        SecondaryTabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            containerColor = TravelMonkTheme.colors.surface,
            contentColor = TravelMonkTheme.colors.primary,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(state.selectedTab.ordinal),
                    color = TravelMonkTheme.colors.primary
                )
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
