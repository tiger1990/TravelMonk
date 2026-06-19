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
import androidx.compose.material3.TextButton
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
import com.travelmonk.feature.onboarding.mvi.OtpEffect
import com.travelmonk.feature.onboarding.mvi.OtpIntent
import com.travelmonk.feature.onboarding.mvi.OtpState
import com.travelmonk.feature.onboardingapi.navigator.OnboardingNavigator
import kotlinx.coroutines.flow.collectLatest

/**
 * AddOn Enhancement Try this later: EventEffect helper
 * This pattern repeats across screens.
 *
 * @Composable
 * fun <T> EventEffect(
 *     flow: Flow<T>,
 *     onEvent: suspend (T) -> Unit
 * ) {
 *     LaunchedEffect(flow) {
 *         flow.collectLatest(onEvent)
 *     }
 * }
 *
 * Usage:
 * EventEffect(viewModel.effect) { effect ->
 *     when (effect) {
 *         OtpEffect.NavigateToPasskeyPrompt ->
 *             navigator.toPasskeyPrompt()
 *     }
 * }
 */
@Composable
fun OtpScreen(
    navigator: OnboardingNavigator,
    phone: String,
    // Migrated to @AssistedInject: `phone` is supplied to the ViewModel at construction and is
    // part of the initial state from frame one. It survives process death with the nav key.
    viewModel: OtpViewModel = hiltViewModel<OtpViewModel, OtpViewModel.Factory> { factory ->
        factory.create(phone)
    },
    // Previous (intent-delivery) wiring, retired — kept for reference:
    // viewModel: OtpViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Retired — phone now arrives via @AssistedInject, no first-composition delivery needed:
    // LaunchedEffect(phone) {
    //     viewModel.onIntent(OtpIntent.SetPhone(phone))
    // }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OtpEffect.NavigateToPasskeyPrompt -> navigator.toPasskeyPrompt()
            }
        }
    }
    OtpContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun OtpContent(
    state: OtpState,
    onIntent: (OtpIntent) -> Unit,
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
                text = stringResource(R.string.feature_onboarding_otp_title),
                style = TravelMonkTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.feature_onboarding_otp_subtitle, state.phone),
                style = TravelMonkTheme.typography.bodyLarge,
                color = TravelMonkTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = state.otp,
                onValueChange = { if (it.length <= 6) onIntent(OtpIntent.OtpChanged(it)) },
                label = { Text(stringResource(R.string.feature_onboarding_otp_field_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = state.error != null,
                supportingText = state.error?.asString(LocalContext.current)?.let { msg -> { Text(msg) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onIntent(OtpIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.otp.length == 6 && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(text = stringResource(R.string.feature_onboarding_verify))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { onIntent(OtpIntent.ResendOtp) },
                enabled = state.resendCooldownSeconds == 0 && !state.isLoading
            ) {
                val label = if (state.resendCooldownSeconds > 0) {
                    stringResource(R.string.feature_onboarding_resend_in, state.resendCooldownSeconds)
                } else {
                    stringResource(R.string.feature_onboarding_resend_code)
                }
                Text(text = label)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OtpContentPreview() {
    TravelMonkTheme {
        OtpContent(
            state = OtpState(phone = "+1 555 000 1234", otp = "123456"),
            onIntent = {}
        )
    }
}
