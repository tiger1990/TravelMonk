package com.travelmonk.core.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.travelmonk.core.design.system.theme.TravelMonkTheme

/**
 * Standard search/input field for TravelMonk.
 *
 * Uses design system tokens for background ([TravelMonkColors.surfaceVariant]),
 * shape ([Radius.small] = 8dp), padding ([Spacing.medium] = 16dp),
 * and placeholder typography ([TravelMonkTypography.bodyLarge]).
 */
@Composable
fun TravelMonkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val colors = TravelMonkTheme.colors
    val radius = TravelMonkTheme.radius
    val spacing = TravelMonkTheme.spacing
    val typography = TravelMonkTheme.typography

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = typography.bodyLarge.copy(color = colors.onSurface),
        keyboardOptions = keyboardOptions,
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colors.surfaceVariant,
                        shape = RoundedCornerShape(radius.small)
                    )
                    .padding(spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(spacing.small))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = typography.bodyLarge,
                            color = colors.grayText
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Preview(name = "Light Empty", showBackground = true)
@Preview(name = "Dark Empty", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkTextFieldEmptyPreview() {
    TravelMonkTheme {
        TravelMonkTextField(
            value = "",
            onValueChange = {},
            placeholder = "Search destinations..."
        )
    }
}

@Preview(name = "Light Filled", showBackground = true)
@Preview(name = "Dark Filled", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkTextFieldFilledPreview() {
    TravelMonkTheme {
        var text by remember { mutableStateOf("New Delhi") }
        TravelMonkTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Search destinations..."
        )
    }
}
