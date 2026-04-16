package com.travelmonk.feature.experiences.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.mvi.ExperienceEffect
import com.travelmonk.feature.experiences.mvi.ExperienceIntent
import com.travelmonk.feature.experiences.mvi.ExperienceState
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator

// Stateful entry point
@Composable
fun ExperiencesScreen(
    navigator: ExperienceNavigator,
    viewModel: ExperienceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExperienceEffect.NavigateToBooking ->
                    navigator.navigateToBookingConfirmation(
                        BookingType.PACKAGE,
                        effect.item.title
                    )
            }
        }
    }

    ExperiencesContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

// Stateless content — previewable without ViewModel
@Composable
fun ExperiencesContent(
    state: ExperienceState,
    onIntent: (ExperienceIntent) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(TravelMonkTheme.colors.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TravelMonkTheme.dimensions.headerHeight)
                .background(
                    TravelMonkTheme.colors.tertiary,
                    RoundedCornerShape(
                        bottomStart = TravelMonkTheme.radius.large,
                        bottomEnd = TravelMonkTheme.radius.large
                    )
                )
                .padding(TravelMonkTheme.spacing.large),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "Experiences",
                color = TravelMonkTheme.colors.onPrimary,
                style = TravelMonkTheme.typography.titleLarge
            )
        }

        SecondaryScrollableTabRow(
            selectedTabIndex = state.selectedCategory.ordinal,
            edgePadding = TravelMonkTheme.spacing.medium,
            divider = {},
            containerColor = Color.Transparent,
            contentColor = TravelMonkTheme.colors.tertiary
        ) {
            ExperienceCategory.entries.forEach { category ->
                Tab(
                    selected = state.selectedCategory == category,
                    onClick = { onIntent(ExperienceIntent.SelectCategory(category)) },
                    selectedContentColor = TravelMonkTheme.colors.tertiary,
                    unselectedContentColor = TravelMonkTheme.colors.grayText,
                    text = {
                        Text(
                            category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = TravelMonkTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TravelMonkTheme.colors.tertiary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(TravelMonkTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
            ) {
                items(state.items, key = { it.id }) { item ->
                    ExperienceCard(item) { onIntent(ExperienceIntent.BookItem(item)) }
                }
            }
        }
    }
}

@Composable
fun ExperienceCard(item: Experience, onBook: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = TravelMonkTheme.dimensions.cardElevation)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TravelMonkTheme.dimensions.imageCardHeight)
                    .clip(
                        RoundedCornerShape(
                            topStart = TravelMonkTheme.radius.medium,
                            topEnd = TravelMonkTheme.radius.medium
                        )
                    ),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(TravelMonkTheme.spacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.title, style = TravelMonkTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(TravelMonkIcons.Star),
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall)
                        )
                        Text(
                            text = item.rating.toString(),
                            style = TravelMonkTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = item.description,
                    style = TravelMonkTheme.typography.bodyLarge,
                    color = TravelMonkTheme.colors.grayText
                )

                Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.price,
                        style = TravelMonkTheme.typography.headlineMedium,
                        color = TravelMonkTheme.colors.tertiary
                    )
                    Button(
                        onClick = onBook,
                        colors = ButtonDefaults.buttonColors(containerColor = TravelMonkTheme.colors.tertiary),
                        shape = RoundedCornerShape(TravelMonkTheme.radius.small)
                    ) {
                        Text(text = "Book Now")
                    }
                }
            }
        }
    }
}

@Preview(name = "Experiences – Light", showBackground = true)
@Preview(
    name = "Experiences – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ExperiencesContentPreview() {
    TravelMonkTheme {
        ExperiencesContent(
            state = ExperienceState(
                items = listOf(
                    Experience(
                        id = "1",
                        title = "Bali Yoga Retreat",
                        description = "7-day immersive yoga experience",
                        price = "$299",
                        rating = 4.8,
                        imageUrl = "",
                        category = ExperienceCategory.YOGA
                    ),
                    Experience(
                        id = "2",
                        title = "City Walking Tour",
                        description = "Explore hidden gems",
                        price = "$49",
                        rating = 4.6,
                        imageUrl = "",
                        category = ExperienceCategory.GUIDES
                    )
                )
            ),
            onIntent = {}
        )
    }
}
