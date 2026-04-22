package com.travelmonk.feature.experiences.ui

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.model.BookingType
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.core.ui.TravelMonkButton
import com.travelmonk.core.ui.utils.TravelMonkSnackBarHost
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsEffect
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsIntent
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsState
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ExperienceDetailsScreen(
    experienceId: String,
    navigator: ExperienceNavigator,
    viewModel: ExperienceDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(experienceId) {
        viewModel.onIntent(ExperienceDetailsIntent.LoadDetails(experienceId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ExperienceDetailsEffect.NavigateToBooking -> {
                    navigator.navigateToBookingConfirmation(
                        type = BookingType.PACKAGE,
                        title = effect.experience.title
                    )
                }
                is ExperienceDetailsEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    ExperienceDetailsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = { navigator.back() }
    )
}

@Composable
fun ExperienceDetailsContent(
    state: ExperienceDetailsState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ExperienceDetailsIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { TravelMonkSnackBarHost(hostState = snackbarHostState) },
        containerColor = TravelMonkTheme.colors.background,
        bottomBar = {
            state.experience?.let { experience ->
                ExperienceDetailsBottomBar(
                    price = experience.price,
                    onBookNow = { onIntent(ExperienceDetailsIntent.BookNow) }
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
            } else if (state.experience != null) {
                ExperienceDetailsScrollableContent(
                    experience = state.experience,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun ExperienceDetailsScrollableContent(
    experience: Experience,
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
                model = experience.imageUrl,
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
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    painter = painterResource(id = TravelMonkIcons.ArrowBack),
                    contentDescription = "Back",
                    tint = TravelMonkTheme.colors.onSurface
                )
            }
        }

        Column(
            modifier = Modifier.padding(TravelMonkTheme.spacing.medium)
        ) {
            Surface(
                color = TravelMonkTheme.colors.primaryContainer,
                shape = RoundedCornerShape(TravelMonkTheme.radius.small)
            ) {
                Text(
                    text = experience.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(
                        horizontal = TravelMonkTheme.spacing.small,
                        vertical = TravelMonkTheme.spacing.extraSmall
                    ),
                    style = TravelMonkTheme.typography.labelMedium,
                    color = TravelMonkTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.small))
            
            Text(
                text = experience.title,
                style = TravelMonkTheme.typography.titleLarge,
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
                    text = experience.rating.toString(),
                    style = TravelMonkTheme.typography.labelMedium,
                    color = TravelMonkTheme.colors.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = TravelMonkTheme.spacing.medium),
                color = TravelMonkTheme.colors.surfaceVariant
            )

            Text(
                text = "Overview",
                style = TravelMonkTheme.typography.titleLarge,
                color = TravelMonkTheme.colors.onBackground
            )

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.small))

            Text(
                text = experience.description.ifEmpty { "Discover the breathtaking beauty and rich culture of this incredible destination. This experience offers a unique blend of adventure and relaxation, perfect for travelers seeking something extraordinary." },
                style = TravelMonkTheme.typography.bodyLarge,
                color = TravelMonkTheme.colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))
        }
    }
}

@Composable
private fun ExperienceDetailsBottomBar(
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
                    text = "Total Price",
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
                text = "Book Now",
                onClick = onBookNow,
                modifier = Modifier.width(TravelMonkTheme.dimensions.bookingButtonWidth)
            )
        }
    }
}

@Preview(name = "Experience Details – Light Full", showSystemUi = true)
@Preview(
    name = "Experience Details – Dark Full",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ExperienceDetailsContentPreview() {
    val sampleExperience = Experience(
        id = "1",
        title = "Ubud Art & Culture Tour",
        description = "A deep dive into Bali's spiritual heart.",
        price = "$45",
        rating = 4.9,
        category = ExperienceCategory.PACKAGES,
        imageUrl = ""
    )
    
    TravelMonkTheme {
        ExperienceDetailsContent(
            state = ExperienceDetailsState(experience = sampleExperience),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onBackClick = {}
        )
    }
}
