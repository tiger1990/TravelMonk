package com.travelmonk.feature.stays.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.travelmonk.feature.staysapi.navigator.StayNavigator

@Composable
fun StayResultsScreen(
    location: String,
    navigator: StayNavigator
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Stay Results Screen — $location")
    }
}
