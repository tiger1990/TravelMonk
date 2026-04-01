package com.travelmonk.feature.transportapi

import androidx.compose.runtime.Composable

interface TransportTabContentProvider {
    val tab: TransportTab
    @Composable
    fun Content()
}
