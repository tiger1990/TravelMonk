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
import com.travelmonk.core.ui.utils.LogScreenLifecycle
import com.travelmonk.core.ui.utils.TravelMonkSnackBarHost
import com.travelmonk.core.ui.TravelMonkTopBar
import com.travelmonk.core.design.system.color.TravelYellow
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.model.BookingType
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.mvi.ExperienceEffect
import com.travelmonk.feature.experiences.mvi.ExperienceIntent
import com.travelmonk.feature.experiences.mvi.ExperienceState
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator
import kotlinx.collections.immutable.persistentListOf

/**
 * Stateful entry point for the Experiences Screen.
 * Connects the ViewModel state and effects to the stateless UI.
 */
@Composable
fun ExperiencesScreen(
    navigator: ExperienceNavigator,
    viewModel: ExperienceViewModel = hiltViewModel()
) {
    LogScreenLifecycle("ExperiencesScreen")

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExperienceEffect.NavigateToDetail ->
                    navigator.navigateToExperienceDetail(effect.experienceId)
                is ExperienceEffect.NavigateToBooking ->
                    navigator.navigateToBookingConfirmation(
                        BookingType.PACKAGE,
                        effect.item.title
                    )
                is ExperienceEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    ExperiencesScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackBarHostState = snackBarHostState
    )
}

/**
 * Stateless full-screen content for Experiences.
 * Includes the TopBar, Tabs, and the main scrollable list.
 * This is the primary composable for Previews.
 */
@Composable
fun ExperiencesScreenContent(
    state: ExperienceState,
    onIntent: (ExperienceIntent) -> Unit,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            ExperiencesTopBar(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { onIntent(ExperienceIntent.SelectCategory(it)) }
            )
        },
        snackbarHost = { TravelMonkSnackBarHost(snackBarHostState) },
        containerColor = TravelMonkTheme.colors.background
    ) { innerPadding ->
        ExperiencesListContent(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Dedicated TopBar for the Experiences screen.
 * Extracted to allow for isolated previews and cleaner Scaffold structure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperiencesTopBar(
    selectedCategory: ExperienceCategory,
    onCategorySelected: (ExperienceCategory) -> Unit
) {
    TravelMonkTopBar(
        title = {
            Text(
                text = "Experiences",
                color = TravelMonkTheme.colors.onPrimary,
                style = TravelMonkTheme.typography.titleLarge
            )
        },
        containerColor = TravelMonkTheme.colors.primary,
        bottomContent = {
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedCategory.ordinal,
                edgePadding = TravelMonkTheme.spacing.medium,
                containerColor = Color.Transparent,
                contentColor = TravelMonkTheme.colors.onPrimary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedCategory.ordinal),
                        color = TravelMonkTheme.colors.onPrimary
                    )
                },
                divider = {} // Clean look without a full-width divider
            ) {
                ExperienceCategory.entries.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        selectedContentColor = TravelMonkTheme.colors.onPrimary,
                        unselectedContentColor = TravelMonkTheme.colors.onPrimary.copy(alpha = 0.6f),
                        text = {
                            Text(
                                category.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = TravelMonkTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    )
}

/**
 * The scrollable list part of the Experiences screen.
 */
@Composable
private fun ExperiencesListContent(
    state: ExperienceState,
    onIntent: (ExperienceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TravelMonkTheme.colors.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(TravelMonkTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
            ) {
                items(state.items, key = { it.id }) { item ->
                    ExperienceCard(
                        item = item,
                        onClick = { onIntent(ExperienceIntent.SelectExperience(item.id)) },
                        onBook = { onIntent(ExperienceIntent.BookItem(item)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExperienceCard(
    item: Experience,
    onClick: () -> Unit,
    onBook: () -> Unit
) {
    Card(
        onClick = onClick,
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
                            tint = TravelYellow,
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
                    color = TravelMonkTheme.colors.onSurfaceVariant
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
                        color = TravelMonkTheme.colors.primary
                    )
                    Button(
                        onClick = onBook,
                        colors = ButtonDefaults.buttonColors(containerColor = TravelMonkTheme.colors.primary),
                        shape = RoundedCornerShape(TravelMonkTheme.radius.small)
                    ) {
                        Text(text = "Book Now")
                    }
                }
            }
        }
    }
}

@Preview(name = "Experiences – Full Screen Light", showSystemUi = true)
@Preview(
    name = "Experiences – Full Screen Dark",
    showSystemUi = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ExperiencesScreenPreview() {
    TravelMonkTheme {
        ExperiencesScreenContent(
            state = ExperienceState(
                items = persistentListOf(
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
