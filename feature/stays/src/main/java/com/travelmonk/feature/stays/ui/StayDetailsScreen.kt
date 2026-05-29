package com.travelmonk.feature.stays.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.travelmonk.feature.stays.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.model.BookingType
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.core.ui.TravelMonkButton
import com.travelmonk.core.ui.utils.LogScreenLifecycle
import com.travelmonk.core.ui.utils.TravelMonkSnackBarHost
import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.mvi.StayDetailsEffect
import com.travelmonk.feature.stays.mvi.StayDetailsIntent
import com.travelmonk.feature.stays.mvi.StayDetailsState
import com.travelmonk.feature.staysapi.navigator.StayNavigator
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StayDetailsScreen(
    stayId: String,
    navigator: StayNavigator,
    viewModel: StayDetailsViewModel = hiltViewModel()
) {
    LogScreenLifecycle("StayDetailsScreen")

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(stayId) {
        viewModel.onIntent(StayDetailsIntent.LoadDetails(stayId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is StayDetailsEffect.NavigateToBooking -> {
                    // navigator.navigateToBooking(effect.stay)
                }
                is StayDetailsEffect.ShowError -> {
                    snackBarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    StayDetailsContent(
        state = state,
        snackbarHostState = snackBarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = { navigator.back() }
    )
}

@Composable
fun StayDetailsContent(
    state: StayDetailsState,
    snackbarHostState: SnackbarHostState,
    onIntent: (StayDetailsIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { TravelMonkSnackBarHost(hostState = snackbarHostState) },
        containerColor = TravelMonkTheme.colors.background,
        bottomBar = {
            state.stay?.let { stay ->
                StayDetailsBottomBar(
                    price = stay.price,
                    onBookNow = { onIntent(StayDetailsIntent.BookNow) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TravelMonkTheme.colors.primary
                )
            } else if (state.error != null) {
                Text(
                    text = state.error,
                    color = TravelMonkTheme.colors.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.stay != null) {
                StayDetailsScrollableContent(
                    stay = state.stay,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun StayDetailsScrollableContent(
    stay: Stay,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TravelMonkTheme.dimensions.heroImageHeight)
        ) {
            AsyncImage(
                model = stay.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(TravelMonkTheme.spacing.medium)
                    .background(
                        color = TravelMonkTheme.colors.surface.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = TravelMonkIcons.ArrowBack),
                    contentDescription = stringResource(R.string.feature_stays_navigate_back_cd),
                    tint = TravelMonkTheme.colors.onSurface
                )
            }
        }

        Column(
            modifier = Modifier.padding(TravelMonkTheme.spacing.medium)
        ) {
            Text(
                text = stay.title,
                style = TravelMonkTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TravelMonkTheme.colors.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = TravelMonkTheme.spacing.small)
            ) {
                Icon(
                    painter = painterResource(id = TravelMonkIcons.Star),
                    contentDescription = null,
                    tint = TravelMonkTheme.colors.primary,
                    modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.extraSmall))
                Text(
                    text = stay.rating,
                    style = TravelMonkTheme.typography.labelMedium,
                    color = TravelMonkTheme.colors.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.small))
                Text(
                    text = "• ${stay.location}",
                    style = TravelMonkTheme.typography.bodyLarge,
                    color = TravelMonkTheme.colors.onSurfaceVariant
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = TravelMonkTheme.spacing.medium),
                color = TravelMonkTheme.colors.surfaceVariant
            )

            Text(
                text = stringResource(R.string.feature_stays_about_this_stay),
                style = TravelMonkTheme.typography.titleLarge,
                color = TravelMonkTheme.colors.onBackground
            )

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.small))

            Text(
                text = stringResource(R.string.feature_stays_description_placeholder, stay.title, stay.location),
                style = TravelMonkTheme.typography.bodyLarge,
                color = TravelMonkTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

            // Facilities Placeholder
            Text(
                text = stringResource(R.string.feature_stays_facilities),
                style = TravelMonkTheme.typography.titleLarge,
                color = TravelMonkTheme.colors.onBackground
            )
            
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.small))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
            ) {
                FacilityItem(icon = TravelMonkIcons.Search, label = stringResource(R.string.feature_stays_facility_wifi))
                FacilityItem(icon = TravelMonkIcons.Search, label = stringResource(R.string.feature_stays_facility_pool))
                FacilityItem(icon = TravelMonkIcons.Search, label = stringResource(R.string.feature_stays_facility_spa))
            }
            
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))
        }
    }
}

@Composable
private fun FacilityItem(icon: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(TravelMonkTheme.spacing.small)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = TravelMonkTheme.colors.primary,
            modifier = Modifier.size(TravelMonkTheme.dimensions.iconMedium)
        )
        Text(
            text = label,
            style = TravelMonkTheme.typography.caption,
            color = TravelMonkTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun StayDetailsBottomBar(
    price: String,
    onBookNow: () -> Unit
) {
    Surface(
        color = TravelMonkTheme.colors.surface,
        tonalElevation = TravelMonkTheme.dimensions.cardElevationLarge,
        shadowElevation = TravelMonkTheme.dimensions.cardElevationLarge
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(TravelMonkTheme.spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.feature_stays_price_per_night_label),
                    style = TravelMonkTheme.typography.labelMedium,
                    color = TravelMonkTheme.colors.onSurfaceVariant
                )
                Text(
                    text = price,
                    style = TravelMonkTheme.typography.titleLarge,
                    color = TravelMonkTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            TravelMonkButton(
                text = stringResource(R.string.feature_stays_book_now),
                onClick = onBookNow,
                modifier = Modifier.width(TravelMonkTheme.dimensions.bookingButtonWidth)
            )
        }
    }
}

@Preview(name = "Stay Details – Light Full", showSystemUi = true)
@Preview(
    name = "Stay Details – Dark Full",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StayDetailsContentPreview() {
    val sampleStay = Stay(
        id = "1",
        title = "Grand Hyatt Bali",
        location = "Nusa Dua, Bali",
        price = "$240",
        rating = "4.8",
        imageUrl = ""
    )
    
    TravelMonkTheme {
        StayDetailsContent(
            state = StayDetailsState(stay = sampleStay),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onBackClick = {}
        )
    }
}
