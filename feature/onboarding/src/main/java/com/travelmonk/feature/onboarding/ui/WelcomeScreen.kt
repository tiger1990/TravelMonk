package com.travelmonk.feature.onboarding.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.mvi.WelcomeEffect
import com.travelmonk.feature.onboarding.mvi.WelcomeIntent
import com.travelmonk.feature.onboarding.mvi.WelcomeState
import com.travelmonk.feature.onboardingapi.navigator.OnboardingNavigator

@Composable
fun WelcomeScreen(
    navigator: OnboardingNavigator,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WelcomeEffect.NavigateToPhoneEntry -> navigator.toPhoneEntry()
                is WelcomeEffect.NavigateToPasskeyPrompt -> navigator.toPasskeyPrompt()
            }
        }
    }
    WelcomeContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun WelcomeContent(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.feature_onboarding_title),
                style = TravelMonkTheme.typography.headlineLarge
            )
            Text(
                text = stringResource(R.string.feature_onboarding_tagline),
                style = TravelMonkTheme.typography.bodyLarge,
                color = TravelMonkTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { onIntent(WelcomeIntent.GetStarted) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text(text = stringResource(R.string.feature_onboarding_get_started))
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onIntent(WelcomeIntent.Login) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text(text = stringResource(R.string.feature_onboarding_have_account))
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WelcomeContentPreview() {
    TravelMonkTheme {
        WelcomeContent(
            state = WelcomeState(),
            onIntent = {}
        )
    }
}
