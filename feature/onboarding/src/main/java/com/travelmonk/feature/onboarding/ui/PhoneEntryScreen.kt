package com.travelmonk.feature.onboarding.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.mvi.PhoneEntryEffect
import com.travelmonk.feature.onboarding.mvi.PhoneEntryIntent
import com.travelmonk.feature.onboarding.mvi.PhoneEntryState
import com.travelmonk.feature.onboardingapi.navigator.OnboardingNavigator

@Composable
fun PhoneEntryScreen(
    navigator: OnboardingNavigator,
    viewModel: PhoneEntryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PhoneEntryEffect.NavigateToOtp -> navigator.toOtp(effect.phone)
            }
        }
    }
    PhoneEntryContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun PhoneEntryContent(
    state: PhoneEntryState,
    onIntent: (PhoneEntryIntent) -> Unit,
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
                text = stringResource(R.string.feature_onboarding_phone_title),
                style = TravelMonkTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.feature_onboarding_phone_subtitle),
                style = TravelMonkTheme.typography.bodyLarge,
                color = TravelMonkTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = state.phone,
                onValueChange = { onIntent(PhoneEntryIntent.PhoneChanged(it)) },
                label = { Text(stringResource(R.string.feature_onboarding_phone_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.error != null,
                supportingText = state.error?.asString(LocalContext.current)?.let { msg -> { Text(msg) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onIntent(PhoneEntryIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.phone.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(text = stringResource(R.string.feature_onboarding_send_code))
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PhoneEntryContentPreview() {
    TravelMonkTheme {
        PhoneEntryContent(
            state = PhoneEntryState(phone = "+1 555 000 1234"),
            onIntent = {}
        )
    }
}
