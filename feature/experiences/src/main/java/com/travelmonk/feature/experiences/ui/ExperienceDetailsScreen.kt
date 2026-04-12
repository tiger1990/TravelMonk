package com.travelmonk.feature.experiences.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator

@Composable
fun ExperienceDetailsScreen(
    experienceId: String,
    navigator: ExperienceNavigator
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Experience Details Screen — $experienceId")
    }
}
