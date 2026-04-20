package com.travelmonk.feature.home.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.home.domain.model.HomeBanner
import com.travelmonk.feature.home.mvi.*
import com.travelmonk.feature.homeapi.navigator.HomeNavigator

// Stateful entry point — only this touches hiltViewModel()
@Composable
fun HomeScreen(
    navigator: HomeNavigator,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetails -> { /* Navigate to details screen when implemented */ }
                is HomeEffect.NavigateToGlobalSearch -> navigator.navigateToSearch()
            }
        }
    }

    HomeContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

// Stateless content — previewable without ViewModel
@Composable
fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    Scaffold(
        topBar = {
            HomeTopBar(onSearchClick = { onIntent(HomeIntent.OnSearchClick) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(TravelMonkTheme.colors.background)
        ) {
            item {
                Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.medium))
                BannerSection(banners = state.banners) { bannerId ->
                    onIntent(HomeIntent.OnBannerClick(bannerId))
                }
            }
            item { CategorySection(categories = state.categories) }
            item { PromoSection() }
        }
    }
}

@Composable
fun HomeTopBar(onSearchClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TravelMonkTheme.colors.primary,
        shape = RoundedCornerShape(bottomStart = TravelMonkTheme.radius.extraLarge, bottomEnd = TravelMonkTheme.radius.extraLarge)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = TravelMonkTheme.spacing.large, vertical = TravelMonkTheme.spacing.extraLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello Traveler,",
                        color = Color.White.copy(alpha = 0.8f),
                        style = TravelMonkTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Where to next?",
                        color = Color.White,
                        style = TravelMonkTheme.typography.headlineMedium
                    )
                }
                Icon(
                    painter = painterResource(TravelMonkIcons.Notifications),
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(TravelMonkTheme.dimensions.iconMedium)
                )
            }
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearchClick() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(TravelMonkIcons.Search), contentDescription = "Search", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Search destinations, hotels...", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun BannerSection(banners: List<HomeBanner>, onBannerClick: (String) -> Unit) {
    Column {
        Text(
            text = "Special Offers",
            style = TravelMonkTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(banners, key = { it.id }) { banner ->
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(150.dp)
                        .clickable { onBannerClick(banner.id) },
                    shape = RoundedCornerShape(TravelMonkTheme.radius.medium)
                ) {
                    Box {
                        AsyncImage(
                            model = banner.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(text = banner.title, color = Color.White, style = TravelMonkTheme.typography.titleLarge)
                            Text(text = banner.description, color = Color.White.copy(alpha = 0.8f), style = TravelMonkTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySection(categories: List<HomeCategory>) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(text = "Categories", style = TravelMonkTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            categories.forEach { category ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(TravelMonkTheme.colors.primary.copy(alpha = 0.1f), RoundedCornerShape(TravelMonkTheme.radius.medium)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painter = painterResource(category.icon), contentDescription = null, tint = TravelMonkTheme.colors.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = category.label, style = TravelMonkTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun PromoSection() {
    Card(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.secondary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(TravelMonkIcons.CardGiftCard), contentDescription = null, tint = TravelMonkTheme.colors.secondary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Refer & Earn", fontWeight = FontWeight.Bold, style = TravelMonkTheme.typography.titleLarge)
                Text(text = "Invite friends and get up to $50 credit", style = TravelMonkTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(name = "Home – Light", showBackground = true)
@Preview(name = "Home – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeContentPreview() {
    TravelMonkTheme {
        HomeContent(
            state = HomeState(
                banners = listOf(
                    HomeBanner("1", "Summer Sale", "Up to 40% off", ""),
                    HomeBanner("2", "Bali Getaway", "From $299", "")
                )
            ),
            onIntent = {}
        )
    }
}
