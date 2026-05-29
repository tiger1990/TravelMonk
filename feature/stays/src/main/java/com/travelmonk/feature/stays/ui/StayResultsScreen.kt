package com.travelmonk.feature.stays.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.core.ui.TravelMonkTopBar
import com.travelmonk.core.ui.utils.LogScreenLifecycle
import com.travelmonk.core.ui.utils.TravelMonkSnackBarHost
import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.mvi.StayResultsEffect
import com.travelmonk.feature.stays.mvi.StayResultsIntent
import com.travelmonk.feature.stays.mvi.StayResultsState
import com.travelmonk.feature.staysapi.navigator.StayNavigator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StayResultsScreen(
    location: String,
    navigator: StayNavigator,
    viewModel: StayResultsViewModel = hiltViewModel()
) {
    LogScreenLifecycle("StayResultsScreen")

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(location) {
        viewModel.onIntent(StayResultsIntent.LoadStays(location))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is StayResultsEffect.NavigateToDetail -> {
                    navigator.navigateToStayDetail(effect.stayId)
                }
                is StayResultsEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    StayResultsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = { navigator.back() }
    )
}

@Composable
fun StayResultsContent(
    state: StayResultsState,
    snackbarHostState: SnackbarHostState,
    onIntent: (StayResultsIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            StayResultsTopBar(
                location = state.location,
                onBackClick = onBackClick
            )
        },
        snackbarHost = { TravelMonkSnackBarHost(hostState = snackbarHostState) },
        containerColor = TravelMonkTheme.colors.background
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(TravelMonkTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
                ) {
                    items(state.stays) { stay ->
                        StayItem(
                            stay = stay,
                            onClick = { onIntent(StayResultsIntent.SelectStay(stay)) },
                            onFavoriteClick = { onIntent(StayResultsIntent.ToggleFavorite(stay.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StayResultsTopBar(
    location: String,
    onBackClick: () -> Unit
) {
    TravelMonkTopBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.stays_search_results),
                    style = TravelMonkTheme.typography.titleLarge,
                    color = TravelMonkTheme.colors.onPrimary
                )
                Text(
                    text = location,
                    style = TravelMonkTheme.typography.labelMedium,
                    color = TravelMonkTheme.colors.onPrimary.copy(alpha = 0.7f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = TravelMonkIcons.ArrowBack),
                    contentDescription = stringResource(R.string.stays_navigate_back_cd),
                    tint = TravelMonkTheme.colors.onPrimary
                )
            }
        },
        containerColor = TravelMonkTheme.colors.primary
    )
}

@Composable
private fun StayItem(
    stay: Stay,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TravelMonkTheme.radius.large),
        colors = CardDefaults.cardColors(
            containerColor = TravelMonkTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = TravelMonkTheme.dimensions.cardElevation)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TravelMonkTheme.dimensions.imageCardHeight)
            ) {
                AsyncImage(
                    model = stay.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(TravelMonkTheme.spacing.small)
                        .background(
                            color = TravelMonkTheme.colors.surface.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = TravelMonkIcons.Star), // Using Search as placeholder for Favorite
                        contentDescription = stringResource(R.string.stays_favorite_cd),
                        tint = TravelMonkTheme.colors.primary,
                        modifier = Modifier.size(TravelMonkTheme.dimensions.iconMedium)
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(TravelMonkTheme.spacing.medium),
                    color = TravelMonkTheme.colors.secondaryContainer,
                    shape = RoundedCornerShape(TravelMonkTheme.radius.small)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = TravelMonkTheme.spacing.small,
                            vertical = TravelMonkTheme.spacing.extraSmall
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = TravelMonkIcons.Star),
                            contentDescription = null,
                            tint = TravelMonkTheme.colors.onSecondary,
                            modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.extraSmall))
                        Text(
                            text = stay.rating,
                            style = TravelMonkTheme.typography.labelMedium,
                            color = TravelMonkTheme.colors.onSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(TravelMonkTheme.spacing.medium)
            ) {
                Text(
                    text = stay.title,
                    style = TravelMonkTheme.typography.titleLarge,
                    color = TravelMonkTheme.colors.onSurface
                )
                Text(
                    text = stay.location,
                    style = TravelMonkTheme.typography.bodyLarge,
                    color = TravelMonkTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.small))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stay.price,
                        style = TravelMonkTheme.typography.headlineMedium,
                        color = TravelMonkTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.extraSmall))
                    Text(
                        text = stringResource(R.string.stays_per_night),
                        style = TravelMonkTheme.typography.labelMedium,
                        color = TravelMonkTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = TravelMonkTheme.spacing.extraSmall)
                    )
                }
            }
        }
    }
}

@Preview(name = "Stay Results – Light Full", showSystemUi = true)
@Preview(
    name = "Stay Results – Dark Full",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StayResultsContentPreview() {
    val sampleStay = Stay(
        id = "1",
        title = "Grand Hyatt Bali",
        location = "Nusa Dua, Bali",
        price = "$240",
        rating = "4.8",
        imageUrl = ""
    )
    
    TravelMonkTheme {
        StayResultsContent(
            state = StayResultsState(
                location = "Bali, Indonesia",
                stays = persistentListOf(sampleStay, sampleStay.copy(id = "2", title = "The Ritz-Carlton"))
            ),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onBackClick = {}
        )
    }
}
