package com.travelmonk.feature.transport.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelmonk.core.designsystem.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons

@Composable
fun TransportSearchCard(
    title: String,
    fromLabel: String,
    fromValue: String,
    toLabel: String,
    toValue: String,
    dateLabel: String,
    dateValue: String,
    buttonText: String,
    accentColor: Color,
    onSearchClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TravelMonkTheme.radius.large),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(TravelMonkTheme.spacing.large)) {
            Text(
                text = title,
                style = TravelMonkTheme.typography.titleLarge,
                color = TravelMonkTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))
            Column {
                SearchField(
                    label = fromLabel,
                    value = fromValue,
                    iconRes = TravelMonkIcons.LocationOn,
                    accentColor = accentColor
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = TravelMonkTheme.spacing.medium))
                SearchField(
                    label = toLabel,
                    value = toValue,
                    iconRes = TravelMonkIcons.LocationOn,
                    accentColor = accentColor
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = TravelMonkTheme.spacing.medium))
                SearchField(
                    label = dateLabel,
                    value = dateValue,
                    iconRes = TravelMonkIcons.CalendarToday,
                    accentColor = accentColor
                )
            }
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.extraLarge))
            Button(
                onClick = onSearchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TravelMonkTheme.dimensions.buttonHeight),
                shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = buttonText,
                    style = TravelMonkTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SearchField(
    label: String,
    value: String,
    @DrawableRes iconRes: Int,
    accentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.medium))
        Column {
            Text(
                text = label,
                color = TravelMonkTheme.colors.grayText,
                style = TravelMonkTheme.typography.caption
            )
            Text(
                text = value,
                style = TravelMonkTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
